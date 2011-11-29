/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import akka.actor.Actor
import akka.actor.Actor._
import com.typesafe.akka.demo.WorkInstruction
import raytrace.Worker

class ClientWorker extends Actor {
  def receive = {
    case instruction: WorkInstruction ⇒ actorOf[Worker].start() ! instruction
  }
}
