package com.knoldus.time

import java.time._
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, ResolverStyle}
import java.time.temporal.ChronoField
import java.util.Locale

import com.knoldus.time.Enhancements._
import org.slf4j
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.{Failure, Try}

object ParsersAndFormatters {
  val tzIdUTC: ZoneId = ZoneId.of("UTC")
  private val log: slf4j.Logger = LoggerFactory.getLogger(getClass)

  def createDateTimeFormatter(pattern: String, tz: ZoneId = tzIdUTC, locale: Locale = Locale.US): DateTimeFormatter =
    new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendPattern(pattern)
      .toFormatter
      .withZone(tz)
      .withLocale(locale)
      .withResolverStyle(ResolverStyle.LENIENT) // STRICT or SMART resolver styles will not allow the hour 23 PM

  private val dateOnlyFmtr = createDateTimeFormatter("yyyy-MM-dd")
  private val dateOnlyFmtrWithDot = createDateTimeFormatter("yyyy.MM.dd")
  private val yearMonthFmtr = createDateTimeFormatter("yyyy-MM")
  private val yearWeekFmtr = createDateTimeFormatter("yyyy-'w'ww")
  private val humanReadableFmtr = createDateTimeFormatter("EEE MMM d, yyyy")
  private val humanReadableAtFmtr = createDateTimeFormatter("yyyy-MM-dd 'at' hh:mm a z")
  private val humanReadableFmtrWithSec = createDateTimeFormatter("yyyy-MM-dd hh:mm:ss a z")
  private val humanReadableFmtrWithTime = createDateTimeFormatter("EEE MMM d hh:mm:ss yyyy z")
  private val humanReadableFmtWithZone = createDateTimeFormatter("yyyy-MM-dd hh:mm:ss (zzz, Z) (zzzz)")

  /**
   * Format an instance as a full ISO 8601 date and time string.  Will always output to the resolution of seconds.
   * Fractional seconds are included if necessary and are presented as either 0, 3, 6, or 9 decimal digits.
   * e.g. 2017-04-23T19:35:42, 2017-04-23T19:35:42.001,
   * 2017-04-23T19:35:42.000001, 2017-04-23T19:35:42.000000001
   *
   * @param i the instance.
   * @return the formatted string
   */
  def formatAsIso(i: Instant): String = DateTimeFormatter.ISO_INSTANT.format(i)

  /**
   * Format an instance as ISO 8601 date and time string always omitting fractional seconds.
   *
   * @param i
   * @return
   */
  def formatAsIsoWithoutMillis(i: Instant): String = formatAsIso(i.truncatedToSeconds)

  /**
   * Format an instance as string the year and week of the instant.  e.g. `1985-w23`
   *
   * @param i
   * @return
   */
  def formatAsYearWeek(i: Instant): String = yearWeekFmtr.format(i)

  /**
   * Format an instance as a string representing on the date portion.  e.g. 2013-12-24
   *
   * @param i
   * @return
   */
  def formatAsIsoDateOnly(i: Instant): String = dateOnlyFmtr.format(i)

  /**
   * Format an instance as a string representing on the date portion with '.' separator.  e.g. 2013.12.24
   *
   * @param i
   * @return
   */
  def formatAsDateOnlyWithDot(i: Instant): String = dateOnlyFmtrWithDot.format(i)

  /**
   * Format an instances as a string reprenting the year and month portion.  e.g. 2013-12
   *
   * @param i
   * @return
   */
  def formatAsYearMonth(i: Instant): String = yearMonthFmtr.format(i)

  def formatAsHumanReadable(i: Instant): String = humanReadableFmtr.format(i)

  def formatAsHumanReadableAt(i: Instant): String = humanReadableAtFmtr.format(i)

  def formatAsHumanReadableSec(i: Instant): String = humanReadableFmtrWithSec.format(i)

  def formatAsHumanReadableWithTime(i: Instant): String = humanReadableFmtrWithTime.format(i)

  def formatAshumanReadableFmtWithZone(i: Instant): String = humanReadableFmtWithZone.format(i)

  private val isoParsers = Seq(
    DateTimeFormatter.ISO_INSTANT.withZone(tzIdUTC),
    DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(tzIdUTC)
  )

  /**
   * Attepmt to create an instance from a string represeing a time and date in ISO8601 format.
   * If the TZ is not present in the String the TZ is assumed to be UTC.
   *
   * @see parse(String, DateTimeFormatter)
   * @param iso8601 the string to parse
   * @return the instance as an option
   */
  def parse8601(iso8601: String): Option[Instant] = parseInstant(iso8601, isoParsers)

  /**
   * Attepmt to create an `Instant` from a string.  If the string cannot be parsed  `None` is returned.
   * Can parse strings representing a date and time, with or without seconds - seconds are assumed to be zero.
   * @param string      the string to parse
   * @param fmtr        the formatter to use for parsing
   * @return the instance as an `Option`
   */
  def parseInstant(string: String, fmtr: DateTimeFormatter): Option[Instant] =
    parseInstantTry(string, Seq(fmtr), defaultHour = None, defaultZone = tzIdUTC).toOption

  /**
   * Attempt to create an `Instant` from a string using a list of formatters.  The formatters
   * are tried in order until one works or all have failed.
   * Can parse strings representing a date and time, with or without seceonds; a date and time; or just a date.
   * If parsing a date without a time, the default hour must be specified, else `None` is returned.
   *
   * @param string      The string to parse
   * @param fmtrs       The formatters to try
   * @param defaultZone the default timezone
   * @param defaultHour the default hour used if parsing a date without time.
   * @return `Some[Instant]`, or `None` if none of the formatters are successful in parsing the string
   */
  def parseInstant(string: String, fmtrs: Seq[DateTimeFormatter], defaultZone: ZoneId = tzIdUTC, defaultHour: Option[Int] = None): Option[Instant] =
    parseInstantTry(string, fmtrs, defaultZone = defaultZone, defaultHour = defaultHour).toOption

  private def withZone(fmtr: DateTimeFormatter, tz: ZoneId) = {
    if (Option(fmtr.getZone) == Some(tz)) fmtr else fmtr.withZone(tz)
  }

  /**
   * Parse a `String` to and `Instant`.
   * Can parse strings representing a date and time, with or without seceonds; a date and time; or just a date.
   * If parsing a date without a time, the default hour must be specified, else `None` is returned.
   *
   * @param string      the string ot parse
   * @param fmtr        the formatter to use to parse the string
   * @param defaultZone the default timezone
   * @param defaultHour the default hour used if parsing a date without time.
   * @return
   */
  private def parseInstantUnsafe(string: String, fmtr: DateTimeFormatter, defaultZone: ZoneId, defaultHour: Option[Int]): Instant = {
    val ta = withZone(fmtr, defaultZone).parse(string)
    if (ta.isSupported(ChronoField.OFFSET_SECONDS)) {
      val od = OffsetDateTime.from(ta)
      Instant.from(od)
    } else if (ta.isSupported(ChronoField.HOUR_OF_DAY) || defaultHour.isEmpty) {
      Instant.from(ta)
    } else {
      val ld = LocalDate.from(ta)
      val lt = LocalTime.of(defaultHour.get, 0)
      Instant.from(LocalDateTime.of(ld, lt).atZone(defaultZone))
    }
  }

  private val fmtrsIsEmptyException = new IllegalArgumentException("fmtrs is an empty Seq")

  /**
   * Attempt to create an `Instant` from a string using a list of formatters.  The formatters
   * are tried in order until one works or all have failed.
   * Can parse strings representing a date and time, with or without seceonds; a date and time; or just a date.
   * If parsing a date without a time, the default hour must be specified, else a `Failure` is returned.
   * Minutes and seconds default to 00.000.
   *
   * @param string      The string to parse
   * @param fmtrs       The formatters to try
   * @param defaultZone the default timezone
   * @param defaultHour the default hour used if parsing a date without time.
   * @return `Success[Instant]`, or `Failure` if none of the formatters are successful in parsing the string
   */
  def parseInstantTry(string: String, fmtrs: Seq[DateTimeFormatter], defaultZone: ZoneId = tzIdUTC, defaultHour: Option[Int]): Try[Instant] = {
    @tailrec
    def loop(string: String, fmtrs: Seq[DateTimeFormatter], acc: Try[Instant]): Try[Instant] = {
      if (acc.isFailure && fmtrs.isEmpty) {
        acc // No date, No more formatters: None
      } else if (acc.isFailure) { // No date, one or more formatters: recurse
        loop(string, fmtrs.tail, Try {
          parseInstantUnsafe(string, fmtrs.head, defaultZone = defaultZone, defaultHour = defaultHour)
        })
      } else {
        acc // We have a winner
      }
    }

    loop(string, fmtrs, Failure(fmtrsIsEmptyException))
  }

  def parseLocalDateTime(string: String, fmtr: DateTimeFormatter, zoneId: ZoneId = tzIdUTC): Option[Instant] =
    parseInstant(string, Seq(fmtr), defaultZone = zoneId)

  def parseLocalDate(string: String, fmtr: DateTimeFormatter, tzId: ZoneId = tzIdUTC): Option[Instant] = Try {
    val parsed = withZone(fmtr, tzId).parse(string)
    LocalDate.from(parsed).atStartOfDay(tzId).toInstant
  }.toOption

  def parseYearAndMonth(`yyyy-MM`: String, tzId: ZoneId = tzIdUTC): Option[Instant] = Try {
    val yyMMRegex = raw"(\d{4})-(\d{2})".r
    val string = `yyyy-MM`
    val yearMonth = string match {
      case yyMMRegex(year, month) =>
        YearMonth.of(year.toInt, month.toInt)
      case _ => throw new IllegalArgumentException(s"$string does not regex $yyMMRegex")
    }
    yearMonth.atDay(1).atStartOfDay(tzId).toInstant
  }.toOption
}
