/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.overseaspensiontransferstubs.helpers

import java.time.{Duration, Instant, LocalDate, ZoneOffset}
import java.util.SplittableRandom

trait TimeDateHelpers {
  private val zone           = ZoneOffset.UTC
  private val now: LocalDate = LocalDate.now(zone)

  private def randomInstantBetween(rng: SplittableRandom, start: Instant, end: Instant): Instant = {
    val s = start.toEpochMilli
    val e = end.toEpochMilli
    Instant.ofEpochMilli(rng.nextLong(s, e))
  }

  private def seedFrom(s: String): Long = {
    s.##.toLong << 32
  }

  /** Generates a recency-biased submission timestamp.
    *
    * Splits the overall range into two half-open windows:
    *   - Older: [from, (now - recentBiasDays)]
    *   - Recent: [recentBiasDays, now] And returns a weighted amount of instants from each range.
    * @param seedStr
    *   String used to seed the RNG, this keep the timestamps deterministic between calls
    * @param from
    *   Inclusive lower-bound date. Default: now.minusYears(10).
    * @param to
    *   Exclusive upper-bound date. Default: today (UTC).
    * @param recentBiasDays
    *   Size of the “recent” window in days counted back from endExclusive. Default: 30.
    * @param recentWeight
    *   Probability in [0.0, 1.0] of sampling from the recent window (e.g., 0.3 = 30%). Default: 0.5.
    * @return
    *   An Instant drawn from either the recent window with probability `recentWeight`, or from the older window.
    */
  def generateBiasedSeededInstant(
      seedStr: String,
      from: LocalDate      = now.minusYears(10),
      to: LocalDate        = now,
      recentBiasDays: Long = 30L,
      recentWeight: Double = 0.5
    ): Instant = {

    val start           = from.atStartOfDay(zone).toInstant
    val rawEndExclusive = to.plusDays(1).atStartOfDay(zone).toInstant
    val endExclusive    = if (rawEndExclusive.isAfter(Instant.now())) Instant.now() else rawEndExclusive

    val recentWindowStart       = {
      val candidate = endExclusive.minus(Duration.ofDays(recentBiasDays))
      if (candidate.isAfter(start)) candidate else start
    }
    val olderWindowEndExclusive = recentWindowStart

    val rng  = new SplittableRandom(seedFrom(seedStr))
    val roll = rng.nextDouble()

    if (roll < recentWeight) {
      randomInstantBetween(rng, recentWindowStart, endExclusive)
    } else {
      randomInstantBetween(rng, start, olderWindowEndExclusive)
    }
  }
}
