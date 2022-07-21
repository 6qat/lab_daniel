package tc.lab.daniel
import utils.*

import zio.*

object ZIOParallelism extends ZIOAppDefault {

  def myZipPar[R, E, A, B](
    zioa: ZIO[R, E, A],
    ziob: ZIO[R, E, B]
  ): ZIO[R, E, (A, B)] =
      val exits: ZIO[R, Nothing, (Exit[E, A], Exit[E, B])] = for
          fiba <- zioa.fork
          fibb <- ziob.fork
          exita <- fiba.await
          exitb <- exita match
              case Exit.Success(_) => fibb.await
              case Exit.Failure(_) => fibb.interrupt
      yield (exita, exitb)

      exits.flatMap {
        case (Exit.Success(a), Exit.Success(b))     => ZIO.succeed((a, b))
        case (Exit.Success(_), Exit.Failure(cause)) => ZIO.failCause(cause)
        case (Exit.Failure(cause), Exit.Success(_)) => ZIO.failCause(cause)
        case (Exit.Failure(causea), Exit.Failure(causeb)) =>
          ZIO.failCause(causea && causeb)
      }

  // parallel combinators
  // zipPar, zipWithPar

  // collectAllPar
  val effects: Seq[ZIO[Any, Nothing, Int]] =
    (1 to 10).map(i => ZIO.succeed(i).debugThread)

  val collectedValues: ZIO[Any, Nothing, Seq[Int]] =
    ZIO.collectAllPar(effects) // "traverse"
  val collectedValuesDiscarded: ZIO[Any, Nothing, Unit] =
    ZIO.collectAllParDiscard(effects) // just side effects executed

  // foreachPar
  val printlnParallel: ZIO[Any, Nothing, List[Unit]] =
    ZIO.foreachPar((1 to 10).toList)(i => ZIO.succeed(println(i)))

  // reduceAllPar, mergeAllPar
  val sumPar = ZIO.reduceAllPar(ZIO.succeed(0), effects)(_ + _)
  val sumPar_v2 = ZIO.mergeAllPar(effects)(0)(_ + _)

  val temp = ZIO.attempt("")

  def run = sumPar.debugThread
}
