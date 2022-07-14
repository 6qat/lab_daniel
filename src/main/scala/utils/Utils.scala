package tc.lab.daniel
package utils

import zio.ZIO

extension [R, E, A](zio: ZIO[R, E, A])
  def debugThread: ZIO[R, E, A] =
    // tap() produces the effect AFTER the original zio is evaluated
    zio.tapBoth(
      error ⇒
        ZIO.succeed(
          println(s"Fiber [${Thread.currentThread().getName}] failed: $error")
        ) *> ZIO.fail(error),
      value ⇒
        ZIO.succeed(
          println(
            s"Fiber [${Thread.currentThread().getName}] succeeded: $value"
          )
        )
    )
