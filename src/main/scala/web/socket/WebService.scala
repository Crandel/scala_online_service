package web.socket

import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.scaladsl.Flow
import io.circe.syntax._

class SenderId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object SenderId {
  private var current_id: Int = 0

  def apply(): SenderId = {
    val id = new SenderId(current_id)
    current_id += 1
    id
  }
}

class WebService(implicit system: ActorSystem) extends Directives {

  lazy val log = Logging(system, classOf[WebService])

  val theServer: ServerActor = ServerActor.create(system)

  lazy val routes: Route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        path("ws_api") {
          parameter("name".?) { name =>
            handleWebSocketMessages(webSocketServerFlow(name))
          }
        } ~
        getFromResourceDirectory("web")
    }

  def webSocketServerFlow(name: Option[String]): Flow[Message, Message, Any] = {
    val sender = name match {
      case Some(n) => n
      case None => SenderId().toString
    }

    // Create Flow for our web socket messages
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) => msg
      }
      .via(theServer.mainFlow(sender))
      .map {
        case msg: Protocol.Message =>
          TextMessage.Strict(msg.asJson.noSpaces)
      }
      .via(reportErrorsFlow)
  }

  def reportErrorsFlow[T]: Flow[T, T, Any] = {
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          log.error(s"WS stream failed with $cause")
        case _ =>
      })
  }
}
