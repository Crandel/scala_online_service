package web.socket

import akka.actor._
import akka.event.Logging
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

trait ServerActor {
  def mainFlow(sender: String): Flow[String, Protocol.Message, Any]
}

object ServerActor {
  def create(system: ActorSystem): ServerActor = {

    val tableActor = system.actorOf(Props[TablesActor])
    val eventActor = system.actorOf(Props(new EventActor(tableActor)))

    def outputSink(sender: String) = Sink.actorRef[InputEvent](eventActor, ParticipantLeft(sender))

    new ServerActor {
      def mainFlow(sender: String): Flow[String, Protocol.Message, Any] = {
        val in =
          Flow[String]
            .map(ReceivedMessage(sender, _))
            .to(outputSink(sender))

        val out =
          Source.actorRef[Protocol.Message](1, OverflowStrategy.fail)
            .mapMaterializedValue(eventActor ! NewParticipant(sender, _))

        Flow.fromSinkAndSource(in, out)
      }
    }
  }
}
