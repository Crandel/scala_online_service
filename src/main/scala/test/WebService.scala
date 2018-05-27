package test

import scala.util.Failure
import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.scaladsl.Flow
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

class SenderId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object SenderId {
  private var counter: Int = 0
  private var current_id: Int = 0

  def apply(): SenderId = {
    current_id = counter
    counter += 1
    new SenderId(current_id)
  }
}

class WebService(implicit system: ActorSystem) extends Directives {

  lazy val log = Logging(system, classOf[WebService])

  val theChat: Chat = Chat.create(system)

  import system.dispatcher

  lazy val routes: Route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        path("ws_api") {
          handleWebSocketMessages(websocketChatFlow())
        } ~
        getFromResourceDirectory("web")
    }

  def websocketChatFlow(): Flow[Message, Message, Any] = {
    val sender = SenderId().toString

    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) ⇒ msg
      }
      .via(theChat.chatFlow(sender))
      .map {
        case msg: Protocol.Message ⇒
          TextMessage.Strict(msg.asJson.noSpaces)
      }
      .via(reportErrorsFlow)
  }

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          log.error(s"WS stream failed with $cause")
        case _ =>
      })
}
