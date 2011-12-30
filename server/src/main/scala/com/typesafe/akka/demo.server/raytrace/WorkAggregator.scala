/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import com.typesafe.akka.demo.raytrace.RayTraceWorkResult
import roygbiv.color.RGBColor

import collection.mutable.ArrayBuffer

import java.awt.image.{ BufferedImage ⇒ JBufferedImage }
import java.io.{ File ⇒ JFile }
import javax.imageio.{ ImageIO ⇒ JImageIO }
import roygbiv.scene.Scene
import com.typesafe.akka.demo.{ Start, WorkResult }
import akka.actor.{Cancellable, Actor}
import java.util.concurrent.TimeUnit
import akka.util.Duration

case object GenerateImage

class WorkAggregator extends Actor {
  import WorkAggregator._

  var scheduled: Option[Cancellable] = None
  var buffer = new ArrayBuffer[RGBColor]()
  var resultCounter = 0
  var previousResultNumber = 0

  var startTime: Long = 0L
  var raysPerSecond: Long = 0L
  var rays = 0L

  override def preStart() {
    if (scheduled.isEmpty) {
      val conf = context.system.settings.config
      val cancellable = 
        context.system.scheduler.schedule(
          Duration(conf.getMilliseconds("akka.scheduler.tickDuration"), TimeUnit.MILLISECONDS),
          Duration(conf.getMilliseconds("akka.scheduler.tickDuration"), TimeUnit.MILLISECONDS),
          self, 
          GenerateImage)
      scheduled = Some(cancellable)
    }
  }

  override def postStop() {
    for (cancellable ← scheduled) cancellable.cancel()
  }

  def receive = {
    case result: WorkResult ⇒ applyResult(result)
    case GenerateImage      ⇒ generateImage()
    case s: Scene           ⇒ scene = Some(s)
    case Start              ⇒ startTime = System.nanoTime
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
      rays = (scene.get.camera.screenWidth * scene.get.camera.screenHeight) * resultCounter
      raysPerSecond = rays / ((System.nanoTime - startTime) / 1000000000)

      println("*** RESULTS DELIVERED: " + resultCounter)
      println("*** RAYS CALCULATED  : " + rays)
      println("*** RAYS/SECOND      : " + raysPerSecond)

    // TODO (HE) : Add logging
    //EventHandler.debug(this, "Applying result from worker [%s], total calculated items: [%s]".format(resultCounter, r.workerId))
  }

  def generateImage() {
    if (resultCounter > previousResultNumber) {
      val camera = scene.get.camera
      previousResultNumber = resultCounter
      val scale = 1.0f / resultCounter
      val image = new JBufferedImage(camera.screenWidth, camera.screenHeight, JBufferedImage.TYPE_INT_RGB)
      image.setRGB(0, 0, camera.screenWidth, camera.screenHeight, buffer.map(color ⇒ (color * scale).asInt).toArray, 0, camera.screenWidth)
      val file = new JFile(context.system.settings.config.getString("akka.raytracing.image.name"))
      JImageIO.write(image, "png", file)
    }
  }
}

object WorkAggregator {
  var scene: Option[Scene] = None
}
