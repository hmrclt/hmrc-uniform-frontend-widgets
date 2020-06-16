package controllers

import cats.implicits._
import javax.inject._
import ltbs.uniform._, interpreters.playframework._, common.web.FutureAdapter
import beardtax._
import play.api.i18n.{Messages => _, _}
import play.api.mvc._
import scala.concurrent._
import play.twirl.api.Html

@Singleton
class BeardController @Inject()(
  implicit ec:ExecutionContext,
  val controllerComponents: ControllerComponents
) extends BaseController with ControllerHelpers with I18nSupport {

  def hod: Hod = DummyHod

  implicit val persistence: PersistenceEngine[Request[AnyContent]] =
    DebugPersistence(UnsafePersistence())

  val interpreter = uk.gov.hmrc.uniformwidgets.HmrcPlayInterpreter(this, messagesApi)

  def beardAction(targetId: String) = Action.async { implicit request: Request[AnyContent] =>

    import interpreter._

    val playProgram = beardProgram[interpreter.WM](
      create[TellTypes, AskTypes](interpreter.messages(request)),
      (style: BeardStyle, length: BeardLength) =>
        FutureAdapter[Html].alwaysRerun(hod.costOfBeard(style, length))
    )

    playProgram.run(targetId, purgeStateUponCompletion = true) {
      i: Int => Ok(s"$i").pure[Future]
    }
  }
}
