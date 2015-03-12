package com.codacy.parsers

import java.io.File

import com.codacy.api.{CodacyCoverageFileReport, CodacyCoverageReport}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class CoberturaReaderTest extends WordSpec with BeforeAndAfterAll with Matchers {

  "CoberturaReader" should {

    "identify if report is invalid" in {
      val reader = new CoberturaParser(new File("src/test/resources/test_jacoco.xml"), new File(""))

      reader.isValidReport shouldBe false
    }

    "identify if report is valid" in {
      val reader = new CoberturaParser(new File("src/test/resources/test_cobertura.xml"), new File(""))

      reader.isValidReport shouldBe true
    }

    "return a valid report" in {
      val reader = new CoberturaParser(new File("src/test/resources/test_cobertura.xml"), new File(""))

      val testReport = CodacyCoverageReport(87, List(
        CodacyCoverageFileReport("src/test/resources/TestSourceFile.scala", 87,
          Map(5 -> 1, 10 -> 1, 6 -> 2, 9 -> 1, 4 -> 1)),
        CodacyCoverageFileReport("src/test/resources/TestSourceFile2.scala", 87,
          Map(1 -> 1, 2 -> 1, 3 -> 1))))

      reader.generateReport() shouldEqual testReport
    }

  }
}
