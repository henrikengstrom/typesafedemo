/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import akka.actor.Actor._
import akka.actor.SupervisorFactory
import akka.config.Config.config
import akka.config.Supervision
import akka.config.Supervision._
import akka.http.RootEndpoint
import raytrace.{ WorkAggregator, WorkDistributor }

class Server {
  import Server._

  supervisorRef.start()
  distributorRef.start()
  aggregatorRef.start()

  val factory = SupervisorFactory(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      Supervision.Supervise(actorOf[RootEndpoint], Permanent) ::
        Supervision.Supervise(supervisorRef, Permanent) ::
        Supervision.Supervise(distributorRef, Permanent) ::
        Supervision.Supervise(aggregatorRef, Permanent) ::
        Nil))

  factory.newInstance.start

  // start the remoting as specified in configuration
  val serverHost = config.getString("akka.remote.server.hostname", "127.0.0.1")
  val serverPort = config.getInt("akka.remote.server.port", 2552)

  remote.start(serverHost, serverPort)
  remote.register(supervisorServiceId, supervisorRef)
  remote.register(aggregatorServiceId, aggregatorRef)
}

object Server {
  lazy val aggregatorRef = actorOf[WorkAggregator]
  lazy val distributorRef = actorOf[WorkDistributor]
  lazy val supervisorRef = actorOf[WorkSupervisor]

  val aggregatorServiceId = config.getString("akka.demo.aggregator.serviceId", "aggregator")
  val supervisorServiceId = config.getString("akka.demo.supervisor.serviceId", "supervisor")
}