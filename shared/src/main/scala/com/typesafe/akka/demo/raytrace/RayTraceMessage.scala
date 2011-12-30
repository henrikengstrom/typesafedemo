/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akka.demo.raytrace

import roygbiv.scene.Scene
import roygbiv.color.RGBColor
import com.typesafe.akka.demo.{ WorkResult, WorkInstruction }

case class RayTraceWorkInstruction(aggregatorServer: String, scene: Scene) extends WorkInstruction

case class RayTraceWorkResult(workerId: String, clientThreads: Int, result: Seq[RGBColor]) extends WorkResult
