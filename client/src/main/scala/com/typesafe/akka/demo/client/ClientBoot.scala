/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import akka.actor.SupervisorFactory
import akka.config.Supervision
import akka.config.Supervision._
import akka.http.RootEndpoint
import akka.config.Config.config
import akka.actor.Actor._
import com.typesafe.akka.demo.ClientRegistration

object Client {
  def main(args: Array[String]) {
    new ClientBoot
  }
}

class ClientBoot {
  val clientHost = config.getString("akka.remote.server.hostname", "127.0.0.1")
  val clientPort = config.getInt("akka.remote.server.port", 2553)
  val clientWorker = actorOf[ClientWorker].start()
  val serverHost = config.getString("akka.typesafedemo.remote.hostname", "127.0.0.1")
  val serverPort = config.getInt("akka.typesafedemo.remote.port", 2552)
  val serviceId = "supervisor"

  val factory = SupervisorFactory(
    SupervisorConfig(OneForOneStrategy(List(classOf[Exception]), 3, 100),
      Supervision.Supervise(actorOf[RootEndpoint], Permanent) ::
        Supervision.Supervise(clientWorker, Permanent) ::
        Nil))

  factory.newInstance.start

  // Register clientWorker as an actor waiting for work
  remote.start(clientHost, clientPort)
  remote.register("clientWorker", clientWorker)

  // Call server and ask for work
  remote.actorFor(serviceId, serverHost, serverPort) ! ClientRegistration("clientWorker", clientHost, clientPort)
}