package com.knoldus.time

import java.time._
import java.time.temporal.ChronoUnit._
import java.time.temporal._

import com.knoldus.time.Time._

import scala.language.implicitConversions

object Enhancements {
  val tzIdUTC: ZoneId = ZoneId.of("UTC")
  final implicit class ImpInstWrapper(val i: Instant)
    extends AnyVal with Ordered[Instant] {
    def compare(that: Instant): Int = i.compareTo(that)

    def after(that: Instant): Boolean = this > that

    def before(that: Instant): Boolean = this < that

    def plusYears(years: Int): Instant = plusCalendarUnit(years.toLong, YEARS)

    def plusMonths(months: Int): Instant = plusCalendarUnit(months.toLong, MONTHS)

    private def plusCalendarUnit(amount: Long, unit: ChronoUnit) =
      LocalDateTime.ofInstant(i, tzIdUTC).plus(amount.toLong, unit).atZone(tzIdUTC).toInstant

    def plusWeeks(weeks: Int): Instant = i.plus(weeks.toLong * 7, DAYS)

    def plusDays(days: Int): Instant = i.plus(days.toLong, DAYS)

    def plusHours(hours: Int): Instant = i.plus(hours.toLong, HOURS)

    def plusMinutes(minutes: Int): Instant = i.plus(minutes.toLong, MINUTES)

    def minusYears(years: Int): Instant = plusYears(-years)

    def minusMonths(months: Int): Instant = plusMonths(-months)

    def minusWeeks(weeks: Int): Instant = plusWeeks(-weeks)

    def minusDays(days: Int): Instant = plusDays(-days)

    def minusHours(hours: Int): Instant = plusHours(-hours)

    def minusMinutes(minutes: Int): Instant = plusMinutes(-minutes)

    def +(d: Duration): Instant = i.plus(d)

    def -(d: Duration): Instant = i.minus(d)

    def -(other: Instant): Duration = Duration.between(other, i)

    def truncatedToMonths: Instant = {
      ZonedDateTime.ofInstant(i.truncatedTo(DAYS), tzIdUTC).`with`(TemporalAdjusters.firstDayOfMonth()).toInstant
    }

    def truncatedToDays: Instant = i.truncatedTo(DAYS)

    def truncatedToSeconds: Instant = i.truncatedTo(SECONDS)

    def truncatedToHours: Instant = i.truncatedTo(HOURS)

    def truncatedToMillis: Instant = i.truncatedTo(MILLIS)

    def closest(a: Instant, b: Instant): Instant = {
      val aDist = (a.getEpochSecond - i.getEpochSecond).abs
      val bDist = (b.getEpochSecond - i.getEpochSecond).abs

      if (aDist < bDist) {
        a
      } else if (bDist < aDist) {
        b
      } else {
        val anDist = (a.getNano - i.getNano).abs
        val bnDist = (b.getNano - i.getNano).abs
        if (anDist < bnDist) a else b
      }

    }

    def getHour(tzid: ZoneId = tzIdUTC): Int = localTime(i, tzid).getHour

    def timeUntil: Duration = Duration.between(Instant.now(), i)

  }

  implicit class DurationDSL(val i: Int) extends AnyVal {
    def MILLIS: Duration = Duration.ofMillis(i.toLong)
    def SECONDS: Duration = Duration.ofSeconds(i.toLong)
    def MINUTES: Duration = Duration.ofMinutes(i.toLong)
    def HOURS: Duration = Duration.ofHours(i.toLong)
    def DAYS: Duration = Duration.ofDays(i.toLong)

    def MILLIS_AGO: Instant = MILLIS.before(Instant.now)
    def MILLIS_FROM_NOW: Instant = MILLIS.after(Instant.now())
    def SECONDS_AGO: Instant = MINUTES.before(Instant.now())
    def SECONDS_FROM_NOW: Instant = MINUTES.after(Instant.now())
    def MINUTES_AGO: Instant = MINUTES.before(Instant.now())
    def MINUTES_FROM_NOW: Instant = MINUTES.after(Instant.now())
    def HOURS_AGO: Instant = HOURS.before(Instant.now())
    def HOURS_FROM_NOW: Instant = HOURS.after(Instant.now())
    def DAYS_AGO: Instant = DAYS.before(Instant.now())
    def DAYS_FROM_NOW: Instant = DAYS.after(Instant.now())
  }

  implicit class DurationEnhancer(val d: Duration) extends AnyVal with Ordered[Duration] {
    import DurationConstants._

    def toDecimalHours: Double = d.toMillis.toDouble / oneHourMillis
    def toIntHours: Long = d.toMillis / oneHourMillis

    def toDecimalSeconds: Double = d.toMillis.toDouble / oneSecondMillis

    def toDecimalDays: Double = d.toMillis.toDouble / oneDayMillis

    def +(i: Instant): Instant = i.plus(d)

    def before(i: Instant): Instant = i - d
    def after(i: Instant): Instant = i + d

    override def compare(that: Duration): Int = {
      val c = d.getSeconds.compareTo(that.getSeconds)
      if (c != 0) c else {
        d.getNano.compareTo(that.getNano)
      }
    }

  }

}

object DurationConstants {
  import Enhancements._

  val oneDayMillis: Long = 1.DAYS.toMillis
  val oneHourMillis: Long = 1.HOURS.toMillis
  val oneSecondMillis: Long = 1.SECONDS.toMillis
}
