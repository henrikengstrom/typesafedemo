/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package controllers

import play.api.mvc._
import com.typesafe.akka.demo.{Pause, Stop, Start}
import com.typesafe.akka.demo.server.Server

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def start = Action {
    Server.start()
    Ok(views.html.index("ok"))
  }

  def pause = Action {
    Server.pause()
    Ok(views.html.index("ok"))
  }

  def stop = Action {
    Server.stop()
    Ok(views.html.index("ok"))
  }
}