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

import shapeless.tag, tag.@@

package object data {

  type NonEmptyString = String @@ NonEmptyString.Tag
  object NonEmptyString extends ValidatedType[String]{
    def validateAndTransform(in: String): Option[String] =
      Some(in).filter(_.nonEmpty)
  }

  type UTR = String @@ UTR.Tag
  object UTR extends RegexValidatedString(
    "^[0-9]{10}$"
  )

  type SafeId = String @@ SafeId.Tag
  object SafeId extends RegexValidatedString(
    "^[A-Z0-9]{1,15}$"
  )

  type FormBundleNumber = String @@ FormBundleNumber.Tag
  object FormBundleNumber extends RegexValidatedString(
    regex = "^[0-9]{12}$"
  )

  type InternalId = String @@ InternalId.Tag
  object InternalId extends RegexValidatedString(
    regex = "^Int-[a-f0-9-]*$"
  )
  
  type Postcode = String @@ Postcode.Tag
  object Postcode extends RegexValidatedString(
    """^(GIR 0A{2})|((([A-Z][0-9]{1,2})|(([A-Z][A-HJ-Y][0-9]{1,2})|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z]))))[ ]?[0-9][A-Z]{2})$""",
    _.toUpperCase
  )

  type CountryCode = String @@ CountryCode.Tag
  object CountryCode extends RegexValidatedString(
    """^[A-Z][A-Z]$""", 
    _.toUpperCase match {
      case "UK" => "GB"
      case other => other
    }
  )

  type AccountNumber = String @@ AccountNumber.Tag
  object AccountNumber extends RegexValidatedString(
    """^[0-9]{8}$""",
    _.filter(_.isDigit)
  )

  implicit def valueToCell[A](in: A): Cell[A] = Cell(in)

}
