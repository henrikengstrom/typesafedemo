/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import akka.actor.Actor
import akka.actor.Scheduler
import akka.config.Config.config
import akka.event.EventHandler

import com.typesafe.akka.demo.WorkResult
import com.typesafe.akka.demo.raytrace.RayTraceWorkResult
import roygbiv.color.RGBColor

import collection.mutable.ArrayBuffer

import java.util.concurrent.{ TimeUnit, ScheduledFuture }
import java.awt.image.{ BufferedImage ⇒ JBufferedImage }
import java.io.{ File ⇒ JFile }
import javax.imageio.{ ImageIO ⇒ JImageIO }

case object GenerateImage

class WorkAggregator extends Actor {
  import WorkAggregator._

  var scheduled: Option[ScheduledFuture[_]] = None
  var buffer = new ArrayBuffer[RGBColor]()
  val width = ImageWidth
  val height = ImageHeight
  var resultCounter = 0
  var previousResultNumber = 0

  override def preStart() {
    if (scheduled.isEmpty) {
      val scheduledFuture = Scheduler.schedule(self, GenerateImage, ImageGenerationFrequency, ImageGenerationFrequency, TimeUnit.MILLISECONDS)
      scheduled = Some(scheduledFuture)
    }
  }

  override def postStop() {
    for (scheduledFuture ← scheduled) scheduledFuture.cancel(false)
  }

  def receive = {
    case result: WorkResult ⇒ applyResult(result)
    case GenerateImage      ⇒ generateImage()
  }

  def applyResult(result: WorkResult) = result match {
    case r: RayTraceWorkResult ⇒
      var counter = 0
      if (buffer.isEmpty) {
        buffer = new ArrayBuffer[RGBColor](r.result.size)
        r.result.foreach({ color ⇒
          buffer += color
          counter += 1
        })
      } else {
        r.result.foreach({ color ⇒
          buffer(counter) = buffer(counter) + color
          counter += 1
        })
      }

      resultCounter += 1
      EventHandler.debug(this, "Applying result from worker [%s], total calculated items: [%s]".format(resultCounter, r.workerId))
  }

  def generateImage() {
    if (resultCounter > previousResultNumber) {
      EventHandler.debug(this, "Generating image")
      previousResultNumber = resultCounter
      val scale = 1.0f / resultCounter
      val image = new JBufferedImage(width, height, JBufferedImage.TYPE_INT_RGB)
      image.setRGB(0, 0, width, height, buffer.map(color ⇒ (color * scale).asInt).toArray, 0, width)
      val file = new JFile(ImageName)
      JImageIO.write(image, "png", file)
    }

  }
}

object WorkAggregator {
  val ImageGenerationFrequency = config.getInt("akka.demo.image.generationFrequency", 5000)
  val ImageWidth = config.getInt("akka.demo.image.width", 800)
  val ImageHeight = config.getInt("akka.demo.image.height", 800)
  val ImageName = config.getString("akka.demo.image.name", "result.png")
}