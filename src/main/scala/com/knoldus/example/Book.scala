package com.knoldus.example
import java.time.Instant

import com.knoldus.string.StringUtils._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class Book(bookId:String,
                title:String,
                author:String,
                price:Int,
                publicationDate:Instant,
                bookDetails:Option[String],
                copiesSold:Int,
                royalty:Option[Double]){
  def trimFieldLength: Book = {
    copy(bookDetails = bookDetails.map(_.truncateToLength()))
  }
}

object Book{
  implicit val r: Reads[Book] = ((JsPath \ "bookId").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "author").read[String] and
    (JsPath \ "price").read[Int] and
    (JsPath \ "publicationDate").read[Instant](Reads.DefaultInstantReads) and
    (JsPath \ "bookDetails").readNullable[String] and
    (JsPath \ "copiesSold").read[Int] and
    (JsPath \ "royalty").readNullable[Double]
    ) (Book.apply _)
}