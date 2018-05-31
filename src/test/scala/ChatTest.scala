import org.scalatest.{ FunSuite, Matchers }
import akka.http.scaladsl.testkit.{ WSProbe, ScalatestRouteTest }
import chat.WebService

class ChatTest extends FunSuite with Matchers with ScalatestRouteTest {

  test("ping webserver") {
    val webService = new WebService()
    val wsClient = WSProbe()

    WS("/ws_api", wsClient.flow) ~> webService.routes ~>
      check {
        wsClient.sendMessage("""{"$type":"ping","seq":1}""")
        wsClient.expectMessage("""{"$type":"pong","seq":1}""")
      }
  }
}
