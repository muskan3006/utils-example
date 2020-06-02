package com.knoldus.example

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

object KafkaSource {
implicit val actorSystem: ActorSystem = ActorSystem.create()
  val consumerSettings: ConsumerSettings[Array[Byte], String] = ConsumerSettings(actorSystem, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("alpakka-demo")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
 def createSource(topic:String): Source[ConsumerRecord[Array[Byte], String], Consumer.Control] ={
   Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))
}
}
