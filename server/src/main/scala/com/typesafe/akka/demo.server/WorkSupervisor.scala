/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import akka.actor.Actor
import akka.actor.Actor._
import akka.event.EventHandler
import com.typesafe.akka.demo.{ Pause, Stop, Start, ClientRegistration }
import raytrace.{ WorkAggregator, WorkDistributor }

class WorkSupervisor extends Actor {
  def receive = {
    case registration: ClientRegistration ⇒
      actorOf[WorkDistributor].start() ! registration
    case Start ⇒
      actorOf[WorkDistributor].start() ! Start
      actorOf[WorkAggregator].start() ! Start
    case Pause ⇒ actorOf[WorkDistributor].start() ! Pause
    case Stop  ⇒ actorOf[WorkDistributor].start() ! Stop
    case other ⇒ EventHandler.info(this, "Received unknown message: " + other)
  }
}