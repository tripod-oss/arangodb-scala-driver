package io.tripod.oss.arangodb.driver

import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.routing.{AddRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.tripod.oss.arangodb.driver.RequestRouter.{
  AddEndpoint,
  GetEndPoints,
  RemoveEndpoint
}

import scala.collection.mutable

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object RequestRouter {
  def props(driverConfig: Config, userName: String, password: String) =
    Props(new RequestRouter(driverConfig, userName, password))
  case class AddEndpoint(endpointRoot: String)
  case class RemoveEndpoint(endpointRoot: String)
  case object GetEndPoints
}

class RequestRouter(driverConfig: Config, userName: String, password: String)
    extends Actor
    with LazyLogging {
  val endpointWorkers = new mutable.HashMap[String, ActorRef]
  var router = Router(RoundRobinRoutingLogic())

  def receive = {
    case AddEndpoint(endpoint: String) =>
      if (endpointWorkers.contains(endpoint)) {
        logger.warn(s"AddEndpoint: '$endpoint' already added")
      } else {
        val workerRef =
          context.actorOf(
            EndpointClientWorker
              .props(endpoint, driverConfig, userName, password))
        endpointWorkers.put(endpoint, workerRef)
        context.watch(workerRef)
        router = router.addRoutee(workerRef)
      }
    case RemoveEndpoint(endpoint: String) =>
      endpointWorkers
        .get(endpoint)
        .map(routee => router.removeRoutee(routee)) match {
        case Some(r) ⇒ router = r
        case None ⇒ logger.warn(s"Endpoint '$endpoint' removal failure")
      }
      endpointWorkers.remove(endpoint)
    case GetEndPoints => sender() ! endpointWorkers.keySet.toList
    case Terminated(w) =>
      endpointWorkers.foreach {
        case (endpoint, worker) =>
          if (w.equals(worker)) {
            router = router.removeRoutee(w)
            endpointWorkers.remove(endpoint)
            self ! AddEndpoint(endpoint)
          }
      }
    case work: WorkMessage[_] => router.route(work, sender())
  }
}
