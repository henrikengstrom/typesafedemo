/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import akka.actor.{Props, ActorSystem}
import akka.kernel.Bootable
import com.typesafe.akka.demo.{WorkInstruction, ClientRegistration}


class Client extends Bootable {
  // Start the actor system and a worker actor
  val system = ActorSystem("RaytraceClient")
  val clientWorker = system.actorOf(Props[ClientWorker], "clientWorker")

  // Call remote server and register client
  system.actorFor(system.settings.config.getString("akka.typesafedemo.server-work-distributor")) !
    ClientRegistration(system.settings.config.getString("akka.typesafedemo.this-client"))

  println("*** RAY TRACE CLIENT STARTED ***")

  def startup() {}

  def shutdown() {
    system.shutdown()
  }
}

object Client extends App {
  new Client
}