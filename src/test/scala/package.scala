package uk.gov.hmrc

import cats.Eq
import play.twirl.api.Html
import org.htmlcleaner.{HtmlCleaner, PrettyXmlSerializer}
import cats.instances.string._

package object uniformwidgets {

  lazy val cleaner = new HtmlCleaner
  lazy val props = {
    val r = cleaner.getProperties
    r.setOmitHtmlEnvelope(true)
    r.setOmitXmlDeclaration(true)
    r.setTrimAttributeValues(true)
    r
  }
  lazy val xmlSerializer = new PrettyXmlSerializer(props)

  def cleanHtml(html: Html): String =
    xmlSerializer.getAsString(cleaner.clean(html.toString))

  implicit lazy val htmlEq: Eq[Html] = {
    Eq.by[Html, String](cleanHtml)
  }

  implicit val htmlMonoid =
    cats.Monoid.instance[Html](
      Html(""),
      {(a,b) => Html(a.toString ++ b.toString)}
    )
}
