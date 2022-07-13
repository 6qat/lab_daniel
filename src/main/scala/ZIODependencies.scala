package tc.lab.daniel

import zio.*

import java.io.IOException
import java.util.concurrent.TimeUnit

object ZIODependencies extends ZIOAppDefault:

    // app to subscribe users to newsletter

    val program: Task[Unit] =
      for
          _ <- subscribe(User("User1", "email1"))
          _ <- subscribe(User("User2", "email2"))
      yield ()

    // Alternative

    val program_v2: ZIO[UserSubscription, Throwable, Unit] =
      for
          _ <- subscribe_v2(User("User1", "email1"))
          _ <- subscribe_v2(User("User2", "email2"))
      yield ()

    import scala.compiletime.uninitialized
    var later: Int = uninitialized
    later = 0

    // program  // program_v2.provide(ZLayer.fromZIO(subscriptionService))
    val runnableProgram = program_v2.provide(userSubscriptionLayer_v2)
    val runnableProgram_v2 = program_v2.provide(
      UserSubscription.live,
      EmailService.live,
      UserDatabase.live,
      ConnectionPool.live(4),
      // ZLayer.Debug.tree
      ZLayer.Debug.mermaid
    )

    val userSubscriptionLayer_v3: ZLayer[Any, Nothing, UserSubscription] =
      ZLayer.make[UserSubscription](
        UserSubscription.live,
        EmailService.live,
        UserDatabase.live,
        ConnectionPool.live(4),
        // ZLayer.Debug.tree
        ZLayer.Debug.mermaid
      )

    val runnableProgram_v3 = program_v2.provide(userSubscriptionLayer_v3)

    // pass through
    val dbWithPoolLayer
      : ZLayer[ConnectionPool, Nothing, ConnectionPool & UserDatabase] =
      UserDatabase.live.passthrough

    // service: take a dep and expose it as a value to further layers
    val dbService: ZLayer[UserDatabase, Nothing, UserDatabase] =
      ZLayer.service[UserDatabase]

    // launch: creates a ZIO that uses the services and never finishes (good for server loops that never finishes)
    val subscriptionLaunch: ZIO[EmailService & UserDatabase, Nothing, Nothing] =
      UserSubscription.live.launch

    // memoization (hidden feature)
    // memoization is the default, unless you call .fresh on layer

    // Services already provided: Clock, Random, System, Console
    val getTime: UIO[Long] = Clock.currentTime(TimeUnit.SECONDS)
    val randomValue: UIO[RuntimeFlags] = Random.nextInt
    val sysVariable: IO[SecurityException, Option[String]] = System.env("HOME")
    val printlnEffect: IO[IOException, Unit] = Console.printLine("Test...")

    override def run = runnableProgram_v3
