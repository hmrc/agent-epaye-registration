/*
 * Copyright 2017 HM Revenue & Customs
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

import java.io.File

import com.typesafe.config.ConfigFactory
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.test.UnitSpec

class ServicesConfigTest extends UnitSpec {

  "baseUrl" should {
    "return url formed from host and port and protocol properties" in {
      val servicesConfig = TestServicesConfig(
        """
          |microservice {
          |  services {
          |    auth {
          |      host = foo
          |      port = 9999
          |      protocol = bar
          |    }
          |  }
          |}
        """.stripMargin, Mode.Test)
      servicesConfig.baseUrl("auth").toString shouldBe "bar://foo:9999"
    }

    "throw exception if port is missing" in {
      val servicesConfig = TestServicesConfig(
        """
          |microservice {
          |  services {
          |    auth {
          |      host = foo
          |    }
          |  }
          |}
        """.stripMargin, Mode.Test)

      val exception = intercept[RuntimeException] {
        servicesConfig.baseUrl("auth")
      }

      exception.getMessage shouldBe "Could not find config auth.port"
    }

    "throw exception if host is missing" in {
      val servicesConfig = TestServicesConfig(
        """
          |microservice {
          |  services {
          |    auth {
          |      port = 9999
          |    }
          |  }
          |}
        """.stripMargin, Mode.Test)

      val exception = intercept[RuntimeException] {
        servicesConfig.baseUrl("auth")
      }

      exception.getMessage shouldBe "Could not find config auth.host"
    }
  }

  "env" should {
    "be Test if Mode==Test" in {
      val servicesConfig = TestServicesConfig("", Mode.Test)
      servicesConfig.env shouldBe "Test"
    }
    "be read from run.mode property if Mode!=Test" in {
      val servicesConfig = TestServicesConfig("""run.mode=FOO""", Mode.Prod)
      servicesConfig.env shouldBe "FOO"
    }
    "be default Dev" in {
      val servicesConfig = TestServicesConfig("", Mode.Prod)
      servicesConfig.env shouldBe "Dev"
    }
  }

  "getConfString" should {
    "return key under microservice.services" in {
      val servicesConfig = TestServicesConfig(
        """
          |Test {
          |  microservice {
          |   services {
          |     foo {
          |       bar = 1111
          |     }
          |   }
          |  }
          |}
          |
          |microservice {
          |  services {
          |    foo {
          |      bar = 9999
          |    }
          |  }
          |}
          |
          |Prod {
          |  microservice {
          |   services {
          |     foo {
          |       bar = 9922299
          |     }
          |   }
          |  }
          |}
          |
          |Dev {
          |  foo {
          |     bar = 33
          |  }
          |}
        """.stripMargin, Mode.Test)
      servicesConfig.getConfString("foo.bar","0") shouldBe "9999"
    }

    "return key under Prod.microservice.services" in {
      val servicesConfig = TestServicesConfig(
        """
          |run.mode = Prod
          |
          |Test {
          |  microservice {
          |   services {
          |     foo {
          |       bar = 1111
          |     }
          |   }
          |  }
          |}
          |
          |Prod {
          |  microservice {
          |   services {
          |     foo {
          |       bar = 9999
          |     }
          |   }
          |  }
          |}
          |
          |Dev {
          |  foo {
          |     bar = 33
          |  }
          |}
        """.stripMargin, Mode.Prod)
      servicesConfig.getConfString("foo.bar","0") shouldBe "9999"
    }

    "return key under foo.bar" in {
      val servicesConfig = TestServicesConfig(
        """
          |Test {
          |  foo {
          |     bar = 1111
          |  }
          |}
          |foo {
          |   bar = 9999
          |}
        """.stripMargin, Mode.Test)
      servicesConfig.getConfString("foo.bar","0") shouldBe "9999"
    }

    "return key under Test.foo.bar" in {
      val servicesConfig = TestServicesConfig(
        """
          |Dev {
          |  foo {
          |     bar = 9999
          |  }
          |}
        """.stripMargin, Mode.Prod)
      servicesConfig.getConfString("foo.bar","0") shouldBe "9999"
    }

    "return key under govuk-tax.Prod.services" in {
      val servicesConfig = TestServicesConfig(
        """
          |run.mode = Prod
          |govuk-tax.Prod.services {
          |  foo {
          |     bar = 9999
          |  }
          |}
        """.stripMargin, Mode.Prod)
      servicesConfig.getConfString("foo.bar","0") shouldBe "9999"
    }
  }
}

object TestServicesConfig {
  def apply(config: String, mode: Mode.Mode): ServicesConfig = new ServicesConfig {
    override def configuration: Configuration = Configuration.apply(ConfigFactory.parseString(config))
    override def environment: Environment = new Environment(new File(""), this.getClass.getClassLoader, mode)
  }
}
