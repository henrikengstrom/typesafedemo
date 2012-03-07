/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import org.scalatest.WordSpec
import akka.testkit.TestActorRef
import raytrace.WorkAggregator
import akka.actor.{ActorSystem, Props}

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class WorkAggregatorSpec extends WordSpec {
  implicit val system = ActorSystem("test")
  val actor: WorkAggregator = TestActorRef(Props[WorkAggregator]).underlyingActor

  // TODO : implement proper test!
  "A" must {
    "B" in {
      actor.generateImage()
    }
  }
}