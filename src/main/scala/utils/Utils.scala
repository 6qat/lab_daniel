package tc.lab.daniel
package utils

import zio.ZIO

extension [R, E, A](zio: ZIO[R, E, A])
  def debugThread: ZIO[R, E, A] =
    // tap() produces the effect AFTER the original zio is evaluated
    zio.tap(value =>
      ZIO.succeed(println(s"[${Thread.currentThread().getName}] $value"))
    )
