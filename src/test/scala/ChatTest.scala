import org.scalatest.{ FunSuite, Matchers }
import akka.http.scaladsl.testkit.{ WSProbe, ScalatestRouteTest }
import chat.WebService

class ChatTest extends FunSuite with Matchers with ScalatestRouteTest {

  def assertSocketConnection()(assertions: (WSProbe) => Unit) = {
    val webService = new WebService()
    val wsClient = WSProbe()
    WS("/ws_api", wsClient.flow) ~> webService.routes ~>
      check(assertions(wsClient))
  }

  test("ping webserver") {
    assertSocketConnection() { wsClient =>
      wsClient.sendMessage("""{"$type":"ping","seq":1}""")
      wsClient.expectMessage("""{"$type":"pong","seq":1}""")
    }
  }

  test("subscribe to table list") {
    assertSocketConnection() { wsClient =>
      wsClient.sendMessage("""{"$type":"subscribe_tables"}""")
      wsClient.expectMessage("""{"$type":"table_list","tables":[]}""")
    }
  }

  test("login successfull") {
    assertSocketConnection() { wsClient =>
      wsClient.sendMessage("""{"$type": "login","username": "admin","password": "admin"}""")
      wsClient.expectMessage("""{"$type":"login_successful","user_type":"admin"}""")
    }
  }

  test("login failed") {
    assertSocketConnection() { wsClient =>
      wsClient.sendMessage("""{"$type": "login","username": "test user","password": "some pass"}""")
      wsClient.expectMessage("""{"$type":"login_failed"}""")
    }
  }

}
