package uk.gov.hmrc.overseaspensiontransferstubs.controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.Results.BadRequest
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CheckHeadersActionImpl14])
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
    val receiptData = headers.get("X-Originating-Date")
    val regimeType = headers.get("X-Regime-Type")
    val transmittingSystem = headers.get("X-Transmitting-System")

    def evaluateHeader(header: Option[String], validate: String => Boolean): Boolean =
      header match {
        case Some(string) => validate(string)
        case None => false
      }

    correlationId.isDefined &&
      evaluateHeader(messageType, (str: String) => str == "FileQROPSTransfer") &&
      evaluateHeader(originatingSystem, (str: String) => str.length > 1 && str.length < 31) &&
      receiptData.isDefined &&
      evaluateHeader(regimeType, (str: String) => str == "PODS") &&
      evaluateHeader(transmittingSystem, (str: String) => str == "PODS")
  }


}
