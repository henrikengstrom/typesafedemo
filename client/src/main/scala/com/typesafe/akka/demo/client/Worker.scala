/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import akka.actor.Actor
import com.typesafe.akka.demo.WorkInstruction

class Worker extends Actor {
  def receive = {
    case work: WorkInstruction => println("working...")
  }
}