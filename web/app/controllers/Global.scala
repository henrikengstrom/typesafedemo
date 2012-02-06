/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package controllers

import play.api.{GlobalSettings, Application => PlayApplication}
import akka.actor.ActorSystem

//import com.typesafe.akka.demo.server.Server

object Global extends GlobalSettings {
  override def onStart(app: PlayApplication) {
    // Start the server
    //new Server()
  }
}
