package com.knoldus.example

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}
import com.knoldus.config.HasDefaultConfig
import com.knoldus.logging.HasLog
import com.knoldus.time.Time
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.elasticsearch.client.RestClient
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object RoyaltyCalculator extends App with HasDefaultConfig with HasLog {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val system: ActorSystem = ActorSystem.create()
  implicit val materializer: Materializer.type = Materializer
  val client: RestClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build()

  val TOPIC: String = config.getString("book.topic")
  val bookData: Source[ConsumerRecord[Array[Byte], String], Consumer.Control] = KafkaSource.createSource(TOPIC)
  val parseBook: Flow[ConsumerRecord[Array[Byte], String], Book, NotUsed] = Flow[ConsumerRecord[Array[Byte], String]].map { message =>
    println("Received a message")
    Json.parse(message.value).as[Book]
  }
//  val flow: Flow[Book, Book, NotUsed] = Flow[Book].map(book => toInstant(book))
//  val esSink2: Sink[WriteMessage[Book, NotUsed], Future[Done]] = ElasticsearchSink
//    .create[Book]("book-index", "book")(client, Play2Spray())

   val flow: Flow[Book, Book, NotUsed] = Flow[Book].map{book =>  calculateRoyalty(book)}
 // val flow: Flow[Book, Instant, NotUsed] = Flow[Book].map{book =>  Instant.parse(book.publicationDate)}
  val print: Sink[Any, Future[Done]] = Sink.foreach(ele => println(ele))
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

  bookData.via(parseBook).via(flow).runWith(print)

}
