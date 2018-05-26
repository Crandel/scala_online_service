package test
import io.circe._
import io.circe.generic.semiauto._

object Protocol {
  sealed trait Message

  case class Login(username: String, password: String) extends Message
  case class LoginSuccessful(user_type: String) extends Message
  case class LoginFailed() extends Message
  case class Ping(seq: Int) extends Message
  case class Pong(seq: Int) extends Message
  case class Subscribe() extends Message
  case class UnSubscribe() extends Message
  case class TableList(tables: Seq[Table]) extends Message
  case class NotAuthorized() extends Message
  case class AddTable(afterId: Int, table: Seq[Table]) extends Message
  case class UpdateTable(table: Seq[Table]) extends Message
  case class UpdateFailed(id: Int) extends Message
  case class RemoveTable(id: Int) extends Message
  case class RemoveFailed(id: Int) extends Message
  case class TableAdded(afterId: Int, table: Seq[Table]) extends Message
  case class TableRemoved(id: Int) extends Message
  case class TableUpdated(table: Seq[Table]) extends Message
  case class ChatMessage(sender: String, message: String) extends Message
}
