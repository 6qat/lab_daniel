package tc.lab.daniel

import zio.*

object ZIOEffects {

  val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(2)
  val aFailure: ZIO[Any, String, Nothing] = ZIO.fail("Something went wrong")
  val aSuspendedZIO: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife)
  val improvedMeaningOfLife: ZIO[Any, Nothing, Int] = meaningOfLife.map(_ * 2)

  def main(args: Array[String]): Unit = {}

}
