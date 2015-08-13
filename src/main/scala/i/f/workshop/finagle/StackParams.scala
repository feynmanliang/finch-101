package i.f.workshop.finagle

import com.twitter.finagle.param
import com.twitter.finagle.httpx.{Response, Request}
import com.twitter.finagle.service.TimeoutFilter
import com.twitter.finagle.transport.Transport
import com.twitter.finagle.{Httpx, Service}
import com.twitter.util._
import com.twitter.conversions.time._

object StackParams extends App {
  implicit val t: Timer = new JavaTimer()
  val s: Service[Request, Response] = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = {
      Future.sleep(3.seconds).map(_ => Response())
    }
  }

  Await.ready(Httpx.server
    //.configured(TimeoutFilter.Param(1.seconds))
    .configured(Transport.Verbose(true))
    .serve(":8081", s))
}
