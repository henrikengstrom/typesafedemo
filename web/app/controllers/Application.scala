/**
 *  Copyright (C) 2012 Typesafe <http://typesafe.com/>
 */
package controllers

import _root_.akka.actor.{Props, ActorSystem, Actor}
import _root_.akka.pattern.ask
import _root_.akka.util.duration._
import play.api._
import play.api.mvc._
import play.api.libs._
import play.api.libs.Comet._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import akka.util.Timeout
import javax.swing.ImageIcon
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream
import org.apache.commons.codec.binary.Base64
import java.awt.image.BufferedImage
import java.awt.{Graphics2D, GraphicsConfiguration}

object Application extends Controller {
  import WebActor._

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def start = Action {
    println("*** START ACTION")
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
        chunks => Ok.stream(chunks &> Comet( callback = "parent.message"))
      }
    }
  }

  def image = Action {
    AsyncResult {
      (WebActorHandler ? "image").mapTo[Enumerator[String]].asPromise.map {
        content => Ok.stream(content &> Comet( callback = "parent.message"))
      }
    }
  }
}

class WebActor extends Actor {
  import WebActor._

  var dataListeners = Seq.empty[PushEnumerator[String]]
  var imageListeners = Seq.empty[PushEnumerator[String]]

  def receive = {
    case "call" => 
      println("**** CALLING SERVER WITH START INFO")
      Aggregator ! "WebReceiver"

    case "rays" =>
      lazy val channel:PushEnumerator[String] = Enumerator.imperative[String](
        onComplete = self ! channel
      )
      dataListeners = dataListeners :+ channel
      sender ! channel

    case "image" =>
      println("*** ADDING IMAGE LISTENER")
      Aggregator ! "WebReceiver"
      lazy val channel: PushEnumerator[String] = Enumerator.imperative[String](
        onComplete = self ! channel  
      )
      imageListeners = imageListeners :+ channel
      sender ! channel
     
    case channel: PushEnumerator[_] => dataListeners = dataListeners.filterNot(_ == channel)

    case x: ImageIcon =>
      println("*** PUSHING IMAGE DATA TO CLIENT(S)")
      val baos = new ByteArrayOutputStream(x.getIconHeight * x.getIconWidth)
      ImageIO.write(convertImage(x.getImage), "jpg", baos)
      baos.flush()
      val image = Base64.encodeBase64(baos.toByteArray)
      println("Image len: " + image.length)
      imageListeners.foreach(_.push(new String(image)))

    case x => dataListeners.foreach(_.push(x.toString))
  }

  def convertImage(src: java.awt.Image): BufferedImage = {
    val image = new BufferedImage(src.getWidth(null), src.getHeight(null), BufferedImage.TYPE_INT_RGB);
    val g2d = image.getGraphics.asInstanceOf[Graphics2D];
    g2d.drawImage(src, 0, 0, null);
    g2d.dispose();
    image
  }
}

object WebActor {
  lazy val WebActorSystem = ActorSystem("WebSystem")
  implicit val timeout = Timeout(5 seconds)
  lazy val Distributor = WebActorSystem.actorFor("akka://RaytraceServer@127.0.0.1:2552/user/distributor")
  lazy val Aggregator = WebActorSystem.actorFor("akka://RaytraceServer@127.0.0.1:2552/user/aggregator")
  lazy val WebActorHandler = WebActorSystem.actorOf(Props[WebActor], "webActor")
}
