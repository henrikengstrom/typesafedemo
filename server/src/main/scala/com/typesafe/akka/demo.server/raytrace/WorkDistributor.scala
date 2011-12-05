/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import akka.actor.Actor
import akka.actor.Actor._
import akka.config.Config.config
import roygbiv.scene.json.JsonSceneLoader
import roygbiv.scene.{ SceneLoaderOrchestrator, LoadScene, Scene }
import com.typesafe.akka.demo.raytrace.RayTraceWorkInstruction
import com.typesafe.akka.demo.{ Stop, Pause, Start, ClientRegistration }

class WorkDistributor extends Actor {
  import WorkDistributor._

  override def preStart() {
    if (!scene.isDefined) loadScene()
  }

  override def postRestart(reason: Throwable) {
    if (!scene.isDefined) loadScene()
  }

  def receive = {
    case s: Scene ⇒
      scene = Some(s)
      actorOf[WorkAggregator].start() ! s
    case c: ClientRegistration ⇒
      clients = c +: clients
      remote.actorFor(c.serviceId, c.server, c.port) ! RayTraceWorkInstruction(aggregatorServer, aggregatorServerPort, aggregatorServiceId, scene.get)
    case Start ⇒
      for (c ← clients) remote.actorFor(c.serviceId, c.server, c.port) ! Start
    case Pause ⇒
      for (c ← clients) remote.actorFor(c.serviceId, c.server, c.port) ! Pause
    case Stop ⇒
      for (c ← clients) remote.actorFor(c.serviceId, c.server, c.port) ! Stop
  }

  private def loadScene() = {
    val loader = actorOf[SceneLoaderOrchestrator].start()
    loader ! LoadScene(JsonSceneLoader.SceneType, sceneDefinition)
  }
}

object WorkDistributor {
  var scene: Option[Scene] = None
  var clients: Vector[ClientRegistration] = Vector()
  val sceneDefinition = config.getString("akka.raytracing.sceneDefinition", "/Volumes/untitled/typesafedemo/server/src/main/resources/TS_600x400.lcj")
  val aggregatorServiceId = config.getString("akka.raytracing.aggregator.serviceId", "aggregator")
  val aggregatorServer = config.getString("akka.raytracing.aggregator.server", "127.0.0.1")
  val aggregatorServerPort = config.getInt("akka.raytracing.aggregator.port", 2552)
}