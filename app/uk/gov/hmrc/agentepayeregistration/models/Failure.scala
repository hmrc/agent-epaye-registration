package uk.gov.hmrc.agentepayeregistration.models

import play.api.libs.json.Json

case class ValidationError(code: String, error: String)

object ValidationError {
  implicit val validationErrorFormat = Json.format[ValidationError]
}

case class Failure(errors: Set[ValidationError])

object Failure {
  implicit val failureFormat = Json.format[Failure]

  def apply(code: String, message: String): Failure = Failure(Set(ValidationError(code, message)))
}