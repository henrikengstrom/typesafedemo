/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package controllers

import play.api.mvc.Controller

class Status extends Controller {

  /*
  def numberClients = Action {
    AsyncResult {
      (Server.supervisorRef ? ClientInfo).mapTo[Enumerator[String]].asPromise.map {
        chunks => Ok(Comet(chunks, callback = "parent.message"))
      }
    }
  }

  def raysPerSecond = Action {
    Ok(views.html.index("ok"))
  }

  def generatedLayers = Action {
    Ok(views.html.index("ok"))
  }
  */
}