package tc.lab.daniel
import utils.*

import zio.*

object ZIOResources extends ZIOAppDefault {

  // finalizers
  def unsafeMethod(): Int = throw new RuntimeException(
    "Not an int here for you!"
  )
  val anAttempt = ZIO.attempt(unsafeMethod())
  val annAttemptWithFinalizer =
    anAttempt.ensuring(ZIO.succeed("finalizer").debugThread)

  val anAttemptWith2Finalizers =
    annAttemptWithFinalizer.ensuring(ZIO.succeed("finalizer 2").debugThread)

  def run = anAttemptWith2Finalizers
}
