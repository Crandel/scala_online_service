package web.chat

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

  // all subscribers of our chat
  private var subscribers = mutable.Map[String, (ActorRef, Option[User])]()

  // subscribers, who will get table changes
  private var tableSubscribers = Set.empty[ActorRef]

  // timeout for ask pattern
  implicit val timeout = Timeout(1.seconds)

  def receive: Receive = {
    case NewParticipant(name, subscriber) =>
      context.watch(subscriber)
      subscribers += (name -> (subscriber, None))

    case msg: ReceivedMessage => dispatch(msg.toChatMessage, msg.name)

    case ParticipantLeft(person) =>
      val entry = subscribers(person)
      entry._1 ! Status.Success(Unit)
      subscribers -= person

    case Terminated(sub) => subscribers = subscribers.filterNot(_._1 == sub)
  }

  def dispatch(msg: Protocol.Message, subscriber: String): Unit = {
    val subTuple = subscribers(subscriber)
    val subActor = subTuple._1

    val isAdmin: Boolean = subTuple._2 match {
      case Some(user) => {
        if (user.role == Role("admin")) {
          true
        } else {
          false
        }
      }
      case _ => false
    }

    msg match {
      case l: Protocol.Login => {
        try {
          // If user exists, add him in subscribers to mark subscriber as logined
          val entry @ user = users.find(u => u.username == l.username && u.password == l.password).get
          subscribers(subscriber) = (subActor, Some(user))
          subActor ! Protocol.LoginSuccessful(user_type = user.role.roleType)
        } catch {
          case e: NoSuchElementException =>
            subActor ! Protocol.LoginFailed()
        }
      }

      case lp: Protocol.Ping => subActor ! Protocol.Pong(seq = lp.seq)

      case sub: Protocol.Subscribe => {
        tablesActor ! GetList(subActor)
        tableSubscribers += subActor
      }

      case unsub: Protocol.UnSubscribe => {
        if (tableSubscribers.contains(subActor)) {
          tableSubscribers -= subActor
        }
      }

      case at: Protocol.AddTable => {
        if (isAdmin) {
          // ask tableActor to add table
          val created = tablesActor ? Create(at.afterId, at.table)
          // wait until Future came
          Try(Await.result(created, 10.seconds)) match {
            case Success(tableId: TableId) => {
              tableSubscribers.foreach(_ ! Protocol.TableAdded(at.afterId, TableRoom(tableId.id, at.table.name, at.table.participants)))
            }
            case Failure(_) => println("Failure" + _)
          }
        } else subActor ! Protocol.NotAuthorized()
      }

      case ut: Protocol.UpdateTable => {
        if (isAdmin) {
          val update = tablesActor ? Update(ut.table)
          Try(Await.result(update, 10.seconds)) match {
            case Success(st: Updated) => {
              if (st.status) {
                tableSubscribers.foreach(_ ! Protocol.TableUpdated(ut.table))
              } else {
                subActor ! Protocol.UpdateFailed(ut.table.id)
              }
            }
            case Failure(_) => println("Failure" + _)
          }
        } else subActor ! Protocol.NotAuthorized()
      }

      case rt: Protocol.RemoveTable => {
        if (isAdmin) {
          val remove = tablesActor ? Remove(rt.id)
          Try(Await.result(remove, 10.seconds)) match {
            case Success(st: Removed) => {
              if (st.status) {
                tableSubscribers.foreach(_ ! Protocol.TableRemoved(rt.id))
              } else {
                subActor ! Protocol.RemoveFailed(rt.id)
              }
            }
            case Failure(_) => println("Failure" + _)
          }
        } else subActor ! Protocol.NotAuthorized()
      }
      // in case we got other Protocol case classes
      case _ =>
    }
  }
}

sealed trait ChatEvent
case class NewParticipant(name: String, subscriber: ActorRef) extends ChatEvent
case class ParticipantLeft(name: String) extends ChatEvent
case class ReceivedMessage(name: String, message: String) extends ChatEvent {
  def toChatMessage: Protocol.Message = {
    decode[Protocol.Message](message) match {
      case Right(msg) => msg
    }
  }
}

