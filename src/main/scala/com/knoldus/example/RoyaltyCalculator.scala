package com.knoldus.example

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}
import com.knoldus.config.HasDefaultConfig
import com.knoldus.logging.HasLog
import com.knoldus.play2spray.Play2Spray
import com.knoldus.time.Time
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.elasticsearch.client.RestClient
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object RoyaltyCalculator extends AkkaStreamJob(ActorSystem("system")) with App with HasDefaultConfig with HasLog {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val client: RestClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build()

  val TOPIC: String = config.getString("book.topic")
  val bookData: Source[ConsumerRecord[Array[Byte], String], Consumer.Control] = KafkaSource.createSource(TOPIC)
  val parseBook: Flow[ConsumerRecord[Array[Byte], String], Book, NotUsed] = Flow[ConsumerRecord[Array[Byte], String]].map { message =>
    log.info("Received a message")
    Json.parse(message.value).as[Book]
  }

  val esSink2: Sink[WriteMessage[Book, NotUsed], Future[Done]] = ElasticsearchSink
    .create[Book]("book-index", "book")(client, Play2Spray(Book.format))

  val flow: Flow[Book, Book, NotUsed] = Flow[Book].map { book => calculateRoyalty(book) }


  val flow2: Flow[Book, WriteMessage[Book, NotUsed], NotUsed] = Flow[Book].map(book => WriteMessage.createIndexMessage(book.bookId, book))

  def calculateRoyalty(book: Book): Book = {
    val timeTillNow = Time.timeSince(book.publicationDate)
    val royalty = if (timeTillNow.toDays > 1000) {
      .10 * book.price * book.copiesSold
    } else {
      .15 * book.price * book.copiesSold
    }
    book.copy(royalty = Some(royalty))
  }

  bookData.via(parseBook).via(flow).via(flow2).runWith(esSink2)

}
