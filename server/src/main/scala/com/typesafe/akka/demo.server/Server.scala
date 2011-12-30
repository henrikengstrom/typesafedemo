/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import akka.kernel.Bootable
import akka.actor.{Props, ActorSystem}
import raytrace.{WorkDistributor, WorkAggregator}
import com.typesafe.akka.demo.{Start, Pause, Stop}

class Server extends Bootable {
  val system = ActorSystem("RaytraceServer")
  val aggregator = system.actorOf(Props[WorkAggregator], "aggregator")
  val distributor = system.actorOf(Props[WorkDistributor], "distributor")
  println("*** RAY TRACE SERVER STARTED ***")

  def startup() {
    // TODO (HE): temp start while Play 2.0-RCx is not running on Akka 2.0
    start()
  }

  def shutdown() {
    system.shutdown()
  }
  
  def start(): Unit = {
    distributor ! Start
    aggregator ! Start
  }

  def pause(): Unit = {
    distributor ! Pause
  }

  def stop(): Unit = {
    distributor ! Stop
  }
}

object Server extends App {

  val s = new Server
  
  def start(): Unit = {
    s.start()  
  }
  
  def pause(): Unit = {
    s.pause()
  }
  
  def stop(): Unit = {
    s.stop()
  }
}
