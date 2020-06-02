package com.knoldus.example

import java.time.Instant

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class Item(id:Int, time:Instant)
object Item{
  //implicit val format: OFormat[Item] =Json.format[Item]

    implicit val r: Reads[Item] = ((JsPath \ "id").read[Int] and
      (JsPath \ "time").read[Instant](Reads.DefaultInstantReads)
      ) (Item.apply _)

}

//case class Response(id: Long, friend_ids: Seq[Long])
//
//object Response {
//
//  val r: Reads[Response] = (
//    (JsPath \ "id").read[Long] and
//      (JsPath \ "friends").read[Seq[Long]](Reads.seq((JsPath \ "id").read[Long]))
//    )(Response.apply _)
//}
