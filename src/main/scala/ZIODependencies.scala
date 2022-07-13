package tc.lab.daniel

import zio.*

object ZIODependencies extends ZIOAppDefault {

  // app to subscribe users to newsletter

  case class User(name: String, email: String)

  class UserSubscription(
      emailService: EmailService,
      userDatabase: UserDatabase
  ):
    def subscribeUser(user: User): Task[Unit] = ???

  class EmailService:
    def email(user: User): Task[Unit] = ???

  class UserDatabase(connectionPool: ConnectionPool):
    def insert(user: User): Task[Unit] = ???

  class ConnectionPool(nConnections: Int):
    def get: Task[Connection] = ???

  case class Connection():
    def runQuery(query: String): Task[Unit] = ???

  import scala.compiletime.uninitialized
  var later: Int = uninitialized

  override def run = ???

}
