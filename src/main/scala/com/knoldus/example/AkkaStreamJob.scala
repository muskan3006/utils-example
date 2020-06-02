package com.knoldus.example

import akka.actor.ActorSystem
import akka.stream.Materializer


abstract class AkkaStreamJob(as: ActorSystem) {
  implicit val actorSystem: ActorSystem = as
  implicit val materializer = Materializer
}