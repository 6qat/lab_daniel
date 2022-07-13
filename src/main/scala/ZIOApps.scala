package tc.lab.daniel

import zio.*

object ZIOApps {

  val meaningOfLife: UIO[Int] = ZIO.succeed({
    // println("1")
    1
  })

  def main(args: Array[String]): Unit = {
    val runtime = Runtime.default

    given trace: Trace = Trace.empty

    Unsafe.unsafeCompat { unsafe =>
      given u: Unsafe = unsafe

      val output: Exit[Nothing, Int] = runtime.unsafe.run(meaningOfLife)
      println(output)
    }
  }

}

object BetterApp extends ZIOAppDefault {

  // provides runtime, trace, ...
  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    //ZIOApps.meaningOfLife.flatMap(mol => ZIO.succeed(println(mol)))
    ZIOApps.meaningOfLife.debug

}

// Not needed
object ManualApp extends ZIOApp {

  override def bootstrap: ZLayer[ZIOAppArgs & Scope, Any, Environment] = ???
  override implicit def environmentTag = ???
  override type Environment = this.type 

  override def run = ???
}
