package uk.gov.hmrc.agentepayeregistration.models

import scala.util.Try

case class AgentReference(value: String) {
  validate(value)

  def newReference: AgentReference = {
    val (prefix, code) = value.splitAt(2)

    if (code.toInt == 9999) {
      AgentReference(s"${nextAlphaCode(prefix)}0001")
    } else {
      AgentReference(s"$prefix${"%04d".format(code.toInt + 1)}")
    }
  }

  private def validate(agentRef: String): Unit = {
    require(agentRef.length == 6, "agent PAYE reference has an incorrect length")

    val (prefix, code) = value.splitAt(2)

    validateAlphaCode(prefix)
    validateNumericCode(code)
  }

  private def validateAlphaCode(alphaCode: String): Unit = {
    require(alphaCode.length == 2 &&
      alphaCode.charAt(0) >= 'H' &&
      alphaCode.charAt(0) <= 'Z' &&
      alphaCode.charAt(1) >= 'A' &&
      alphaCode.charAt(1) <= 'Z',
      "agent PAYE reference has an invalid alpha code portion")
  }

  private def validateNumericCode(numericCode: String): Unit = {
    require(numericCode.length == 4, "agent PAYE reference does not have a 4 digit numeric portion")
    require(Try(numericCode.toInt).isSuccess, "agent PAYE reference has an invalid numeric portion")
  }

  private def nextAlphaCode(code: String) = {
    validateAlphaCode(code)

    val c1 = code.charAt(0).toUpper
    val c2 = code.charAt(1).toUpper

    if (c2 == 'Z')
      s"${(c1 + 1).toChar}A"
    else
      s"$c1${(c2.toUpper + 1).toChar}"
  }
}