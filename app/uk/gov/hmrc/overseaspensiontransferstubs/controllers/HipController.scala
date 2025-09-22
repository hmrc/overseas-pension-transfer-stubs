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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.overseaspensiontransferstubs.controllers.actions.CheckHeadersAction
import uk.gov.hmrc.overseaspensiontransferstubs.services.ResourceService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Instant
import scala.util.Random

class HipController @Inject()(
                             cc: ControllerComponents,
                             checkHeaders: CheckHeadersAction,
                             resourceService: ResourceService
                             ) extends BackendController(cc) with Logging {

  def submitTransfer: Action[JsValue] = checkHeaders(parse.json) {
    _ =>
      def getRandomFormBundle: String = Random.nextLong(999999999999L).toString
      def getRandomQtNumber: String = s"QT${100000 + Random.nextInt(900000)}"

      Created(Json.obj("success" -> Json.obj(
        "processingDate" -> Instant.now,
        "formBundleNumber" -> getRandomFormBundle,
        "qtReference" -> getRandomQtNumber
      )))
    }



  def getAll(dateFrom: String, dateTo: String, pstr: String, qtRef: Option[String] = None): Action[AnyContent] = checkHeaders {
    _ =>
      resourceService.getResource("getAll", pstr).fold(
        NotFound("getAll resource not found")
      )(
        json =>
          Ok(json)
      )
  }

  def getSpecific(
                   pstr: String,
                   qtNumber: Option[String] = None,
                   versionNumber: Option[String] = None
                 ): Action[AnyContent] = checkHeaders {
    _ =>
      resourceService.getResource("getSpecific", qtNumber.get).fold(
        NotFound("getSpecific resource not found")
      )(
        json =>
          Ok(json)
      )
  }

}
