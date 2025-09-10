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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

class HipController @Inject()(
                             cc: ControllerComponents
                             ) extends BackendController(cc) with Logging {

  def submitTransfer: Action[AnyContent] = Action {
    _ =>
      Created(Json.obj("success" -> Json.obj(
        "processingDate" -> "2022-01-31T09:26:17Z",
        "formBundleNumber" -> "119000004320",
        "qtReference" -> "QT123456"
      )))
    }



  def getAll = ???

  def getSpecific = ???

}
