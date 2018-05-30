package test
import io.circe.{ Decoder, Encoder, HCursor, Json }

object Protocol {
  sealed trait Message

  private def wrapTable(table: TableRoom): Json = {
    Json.fromFields(
      Seq(
        ("id", Json.fromInt(table.id)),
        ("name", Json.fromString(table.name)),
        ("partisipants", Json.fromInt(table.participants))))
  }

  private def wrapCreateTable(table: CreateTableRoom): Json = {
    Json.fromFields(
      Seq(
        ("name", Json.fromString(table.name)),
        ("partisipants", Json.fromInt(table.participants))))
  }

  implicit val encodeMessage: Encoder[Message] = new Encoder[Message] {
    final def apply(a: Message): Json = {
      a match {
        case l: Login => Json.obj(
          ("$type", Json.fromString("login")),
          ("username", Json.fromString(l.username)),
          ("password", Json.fromString(l.password)))

        case ls: LoginSuccessful => Json.obj(
          ("$type", Json.fromString("login_successful")),
          ("user_type", Json.fromString(ls.user_type)))

        case lf: LoginFailed => Json.obj(
          ("$type", Json.fromString("login_failed")))

        case pi: Ping => Json.obj(
          ("$type", Json.fromString("ping")),
          ("seq", Json.fromInt(pi.seq)))

        case po: Pong => Json.obj(
          ("$type", Json.fromString("pong")),
          ("seq", Json.fromInt(po.seq)))

        case sub: Subscribe => Json.obj(
          ("$type", Json.fromString("subscribe_tables")))

        case unsub: UnSubscribe => Json.obj(
          ("$type", Json.fromString("unsubscribe_tables")))

        case nota: NotAuthorized => Json.obj(
          ("$type", Json.fromString("not_authorized")))

        case tl: TableList => Json.obj(
          ("$type", Json.fromString("table_list")),
          ("tables", Json.fromValues(tl.tables.map(table => wrapTable(table)))))

        case at: AddTable => Json.obj(
          ("$type", Json.fromString("add_table")),
          ("after_id", Json.fromInt(at.afterId)),
          ("table", wrapCreateTable(at.table)))

        case ut: UpdateTable => Json.obj(
          ("$type", Json.fromString("update_table")),
          ("table", wrapTable(ut.table)))

        case uf: UpdateFailed => Json.obj(
          ("$type", Json.fromString("update_failed")),
          ("id", Json.fromInt(uf.id)))

        case rt: RemoveTable => Json.obj(
          ("$type", Json.fromString("remove_table")),
          ("id", Json.fromInt(rt.id)))

        case rf: RemoveFailed => Json.obj(
          ("$type", Json.fromString("removal_failed")),
          ("id", Json.fromInt(rf.id)))

        case ta: TableAdded => Json.obj(
          ("$type", Json.fromString("table_added")),
          ("after_id", Json.fromInt(ta.afterId)),
          ("table", wrapTable(ta.table)))

        case tu: TableUpdated => Json.obj(
          ("$type", Json.fromString("table_updated")),
          ("table", wrapTable(tu.table)))

        case tr: TableRemoved => Json.obj(
          ("$type", Json.fromString("table_removed")),
          ("id", Json.fromInt(tr.id)))
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
          } yield Login(username, password)

          case "login_successful" => for {
            user_type <- c.downField("user_type").as[String]
          } yield LoginSuccessful(user_type)

          case "login_failed" => Right(LoginFailed())

          case "ping" => for {
            seq <- c.downField("seq").as[Int]
          } yield Ping(seq)

          case "pong" => for {
            seq <- c.downField("seq").as[Int]
          } yield Pong(seq)

          case "subscribe_tables" => Right(Subscribe())

          case "unsubscribe_tables" => Right(UnSubscribe())

          case "not_authorized" => Right(NotAuthorized())

          case "table_list" => for {
            tables <- c.downField("tables").as[Seq[TableRoom]]
          } yield TableList(tables)

          case "add_table" => for {
            after_id <- c.downField("after_id").as[Int]
            table <- c.downField("table").as[CreateTableRoom]
          } yield AddTable(after_id, table)

          case "update_table" => for {
            table <- c.downField("table").as[TableRoom]
          } yield UpdateTable(table)

          case "update_failed" => for {
            id <- c.downField("id").as[Int]
          } yield UpdateFailed(id)

          case "remove_table" => for {
            id <- c.downField("id").as[Int]
          } yield RemoveTable(id)

          case "removal_failed" => for {
            id <- c.downField("id").as[Int]
          } yield RemoveFailed(id)

          case "table_added" => for {
            after_id <- c.downField("after_id").as[Int]
            table <- c.downField("table").as[TableRoom]
          } yield TableAdded(after_id, table)

          case "table_updated" => for {
            table <- c.downField("table").as[TableRoom]
          } yield TableUpdated(table)

          case "table_removed" => for {
            id <- c.downField("id").as[Int]
          } yield TableRemoved(id)
          case _ => Left("error")
        }
        result match {
          case Right(msg) => msg
        }
      }
    }
  }

  case class Login(username: String, password: String) extends Message
  case class LoginSuccessful(user_type: String) extends Message
  case class LoginFailed() extends Message
  case class Ping(seq: Int) extends Message
  case class Pong(seq: Int) extends Message
  case class Subscribe() extends Message
  case class UnSubscribe() extends Message
  case class TableList(tables: Seq[TableRoom]) extends Message
  case class NotAuthorized() extends Message
  case class AddTable(afterId: Int, table: CreateTableRoom) extends Message
  case class UpdateTable(table: TableRoom) extends Message
  case class UpdateFailed(id: Int) extends Message
  case class RemoveTable(id: Int) extends Message
  case class RemoveFailed(id: Int) extends Message
  case class TableAdded(afterId: Int, table: TableRoom) extends Message
  case class TableRemoved(id: Int) extends Message
  case class TableUpdated(table: TableRoom) extends Message
}
