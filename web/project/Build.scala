import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "typesafedemo"
    val appVersion      = "0.6-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.typesafe.akka" % "akka-remote" % "2.0-M2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
