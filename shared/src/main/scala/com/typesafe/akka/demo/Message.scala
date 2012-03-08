package com.typesafe.akka.demo

trait Message

// ***************************
// *** LIFE CYCLE MESSAGES ***
// ***************************

// Used to register a client - the sender of the message
case object ClientRegistration extends Message

// Used to pause the client - keeps state
case object Pause extends Message

// Used to stop the client - keeps state
case object Stop extends Message

// Can be used when a client has been paused or stopped to start it again
case object Start extends Message

// Used to stop and reset state in a client
case object Reset extends Message

// *****************************
// *** WORK RELATED MESSAGES ***
// *****************************

trait WorkInstruction extends Message {
  def aggregatorServer: String
}

trait WorkResult extends Message {
  def workerId: String
}

case object ClientInfo extends Message

case class RenderingStatistics(resultCounter: Int, totalRays: Long, raysPerSecond: Long)