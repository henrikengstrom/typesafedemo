/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import raytrace.{ WorkAggregator, WorkDistributor }
import akka.remote
import akka.actor.ActorSystem

class Server extends Bootable {
  import Server._

  val system = ActorSystem("RaytraceServer")

  // start the remoting as specified in configuration
  val serverHost = system.settings.config.getString("akka.remote.server.hostname")
  val serverPort = system.settings.config.getInt("akka.remote.server.port")

  println("*** STARTING SERVER [%s, %s]".format(serverHost, serverPort))

  remote.start(serverHost, serverPort)
  remote.register(supervisorServiceId, supervisorRef)
  remote.register(aggregatorServiceId, aggregatorRef)
}

object Server {
  lazy val supervisorRef = system.actorOf[WorkSupervisor]

  val aggregatorServiceId = config.getString("akka.demo.aggregator.serviceId", "aggregator")
  val supervisorServiceId = config.getString("akka.demo.supervisor.serviceId", "supervisor")
}