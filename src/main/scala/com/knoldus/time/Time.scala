package com.knoldus.time

import java.time.temporal.ChronoField.{MONTH_OF_YEAR, YEAR}
import java.time.temporal.TemporalAdjusters
import java.time._

object Time {
  val tzIdUTC: ZoneId = ZoneId.of("UTC")
  def isExpired(date: Instant, now: Instant): Boolean = now isAfter  date

  def isExpired(date: Instant): Boolean = isExpired(date, Instant.now())

  def equalYearMonth(a: Instant, b: Instant, zoneId: ZoneId = tzIdUTC): Boolean = {
    val ao = a.atZone(zoneId)
    val bo = b.atZone(zoneId)
    ao.get(MONTH_OF_YEAR) == bo.get(MONTH_OF_YEAR) && ao.get(YEAR) == bo.get(YEAR)
  }

  def instantOfDateUTC(year: Int, month: Month, day: Int): Instant =
    instantOfDateAndTimeUTC(year, month, day, 0, 0, 0)

  def instantOfDateAndTimeUTC(year: Int, month: Month, day: Int, hour: Int, minute: Int, second: Int, nanoOfSecond: Int = 0): Instant =
    instantOfDateAndTime(year, month, day, hour, minute, second, nanoOfSecond, tzIdUTC)

  def instantOfDate(year: Int, month: Month, day: Int, tzid: ZoneId = tzIdUTC): Instant =

    instantOfDateAndTime(year, month, day, 0, 0, 0, tzid = tzid)

  def instantOfDateAndTime(year: Int, month: Month, day: Int, hour: Int, minute: Int, second: Int,
                           nanoOfSecond: Int = 0, tzid: ZoneId): Instant =
    LocalDateTime.of(year, month, day, hour, minute, second, nanoOfSecond).atZone(tzid).toInstant

  def timeSince(from: Instant): Duration = Duration.between(from, Instant.now())

  def localTime(from: Instant, tzid: ZoneId = tzIdUTC): LocalTime = LocalTime.from(from.atZone(tzid))

  def localDate(from: Instant, tzid: ZoneId = tzIdUTC): LocalDate = LocalDate.from(from.atZone(tzid))

  /**
   * Get the middle of the month in which the instant falls.
   * @param i the instance
   * @return the instance of the start of the day of the 15th day of the month in which `i` falls
   */
  def middleOfMonth(i: Instant): Instant = LocalDateTime.ofInstant(i, tzIdUTC).toLocalDate.withDayOfMonth(15)
    .atStartOfDay(tzIdUTC).toInstant

  def lastOfMonth(i: Instant): Instant = LocalDateTime.ofInstant(i, tzIdUTC).toLocalDate.`with`(TemporalAdjusters.lastDayOfMonth()).
    atStartOfDay(tzIdUTC).toInstant

  def isLastOfMonth(i: Instant): Boolean = lastOfMonth(i) == i

  def isLastOfMonth: Boolean = isLastOfMonth(Instant.now())

  /**
   * Return the earlier of `a` and `b`, or `a` if `a.compareTo(b) == 0`
   */
  def earlierOf(a: Instant, b: Instant): Instant = if (a isBefore  b) a else b

  /** Return the later of `a` and `b`, or `b` if `a.compareTo(b) == 0` */
  def laterOf(a: Instant, b: Instant): Instant = if(a isAfter b) a else b

  /** Return fractional seconds as a integral number of nano-seconds */
  def fracsToNanos(frac: Double): Int = (frac * Math.pow(10, 9)).toInt
}
