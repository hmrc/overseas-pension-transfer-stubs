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

package uk.gov.hmrc.overseaspensiontransferstubs.controllers.actions


import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.Ok
import play.api.mvc.{BodyParsers, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, contentAsString, defaultAwaitTimeout, status}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class CheckHeadersActionSpec extends AnyFreeSpec with Matchers {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val application = new GuiceApplicationBuilder().build()

  private val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

  private val action = new CheckHeadersActionImpl(bodyParsers)

  "invokeBlock" - {
    "return Ok" - {
      "all headers pass validation" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Originating-System" -> "MDTP",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe OK
        contentAsString(result) mustBe "Success"
      }
    }

    "return BadRequest" - {
      "when correlationId is missing" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Originating-System" -> "MDTP",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Message-Type is missing" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Originating-System" -> "MDTP",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Originating-System is missing" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Originating-System length is 0" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "Not valid",
            "X-Originating-System" -> "",
            "X-Originating-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Originating-System length is > 30" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "Not valid",
            "X-Originating-System" -> "A" * 31,
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Receipt-Date is missing" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Originating-System" -> "MDTP",
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Regime-Type is missing" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Originating-System" -> "MDTP",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Regime-Type is invalid" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "Not valid",
            "X-Originating-System" -> "MDTP",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "MDTP",
            "X-Transmitting-System" -> "HIP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Transmitting-System is missing" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "FileQROPSTransfer",
            "X-Originating-System" -> "MDTP",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }

      "when X-Transmitting-System length is invalid" in {
        val request = FakeRequest(POST, "/RESTAdapter/pods/reports/qrops-transfer")
          .withHeaders(
            "correlationId" -> "correlationId",
            "X-Message-Type" -> "Not valid",
            "X-Originating-System" -> "",
            "X-Receipt-Date" -> LocalDateTime.now.toString,
            "X-Regime-Type" -> "PODS",
            "X-Transmitting-System" -> "MDTP"
          ).withBody(Json.obj("key" -> "value"))

        val result = action.invokeBlock(
          request,
          { request: Request[JsObject] =>
            Future.successful(Ok("Success"))
          }
        )

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe "Error with headers"
      }
    }
  }
}
