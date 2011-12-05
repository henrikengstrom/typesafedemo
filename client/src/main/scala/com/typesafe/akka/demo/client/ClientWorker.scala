/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import akka.actor.Actor._
import com.typesafe.akka.demo.WorkInstruction
import com.typesafe.akka.demo.{ Start, Stop, Pause }
import raytrace.Worker
import akka.actor.{ PoisonPill, ActorRef, Actor }

class ClientWorker extends Actor {
  var worker: Option[ActorRef] = None

  def receive = {
    case instruction: WorkInstruction ⇒
      val actor = actorOf[Worker]
      actor.start() ! instruction
      worker = Some(actor)
    case Start ⇒
      for (w ← worker) w ! Start
    case Pause ⇒
      for (w ← worker) w ! Pause
    case Stop ⇒
      for (w ← worker) w ! Stop
      for (w ← worker) w ! PoisonPill
      worker = None
  }
}
