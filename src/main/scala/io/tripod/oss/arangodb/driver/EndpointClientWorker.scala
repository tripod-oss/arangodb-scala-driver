package io.tripod.oss.arangodb.driver

import akka.actor.{Actor, Props}

object EndpointClientWorker {
  def props(endPointRoot: String): Props =
    Props(new EndpointClientWorker(endPointRoot))
}

class EndpointClientWorker(endPointRoot: String) extends Actor {
  def receive = {
    case _ => println
  }
}
