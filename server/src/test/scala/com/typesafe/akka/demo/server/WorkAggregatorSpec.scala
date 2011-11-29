/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import org.scalatest.WordSpec
import akka.testkit.TestActorRef
import raytrace.WorkAggregator

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class WorkAggregatorSpec extends WordSpec {
  val actor: WorkAggregator = TestActorRef[WorkAggregator].underlyingActor

  // TODO : implement proper test!
  "A" must {
    "B" in {
      actor.generateImage()
    }
  }
}