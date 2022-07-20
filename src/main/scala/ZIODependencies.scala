package tc.lab.daniel

import zio.*

import java.io.IOException
import java.util.concurrent.TimeUnit

object ZIODependencies extends ZIOAppDefault:

    val subscriptionService: UIO[UserSubscription] =
      ZIO.succeed( // Dependency injection
        UserSubscription.create(
          EmailService.create(),
          UserDatabase.create(
            ConnectionPool.create(8)
          )
        )
      )

    def subscribe(user: User): Task[Unit] =
      for
          sub <- subscriptionService
          _ <- sub.subscribeUser(user)
      yield ()

    def subscribe_v2(user: User): ZIO[UserSubscription, Throwable, Unit] =
      for
          // sub is of type ZIO[UserSubscription, Nothing, UserSubscriptionService
          sub <- ZIO.service[UserSubscription]
          _ <- sub.subscribeUser(user)
      yield ()

    /** ZLayers
      */

    val connectionPoolLayer: ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(ConnectionPool.create(8))

    val databaseLayer: ZLayer[ConnectionPool, Nothing, UserDatabase] =
      ZLayer.fromFunction(UserDatabase.create _)

    val emailServiceLayer: ZLayer[Any, Nothing, EmailService] =
      ZLayer.fromFunction(EmailService.create _)

    val userSubscriptionServiceLayer
      : ZLayer[EmailService & UserDatabase, Nothing, UserSubscription] =
      ZLayer.fromFunction(UserSubscription.create _)

    // composing layers
    // vertical layer >>>
    val databaseLayerFull: ZLayer[Any, Nothing, UserDatabase] =
      connectionPoolLayer >>> databaseLayer
    // horizontal layer ++ : combine the dependencies of  both layers AND the values
    // of both layers
    val subscriptionRequirementsLayer
      : ZLayer[Any, Nothing, UserDatabase & EmailService] =
      databaseLayerFull ++ emailServiceLayer

    val userSubscriptionLayer: ZLayer[Any, Nothing, UserSubscription] =
      subscriptionRequirementsLayer >>> userSubscriptionServiceLayer

    val userSubscriptionLayer_v2: ZLayer[Any, Nothing, UserSubscription] =
      databaseLayerFull ++ emailServiceLayer >>> userSubscriptionLayer

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

end ZIODependencies
