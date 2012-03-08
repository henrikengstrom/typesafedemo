/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import akka.kernel.Bootable
import akka.actor.{Props, ActorSystem}
import raytrace.{WorkDistributor, WorkAggregator}

class Server extends Bootable {
  val system = ActorSystem("RaytraceServer")
  val aggregator = system.actorOf(Props[WorkAggregator], "aggregator")
  val distributor = system.actorOf(Props[WorkDistributor], "distributor")

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object Server extends App {
  new Server
}