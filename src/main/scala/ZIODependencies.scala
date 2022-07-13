package tc.lab.daniel

import zio.*

object ZIODependencies extends ZIOAppDefault {

  // app to subscribe users to newsletter

  case class user(name: String, email: String)
  class UserSubscription
  class EmailService
  class userDatabase



  override def run = ???

}
