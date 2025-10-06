package uk.gov.hmrc.overseaspensiontransferstubs.helpers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.{Duration, LocalDate, ZoneOffset}

final class TimeDateHelpersSpec extends AnyFreeSpec with Matchers with TimeDateHelpers {

  private val zone = ZoneOffset.UTC

  "generateBiasedSeededInstant" - {

    "must return the same instant for the same seed and bounds" in {
      val from = LocalDate.of(2023, 1, 1)
      val to   = LocalDate.of(2023, 12, 31)

      val a = generateBiasedSeededInstant(
        seedStr = "PSTR42",
        from    = from,
        to      = to
      )
      val b = generateBiasedSeededInstant(
        seedStr = "PSTR42",
        from    = from,
        to      = to
      )
      b mustBe a
    }

    "must produce different instants for different seeds" in {
      val from = LocalDate.of(2023, 1, 1)
      val to   = LocalDate.of(2023, 12, 31)

      val a = generateBiasedSeededInstant("pstr1", from, to)
      val b = generateBiasedSeededInstant("pstr2", from, to)

      b must not be a
    }

    "must lie within [from@00:00Z, to+1@00:00Z) when 'to' is in the past" in {
      val from = LocalDate.of(2024, 1, 1)
      val to   = LocalDate.of(2024, 1, 31)

      val start        = from.atStartOfDay(zone).toInstant
      val endExclusive = to.plusDays(1).atStartOfDay(zone).toInstant

      val x = generateBiasedSeededInstant("seed-123", from, to, recentBiasDays = 7L)

      x.isBefore(endExclusive) mustBe true
      x.isBefore(start) mustBe false
    }


    "must return only instants from the recent window when recentWeight = 1.0" in {
      val from = LocalDate.of(2024, 3, 1)
      val to   = LocalDate.of(2024, 4, 30)

      val start        = from.atStartOfDay(zone).toInstant
      val endExclusive = to.plusDays(1).atStartOfDay(zone).toInstant
      val recentDays   = 30L

      val candidate         = endExclusive.minus(Duration.ofDays(recentDays))
      val recentWindowStart = if (candidate.isAfter(start)) candidate else start

      (0 until 200).foreach { i =>
        val x = generateBiasedSeededInstant(
          seedStr        = s"pstr#row#$i",
          from           = from,
          to             = to,
          recentBiasDays = recentDays,
          recentWeight   = 1.0
        )
        withClue(s"seed index $i -> $x not in recent window") {
          !x.isBefore(recentWindowStart) && x.isBefore(endExclusive) mustBe true
        }
      }
    }

    "must return only instants from the older window when recentWeight = 0.0" in {
      val from = LocalDate.of(2024, 3, 1)
      val to   = LocalDate.of(2024, 4, 30)

      val start        = from.atStartOfDay(zone).toInstant
      val endExclusive = to.plusDays(1).atStartOfDay(zone).toInstant
      val recentDays   = 30L

      val candidate         = endExclusive.minus(Duration.ofDays(recentDays))
      val recentWindowStart = if (candidate.isAfter(start)) candidate else start

      (0 until 200).foreach { i =>
        val x = generateBiasedSeededInstant(
          seedStr        = s"pstr#row#$i",
          from           = from,
          to             = to,
          recentBiasDays = recentDays,
          recentWeight   = 0.0
        )
        withClue(s"seed index $i -> $x not in recent window") {
          !x.isBefore(start) && x.isBefore(recentWindowStart) mustBe true
        }
      }
    }
  }
}
