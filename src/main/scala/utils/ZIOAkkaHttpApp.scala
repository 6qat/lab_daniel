package tc.lab.daniel
package utils

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}
import akka.{Done, NotUsed}
import zio.*

import scala.concurrent.Future

object ZIOAkkaHttpApp extends ZIOAppDefault {

  // : ZIO[Any & Scope, Throwable, ActorSystem[Nothing]]
  def scopedActorSystemLayer(
    name: String
  ): ZLayer[Any & Scope, Throwable, ActorSystem[Nothing]] = ZLayer.fromZIO(
    ZIO.acquireRelease(
      ZIO.attempt(ActorSystem(Behaviors.empty, name))
    )(r => ZIO.succeed(r.terminate()))
  )

  val source = Source(List(1, 2, 3))
  val graph: RunnableGraph[Future[Done]] =
    source.toMat(Sink.foreach(println(_)))(Keep.right)

  val graphRunner: ZIO[ActorSystem[Nothing], Throwable, Done] = for {
    system <- ZIO.service[ActorSystem[Nothing]]
    doneFuture = graph.run()(using Materializer.matFromSystem(system))
    done <- ZIO.fromFuture(_ => doneFuture)
  } yield done

//  val program = ZIO.scoped {}

  override def run = ZIO.scoped(
    scopedActorSystemLayer("system")(
      graphRunner
    )
  )

}
