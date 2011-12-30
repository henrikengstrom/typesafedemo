/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import com.typesafe.akka.demo.WorkInstruction
import com.typesafe.akka.demo.{ Start, Stop, Pause }
import raytrace.Worker
import akka.actor.{Props, ActorRef, Actor}

class ClientWorker extends Actor {
  var worker: Option[ActorRef] = None

  def receive = {
    case instruction: WorkInstruction ⇒
      val actor = context.actorOf(Props[Worker], "worker")
      actor ! instruction
      worker = Some(actor)
    case Start ⇒
      for (w ← worker) w ! Start
    case Pause ⇒
      for (w ← worker) w ! Pause
    case Stop ⇒
      for (w ← worker) w ! Stop
      worker = None
  }
}
