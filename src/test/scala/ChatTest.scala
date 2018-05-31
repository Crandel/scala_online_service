import org.scalatest.{ FunSuite, Matchers }
import chat.WebService
import akka.http.scaladsl.testkit.{ WSProbe, ScalatestRouteTest }

class ChatTest extends FunSuite with Matchers with ScalatestRouteTest {
  val webService = new WebService()
  val wsClient = WSProbe()

  test("connect to webserver") {
    WS("/ws_api", wsClient.flow) ~> webService.routes ~>
      check {
        isWebSocketUpgrade shouldEqual true
      }
  }
}
