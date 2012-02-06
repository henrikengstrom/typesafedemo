/**
 *  Copyright (C) 2011 Typesafe <http://typesafe.com/>
 */
package controllers

import _root_.akka.actor.{Props, ActorSystem, Actor}
import _root_.akka.util.duration._
import play.api._
import play.api.mvc._
import play.api.libs._
import play.api.libs.akka._
import play.api.libs.iteratee._

object Application extends Controller {
  import WebActor._

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def start = Action {
    Distributor ! "Start"
    WebActorHandler ! "call"
    Ok(views.html.index("ok"))
  }

  def pause = Action {
    Distributor ! "Pause"
    Ok(views.html.index("ok"))
  }

  def stop = Action {
    Distributor ! "Stop"
    Ok(views.html.index("ok"))
  }

  def rays = Action {
    AsyncResult {
      (WebActorHandler ? "rays").mapTo[Enumerator[String]].asPromise.map {
        chunks =>
          Ok.stream(chunks &> Comet( callback = "parent.message"))
      }
    }
  }
}

class WebActor extends Actor {
  import WebActor._
  var clientListeners = Seq.empty[CallbackEnumerator[String]]

  def receive = {
    case "call" => Aggregator ! "WebReceiver"
    case "rays" =>
      lazy val channel:CallbackEnumerator[String] = new CallbackEnumerator[String](
        onComplete = self ! channel
      )
      clientListeners = clientListeners :+ channel
      sender ! channel
    case channel: CallbackEnumerator[_] => clientListeners = clientListeners.filterNot(_ == channel)
    case x => clientListeners.foreach(_.push(x.toString))
  }
}

object WebActor {
  lazy val WebActorSystem = ActorSystem("WebSystem")
  implicit val timeout = WebActorSystem.settings.ActorTimeout
  lazy val Distributor = WebActorSystem.actorFor("akka://RaytraceServer@127.0.0.1:2552/user/distributor")
  lazy val Aggregator = WebActorSystem.actorFor("akka://RaytraceServer@127.0.0.1:2552/user/aggregator")
  lazy val WebActorHandler = WebActorSystem.actorOf(Props[WebActor], "webActor")
}
