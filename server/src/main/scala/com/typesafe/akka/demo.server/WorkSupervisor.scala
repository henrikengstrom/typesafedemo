/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import com.typesafe.akka.demo.{ Pause, Stop, Start, ClientRegistration }
import raytrace.{ WorkAggregator, WorkDistributor }
import akka.actor.{ Props, Actor }

class WorkSupervisor extends Actor {

  def receive = {
    case registration: ClientRegistration ⇒
      context.actorOf(Props[WorkDistributor]) ! registration
    case Start ⇒
      context.actorOf(Props[WorkDistributor]) ! Start
      context.actorOf(Props[WorkAggregator]) ! Start
    case Pause ⇒
      context.actorOf(Props[WorkDistributor]) ! Pause
    case Stop ⇒
      context.actorOf(Props[WorkDistributor]) ! Stop
    case other ⇒
    // TODO (HE) : Add logging
    //EventHandler.info(this, "Received unknown message: " + other)
  }
}