import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {
    val appName         = "typesafedemo"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "com.typesafe.akka.demo" % "server" % "1.0-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies).settings(defaultScalaSettings:_*).settings(
    )
}
