package test

import scala.collection.mutable.ArrayBuffer

import akka.actor.Actor

case class Create(begin: Boolean, table: Table)
case class Update(id: Int, table: Table)
case class Remove(id: Int)
case class Removed(status: Boolean)
case class Updated(status: Boolean)
case class GetList()
case class TableRoom(id: Int, name: String, participants: Int)
case class List(tables: Seq[TableRoom])
case class TableId(id: Int)


class TablesActor extends Actor{

  private var tables = ArrayBuffer[Table]()

  override def receive: Receive = {
    case Create(begin, table) => {
      if (begin){
        tables += table
      } else {
        tables.insert(0, table)
      }
      val id = tables.indexOf(table)
      sender ! TableId(id)
    }
    case Update(id, table) => {
      var status = false
      if (tables.length < id){
        tables.insert(id, table)
        status = true
      }
      sender ! Updated(status)
    }
    case Remove(id) => {
      var status = false
      if (tables.length < id){
        tables.remove(id)
        status = true
      }
      sender ! Removed(status)
    }
    case GetList => {
      val tableList: Seq[TableRoom] = for ((table, i)<- tables.zipWithIndex) yield TableRoom(i, table.name, table.participants.length)
      sender ! List(tableList)
    }
  }
}
