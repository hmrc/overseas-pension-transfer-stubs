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

package uk.gov.hmrc.overseaspensiontransferstubs.controllers

import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.LocalDateTime

class HipControllerSpec extends AnyFreeSpec with Matchers {

  "submitTransfer" - {
    "return Created with Json body" in {
      val application: Application = GuiceApplicationBuilder().build()

      running(application) {
        val request: FakeRequest[JsObject] = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Originating-System" -> "MDTP",
            "X-Originating-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = route(application, request).value

        status(result) mustBe CREATED
      }
    }
  }
}
