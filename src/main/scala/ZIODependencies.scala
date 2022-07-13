package tc.lab.daniel

import zio.*

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

    override def run = runnableProgram_v3
