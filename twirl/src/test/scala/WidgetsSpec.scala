/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.uniformwidgets

import scala.concurrent._
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
// import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import play.twirl.api.Html
import ltbs.uniform.{NonEmptyString => _, _}
import data._

object CachedBrowser {
  val browser = JsoupBrowser()

  def fetchPage(path: String) = {
    import java.io._
    {new File("/tmp/hmrc-uniform-frontend-widgets")}.mkdirs()
    val tempFile = new File("/tmp/hmrc-uniform-frontend-widgets/" + path.replace("/", "_"))
    if (tempFile.exists()) {
      browser.parseFile(tempFile)
    } else {
      println(s"Fetching $path")
      val r = browser.get(path)
      val bw = new BufferedWriter(new FileWriter(tempFile))
      bw.write(r.toHtml)
      bw.close()
      r
    }
  }
}

class WidgetSpec extends AnyFlatSpec with Matchers with cats.tests.StrictCatsEquality {

  implicit val ec = ExecutionContext.global

  val baseUrl = "https://design-system.service.gov.uk"
  val doc = CachedBrowser.fetchPage(baseUrl + "/components")

  def extractIframeUrls(page: String): List[String] = 
    (CachedBrowser.fetchPage(page) >> elementList("iframe")).map{_.attr("src")}.distinct

  lazy val examplePages: List[(String, List[String])] = 
    (doc >> elementList(".app-subnav__section li a"))
      .map { e =>
      e.text -> extractIframeUrls(baseUrl ++ e.attr("href")) 
    }

  val generated: Map[(String, String), Html] = Map(
    ("table", "default") -> html.table(
      "Dates and amounts",
      data.Table[String](
        List(
          List("Date", "Amount"),
          List("First 6 weeks", "£109.80 per week"),
          List("Next 33 weeks", "£109.80 per week"),
          List("Total estimated pay", "£4,282.20")
        )
      ).modify{
        case (x, y, c) if y == 0 || x == 0 => c.copy(heading = true)
      }.map{Html.apply}
    ),

    // ("table", "default") -> html.table(
    //   "Month you apply",
    //   data.Table[String](
    //     List(
    //       List("Date", "Rate for vehicles", "Rate for bicycles"),
    //       List("First 6 weeks", "£109.80 per week", "£59.10 per week"),
    //       List("Next 33 weeks", "£159.80 per week", "£89.10 per week"),
    //       List("Total estimated pay", "£4,282.20", "£2,182.20")
    //     )
    //   ).map{Html.apply}
    // ),

    ("text-input", "default") -> html.text_input(
      "event-name" :: Nil,
      "",
      ErrorTree.empty,
      UniformMessages.fromMap(
        Map("event-name.label" -> List("Event name"))
      ).map{Html.apply}
    ),
    ("text-input", "input-fixed-width") -> {
      implicit def forLength(length: Int): Html =
        html.text_input(
          s"width-$length" :: Nil,
          "",
          ErrorTree.empty,
          UniformMessages.fromMap(
            Map(s"width-$length.label" -> List(s"$length character width"))
          ).map{Html.apply},
          Some(Left(length))
        )
      import cats.implicits._
      List[Html](20,10,5,4,3,2).combineAll
    },
    ("text-input", "input-fluid-width") -> {
      implicit def forLength(length: String): Html =
        html.text_input(
          length :: Nil,
          "",
          ErrorTree.empty,
          UniformMessages.fromMap(
            Map(s"$length.label" -> List(s"${length.capitalize} width"))
          ).map{Html.apply},
          Some(Right(length))
        )
      import cats.implicits._
      List[Html](
        "full",
        "three-quarters",
        "two-thirds",
        "one-half",
        "one-third",
        "one-quarter"
      ).combineAll
    },
    ("text-input", "input-hint-text") -> html.text_input(
      "event-name" :: Nil,
      "",
      ErrorTree.empty,
      UniformMessages.fromMap(
        Map(
          "event-name.label" -> List("Event name"),
          "event-name.hint" -> List("The name you’ll use on promotional material.")
        )
      ).map{Html.apply}
    ),
    ("text-input", "error") -> html.text_input(
      "event-name" :: Nil,
      "",
      ErrorMsg("required").toTree,
      UniformMessages.fromMap(
        Map(
          "event-name.label" -> List("Event name"),
          "event-name.hint" -> List("The name you’ll use on promotional material."),
          "event-name.required" -> List("Enter an event name"),
          "error" -> List("Error"),
        )
      ).map{Html.apply}
    ),
    ("text-input", "number-input") -> html.text_input(
      "account-number" :: Nil,
      "",
      ErrorTree.empty,
      UniformMessages.fromMap(
        Map(
          "account-number.label" -> List("Account number"),
          "account-number.hint" -> List("Must be between 6 and 8 digits long")
        )
      ).map{Html.apply},
      width = Some(Left(10)),
      pattern = Some("[0-9]*"),
      spellcheck = false,
      inputMode = Some("numeric")
    )    
  )

  def doTest(example: String, exampleHtml: Html): Assertion = {
    val url = baseUrl ++ example
    cleanHtml(exampleHtml) should be (cleanHtml(
      Html((CachedBrowser.fetchPage(url) >> element("form")).innerHtml)
    ))
  }

  examplePages map { case (name, examples) =>
    examples.headOption.map { examplePath =>
      val (_::_::component::example::_) = examplePath.split("/").toList
      val message = example match {
        case "default" => "have a default example"
        case x => s"have an example for $x"
      }
      generated.get((component,example)) match {
        case Some(exampleHtml) =>
          name should message in {
            doTest(examplePath, exampleHtml)
          }
        case None =>
          name should message ignore { }
      }
    }

    examples.drop(1).map { examplePath =>
      val (_::_::component::example::_) = examplePath.split("/").toList
      val message = example match {
        case "default" => "have a default example"
        case x => s"have an example for $x"
      }      
      generated.get((component, example)) match {
        case Some(exampleHtml) =>
          it should message in {
            doTest(examplePath, exampleHtml)            
          }
        case None =>
          it should message ignore { }
      }
    }    
  }

  "Html" should "have an equivalence relation" in {
    Html("<h1>Foo</h1>") should === (Html("<h1 >\nFoo</h1>"))
    Html("<h1>Foo</h1>") should !== (Html("<h1>Bar</h1>"))
    Html("<h1>Foo</h1>") should !== (Html("""<h1 someattr="baz">Foo</h1>"""))
    Html("<br></br>") should === (Html("<br />"))    
  }
  
}
