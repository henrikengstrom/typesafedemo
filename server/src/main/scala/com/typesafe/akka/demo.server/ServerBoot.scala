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

class ServerBoot {
  import ServerBoot._

  val workSupervisor = actorOf[WorkSupervisor].start()
  val workDistributor = actorOf[WorkDistributor].start()
  val workAggregator = actorOf[WorkAggregator].start()

  val factory = SupervisorFactory(
    SupervisorConfig(
      OneForOneStrategy(List(classOf[Exception]), 3, 100),
      Supervision.Supervise(actorOf[RootEndpoint], Permanent) ::
        Supervision.Supervise(workSupervisor, Permanent) ::
        Supervision.Supervise(workDistributor, Permanent) ::
        Supervision.Supervise(workAggregator, Permanent) ::
        Nil))

  factory.newInstance.start

  val supervisor = actorOf[WorkSupervisor].start()
  val aggregator = actorOf[WorkAggregator].start()

  // start the remoting as specified in configuration
  val serverHost = config.getString("akka.remote.server.hostname", "127.0.0.1")
  val serverPort = config.getInt("akka.remote.server.port", 2552)

  remote.start(serverHost, serverPort)
  remote.register(supervisorServiceId, supervisor)
  remote.register(aggregatorServiceId, aggregator)
}

object ServerBoot {
  val aggregatorServiceId = config.getString("akka.demo.aggregator.serviceId", "aggregator")
  val supervisorServiceId = config.getString("akka.demo.supervisor.serviceId", "supervisor")
}