package tc.lab.daniel
import zio.*

object ZIOInterruptions extends ZIOAppDefault {

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

  def run = ???
}
