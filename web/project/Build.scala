import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "typesafedemo"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.typesafe.akka" % "akka-remote" % "2.0",
      "com.typesafe.akka.demo" % "shared" % "1.0-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
