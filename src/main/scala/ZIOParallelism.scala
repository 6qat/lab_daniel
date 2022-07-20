package tc.lab.daniel
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

  def run = ???
}
