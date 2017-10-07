package utils

import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath

/**
  * Created by moglix on 4/6/17.
  */
object Errors {
  /**
    * Small utility to show the errors as a string
    */
  def show(errors: Seq[(JsPath, Seq[ValidationError])]): String = {
    errors.map {
      case (path, e) => path.toString() + " : " +
        e.map( valError => valError.message).mkString(" ")
    }.mkString("\n")
  }
}
