package chat

import akka.actor._
import akka.event.Logging
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

trait Chat {
  def chatFlow(sender: String): Flow[String, Protocol.Message, Any]
}

object Chat {
  def create(system: ActorSystem): Chat = {
    lazy val log = Logging(system, classOf[Chat])

    val tableActor = system.actorOf(Props[TablesActor])
    val chatActor = system.actorOf(Props(new ChatActor(tableActor)))

    def chatInSink(sender: String) = Sink.actorRef[ChatEvent](chatActor, ParticipantLeft(sender))

    new Chat {
      def chatFlow(sender: String): Flow[String, Protocol.Message, Any] = {
        val in =
          Flow[String]
            .map(ReceivedMessage(sender, _))
            .to(chatInSink(sender))

        val out =
          Source.actorRef[Protocol.Message](1, OverflowStrategy.fail)
            .mapMaterializedValue(chatActor ! NewParticipant(sender, _))

        Flow.fromSinkAndSource(in, out)
      }
    }
  }
}
