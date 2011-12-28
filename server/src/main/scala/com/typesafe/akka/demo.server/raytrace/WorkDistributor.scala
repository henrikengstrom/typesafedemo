/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import akka.config.Config.config
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
      remote.actorFor(c.serviceId, c.server, c.port) !
        RayTraceWorkInstruction(
          context.system.settings.config.getString("akka.raytracing.aggregator.server"),
          context.system.settings.config.getInt("akka.raytracing.aggregator.port"),
          context.system.settings.config.getString("akka.raytracing.aggregator.serviceId"),
          scene.get)
      if (state == Started) remote.actorFor(c.serviceId, c.server, c.port) ! Start
    case Start ⇒
      state = Started
      for (c ← clients) remote.actorFor(c.serviceId, c.server, c.port) ! Start
    case Pause ⇒
      state = Paused
      for (c ← clients) remote.actorFor(c.serviceId, c.server, c.port) ! Pause
    case Stop ⇒
      state = Stopped
      for (c ← clients) remote.actorFor(c.serviceId, c.server, c.port) ! Stop
  }

  private def loadScene() = {
    val loader = context.actorOf(Props[SceneLoaderOrchestrator])
    loader ! LoadScene(JsonSceneLoader.SceneType,
      context.system.settings.config.getString("akka.raytracing.sceneDefinition"))
  }
}

object WorkDistributor {
  var scene: Option[Scene] = None
  var clients: Vector[ClientRegistration] = Vector()
  final val Started = 1
  final val Paused = 2
  final val Stopped = 3
}