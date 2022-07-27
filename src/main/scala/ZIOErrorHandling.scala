package tc.lab.daniel

import zio.*

import java.io.IOException
import java.net.NoRouteToHostException
import scala.annotation.unused
import scala.util.Try

object ZIOErrorHandling extends ZIOAppDefault {

  @unused
  val aFailedZIO = ZIO.fail("Something went wrong")
  val failedWithThrowable = ZIO.fail(new RuntimeException("Boom!!!"))
  @unused
  val failedWithDescription = failedWithThrowable.mapError(_.getMessage)

  val anAttempt: Task[Int] = ZIO.attempt {
    println("Trying something...")
    val string: String = null
    string.length
  }

  @unused
  val stillCanFail: ZIO[Any, Throwable, Any] =
    anAttempt.catchAll(e => ZIO.attempt(s"Still an error $e"))

  val cannotFailAnymore: ZIO[Any, Nothing, Any] =
    anAttempt.catchAll(e =>
      ZIO.succeed(s"Returning a different value because $e")
    )

  // catchSome function KEEPS the error channel. It actually broader the error channel
  @unused
  val catchSelectiveErrors: ZIO[Any, Throwable | String, Any] =
    anAttempt.catchSome {
      case e: RuntimeException =>
        ZIO.succeed(s"Ignoring runtime exceptions: $e")
      case _ => ZIO.fail("Ignoring everything else")
    }

  @unused
  val aBetterAttempt: ZIO[Any, Nothing, Int] =
    anAttempt.orElse(ZIO.succeed(30))

  @unused
  val handleBoth: URIO[Any, String] =
    anAttempt.fold(
      ex => s"Something bad happened $ex",
      value => s"Length of the string was $value"
    )

  @unused
  val handleBoth_v2 =
    anAttempt.foldZIO(
      ex => ZIO.succeed(s"Something bad happened $ex"),
      value => ZIO.succeed(s"Length of the string was $value")
    )

  @unused
  val aTryToZio: Task[Int] = ZIO.fromTry(Try(42 / 0))

  val anEither: Either[Int, String] = Right("Success")
  @unused
  val anEithyerToZIO: IO[Int, String] = ZIO.fromEither(anEither)

  val eitherZIO: URIO[Any, Either[Throwable, Int]] = anAttempt.either
  @unused
  val anAttempt_v2: ZIO[Any, Throwable, Int] = eitherZIO.absolve

  @unused
  val anOptionToZIO: IO[Option[Nothing], Int] = ZIO.fromOption(Some(42))

  val failedInt: ZIO[Any, String, Nothing] =
    ZIO.fail("I failed!")

  val failureWithCauseExposed: ZIO[Any, Cause[String], Nothing] =
    failedInt.sandbox

  @unused
  val failureCauseHidden: ZIO[Any, String, Nothing] =
    failureWithCauseExposed.unsandbox

  def callHTTPEndpoint(url: String): ZIO[Any, IOException, String] =
    ZIO.fail(new IOException(""))

  val endpointCallWithDefects: ZIO[Any, Nothing, String] =
    callHTTPEndpoint("").orDie

  // Refining the error channel

  def callHTTPEndpointWideError(url: String): ZIO[Any, Exception, String] =
    ZIO.fail(new IOException(""))

  @unused
  def callHTTPEndpoint_v2(url: String): ZIO[Any, IOException, String] =
    callHTTPEndpointWideError(url).refineOrDie[IOException] {
      case e: IOException => e
      case _: NoRouteToHostException =>
        new IOException(s"No route to host to $url")
    }

  @unused
  val endpointCallWithError: ZIO[Any, String, String] =
    endpointCallWithDefects.unrefine { case e =>
      e.getMessage
    }

  // Combining effects with different errors

  case class IndexError(message: String)
  case class DbError(message: Int)

  val test: IndexError | DbError = DbError(2)
  @unused
  val test2 = test

  val callApi: ZIO[Any, IndexError, String] = ZIO.succeed("page: <html></html>")
  val queryDb: ZIO[Any, DbError, Int] = ZIO.succeed(1)
  @unused
  val combinedApi: ZIO[Any, IndexError | DbError, (String, Int)] = for {
    page <- callApi
    rowsAffected <- queryDb
  } yield (page, rowsAffected)

  override def run = cannotFailAnymore

}
