package chat

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
case class TableId(id: Int)

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

class TablesActor extends Actor {

  private val tables = ArrayBuffer[CreateTableRoom]()

  override def receive: Receive = {
    case Create(after_id, table) => {
      var ind = 0
      if (after_id > 0 && after_id < tables.length) {
        ind = after_id
      } else if (after_id == -1) {
        ind = 0
      } else {
        ind = tables.length
      }
      val tableRoom = CreateTableRoom(table.name, table.participants)
      tables.insert(ind, tableRoom)
      sender ! TableId(ind)
    }

    case Update(table) => {
      var status = false
      if (tables.length > table.id) {
        tables.insert(table.id, CreateTableRoom(table.name, table.participants))
        status = true
      }
      sender ! Updated(status)
    }

    case Remove(id) => {
      var status = false
      if (tables.length > id) {
        tables.remove(id)
        status = true
      }
      sender ! Removed(status)
    }

    case GetList(subscriber) => {
      val tableRoomSeq: ArrayBuffer[(CreateTableRoom, Int)] = tables.zipWithIndex
      subscriber ! Protocol.TableList(tableRoomSeq.map(t => TableRoom(t._2, t._1.name, t._1.participants)))
    }
  }
}
