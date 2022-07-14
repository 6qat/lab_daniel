package tc.lab.daniel
package utils

import zio.ZIO

def logError[E](error: E): Unit =
  println(s"Fiber [${Thread.currentThread().getName}] failed: $error")

def logSucceed[A](value: A): Unit =
  println(
    s"Fiber [${Thread.currentThread().getName}] succeeded: $value"
  )

extension [R, E, A](zio: ZIO[R, E, A])
    def debugThread: ZIO[R, E, A] = debugThreadEither

    def debugThreadBoth: ZIO[R, E, A] =
      // tap() produces the effect AFTER the original zio is evaluated
      zio.tapBoth(
        error =>
          ZIO.succeed(
            logError(error)
          ) *> ZIO.fail(error),
        value =>
          ZIO.succeed(
            println(
              logSucceed(value)
            )
          ) *> ZIO.succeed(value)
      )
    def debugThreadSucceed: ZIO[R, E, A] =
      zio.tap(value => ZIO.succeed(logSucceed(value)))

    def debugThreadEither: ZIO[R, E, A] =
      zio.tapEither(either =>
        either match
            case Right(value) => ZIO.succeed(logSucceed(value))
            case Left(value)  => ZIO.succeed(logError(value))
      )
