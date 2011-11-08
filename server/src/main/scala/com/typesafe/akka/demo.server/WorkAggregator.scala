/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.server

import akka.actor.Actor

class WorkAggregator extends Actor {
  def receive = {
    case result: WorkResult => applyResult(result)
    case GenerateImage => generateImage()
  }
}