package i.f.workshop.finch

import com.twitter.finagle.Httpx
import com.twitter.util.Await

import io.finch.route._

object HelloWorld extends App {

  val r: Router[String] = get("hello") { "Hello, World!" }

  Await.ready(Httpx.server.serve(":8081", r.toService))
}
