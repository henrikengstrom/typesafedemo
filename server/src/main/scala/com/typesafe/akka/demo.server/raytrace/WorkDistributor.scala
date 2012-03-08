/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import roygbiv.scene.json.JsonSceneLoader
import roygbiv.scene.{ SceneLoaderOrchestrator, LoadScene, Scene }
import com.typesafe.akka.demo.raytrace.RayTraceWorkInstruction
import com.typesafe.akka.demo._
import akka.actor.{ActorRef, Props, Actor}

class WorkDistributor extends Actor {
  var scene: Option[Scene] = None
  var clients: Vector[ActorRef] = Vector()
  final val Started = 1
  final val Paused = 2
  final val Stopped = 3

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
      context.actorFor("/user/aggregator")  ! s
    case ClientRegistration ⇒
      clients = sender +: clients
      sender !
        RayTraceWorkInstruction(
          context.system.settings.config.getString("akka.raytracing.aggregator.address"),
          scene.get)
      if (state == Started) sender ! Start
    case Start ⇒
      println("*** STARTING")
      state = Started
      for (c ← clients) c ! Start
    case Pause ⇒
      state = Paused
      for (c ← clients) c ! Pause
    case Stop ⇒
      println("*** STOPPING")
      state = Stopped
      for (c ← clients) c ! Stop
  }

  private def loadScene() = {
    val loader = context.actorOf(Props[SceneLoaderOrchestrator], "sceneLoaderOrchestrator")
    loader ! LoadScene(JsonSceneLoader.SceneType,
      context.system.settings.config.getString("akka.raytracing.scene-definition"))
  }
}