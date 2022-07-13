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

   class EmailService:
      def email(user: User): Task[Unit] =
        ZIO.succeed(s"You've just been subscribed, ${user.name}")

   class UserDatabase(connectionPool: ConnectionPool):
      def insert(user: User): Task[Unit] =
        for
           connection <- connectionPool.get
           _ <- connection.runQuery(s"Insert $user into database")
        yield ()

   class ConnectionPool(nConnections: Int):
      def get: Task[Connection] =
        ZIO.succeed(println("Acquired connection.")) *> ZIO.succeed(
          Connection()
        )

   case class Connection():
      def runQuery(query: String): Task[Unit] =
        ZIO.succeed(println(s"Running query: $query"))

   import scala.compiletime.uninitialized
   var later: Int = uninitialized
   later = 0

   override def run = ???
