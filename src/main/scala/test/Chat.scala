package test

import akka.actor._
import akka.event.Logging
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import test.Protocol.Message
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

trait Chat {
  def chatFlow(sender: String): Flow[String, Protocol.Message, Any]
}

object Chat {
  def create(system: ActorSystem): Chat = {
    lazy val log = Logging(system, classOf[Chat])
    val tableActor = system.actorOf(Props[TablesActor])

    val chatActor =
      system.actorOf(Props(new Actor {
        var users = Set(("admin", User("admin", "admin"), Role("admin")))
        var subscribers = Set.empty[(String, ActorRef)]

        def receive: Receive = {
          case NewParticipant(name, subscriber) ⇒
            context.watch(subscriber)
            subscribers += (name -> subscriber)
          case msg: ReceivedMessage ⇒ dispatch(msg.toChatMessage)
          case msg: Protocol.Message =>
          case ParticipantLeft(person) ⇒
            val entry @ (name, ref) = subscribers.find(_._1 == person).get
            ref ! Status.Success(Unit)
            subscribers -= entry
          case Terminated(sub) ⇒
            subscribers = subscribers.filterNot(_._2 == sub)
        }
        def dispatch(msg: Either[Error, Protocol.Message]): Unit = {
          msg match {
            case Right(message) =>
              subscribers.foreach(_._2 ! message)
            case Left(e) => log.error(equals().toString)
          }
        }

        def table_dispatch(msg: Protocol.Message) = {
          msg match {
            case l:Protocol.Login => {
                val user @ (name, userObj, role) = users.find(_._1 == l.username).get
                sender ! Protocol.LoginSuccessful(role.roleType)
            }
          }

        }
      }))

    def chatInSink(sender: String) = Sink.actorRef[ChatEvent](chatActor, ParticipantLeft(sender))

    new Chat {
      def chatFlow(sender: String): Flow[String, Protocol.Message, Any] = {
        val in =
          Flow[String]
            .map(ReceivedMessage(sender, _))
            .to(chatInSink(sender))

        // The counter-part which is a source that will create a target ActorRef per
        // materialization where the chatActor will send its messages to.
        // This source will only buffer one element and will fail if the client doesn't read
        // messages fast enough.
        val out =
        Source.actorRef[Protocol.Message](1, OverflowStrategy.fail)
          .mapMaterializedValue(chatActor ! NewParticipant(sender, _))

        Flow.fromSinkAndSource(in, out)
      }
      def injectMessage(message: Protocol.Message): Unit = chatActor ! message // non-streams interface
    }
  }

  private sealed trait ChatEvent
  private case class NewParticipant(name: String, subscriber: ActorRef) extends ChatEvent
  private case class ParticipantLeft(name: String) extends ChatEvent
  private case class ReceivedMessage(sender: String, message: String) extends ChatEvent {
    def toChatMessage: Either[Error, Protocol.Message] = {
      decode[Protocol.Message](message)
    }
  }
}
