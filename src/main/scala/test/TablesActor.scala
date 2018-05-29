package test

import scala.collection.mutable.ArrayBuffer
import akka.actor.{ Actor, ActorRef }
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

case class Create(begin: Boolean, table: CreateTableRoom)
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

case class List(tables: Seq[TableRoom])
case class TableId(id: Int)

class TablesActor extends Actor {

  private val tables = ArrayBuffer[TableRoom]()

  override def receive: Receive = {
    case Create(begin, table) => {
      var ind = 0
      if (!begin) {
        ind = tables.length + 1
      }
      val tableRoom = TableRoom(ind, table.name, table.participants)
      tables.insert(0, tableRoom)
      sender ! TableId(ind)
    }

    case Update(table) => {
      var status = false
      if (tables.length < table.id) {
        tables.insert(table.id, table)
        status = true
      }
      sender ! Updated(status)
    }

    case Remove(id) => {
      var status = false
      if (tables.length < id) {
        tables.remove(id)
        status = true
      }
      sender ! Removed(status)
    }

    case GetList(subscriber) => {
      subscriber ! List(tables)
    }
  }
}
