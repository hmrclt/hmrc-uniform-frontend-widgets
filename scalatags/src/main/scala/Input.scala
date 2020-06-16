import ltbs.uniform._
import scalatags.Text.all._

object Input {

  def text_input(
    keys: List[String],
    value: String,
    errors: ErrorTree,
    messages: UniformMessages[Tag],
    width: Option[Either[Int,String]] = None,
    autoComplete: Option[String] = None,
    spellCheck: Boolean = true,
    inputMode: Option[String] = None,
    patternV: Option[String] = None
  ): Tag = {
    val classes = List(
      Some("govuk-input"),
      width.map{_.fold("govuk-input--width-" + _, "govuk-!-width-" + _)},
      Some("govuk-input--error").filter(_ => errors.definedAtRoot)
    ).flatten.mkString(" ")

    val ariaValue: Option[String] = Some(List(
      Some(keys.mkString(".") + "-hint").filter(_ => messages.get({keys :+ "hint"}.mkString(".")).nonEmpty),
      Some(keys.mkString(".") + "-error").filter(_ => errors.definedAtRoot)
    ).flatten).filter(_.nonEmpty).map(_.mkString(" "))

    div(cls := s"govuk-form-group${if(errors.definedAtRoot)(" govuk-form-group--error")}") (
      label(cls := "govuk-label", `for` := keys.mkString("_")) (
        messages.decompose({keys :+ "label"}.mkString("."))
      ),
      input(
        (List(
          cls := classes,
	  id := keys.mkString("_"),
	  name := keys.mkString("."),
	  `type` :="text"
        ) ++ List(
          Some(attr("value") := value).filter(_ => value.nonEmpty),
	 ariaValue.filter(_.nonEmpty).map(aria.describedby := _),
	 autoComplete.map { ac => autocomplete := ac }, 
         patternV.map{ i => pattern := i },
          inputMode.map{ i => attr("inputmode") := i },
          Some( spellcheck := "false" ).filterNot(_ => spellCheck)
        ).flatten) :_*
      )
    )
  }
}
