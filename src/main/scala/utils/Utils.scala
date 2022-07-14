package tc.lab.daniel
package utils

import zio.ZIO

def printMessageError[E](error: E): Unit =
  println(s"Fiber [${Thread.currentThread().getName}] failed: $error")

def printMessageSucceed[A](value: A): Unit =
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
            printMessageError(error)
          ) *> ZIO.fail(error),
        value =>
          ZIO.succeed(
            println(
              printMessageSucceed(value)
            )
          ) *> ZIO.succeed(value)
      )
    def debugThreadSucceed: ZIO[R, E, A] =
      zio.tap(value => ZIO.succeed(printMessageSucceed(value)))

    def debugThreadEither: ZIO[R, E, A] =
      zio.tapEither(either =>
        either match
            case Right(value) => ZIO.succeed(printMessageSucceed(value))
            case Left(value)  => ZIO.succeed(printMessageError(value))
      )
