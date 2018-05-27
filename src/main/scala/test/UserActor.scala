package test

import java.util.NoSuchElementException

import akka.actor.Actor

class UserActor extends Actor {

  override def receive: Receive = {
    case lf: Protocol.LoginFailed => sender ! lf
    case ls: Protocol.LoginSuccessful => sender ! ls
    case lp: Protocol.Ping => sender ! Protocol.Pong(seq = lp.seq)
  }
}

