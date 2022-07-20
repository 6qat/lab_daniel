package tc.lab.daniel
package utils

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.RunnableGraph

object AkkaUtil {
  case class GraphRunner[A, M](system: ActorSystem[A], graph: RunnableGraph[M])
}
