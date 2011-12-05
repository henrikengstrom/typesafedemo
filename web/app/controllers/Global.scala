package controllers

import play.api.{GlobalSettings, Application => PlayApplication}
import com.typesafe.akka.demo.server.Server

object Global extends GlobalSettings {
  override def onStart(app: PlayApplication) {
    // Initialize server
    new Server()
  }
}
