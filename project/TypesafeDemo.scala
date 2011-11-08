package typesafe.akka.demo

import sbt._
import Keys._
import com.typesafe.sbtscalariform.ScalariformPlugin
import ScalariformPlugin.{ format, formatPreferences }

object TypesafeDemoBuild extends Build {
  val Organization = "com.typesafe.akka.demo"
  val Version      = "1.0-SNAPSHOT"
  val ScalaVersion = "2.9.1"

  lazy val parentSettings = buildSettings

  lazy val atmos = Project(
    id = "typesafedemo",
    base = file("."),
    settings = parentSettings,
    aggregate = Seq(shared, server, client)
  )

  lazy val shared = Project(
    id = "shared",
    base = file("shared"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.typesafeDemo)
  )

  lazy val server = Project(
    id = "server",
    base = file("server"),
    dependencies = Seq(shared),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.typesafeDemo)
  )

  lazy val client = Project(
    id = "client",
    base = file("client"),
    dependencies = Seq(shared),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.typesafeDemo)
  )

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version      := Version,
    scalaVersion := ScalaVersion,
    crossPaths   := false,
    publishArtifact in packageSrc := false,
    publishArtifact in packageDoc := false,
    organizationName := "Typesafe Inc.",
    organizationHomepage := Some(url("http://www.typesafe.com"))
  )

  lazy val defaultSettings = buildSettings ++ formatSettings ++ Seq(    
    resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",

    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-optimise", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),

    // disable parallel tests
    parallelExecution in Test := false
  )

  // Format settings

  lazy val formatSettings = ScalariformPlugin.settings ++ Seq(
    formatPreferences in Compile := formattingPreferences,
    formatPreferences in Test    := formattingPreferences
  )

  def formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
    .setPreference(RewriteArrowSymbols, true)
    .setPreference(PreserveSpaceBeforeArguments, true)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(PreserveDanglingCloseParenthesis, true)
  }
}

object Dependencies {
  import Dependency._

  val typesafeDemo = Seq(akkaKernel, akkaRemote, sjson, slf4j, logback, scalatest, junit)
}

object Dependency {
  object V {
    val Akka      = "1.2"
    val Scalatest = "1.6.1"
    val Slf4j     = "1.6.0"
  }

  val akkaKernel        = "se.scalablesolutions.akka" % "akka-kernel"        % V.Akka
  val akkaRemote        = "se.scalablesolutions.akka" % "akka-remote"        % V.Akka
  val akkaSlf4j         = "se.scalablesolutions.akka" % "akka-slf4j"         % V.Akka
  val sjson             = "net.debasishg"             % "sjson_2.9.0"        % "0.11"
  val slf4j             = "org.slf4j"                 % "slf4j-api"          % V.Slf4j
  val logback           = "ch.qos.logback"            % "logback-classic"    % "0.9.24"
  val scalatest         = "org.scalatest"             % "scalatest_2.9.0"    % V.Scalatest % "test"
  val junit             = "junit"                     % "junit"              % "4.5"       % "test"
}


