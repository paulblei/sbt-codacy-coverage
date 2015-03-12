package com.codacy.parsers

import java.io.File

import com.codacy.api.{CodacyCoverageFileReport, CodacyCoverageReport}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class JacocoReaderTest extends WordSpec with BeforeAndAfterAll with Matchers {

  "JacocoReader" should {

    "identify if report is invalid" in {
      val reader = new JacocoParser(new File("src/test/resources/test_cobertura.xml"), new File(""))

      reader.isValidReport shouldBe false
    }

    "identify if report is valid" in {
      val reader = new JacocoParser(new File("src/test/resources/test_jacoco.xml"), new File(""))

      reader.isValidReport shouldBe true
    }

    "return a valid report" in {
      val reader = new JacocoParser(new File("src/test/resources/test_jacoco.xml"), new File(""))

      val testReport = CodacyCoverageReport(73, List(
        CodacyCoverageFileReport("org/eluder/coverage/sample/InnerClassCoverage.java", 81,
          Map(10 -> 1, 6 -> 1, 9 -> 1, 13 -> 1, 22 -> 1, 12 -> 1, 3 -> 1, 16 -> 1, 19 -> 1)),
        CodacyCoverageFileReport("org/eluder/coverage/sample/SimpleCoverage.java", 50,
          Map(3 -> 1, 6 -> 1))))

      reader.generateReport() shouldEqual testReport
    }

  }
}
