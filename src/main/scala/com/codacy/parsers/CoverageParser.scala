package com.codacy.parsers

import java.io.File

import com.codacy.api.CodacyCoverageReport

import scala.util.Try
import scala.xml.Elem

trait CoverageParser {

  val coverageReport: File
  val xml: Elem = Try(XML.loadFile(coverageReport)).toOption.getOrElse(<root></root>)

  val rootProject: File

  def isValidReport: Boolean

  def generateReport(): CodacyCoverageReport

}

object CoverageParser {

  def generateReport(reportFiles: Seq[File], rootProject: File): Option[CodacyCoverageReport] = {
    val implementations = reportFiles.flatMap {
      reportFile =>
        Seq(
          new CoberturaParser(reportFile, rootProject),
          new JacocoParser(reportFile, rootProject)
        )
    }

    implementations.collectFirst {
      case implementation if implementation.isValidReport =>
        implementation.generateReport()
    }
  }

}
