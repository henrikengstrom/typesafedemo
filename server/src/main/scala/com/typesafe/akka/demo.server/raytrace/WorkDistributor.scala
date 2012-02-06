/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import roygbiv.scene.json.JsonSceneLoader
import roygbiv.scene.{ SceneLoaderOrchestrator, LoadScene, Scene }
import com.typesafe.akka.demo.raytrace.RayTraceWorkInstruction
import com.typesafe.akka.demo.{ Stop, Pause, Start, ClientRegistration }
import akka.actor.{Props, Actor}

class WorkDistributor extends Actor {
  import WorkDistributor._

  // TODO : use Actors as state machine
  var state: Int = _

  override def preStart() {
    if (!scene.isDefined) loadScene()
  }

  override def postRestart(reason: Throwable) {
    if (!scene.isDefined) loadScene()
  }

  def receive = {
    case s: Scene ⇒
      scene = Some(s)
      context.actorOf(Props[WorkAggregator]) ! s
    case c: ClientRegistration ⇒
      clients = c +: clients
      context.actorFor(c.remoteAddress) !
        RayTraceWorkInstruction(
          context.system.settings.config.getString("akka.raytracing.aggregator.address"),
          scene.get)
      if (state == Started) context.actorFor(c.remoteAddress) ! Start
    case "Start" ⇒
      println("*** server started")
      state = Started
      for (c ← clients) context.actorFor(c.remoteAddress) ! Start
    case "Pause" ⇒
      println("*** server paused")
      state = Paused
      for (c ← clients) context.actorFor(c.remoteAddress) ! Pause
    case "Stop" ⇒
      println("*** server stopped")
      state = Stopped
      for (c ← clients) context.actorFor(c.remoteAddress) ! Stop
  }

  private def loadScene() = {
    val loader = context.actorOf(Props[SceneLoaderOrchestrator], "sceneLoaderOrchestrator")
    loader ! LoadScene(JsonSceneLoader.SceneType,
      context.system.settings.config.getString("akka.raytracing.scenedefinition"))
  }
}

object WorkDistributor {
  var scene: Option[Scene] = None
  var clients: Vector[ClientRegistration] = Vector()
  final val Started = 1
  final val Paused = 2
  final val Stopped = 3
}