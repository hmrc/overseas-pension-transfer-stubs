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

import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.Results.BadRequest
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CheckHeadersActionImpl])
trait CheckHeadersAction extends ActionBuilder[Request, AnyContent]

class CheckHeadersActionImpl @Inject()(
                                        val parser: BodyParsers.Default
                                      )(
                                      implicit val executionContext: ExecutionContext
) extends CheckHeadersAction {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    if (validateHeaders(request.headers)) block(request)
    else Future.successful(BadRequest("Error with headers"))


  private def validateHeaders(headers: Headers): Boolean = {
    val correlationId = headers.get("correlationId")
    val messageType = headers.get("X-Message-Type")
    val originatingSystem = headers.get("X-Originating-System")
    val receiptDate = headers.get("X-Receipt-Date")
    val regimeType = headers.get("X-Regime-Type")
    val transmittingSystem = headers.get("X-Transmitting-System")

    def evaluateHeader(header: Option[String], validate: String => Boolean): Boolean =
      header match {
        case Some(string) => validate(string)
        case None => false
      }

    correlationId.isDefined &&
      messageType.isDefined &&
      evaluateHeader(originatingSystem, (str: String) => str == "MDTP") &&
      receiptDate.isDefined &&
      evaluateHeader(regimeType, (str: String) => str == "PODS") &&
      evaluateHeader(transmittingSystem, (str: String) => str == "HIP")
  }


}
