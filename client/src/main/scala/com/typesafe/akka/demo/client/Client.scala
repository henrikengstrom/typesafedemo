/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import akka.actor.{Props, ActorSystem}
import akka.kernel.Bootable

class Client extends Bootable {
  // Start the actor system and a worker actor
  val system = ActorSystem("RaytraceClient")
  val clientWorker = system.actorOf(Props[ClientWorker])

  def startup() {
    clientWorker ! InitializeCommunication
  }

  def shutdown() {
    system.shutdown()
  }
}

object Client extends App {
  new Client
}