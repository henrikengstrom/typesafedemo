package typesafe.akka.demo

import sbt._
import Keys._
//import com.typesafe.sbtscalariform.ScalariformPlugin
//import ScalariformPlugin.{ format, formatPreferences }

object TypesafeDemoBuild extends Build {
  val Organization = "com.typesafe.akka.demo"
  val Version      = "0.6-SNAPSHOT"
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

  lazy val web = Project(
    id = "web",
    base = file("web"),
    dependencies = Seq(shared, server),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.typesafeDemo ++ Dependencies.playLibs)
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

  // TODO (h3nk3) : Add line below
  //lazy val defaultSettings = buildSettings ++ formatSettings ++ Seq(
  lazy val defaultSettings = buildSettings ++ Seq(
    resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",

    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-optimise", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),

    // disable parallel tests
    parallelExecution in Test := false
  )

  // Format settings

  // TODO (h3nk3) : Resolve SBT dependency
  /*
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
  */
}

object Dependencies {
  import Dependency._

  val typesafeDemo = Seq(akkaActor, akkaRemote, akkaKernel, sjson, slf4j, roygbiv, logback, scalatest, junit, akkaTestKit)

  val playLibs = Seq(play, protobuf)
}

object Dependency {
  object V {
    val Akka      = "2.0-SNAPSHOT"
    val Scalatest = "1.6.1"
    val Slf4j     = "1.6.0"
    val Roygbiv   = "0.6-SNAPSHOT"
    val Sjson     = "0.11"
    val Play      = "2.0-RC1-SNAPSHOT"
    val Protobuf  = "2.4.1"
  }

  val akkaActor         = "com.typesafe.akka"         % "akka-actor"         % V.Akka
  val akkaRemote        = "com.typesafe.akka"         % "akka-remote"        % V.Akka
  val akkaKernel        = "com.typesafe.akka"         % "akka-kernel"        % V.Akka
  val akkaTestKit       = "com.typesafe.akka"         % "akka-testkit"       % V.Akka
  val sjson             = "net.debasishg"             % "sjson_2.9.0"        % V.Sjson
  val slf4j             = "org.slf4j"                 % "slf4j-api"          % V.Slf4j
  val roygbiv           = "roygbiv"                   % "shared_2.9.1"       % V.Roygbiv
  val play              = "play"                      % "play_2.9.1"         % V.Play
  val protobuf          = "com.google.protobuf"       % "protobuf-java"      % V.Protobuf

  val logback           = "ch.qos.logback"            % "logback-classic"    % "0.9.24"
  val scalatest         = "org.scalatest"             % "scalatest_2.9.0"    % V.Scalatest % "test"
  val junit             = "junit"                     % "junit"              % "4.5"       % "test"
}


