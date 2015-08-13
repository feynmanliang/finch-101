package i.f.workshop.finch

import com.twitter.finagle.Httpx
import com.twitter.util.Await

import io.finch.request._
import io.finch.route._

object Greetings extends App {

  // Get /hi/:name -> "Hi, $name"
  val hi: Router[String] = get("hi" / string) { name: String =>
    s"Hi, $name"
  }

  // Reader Monad, request is mutable
  val title: RequestReader[String] = paramOption("title").withDefault("")
  val hello: Router[String] =
    get("hello" / string ? title) { (name: String, title: String) =>
      s"Hello, $title$name"
    }

  // GET /hello2?name=Bob&title=Dr.
  case class Who(n: String, t: String)
  val who: RequestReader[Who] = (param("name") :: title).as[Who]
  val hello2: Router[String] =
    get("hello2" ? who) { w: Who =>
      s"Hello2, ${w.t}${w.n}!"
    }

  // :+: creates a shapeless coproduct type
  Await.ready(Httpx.server.serve(":8081", (hi :+: hello :+: hello2).toService))
}
