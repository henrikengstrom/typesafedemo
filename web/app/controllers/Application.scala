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
    Server.supervisorRef ! Start
    Ok(views.html.index("ok"))
  }

  def pause = Action {
    Server.supervisorRef ! Pause
    Ok(views.html.index("ok"))
  }

  def stop = Action {
    Server.supervisorRef ! Stop
    Ok(views.html.index("ok"))
  }
}