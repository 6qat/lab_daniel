package tc.lab.daniel

import zio.*

object ZIODependencies extends ZIOAppDefault:

   // app to subscribe users to newsletter

   def subscribe(user: User): Task[Unit] =
     for
        sub <- subscriptionService
        _ <- sub.subscribeUser(user)
     yield ()

   val program: Task[Unit] =
     for
        _ <- subscribe(User("User1", "email1"))
        _ <- subscribe(User("User2", "email2"))
     yield ()

   // Alternative

   def subscribe_v2(user: User): ZIO[UserSubscription, Throwable, Unit] =
     for
        // sub is of type ZIO[UserSubscription, Nothing, UserSubscriptionService
        sub <- ZIO.service[UserSubscription]
        _ <- sub.subscribeUser(user)
     yield ()

   val program_v2: ZIO[UserSubscription, Throwable, Unit] =
     for
        _ <- subscribe_v2(User("User1", "email1"))
        _ <- subscribe_v2(User("User2", "email2"))
     yield ()

   import scala.compiletime.uninitialized
   var later: Int = uninitialized
   later = 0

   override def run = program  // program_v2.provide(ZLayer.fromZIO(subscriptionService))
