package test
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object Protocol {
  sealed trait Message
  implicit val encodeMessage: Encoder[Message] = new Encoder[Message] {
    final def apply(a: Message): Json = {
      a match {
        case l: Login => Json.obj(
          ("$type", Json.fromString("login")),
          ("username", Json.fromString(l.username)),
          ("password", Json.fromString(l.password))
        )
        case ls: LoginSuccessful => Json.obj(
          ("$type", Json.fromString("login_successful")),
          ("user_type", Json.fromString(ls.user_type))
        )
      }
    }
  }

  implicit val decodeMessage: Decoder[Message] = new Decoder[Message] {
    final def apply(c: HCursor): Decoder.Result[Message] = {
      for {
        m_type <- c.downField("$type").as[String]
      } yield {
        val result = m_type match {
          case "login" => for {
            username <- c.downField("username").as[String]
            password <- c.downField("password").as[String]
          } yield {
            Login(username, password)
          }
          case "login_successful" => for {
            user_type <- c.downField("user_type").as[String]
          } yield {
            LoginSuccessful(user_type)
          }
        }
        result match {
          case Right(msg) => msg
        }
      }
    }
  }

  case class Login(username: String, password: String) extends Message
  implicit val loginEncoder: Encoder[Login] = deriveEncoder
  implicit val loginDecoder: Decoder[Login] = deriveDecoder

  case class LoginSuccessful(user_type: String) extends Message
  implicit val loginSuccessDecoder: Decoder[LoginSuccessful] = deriveDecoder
  implicit val loginSuccessEncoder: Encoder[LoginSuccessful] = deriveEncoder

  case class LoginFailed() extends Message
  implicit val loginFailDecoder: Decoder[LoginFailed] = deriveDecoder
  implicit val loginFailEncoder: Encoder[LoginFailed] = deriveEncoder

  case class Ping(seq: Int) extends Message
  implicit val pingDecoder: Decoder[Ping] = deriveDecoder
  implicit val pingEncoder: Encoder[Ping] = deriveEncoder

  case class Pong(seq: Int) extends Message
  implicit val pongDecoder: Decoder[Pong] = deriveDecoder
  implicit val pongEncoder: Encoder[Pong] = deriveEncoder

  case class Subscribe() extends Message
  implicit val subscribeDecoder: Decoder[Subscribe] = deriveDecoder
  implicit val subscribeEncoder: Encoder[Subscribe] = deriveEncoder

  case class UnSubscribe() extends Message
  implicit val unsubscribeDecoder: Decoder[UnSubscribe] = deriveDecoder
  implicit val unsubscribeEncoder: Encoder[UnSubscribe] = deriveEncoder

  case class TableList(tables: Seq[TableRoom]) extends Message
  implicit val tableListDecoder: Decoder[TableList] = deriveDecoder
  implicit val tableListEncoder: Encoder[TableList] = deriveEncoder

  case class NotAuthorized() extends Message
  implicit val notAuthDecoder: Decoder[NotAuthorized] = deriveDecoder
  implicit val notAuthEncoder: Encoder[NotAuthorized] = deriveEncoder

  case class AddTable(afterId: Int, table: Seq[TableRoom]) extends Message
  implicit val addTableDecoder: Decoder[AddTable] = deriveDecoder
  implicit val addTableEncoder: Encoder[AddTable] = deriveEncoder

  case class UpdateTable(table: Seq[TableRoom]) extends Message
  implicit val updateTableDecoder: Decoder[UpdateTable] = deriveDecoder
  implicit val updateTableEncoder: Encoder[UpdateTable] = deriveEncoder

  case class UpdateFailed(id: Int) extends Message
  implicit val updateFailedDecoder: Decoder[UpdateFailed] = deriveDecoder
  implicit val updateFailedEncoder: Encoder[UpdateFailed] = deriveEncoder

  case class RemoveTable(id: Int) extends Message
  implicit val removeTableDecoder: Decoder[RemoveTable] = deriveDecoder
  implicit val removeTableEncoder: Encoder[RemoveTable] = deriveEncoder

  case class RemoveFailed(id: Int) extends Message
  implicit val removeFailDecoder: Decoder[RemoveFailed] = deriveDecoder
  implicit val removeFailEncoder: Encoder[RemoveFailed] = deriveEncoder

  case class TableAdded(afterId: Int, table: Seq[TableRoom]) extends Message
  implicit val tableAddedDecoder: Decoder[TableAdded] = deriveDecoder
  implicit val tableAddedEncoder: Encoder[TableAdded] = deriveEncoder

  case class TableRemoved(id: Int) extends Message
  implicit val tableRemovedDecoder: Decoder[TableRemoved] = deriveDecoder
  implicit val tableRemovedEncoder: Encoder[TableRemoved] = deriveEncoder

  case class TableUpdated(table: Seq[TableRoom]) extends Message
  implicit val tableUpdatedDecoder: Decoder[TableUpdated] = deriveDecoder
  implicit val tableUpdatedEncoder: Encoder[TableUpdated] = deriveEncoder
}
