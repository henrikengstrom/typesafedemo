/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.client.raytrace

import akka.actor.Actor
import akka.actor.Actor._
import akka.event.EventHandler
import akka.dispatch.Dispatchers
import com.typesafe.akka.demo.raytrace.{ RayTraceWorkResult, RayTraceWorkInstruction }
import roygbiv.common.WorkResult
import roygbiv.worker.RayTracer

class Worker() extends Actor {

  import Worker._

  var aggregatorServer: String = ""
  var aggregatorPort: Int = 0
  var aggregatorServiceId: String = ""

  def receive = {
    case RayTraceWorkInstruction(server, port, serviceId, scene) ⇒
      aggregatorServer = server
      aggregatorPort = port
      aggregatorServiceId = serviceId
      // Start as many workers as there are cores on the machine (to optimize power)
      for (i <- 1.until(availableProcessors)) {
        actorOf(new RayTracer(WorkerDispatcher)).start() ! new roygbiv.worker.Work(scene)
      }
    case wr: WorkResult ⇒
      remote.actorFor(aggregatorServiceId, aggregatorServer, aggregatorPort) ! RayTraceWorkResult(wr.workerId, wr.result)
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
