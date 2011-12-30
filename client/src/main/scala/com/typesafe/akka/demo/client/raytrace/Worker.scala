/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client.raytrace

import com.typesafe.akka.demo.raytrace.{ RayTraceWorkResult, RayTraceWorkInstruction }
import com.typesafe.akka.demo.{ Start, Stop, Pause }
import roygbiv.scene.Scene
import roygbiv.common.WorkResult
import roygbiv.worker.RayTracer
import akka.actor.{ Props, PoisonPill, ActorRef, Actor }

class Worker extends Actor {

  import Worker._

  var aggregatorServer: String = ""
  var scene: Option[Scene] = None
  var workerHandles: Vector[ActorRef] = Vector()

  def receive = {
    case RayTraceWorkInstruction(server, theScene) ⇒
      aggregatorServer = server
      scene = Some(theScene)
    case Start ⇒
      if (scene.isDefined) {
        // Start as many workers as there are cores on the machine (to optimize power)
        // Each of the workers get a pinned dispatcher: http://akka.io/docs/akka/2.0-M1/scala/dispatchers.html
        1 until availableProcessors foreach { i =>
          val worker = context.actorOf(Props[RayTracer].withDispatcher(context.system.dispatcherFactory.newPinnedDispatcher("rt" + i)))
          worker ! new roygbiv.worker.Work(scene.get)
          workerHandles = worker +: workerHandles
        }
      }
    case Pause ⇒
      workerHandles.foreach(handle ⇒ handle ! roygbiv.worker.Pause)
    case Stop ⇒
      workerHandles.foreach(handle ⇒ handle ! PoisonPill)
      workerHandles = Vector()
      self ! PoisonPill
    case wr: WorkResult ⇒
      context.actorFor(aggregatorServer) ! RayTraceWorkResult(wr.workerId, availableProcessors, wr.result)
    case other ⇒
    // TODO (HE): Log
  }
}

object Worker {
  val availableProcessors = Runtime.getRuntime.availableProcessors
}
