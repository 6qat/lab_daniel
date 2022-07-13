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
    override def run = program_v2.provide(
      ZLayer.succeed( // Dependency injection
        UserSubscription.create(
          EmailService.create(),
          UserDatabase.create(
            ConnectionPool.create(8)
          )
        )
      )
    )
