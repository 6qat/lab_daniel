package tc.lab.daniel
package utils

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.RunnableGraph
import zio.ZIO

import scala.annotation.unused

def printMessageError[E](error: E, pre: String = ""): Unit =
  println(s"${pre}Fiber [${Thread.currentThread().getName}] failed: $error")

def printMessageSucceed[A](value: A, pre: String = ""): Unit =
  println(
    s"${pre}Fiber [${Thread.currentThread().getName}] succeeded: $value"
  )

extension [R, E, A](zio: ZIO[R, E, A])
    def debugThread: ZIO[R, E, A] =
      debugThreadEither

    @unused
    def debugThreadBoth: ZIO[R, E, A] =
      // tap() produces the effect AFTER the original zio is evaluated
      zio.tapBoth(
        error =>
          ZIO.succeed(
            printMessageError(error)
          ) *> ZIO.fail(error),
        value =>
          ZIO.succeed(
            println(
              printMessageSucceed(value)
            )
          ) *> ZIO.succeed(value)
      )
    def debugThreadDaniel: ZIO[R, E, A] =
      zio
        .tap(value => ZIO.succeed(printMessageSucceed(value)))
        .tapErrorCause(cause => ZIO.succeed(printMessageError(cause)))

    def debugThreadEither: ZIO[R, E, A] =
      zio.tapEither(either =>
        either match
            case Right(value) => ZIO.succeed(printMessageSucceed(value))
            case Left(value)  => ZIO.succeed(printMessageError(value))
      )

extension [M](@unused graph: RunnableGraph[M])
  @unused
  def runZIO(@unused system: ActorSystem[Nothing]): String = "teste"
