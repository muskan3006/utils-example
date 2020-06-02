package com.knoldus.string

object StringUtils {
  val encoding = "UTF-8"
  val maxStringLength = 10

  def truncateToLength(str: String, enc: String = encoding, maxLength: Int = maxStringLength): String = {
    str.getBytes(enc).take(maxLength).map(_.toChar).mkString
  }

  implicit class Ops(val s: String) extends AnyVal {
    def truncateToLength(enc: String = encoding, maxLength: Int = maxStringLength): String = StringUtils.truncateToLength(s, enc, maxLength)
  }
}

