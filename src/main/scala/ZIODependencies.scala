package tc.lab.daniel

import zio.*

object ZIODependencies extends ZIOAppDefault:

   // app to subscribe users to newsletter

   case class User(name: String, email: String)

   class UserSubscription(
       emailService: EmailService,
       userDatabase: UserDatabase
   ):
      def subscribeUser(user: User): Task[Unit] =
        for
           _ <- emailService.email(user)
           _ <- userDatabase.insert(user)
        yield ()

   object UserSubscription:
      def create(
        emailService: EmailService,
        userDatabase: UserDatabase
      ): UserSubscription =
        UserSubscription(emailService, userDatabase)

   class EmailService:
      def email(user: User): Task[Unit] =
        ZIO.succeed(s"You've just been subscribed, ${user.name}")
   object EmailService:
      def create(): EmailService =
        EmailService()

   class UserDatabase(connectionPool: ConnectionPool):
      def insert(user: User): Task[Unit] =
        for
           connection <- connectionPool.get
           _ <- connection.runQuery(s"Insert $user into database")
        yield ()
   object UserDatabase:
      def create(connectionPool: ConnectionPool): UserDatabase =
        UserDatabase(connectionPool)

   class ConnectionPool(nConnections: Int):
      def get: Task[Connection] =
        ZIO.succeed(
          println(s"Acquired connection out of $nConnections connections.")
        )
          *> ZIO.succeed(
            Connection()
          )
   object ConnectionPool:
      def create(nConnections: Int): ConnectionPool =
        ConnectionPool(nConnections)

   case class Connection():
      def runQuery(query: String): Task[Unit] =
        ZIO.succeed(println(s"Running query: $query"))

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

   import scala.compiletime.uninitialized
   var later: Int = uninitialized
   later = 0

   override def run = subscribe(User("Guilherme", "guiga@usa.net"))
