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

import com.google.inject.Inject
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.overseaspensiontransferstubs.controllers.actions.CheckHeadersAction
import uk.gov.hmrc.overseaspensiontransferstubs.helpers.TimeDateHelpers
import uk.gov.hmrc.overseaspensiontransferstubs.services.ResourceService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Instant
import scala.util.Random

class HipController @Inject() (
    cc: ControllerComponents,
    checkHeaders: CheckHeadersAction,
    resourceService: ResourceService
  ) extends BackendController(cc) with TimeDateHelpers with Logging {

  def submitTransfer: Action[JsValue] = checkHeaders(parse.json) {
    request =>
      val userId = (request.body \ "qtDeclaration" \ "submitterId").as[String]
      if (userId == "A2100060") {
        InternalServerError(Json.obj(
          "origin"   -> "HoD",
          "response" -> Json.obj(
            "error" -> Json.obj(
              "code"    -> "500",
              "logID"   -> "C0000AB8190C8E1F000000C700006836",
              "message" -> "Error&#x20;while&#x20;sending&#x20;message&#x20;to&#x20;module&#x20;processor&#x3a;&#x20;Sender&#x20;Channel&#x20;&#x27;CCOS_01_REST_Out_eSSTTP_PaymentLock_Set&#x27;&#x20;&#x28;ID&#x3a;&#x20;624ce8b4f8773eb0b227f1a90b6f40a5&#x29;&#x3a;&#x20;Catching&#x20;exception&#x20;calling&#x20;messaging&#x20;system&#x3a;&#x20;com.sap.aii.af.sdk.xi.srt.BubbleException&#x3a;&#x20;System&#x20;Error&#x20;Received.&#x20;HTTP&#x20;Status&#x20;Code&#x20;&#x3d;&#x20;200&#x3a;&#x20;However&#x20;System&#x20;Error&#x20;received&#x20;in&#x20;payload&#x20;ErrorCode&#x20;&#x3d;&#x20;INCORRECT_PAYLOAD_DATA&#x20;ErrorCategory&#x20;&#x3d;&#x20;XIServer&#x20;Parameter1&#x20;&#x3d;&#x20;&#x20;Parameter2&#x20;&#x3d;&#x20;&#x20;Parameter3&#x20;&#x3d;&#x20;&#x20;Parameter4&#x20;&#x3d;&#x20;&#x20;Additional&#x20;text&#x20;&#x3d;&#x20;&#x20;ErrorStack&#x20;&#x3d;&#x20;Error&#x20;while&#x20;processing&#x20;message&#x20;payload&#xa;&#xa;An&#x20;error&#x20;occurred&#x20;when&#x20;deserializing&#x20;in&#x20;the&#x20;simple&#x20;transformation&#x20;program&#x20;&#x2f;1SAI&#x2f;SAS476436EAEE64FB2B9096&#xa;Value&#x20;is&#x20;longer&#x20;than&#x20;the&#x20;maximum&#x20;permitted&#x20;length&#x20;15&#x3a;&#x20;&quot;864FZ077777770049&quot;&#xa;&#x20;&#x5b;http&#x3a;&#x2f;&#x2f;sap.com&#x2f;xi&#x2f;XI&#x2f;Message&#x2f;30&#x5e;Error&#x20;&quot;INCORRECT_PAYLOAD_DATA&quot;&#x5d;"
            )
          )
        ))
      } else {
        def getRandomFormBundle: String = Random.nextLong(999999999999L).toString
        def getRandomQtNumber: String   = s"QT${100000 + Random.nextInt(900000)}"

        Created(Json.obj("success" -> Json.obj(
          "processingDate"   -> Instant.now,
          "formBundleNumber" -> getRandomFormBundle,
          "qtReference"      -> getRandomQtNumber
        )))
      }
  }

  def getAll(dateFrom: String, dateTo: String, pstr: String, qtRef: Option[String] = None): Action[AnyContent] = checkHeaders {
    _ =>
      resourceService.getResource("getAll", pstr).fold(
        UnprocessableEntity(Json.obj("errors" -> Json.obj(
          "processingDate" -> "2025-10-17T15:24:15.128497Z", // Aware this isn't dynamic - date is irrelevant in service and simplifies unit test
          "code"           -> "183",
          "text"           -> "Not Found"
        )))
      )(json => {
        val status  = (json \ "status").as[Int]
        val payload = (json \ "payload").toOption.getOrElse(Json.obj())

        val subsWithRandomDates = setAllSubmissionDates(payload, pstr)
        status match {
          case 200 => Ok(subsWithRandomDates)
          case 422 => UnprocessableEntity(payload)
          case _   => InternalServerError
        }
      })
  }

  private def setAllSubmissionDates(payload: JsValue, pstr: String): JsValue = {
    val path = (__ \ "success" \ "qropsTransferOverview")

    (payload \ "success" \ "qropsTransferOverview").asOpt[JsArray] match {
      case Some(arr) =>
        val updatedArr = JsArray(
          arr.value.zipWithIndex.map {
            case (o: JsObject, idx) =>
              val seed = s"$pstr#$idx"
              // We could also add the dateFrom and dateTo into the generator
              val inst = generateBiasedSeededInstant(seed)
              o + ("submissionCompilationDate" -> JsString(inst.toString))
          }
        )
        payload.transform(path.json.put(updatedArr)).getOrElse(payload)
      case None      =>
        logger.info("Submission dates not set")
        payload
    }
  }

  def getSpecific(
      pstr: String,
      qtNumber: Option[String]      = None,
      versionNumber: Option[String] = None
    ): Action[AnyContent] = checkHeaders {
    _ =>
      resourceService.getResource("getSpecific", qtNumber.get).fold(
        NotFound("getSpecific resource not found")
      )(json =>
        Ok(json)
      )
  }
}
