package tc.lab.daniel

import zio.*

// best practice: create layers in the companion object of the service you are exposing

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

    val live: ZLayer[EmailService & UserDatabase, Nothing, UserSubscription] =
      ZLayer.fromFunction(create _)

case class User(name: String, email: String)

class EmailService:
    def email(user: User): Task[Unit] =
      ZIO.succeed(s"You've just been subscribed, ${user.name}")

object EmailService:
    def create(): EmailService =
      EmailService()
    val live: ZLayer[Any, Nothing, EmailService] =
      ZLayer.succeed(create())

class UserDatabase(connectionPool: ConnectionPool):
    def insert(user: User): Task[Unit] =
      for
          connection <- connectionPool.get
          _ <- connection.runQuery(s"Insert $user into database")
      yield ()

object UserDatabase:
    def create(connectionPool: ConnectionPool): UserDatabase =
      UserDatabase(connectionPool)
    val live: ZLayer[ConnectionPool, Nothing, UserDatabase] =
      ZLayer.fromFunction(create _)

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
    def live(n: Int): ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(create(n))

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


