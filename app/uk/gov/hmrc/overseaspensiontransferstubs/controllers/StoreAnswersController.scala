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
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class StoreAnswersController @Inject() (
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  private var store: Map[String, JsObject] = Map.empty

  def getAnswers(id: String): Action[AnyContent] = Action.async {
    store.get(id) match {
      case Some(existing) =>
        Future.successful(Ok(existing))
      case None =>
        Future.successful(
          NotFound(Json.obj("error" -> s"No user answers found for id='$id'"))
        )
    }
  }

  private val baseQtNumber = "QT123456"

  private def getRandomQtNumber = s"QT${100000 + Random.nextInt(900000)}"

  def putAnswers(id: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      logger.info(request.body.toString())

      request.body.validate[JsObject] match {
        case JsSuccess(jsObj, _) =>
          val updatedWithQt = insertQtNumber(jsObj, baseQtNumber)
          store = store.updated(id, updatedWithQt)

          Future.successful(Ok(updatedWithQt))

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

  private def insertQtNumber(original: JsObject, qtNumber: String): JsObject = {
    val maybeData: JsObject =
      (original \ "data").asOpt[JsObject].getOrElse(Json.obj())
    if (maybeData.keys.contains("qtNumber")) {
      original
    } else {
      val dataWithQt = maybeData + ("qtNumber" -> JsString(qtNumber))
      original + ("data" -> dataWithQt)
    }
  }
}
