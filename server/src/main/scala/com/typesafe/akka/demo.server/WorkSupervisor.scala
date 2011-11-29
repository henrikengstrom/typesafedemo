/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import akka.actor.Actor
import akka.actor.Actor._
import com.typesafe.akka.demo.ClientRegistration
import akka.event.EventHandler
import raytrace.WorkDistributor

class WorkSupervisor extends Actor {
  def receive = {
    case registration : ClientRegistration ⇒ actorOf[WorkDistributor].start() ! registration
    case other                             ⇒ EventHandler.info(this, "Received unknown message: " + other)
  }
}