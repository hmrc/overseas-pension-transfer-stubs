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

import play.api.Logging

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsObject, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreAnswersController @Inject() (
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with Logging {

  private var store: Map[String, JsObject] = Map.empty

  def getAnswers(id: String): Action[AnyContent] = Action.async {
    store.get(id) match {
      case Some(existing) =>
        Future.successful(Ok(Json.toJson(existing)))
      case None =>
        Future.successful(
          NotFound(Json.obj("error" -> s"No user answers found for id='$id'"))
        )
    }
  }

  def putAnswers(id: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      logger.info(request.body.toString())
      request.body.validate[JsObject] match {
        case JsSuccess(jsObj, _) =>
          store = store.updated(id, jsObj)
          Future.successful(Ok(jsObj))

        case JsError(errors) =>
          Future.successful(
            BadRequest(
              Json.obj(
                "error" -> "Invalid JSON",
                "details" -> errors.toString
              )
            )
          )
      }
  }
}
