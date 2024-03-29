package tc.lab.daniel
import utils.*

import zio.*

import scala.annotation.unused

object ZIOResources extends ZIOAppDefault {

  // finalizers
  def unsafeMethod(): Int = throw new RuntimeException(
    "Not an int here for you!"
  )
  val anAttempt: Task[Int] = ZIO.attempt(unsafeMethod())
  val annAttemptWithFinalizer: ZIO[Any, Throwable, Int] =
    anAttempt.ensuring(ZIO.succeed("finalizer").debugThread)

  @unused
  val anAttemptWith2Finalizers: ZIO[Any, Throwable, Int] =
    annAttemptWithFinalizer.ensuring(ZIO.succeed("finalizer 2").debugThread)
    // onInterrupt, onError, onDone, onExit

  // resource lifecycle
  class Connection(url: String):
      def open() = ZIO.succeed(s"Opening connection to $url").debugThread
      def close() = ZIO.succeed(s"Closing connection to $url").debugThread

  object Connection:
      def create(url: String): ZIO[Any, Nothing, Connection] =
        ZIO.succeed(new Connection(url))

  @unused
  val fetchUrl: ZIO[Any, Nothing, Unit] =
    for
        conn <- Connection.create("rockthejvm.com")
        fiber <- (conn.open() *> Clock.sleep(300.seconds)).fork
        _ <- Clock.sleep(1.second) *>
          ZIO.succeed("interrupting").debugThread *> fiber.interrupt
        _ <- fiber.join
    yield () // connection leak

  @unused
  val correctFetchUrl: ZIO[Any, Nothing, Unit] =
    for
        conn <- Connection.create("rockthejvm.com")
        fiber <- (conn.open() *> Clock.sleep(300.seconds))
          .ensuring(conn.close())
          .fork
        _ <- Clock.sleep(1.second) *> ZIO
          .succeed("interrupting")
          .debugThread *> fiber.interrupt
        _ <- fiber.join
    yield () // prevents connection leak

  // acquireRelease
  val cleanConnection: ZIO[Scope, Nothing, Connection] =
    ZIO.acquireRelease(Connection.create("rockthejvm.com"))(_.close())

  // *** In the main application, Scope will automatically be provided
  val fetchWithResource: ZIO[Scope, Nothing, Unit] =
    for
        conn <- cleanConnection
        fiber <- (conn.open() *> Clock.sleep(300.seconds)).fork
        _ <- Clock.sleep(1.second) *> ZIO
          .succeed("interrupting")
          .debugThread *> fiber.interrupt
        _ <- fiber.join
    yield ()

  // not really needed when running the effect direct on the main run method.
  @unused
  val fetchWithScopedResource: ZIO[Any, Nothing, Unit] =
    ZIO.scoped(fetchWithResource)

  // acquireReleaseWith
  val cleanConnection_v2: ZIO[Any, Nothing, Unit] =
    ZIO.acquireReleaseWith(Connection.create("rockthejvm.com"))(_.close())(
      conn => conn.open() *> Clock.sleep(300.seconds)
    )

  val fetchWithResource_v2: ZIO[Any, Nothing, Unit] =
    for
        fiber <- cleanConnection_v2.fork
        _ <- Clock.sleep(1.second) *> ZIO
          .succeed("interrupting")
          .debugThread *> fiber.interrupt
        _ <- fiber.join
    yield ()

  def run = fetchWithResource_v2
}
