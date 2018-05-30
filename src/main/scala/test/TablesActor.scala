package test

import scala.collection.mutable.ArrayBuffer
import akka.actor.{ Actor, ActorRef }
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

case class Create(after_id: Int, table: CreateTableRoom)
case class Update(table: TableRoom)
case class Remove(id: Int)
case class Removed(status: Boolean)
case class Updated(status: Boolean)
case class GetList(subscriber: ActorRef)
case class CreateTableRoom(name: String, participants: Int)
object CreateTableRoom {
  implicit val createTableRoomDecoder: Decoder[CreateTableRoom] = deriveDecoder
  implicit val createTableRoomEncoder: Encoder[CreateTableRoom] = deriveEncoder
}
case class TableRoom(id: Int, name: String, participants: Int)
object TableRoom {
  implicit val tableRoomDecoder: Decoder[TableRoom] = deriveDecoder
  implicit val tableRoomEncoder: Encoder[TableRoom] = deriveEncoder
}

case class TableId(id: Int)

class TablesActor extends Actor {

  private val tables = ArrayBuffer[TableRoom]()

  override def receive: Receive = {
    case Create(after_id, table) => {
      println("create tables")
      println(tables)
      println(after_id, table)

      var ind = 0
      if (after_id > 0 && after_id < tables.length) {
        ind = after_id
      } else if (after_id == -1) {
        ind = 0
      } else {
        ind = tables.length
      }
      val tableRoom = TableRoom(ind, table.name, table.participants)
      tables.insert(ind, tableRoom)
      sender ! TableId(ind)
    }

    case Update(table) => {
      println("update tables")
      println(tables)
      println(table)

      var status = false
      if (tables.length < table.id) {
        tables.insert(table.id, table)
        status = true
      }
      sender ! Updated(status)
    }

    case Remove(id) => {
      println("remove tables")
      println(tables)
      println(id)
      var status = false
      if (tables.length < id) {
        tables.remove(id)
        status = true
      }
      sender ! Removed(status)
    }

    case GetList(subscriber) => {
      subscriber ! Protocol.TableList(tables)
    }
  }
}
