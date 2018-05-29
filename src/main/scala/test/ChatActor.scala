package test

import akka.actor.{ Actor, ActorRef, Status, Terminated }
import io.circe.parser._
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{ Failure, Success, Try }
import scala.collection.mutable
import java.util.NoSuchElementException

case class User(username: String, password: String, role: Role)

case class Role(roleType: String)

class ChatActor(tablesActor: ActorRef) extends Actor {
  private var users = Set(
    User("admin", "admin", Role("admin")),
    User("test", "test", Role("test")))
  private var subscribers = mutable.Map[String, (ActorRef, Option[User])]()
  private var tableSubscribers = Set.empty[ActorRef]
  implicit val timeout = Timeout(1.seconds)

  def receive: Receive = {
    case NewParticipant(name, subscriber) ⇒
      context.watch(subscriber)
      subscribers += (name -> (subscriber, None))
    case msg: ReceivedMessage => dispatch(msg.toChatMessage)
    case ParticipantLeft(person) =>
      val entry = subscribers(person)
      entry._1 ! Status.Success(Unit)
      subscribers -= person
    case Terminated(sub) => subscribers = subscribers.filterNot(_._2 == sub)
  }

  def dispatch(msg: Protocol.Message): Unit = {
    msg match {
      case l: Protocol.Login => {
        try {
          val entry @ user = users.find(u => u.username == l.username && u.password == l.password).get
          val subscriber @ (name, (ref, usobj)) = subscribers.find(_._2 == sender).get
          subscribers(name) = (ref, Some(user))
          sender ! Protocol.LoginSuccessful(user_type = user.role.roleType)
        } catch {
          case e: NoSuchElementException =>
            sender ! Protocol.LoginFailed()
        }
      }

      case lp: Protocol.Ping => sender ! Protocol.Pong(seq = lp.seq)

      case sub: Protocol.Subscribe => {
        val tables = tablesActor ? GetList(sender)
        Try(Await.result(tables, 10.seconds)) match {
          case Success(extractTables: Seq[TableRoom]) => {
            sender ! Protocol.TableList(tables = extractTables)
            tableSubscribers += sender
          }
          case Failure(_) => println("Failure" + _)
        }
      }
      case unsub: Protocol.UnSubscribe => tableSubscribers -= sender

      case at: Protocol.AddTable => {
        try {
          val subscriber @ (name, (ref, usobj)) = subscribers.find(_._2 == sender).get
          usobj match {
            case Some(user) => {
              if (user.role == Role("admin")) {
                var begin = false
                if (at.afterId == -1) {
                  begin = true
                }
                val created = tablesActor ? Create(begin, at.table)
                Try(Await.result(created, 10.seconds)) match {
                  case Success(tableId: TableId) => {
                    tableSubscribers.foreach(_ ! Protocol.TableAdded(at.afterId, TableRoom(tableId.id, at.table.name, at.table.participants)))
                  }
                  case Failure(_) => println("Failure" + _)
                }
              } else sender ! Protocol.NotAuthorized()
            }
            case None => sender ! Protocol.NotAuthorized()
          }
        } catch {
          case e: NoSuchElementException =>
            sender ! Protocol.NotAuthorized()
        }
      }
    }
  }
}

sealed trait ChatEvent
case class NewParticipant(name: String, subscriber: ActorRef) extends ChatEvent
case class ParticipantLeft(name: String) extends ChatEvent
case class ReceivedMessage(sender: String, message: String) extends ChatEvent {
  def toChatMessage: Protocol.Message = {
    decode[Protocol.Message](message) match {
      case Right(msg) => msg
    }
  }
}

