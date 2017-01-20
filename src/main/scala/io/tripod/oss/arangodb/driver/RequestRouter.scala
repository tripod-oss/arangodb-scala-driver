package io.tripod.oss.arangodb.driver

import akka.actor.{Actor, ActorRef, Terminated}
import akka.routing.{AddRoutee, RoundRobinRoutingLogic, Router}
import io.tripod.oss.arangodb.driver.RequestRouter.{
  AddEndpoint,
  GetEndPoints,
  RemoveEndpoint
}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by nicolas.jouanin on 20/01/17.
  */
object RequestRouter {
  case class AddEndpoint(endpointRoot: String)
  case class RemoveEndpoint(endpointRoot: String)
  case object GetEndPoints
}

class RequestRouter extends Actor {
  val endpointWorkers = new mutable.HashMap[String, ActorRef]
  val router = Router(RoundRobinRoutingLogic())

  def receive = {
    case AddEndpoint(endpoint: String) =>
      if (endpointWorkers.contains(endpoint)) {
        //End point already declared
      } else {
        val workerRef = context.actorOf(EndpointClientWorker.props(endpoint))
        endpointWorkers.put(endpoint, workerRef)
        context.watch(workerRef)
        router.addRoutee(workerRef)
      }
    case RemoveEndpoint(endpoint: String) =>
      endpointWorkers.get(endpoint).map(routee => router.removeRoutee(routee))
      endpointWorkers.remove(endpoint)
    case GetEndPoints => sender() ! endpointWorkers.keySet.toList
    case Terminated(w) =>
      endpointWorkers.foreach {
        case (endpoint, worker) =>
          if (w.equals(worker)) {
            router.removeRoutee(w)
            endpointWorkers.remove(endpoint)
            self ! AddEndpoint(endpoint)
          }
      }
    //case work => println(work) //router.route(work, sender())
  }
}
