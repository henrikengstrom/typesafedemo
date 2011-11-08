/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client

import akka.actor.Actor
import akka.event.EventHandler
import com.typesafe.akka.demo.WorkInstruction

class ClientWorker extends Actor {
  import ClientWorker._

  def receive = {
    case work: WorkInstruction => actorOf(new Worker(ServerHost, ServerPort)).start() ! work
    case other => EventHandler.error(this, "Received unexpected message [%s]".format(other))
  }
}

object ClientWorker {
  val ServerHost = config.getString("akka.roygbiv.remote.hostname", "127.0.0.1")
  val ServerPort = config.getInt("akka.roygbiv.remote.port", 2552)
}
