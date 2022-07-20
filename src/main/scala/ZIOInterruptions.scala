package tc.lab.daniel
import utils.*

import zio.*

object ZIOInterruptions extends ZIOAppDefault {

  val zioWithTime: ZIO[Any, Nothing, Int] =
    (ZIO.succeed("starting computation").debugThread *>
      ZIO.sleep(2.seconds) *>
      ZIO.succeed(42).debugThread)
      .onInterrupt(ZIO.succeed("I was interrupted").debugThread)
    // .onInterrupt, .onDone,

  val interruption: ZIO[Any, Nothing, Int] = for
      fiber <- zioWithTime.fork
      _ <- ZIO.sleep(1.second) *> ZIO
        .succeed("Interrupting!")
        .debugThread *> fiber.interrupt // this is an effect
      _ <- ZIO.succeed("Interruption successful").debugThread
      result <- fiber.join
  yield result

  def timeout[R, E, A](zio: ZIO[R, E, A], time: Duration): ZIO[R, E, A] =
    for
        fiber <- zio.fork
        _ <- (ZIO.sleep(time) *> fiber.interrupt).fork
        result <- fiber.join
    yield result

  def timeout_v2[R, E, A](
    zio: ZIO[R, E, A],
    time: Duration
  ): ZIO[R, E, Option[A]] = timeout(zio, time).foldCauseZIO(
    cause =>
      if cause.isInterrupted then ZIO.succeed(None) else ZIO.failCause(cause),
    value => ZIO.succeed(Some(value))
  )

  def run = interruption
}
