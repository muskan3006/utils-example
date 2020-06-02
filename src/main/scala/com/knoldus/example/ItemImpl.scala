package com.knoldus.example

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink}
import com.knoldus.logging.HasLog
import org.apache.kafka.clients.consumer.ConsumerRecord
import play.api.libs.json.Json

import scala.concurrent.Future

object ItemImpl extends AkkaStreamJob(ActorSystem.create()) with App with HasLog{
val source = KafkaSource.createSource("item")
 val flow = Flow[ConsumerRecord[Array[Byte], String]].map { message =>
   log.info("Received a message")
   Json.parse(message.value).as[Item]
 }
  val print: Sink[Any, Future[Done]] = Sink.foreach(ele => println(ele))
  source.via(flow).runWith(print)
}
