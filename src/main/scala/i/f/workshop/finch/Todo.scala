package i.f.workshop.finch

import java.util.UUID

import com.twitter.app.Flag
import com.twitter.finagle.param.Stats
import com.twitter.finagle.stats.Counter

import scala.collection.mutable
import com.twitter.finagle.{Httpx, ListeningServer, Service, SimpleFilter}
import com.twitter.finagle.httpx.{Request, Response}
import com.twitter.server.TwitterServer
import com.twitter.util.{Future, Await}

import io.finch.request._
import io.finch.response._
import io.finch.route._
import io.finch.circe._
import io.circe.generic.auto._

object Todo extends App {

  case class Todo(id: String, title: String, completed: Boolean, order: Int)

  object Todo {
    private[this] val db: mutable.Map[String, Todo] = mutable.Map.empty[String, Todo]

    def get(id: String): Option[Todo] = synchronized { db.get(id) }
    def list(): List[Todo] = synchronized { db.values.toList }
    def save(t: Todo): Unit = synchronized { db += (t.id -> t) }
    def delete(id: String): Unit = synchronized { db -= id }
  }

  val todo: RequestReader[Todo] = body.as[String => Todo]
  .map(_(UUID.randomUUID().toString))

  val postTodo: Router[Todo] = post("todos" ? todo) { t: Todo =>
    Todo.save(t)

    t
  }

  val getTodos: Router[List[Todo]] = get("todos") { Todo.list() }
  val patchedTodo: RequestReader[Todo => Todo] = body.as[Todo => Todo]
  val patchTodo: Router[Todo] =
    patch("todos" / string ? patchedTodo) { (id: String, pt: Todo => Todo) =>
      val currentTodo = Todo.get(id).get
      val newTodo = pt(currentTodo)
      Todo.save(newTodo)

      newTodo
    }

  Await.ready(Httpx.server.serve(":8081", (getTodos :+: postTodo :+: patchTodo).toService))
}
