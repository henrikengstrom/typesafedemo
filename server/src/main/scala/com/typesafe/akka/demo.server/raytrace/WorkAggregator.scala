/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import com.typesafe.akka.demo.raytrace.RayTraceWorkResult
import roygbiv.color.RGBColor

import collection.mutable.ArrayBuffer

import java.awt.image.{BufferedImage ⇒ JBufferedImage}
import java.io.{File ⇒ JFile}
import javax.imageio.{ImageIO ⇒ JImageIO}
import roygbiv.scene.Scene
import com.typesafe.akka.demo.{Start, WorkResult}
import java.util.concurrent.TimeUnit
import akka.util.Duration
import akka.actor.{ActorRef, Cancellable, Actor}
import javax.swing.ImageIcon

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

  var webClient: Option[ActorRef] = None
  
  override def preStart() {
    if (scheduled.isEmpty) {
      val conf = context.system.settings.config
      val cancellable =
        context.system.scheduler.schedule(
          Duration(conf.getMilliseconds("akka.raytracing.image.generation-frequency"), TimeUnit.MILLISECONDS),
          Duration(conf.getMilliseconds("akka.raytracing.image.generation-frequency"), TimeUnit.MILLISECONDS),
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
    case GenerateImage ⇒ generateImage()
    case s: Scene ⇒ scene = Some(s)
    case "Start" ⇒ startTime = System.nanoTime
    case "WebReceiver" =>
      // TODO : add a list of listeners/subscribers
      webClient = Some(sender)
  }

  def applyResult(result: WorkResult) = result match {
    case r: RayTraceWorkResult ⇒
      var counter = 0
      if (buffer.isEmpty) {
        buffer = new ArrayBuffer[RGBColor](r.result.size)
        r.result.foreach({
          color ⇒
            buffer += color
            counter += 1
        })
      } else {
        r.result.foreach({
          color ⇒
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

      // Update clients
      println("*** sending result to: " + webClient)
      for (web <- webClient) web ! raysPerSecond
  }

  def generateImage() {
    if (resultCounter > previousResultNumber) {
      val camera = scene.get.camera
      previousResultNumber = resultCounter
      val scale = 1.0f / resultCounter
      val image = new JBufferedImage(camera.screenWidth, camera.screenHeight, JBufferedImage.TYPE_INT_RGB)
      image.setRGB(0, 0, camera.screenWidth, camera.screenHeight, buffer.map(color ⇒ (color * scale).asInt).toArray, 0, camera.screenWidth)
      //val file = new JFile(context.system.settings.config.getString("akka.raytracing.image.name"))
      //JImageIO.write(image, "png", file)
      for (web <- webClient) web ! new ImageIcon(image)
    }
  }
}

object WorkAggregator {
  var scene: Option[Scene] = None
}
