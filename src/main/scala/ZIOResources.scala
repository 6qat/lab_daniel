package tc.lab.daniel
import utils.*

import zio.*

object ZIOResources extends ZIOAppDefault {

  // finalizers
  def unsafeMethod(): Int = throw new RuntimeException(
    "Not an int here for you!"
  )
  val anAttempt = ZIO.attempt(unsafeMethod())
  val annAttemptWithFinalizer =
    anAttempt.ensuring(ZIO.succeed("finalizer").debugThread)

  val anAttemptWith2Finalizers =
    annAttemptWithFinalizer.ensuring(ZIO.succeed("finalizer 2").debugThread)
    // onInterrupt, onError, onDone, onExit

  // resource lifecycle
  class Connection(url: String):
      def open() = ZIO.succeed(s"Opening connection to $url").debugThread
      def close() = ZIO.succeed(s"Closing connection to $url").debugThread

  object Connection:
      def create(url: String): ZIO[Any, Nothing, Connection] =
        ZIO.succeed(new Connection(url))

  val fetchUrl =
    for
        conn <- Connection.create("rockthejvm.com")
        fiber <- (conn.open() *> Clock.sleep(300.seconds)).fork
        _ <- Clock.sleep(1.second) *> ZIO
          .succeed("interrupting")
          .debugThread *> fiber.interrupt
        _ <- fiber.join
    yield ()

  val correctFetchUrl =
    for
        conn <- Connection.create("rockthejvm.com")
        fiber <- (conn.open() *> Clock.sleep(300.seconds))
          .ensuring(conn.close())
          .fork
        _ <- Clock.sleep(1.second) *> ZIO
          .succeed("interrupting")
          .debugThread *> fiber.interrupt
        _ <- fiber.join
    yield ()

  def run = correctFetchUrl
}
