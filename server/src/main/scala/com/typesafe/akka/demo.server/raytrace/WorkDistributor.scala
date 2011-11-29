/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server.raytrace

import akka.actor.Actor
import akka.actor.Actor._
import akka.config.Config.config
import roygbiv.scene.json.JsonSceneLoader
import roygbiv.scene.{ SceneLoaderOrchestrator, LoadScene, Scene }
import com.typesafe.akka.demo.ClientRegistration
import com.typesafe.akka.demo.raytrace.RayTraceWorkInstruction

class WorkDistributor extends Actor {
  import WorkDistributor._

  override def preStart() {
    if (!scene.isDefined) loadScene()
  }

  override def postRestart(reason: Throwable) {
    if (!scene.isDefined) loadScene()
  }

  def receive = {
    case s: Scene ⇒ scene = Some(s)
    case ClientRegistration(serviceId, server, port) ⇒
      remote.actorFor(serviceId, server, port) ! RayTraceWorkInstruction(aggregatorServer, aggregatorServerPort, aggregatorServiceId, scene.get)
  }

  private def loadScene() = {
    val loader = actorOf[SceneLoaderOrchestrator].start()
    loader ! LoadScene(JsonSceneLoader.SceneType, sceneDefinition)
  }
}

object WorkDistributor {
  var scene: Option[Scene] = None
  val sceneDefinition = config.getString("akka.demo.sceneDefinition", "TypesafeDemoScene.lcj")
  val aggregatorServiceId = config.getString("akka.demo.aggregator.serviceId", "aggregator")
  val aggregatorServer = config.getString("akka.demo.aggregator.server", "127.0.0.1")
  val aggregatorServerPort = config.getInt("akka.demo.aggregator.port", 2552)
}