package tc.lab.daniel
import utils.*

import zio.*

object ZIOFibers extends ZIOAppDefault:

    val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(10)
    val favLang: ZIO[Any, Nothing, String] = ZIO.succeed("Scala")

    def createFiber: Fiber[Throwable, String] = ???

    val combinator: ZIO[Any, Nothing, (Int, String)] =
      for
          mol <- meaningOfLife.debugThread
          lang <- favLang.debugThread
      yield (mol, lang)

    // actually it's a different virtual thread (fiber)
    val differentThreadIO =
      for
          _ <- meaningOfLife.debugThread.fork
          _ <- favLang.debugThread.fork
      yield ()

    val meaningOfLifeFiber: UIO[Fiber[Throwable, Int]] =
      meaningOfLife.fork

    // join a fiber
    def runOnAnotherThread[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] =
      for
          fiber <- zio.debugThread.fork
          result <- fiber.join
      yield result

    // await a fiber
    def runOnAnotherThread_v2[R, E, A](zio: ZIO[R, E, A]) =
      for
          fiber <- zio.debugThread.fork
          result <- fiber.await
      yield result match
          case Exit.Success(value) => s"Succeeded with value $value"
          case Exit.Failure(cause) => s"Failed with $cause"

    // poll: peek at the result of the fiber RIGHT NOW, without blocking
    val peekFiber =
      for
          fiber <- ZIO.attempt {
            Thread.sleep(1000)
            42
          }.fork
          result <- fiber.poll // result type: Option[Exit[Throwable, Int]]
      yield result match
          case None => 0
          case Some(exit) =>
            exit match
                case Exit.Success(value) => value
                case Exit.Failure(cause) =>
                  cause match
                      case Cause.Empty           => 1
                      case Cause.Fail(_, _)      => 2
                      case Cause.Die(_, _)       => 3
                      case Cause.Stackless(_, _) => 4
                      case Cause.Both(_, _)      => 5
                      case Cause.Interrupt(_, _) => 6
                      case Cause.Then(_, _)      => 7

    val zippedFibers =
      for
          fiber1 <- ZIO.succeed("Result 1").debugThread.fork
          fiber2 <- ZIO.succeed("Result 2").debugThread.fork
          fiber = fiber1.zip(fiber2)
          resultTuple <- fiber.join
      yield resultTuple

    val chainedFibers: ZIO[Any, Nothing, String] =
      for
          fiber1: Fiber[String, Nothing] <- ZIO
            .fail("Not good")
            .debugThreadDaniel
            .fork
          fiber2: Fiber[Nothing, String] <- ZIO.succeed("Ok").debugThread.fork
          fiber: Fiber[Nothing, String] = fiber1.orElse(fiber2)
          result: String <- fiber.join
      yield result

    /** Exercises
      *   1. Zip 2 fibers without using the zip combinator hing (hint: create a
      *      fiber that waits for both) 2. Same thing with orElse
      */

    def zipFibers[E, E1 <: E, E2 <: E, A, B](
      fiber1: Fiber[E1, A],
      fiber2: Fiber[E2, B]
    ): ZIO[Any, Nothing, Fiber[E, (A, B)]] =
      (for
          a <- fiber1.join
          b <- fiber2.join
      yield (a, b)).fork

    def orElseFibers[E, A](
      fiber1: Fiber[E, A],
      fiber2: Fiber[E, A]
    ): ZIO[Any, Nothing, Fiber[E, A]] =
      fiber1.join.orElse(fiber2.join).fork

    override def run = chainedFibers.debugThread
