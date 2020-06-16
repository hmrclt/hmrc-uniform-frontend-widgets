package uk.gov.hmrc.uniformwidgets

import cats.syntax.semigroup._
import ltbs.uniform._, interpreters.playframework._
import ltbs.uniform.common.web._
import play.api.mvc.{Results, Request, AnyContent}
import play.twirl.api.Html
import scala.concurrent.ExecutionContext

case class HmrcPlayInterpreter(
  results: Results,
  messagesApi: play.api.i18n.MessagesApi
)(implicit ec: ExecutionContext) extends PlayInterpreter[Html](results)
    with InferFormFieldProduct[Html]
    with InferFormFieldCoProduct[Html]
    with InferListingPages[Html]
    with Widgets {

  implicit val tellTwirlUnit = new WebTell[Unit] {
    def render(in: Unit, key: String, messages: UniformMessages[Html]): Html = blankTell
  }

  def blankTell: Html = Html("")

  def messages(
    request: Request[AnyContent]
  ): UniformMessages[Html] =
    { messagesApi.preferred(request).convertMessages() |+|
      UniformMessages.bestGuess }.map{Html(_)}

  def pageChrome(
    keyList: List[String],
    errors: ErrorTree,
    tell: Html,
    ask: Html,
    breadcrumbs: List[String],
    request: Request[AnyContent],
    messages: UniformMessages[Html],
    stats: FormFieldStats
  ): Html = {

    val content: Html = 
      html.form_wrapper(
        keyList,
        errors,
        Html(tell.toString + ask.toString),
        List(breadcrumbs.drop(1)),
        stats
      )(messages, request)

    val errorTitle: String = if(errors.isNonEmpty) s"${messages("common.error")}: " else ""
    html.main_template(title =
      errorTitle + s"${messages(keyList.mkString("-") + ".heading")} - ${messages("common.title")} - ${messages("common.title.suffix")}")(
      content)(request, messages)

  }

  def renderCoproduct[A](
    pageKey: List[String],
    fieldKey: List[String],
    path: Breadcrumbs,
    values: Input,
    errors: ErrorTree,
    messages: UniformMessages[Html],
    cfl: CoproductFieldList[A,Html]
  ): Html = {
    val value: Option[String] = values.valueAtRoot.flatMap {_.headOption}
    html.radios(
      fieldKey,
      cfl.inner.map{_._1}, //.orderYesNoRadio,
      value,
      errors,
      messages,
      cfl.inner.map{
        case(subkey,f) => subkey -> f(pageKey, fieldKey :+ subkey, path, {values / subkey}, errors / subkey, messages)
      }.filter(_._2.toString.trim.nonEmpty).toMap
    )
  }

// Members declared in InferFormFieldProduct
  def renderProduct[A](
    pageKey: List[String],
    fieldKey: List[String],
    path: Breadcrumbs,
    values: Input,
    errors: ErrorTree,
    messages: UniformMessages[Html],
    pfl: ProductFieldList[A,Html]
  ): Html = Html(
    pfl.inner.map { case (subFieldId, f) =>
      f(pageKey, fieldKey :+ subFieldId, path, values / subFieldId, errors / subFieldId, messages).toString
    }.mkString
  )

}


