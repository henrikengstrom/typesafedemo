/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client.raytrace

import akka.actor.Actor._
import akka.event.EventHandler
import akka.dispatch.Dispatchers
import akka.actor.{ PoisonPill, ActorRef, Actor }
import com.typesafe.akka.demo.raytrace.{ RayTraceWorkResult, RayTraceWorkInstruction }
import com.typesafe.akka.demo.{ Start, Stop, Pause }
import roygbiv.scene.Scene
import roygbiv.common.WorkResult
import roygbiv.worker.RayTracer

class Worker() extends Actor {

  import Worker._

  var aggregatorServer: String = ""
  var aggregatorPort: Int = 0
  var aggregatorServiceId: String = ""
  var scene: Option[Scene] = None
  var workerHandles: Vector[ActorRef] = Vector()

  def receive = {
    case RayTraceWorkInstruction(server, port, serviceId, theScene) ⇒
      aggregatorServer = server
      aggregatorPort = port
      aggregatorServiceId = serviceId
      scene = Some(theScene)
    case Start ⇒
      if (scene.isDefined) {
        // Start as many workers as there are cores on the machine (to optimize power)
        for (i ← 1.until(availableProcessors)) {
          val worker = actorOf(new RayTracer(WorkerDispatcher))
          worker.start() ! new roygbiv.worker.Work(scene.get)
          workerHandles = worker +: workerHandles
        }
      }
    case Pause ⇒
      workerHandles.foreach(handle ⇒ handle ! roygbiv.worker.Pause)
    case Stop ⇒
      workerHandles.foreach(handle ⇒ handle ! PoisonPill)
      workerHandles = Vector()
    case wr: WorkResult ⇒
      remote.actorFor(aggregatorServiceId, aggregatorServer, aggregatorPort) ! RayTraceWorkResult(wr.workerId, availableProcessors, wr.result)
    case other ⇒
      EventHandler.error(this, "Received unexpected message!")
  }
}

/**
 * This worker implementation "knows" what work will be performed and can, therefore, decide what dispatcher to use.
 * In this example we will render images and the best way to achieve maximal performance is to have dedicated threads
 * for the rendering. This means that on a 4 core machine there will be 4 dedicated threads to render in. This will
 * minimize context switching between threads and, hopefully, increase the performance of the application.
 */
object Worker {
  val availableProcessors = Runtime.getRuntime.availableProcessors

  lazy val WorkerDispatcher = Dispatchers.newExecutorBasedEventDrivenDispatcher("worker-dispatcher")
    .setCorePoolSize(availableProcessors)
    .build
}
