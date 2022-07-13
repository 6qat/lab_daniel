package tc.lab.daniel
import zio.*
import utils.*

object ZioFibers extends ZIOAppDefault:

    override def run = ZIO.succeed(println("Test")).debugThread
