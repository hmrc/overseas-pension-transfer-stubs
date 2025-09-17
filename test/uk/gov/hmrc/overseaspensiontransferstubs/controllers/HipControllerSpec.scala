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

import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.Mockito.when
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.overseaspensiontransferstubs.services.ResourceService

import java.time.Instant

class HipControllerSpec extends AnyFreeSpec with Matchers {

  private val mockResourceService: ResourceService = mock[ResourceService]

  "submitTransfer" - {
    "return Created with Json body" in {
      val application: Application = GuiceApplicationBuilder().build()

      running(application) {
        val request: FakeRequest[JsObject] = FakeRequest(POST, "/etmp/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Originating-System" -> "MDTP",
            "X-Receipt-Date" -> Instant.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = route(application, request).value

        status(result) mustBe CREATED
      }
    }
  }

  "getAll" - {
    "return 200 with Json body when resourceService returns Some(json)" in {
      val application: Application = GuiceApplicationBuilder().overrides(
        bind[ResourceService].toInstance(mockResourceService)
      ).build()

      when(mockResourceService.getResource(meq("getAll"), meq("12345678AB")))
        .thenReturn(Some(JsString("Success")))

      running(application) {
        val request =
          FakeRequest(GET, "/etmp/RESTAdapter/pods/reports/qrops-transfer-overview?fromDate=2025-01-01&toDate=2025-01-02&pstr=12345678AB")
            .withHeaders(
              "correlationId" -> "correlationId",
              "X-Message-Type" -> "FileQROPSTransfer",
              "X-Originating-System" -> "MDTP",
              "X-Receipt-Date" -> Instant.now.toString,
              "X-Regime-Type" -> "PODS",
              "X-Transmitting-System" -> "HIP"
            )

        val result = route(application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe JsString("Success")
      }
    }

    "return 404 with String body when resourceService returns None" in {
      val application: Application = GuiceApplicationBuilder().overrides(
        bind[ResourceService].toInstance(mockResourceService)
      ).build()

      when(mockResourceService.getResource(meq("getAll"), meq("12345678AB")))
        .thenReturn(None)

      running(application) {
        val request =
          FakeRequest(GET, "/etmp/RESTAdapter/pods/reports/qrops-transfer-overview?fromDate=2025-01-01&toDate=2025-01-02&pstr=12345678AB")
            .withHeaders(
              "correlationId" -> "correlationId",
              "X-Message-Type" -> "FileQROPSTransfer",
              "X-Originating-System" -> "MDTP",
              "X-Receipt-Date" -> Instant.now.toString,
              "X-Regime-Type" -> "PODS",
              "X-Transmitting-System" -> "HIP"
            )

        val result = route(application, request).value

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe "getAll resource not found"
      }
    }
  }

  "getSpecific" - {
    "return 200 with Json body when resourceService returns Some(json)" in {
      val application: Application = GuiceApplicationBuilder().overrides(
        bind[ResourceService].toInstance(mockResourceService)
      ).build()

      when(mockResourceService.getResource(meq("getSpecific"), meq("QT564321")))
        .thenReturn(Some(JsString("Success")))

      running(application) {
        val request =
          FakeRequest(GET, "/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=12345678AB&qtNumber=QT564321&versionNumber=001")
            .withHeaders(
              "correlationId" -> "correlationId",
              "X-Message-Type" -> "FileQROPSTransfer",
              "X-Originating-System" -> "MDTP",
              "X-Receipt-Date" -> Instant.now.toString,
              "X-Regime-Type" -> "PODS",
              "X-Transmitting-System" -> "HIP"
            )

        val result = route(application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe JsString("Success")
      }
    }

    "return 404 with String body when resourceService returns None" in {
      val application: Application = GuiceApplicationBuilder().overrides(
        bind[ResourceService].toInstance(mockResourceService)
      ).build()

      when(mockResourceService.getResource(meq("getSpecific"), meq("QT564321")))
        .thenReturn(None)

      running(application) {
        val request =
          FakeRequest(GET, "/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=12345678AB&qtNumber=QT564321&versionNumber=001")
            .withHeaders(
              "correlationId" -> "correlationId",
              "X-Message-Type" -> "FileQROPSTransfer",
              "X-Originating-System" -> "MDTP",
              "X-Receipt-Date" -> Instant.now.toString,
              "X-Regime-Type" -> "PODS",
              "X-Transmitting-System" -> "HIP"
            )

        val result = route(application, request).value

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe "getSpecific resource not found"
      }
    }
  }
}
