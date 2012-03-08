/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import com.typesafe.akka.demo.client.raytrace.Worker
import akka.actor.{Props, ActorRef, Actor}
import com.typesafe.akka.demo._

case object InitializeCommunication

class ClientWorker extends Actor {
  var worker: Option[ActorRef] = None

  def receive = {
    case InitializeCommunication =>
      context.actorFor(context.system.settings.config.getString("akka.typesafedemo.server-work-distributor")) ! ClientRegistration
    case instruction: WorkInstruction ⇒
      val actor = context.actorOf(Props[Worker], "worker")
      actor ! instruction
      worker = Some(actor)
    case Start ⇒ for (w ← worker) w ! Start
    case Pause ⇒ for (w ← worker) w ! Pause
    case Stop ⇒
      for (w ← worker) w ! Stop
      worker = None
  }
}
