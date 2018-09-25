/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.agentepayeregistration.jobs
import scala.concurrent.duration.{FiniteDuration, Duration => ScalaDuration}
import org.joda.time.{Duration => JodaDuration}
import play.api.Play
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob


trait JobConfig extends ServicesConfig with ExclusiveScheduledJob {

  def mode = Play.current.mode
  def runModeConfiguration = Play.current.configuration

  val name: String

  lazy val INITIAL_DELAY       = s"$name.schedule.initialDelay"
  lazy val INTERVAL            = s"$name.schedule.interval"
  lazy val LOCK_TIMEOUT        = s"$name.schedule.lockTimeout"

  lazy val initialDelay = {
    val dur = ScalaDuration.create(getConfString(INITIAL_DELAY,
      throw new RuntimeException(s"Could not find config $INITIAL_DELAY")))
    FiniteDuration(dur.length, dur.unit)
  }

    lazy val interval = {
      val dur = ScalaDuration.create(getConfString(INTERVAL,

        throw new RuntimeException(s"Could not find config $INTERVAL")))

    FiniteDuration(dur.length, dur.unit)
  }

      lazy val lockTimeout : JodaDuration = {
       val dur = ScalaDuration.create(getConfString(LOCK_TIMEOUT,
          throw new RuntimeException(s"Could not find config $LOCK_TIMEOUT")))
        JodaDuration.standardSeconds( FiniteDuration(dur.length, dur.unit).toSeconds )
     }
  }