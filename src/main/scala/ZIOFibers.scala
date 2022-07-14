package tc.lab.daniel
import utils.*

import zio.*

object ZIOFibers extends ZIOAppDefault:

    val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(10)
    val favLang: ZIO[Any, Nothing, String] = ZIO.succeed("Scala")

    val combinator: ZIO[Any, Nothing, (Int, String)] =
      for
          mol <- meaningOfLife.debugThread
          lang <- favLang.debugThread
      yield (mol, lang)

    override def run: URIO[Any, ExitCode] = combinator.exitCode
