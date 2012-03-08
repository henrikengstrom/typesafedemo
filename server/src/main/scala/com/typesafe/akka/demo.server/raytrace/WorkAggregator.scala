/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import com.typesafe.akka.demo.raytrace.RayTraceWorkResult
import roygbiv.color.RGBColor

import collection.mutable.ArrayBuffer

import java.awt.image.{BufferedImage ⇒ JBufferedImage}
import roygbiv.scene.Scene
import java.util.concurrent.TimeUnit
import akka.util.Duration
import akka.actor.{ActorRef, Cancellable, Actor}
import javax.swing.ImageIcon
import com.typesafe.akka.demo.{RenderingStatistics, ClientRegistration, Start, WorkResult}

case object GenerateImage

class WorkAggregator extends Actor {
  var scheduled: Option[Cancellable] = None
  var previousLayers = 0L
  var buffer = new ArrayBuffer[RGBColor]()
  var scene: Option[Scene] = None
  var clients = Seq[ActorRef]()
  var startTime = 0L
  var rays = 0L
  var layers = 0
  var rayPayload = 0L
    
  override def preStart() {
    if (scheduled.isEmpty) {
      val cancellable =
        context.system.scheduler.schedule(
          Duration(context.system.settings.config.getMilliseconds("akka.raytracing.image.generation-frequency"), TimeUnit.MILLISECONDS),
          Duration(context.system.settings.config.getMilliseconds("akka.raytracing.image.generation-frequency"), TimeUnit.MILLISECONDS),
          self,
          GenerateImage)
      scheduled = Some(cancellable)
    }
  }

  override def postStop() {
    for (cancellable ← scheduled) cancellable.cancel()
  }

  def receive = {
    case s: Scene =>
      println("****** SETTING SCENE IN ACTOR : " + self)
      startTime = System.nanoTime
      scene = Some(s)
      rayPayload = s.camera.screenWidth * s.camera.screenHeight
    case result: WorkResult ⇒ 
      println("****** GOT RESULT FROM: " + result.workerId)
      applyResult(result)
    case GenerateImage ⇒ generateImage()
    case client: ActorRef => 
      println("****** ADDING CLIENT: " + client)
      clients = client +: clients
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

      rays = rays + rayPayload
      layers = layers + 1

      val raysPerSecond = rays / ((System.nanoTime - startTime) / 1000000000)
      pushResult(RenderingStatistics(layers, rays, raysPerSecond))
  }

  def generateImage() {
    if (layers > previousLayers && buffer.size != 0) {
      previousLayers = layers
      val camera = scene.get.camera
      val scale = 1.0f / layers           
      val image = new JBufferedImage(camera.screenWidth, camera.screenHeight, JBufferedImage.TYPE_INT_RGB)
      image.setRGB(0, 0, camera.screenWidth, camera.screenHeight, buffer.map(color ⇒ (color * scale).asInt).toArray, 0, camera.screenWidth)
      pushResult(new ImageIcon(image))
    }
  }

  def pushResult(result: Any) = {
    def pushToClient(client: ActorRef, result: Any) = {
      try {
        println("*** pushing result to client: " + client)
        client ! result
      } catch {
        case e: Exception =>
          // Simplistic/naive error handing -
          // just remove from client from clients since we could not communicate with it.
          // Should be more forgiving in a real-app situation.
          println("*********************************************************")
          println("--> REMOVING CLIENT: " + client)
          println("--> EXCEPTION      : " + e.toString)
          println("*********************************************************")
          clients = clients.filterNot(_ == client)
      }
    }

    for (client <- clients) pushToClient(client, result)
  }
}