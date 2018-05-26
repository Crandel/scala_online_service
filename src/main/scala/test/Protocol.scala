package test
import io.circe._
import io.circe.generic.semiauto._

object Protocol {
  sealed trait Message
  implicit val messageDecoder: Decoder[Message] = deriveDecoder
  implicit val messageEncoder: Encoder[Message] = deriveEncoder

  case class Login($type: String = "login", username: String, password: String) extends Message
  case class LoginSuccessful($type: String = "login_successful", user_type: String) extends Message
  case class LoginFailed($type: String = "login_failed") extends Message
  case class Ping($type: String = "ping", seq: Int) extends Message
  case class Pong($type: String = "pong", seq: Int) extends Message
  case class Subscribe($type: String = "subscribe_tables") extends Message
  case class UnSubscribe($type: String = "unsubscribe_tables") extends Message
  case class TableList($type: String = "table_list", tables: Seq[Table]) extends Message
  case class NotAuthorized($type: String = "not_authorized") extends Message
  case class AddTable($type: String = "add_table", afterId: Int, table: Seq[Table]) extends Message
  case class UpdateTable($type: String = "update_table", table: Seq[Table]) extends Message
  case class UpdateFailed($type: String = "update_failed", id: Int) extends Message
  case class RemoveTable($type: String = "remove_table", id: Int) extends Message
  case class RemoveFailed($type: String = "removal_failed", id: Int) extends Message
  case class TableAdded($type: String = "table_added", afterId: Int, table: Seq[Table]) extends Message
  case class TableRemoved($type: String = "table_removed", id: Int) extends Message
  case class TableUpdated($type: String = "table_updated", table: Seq[Table]) extends Message
}
