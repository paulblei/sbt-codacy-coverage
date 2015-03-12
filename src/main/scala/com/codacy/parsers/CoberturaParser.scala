package com.codacy.parsers

import java.io.File

import com.codacy.api.{CodacyCoverageFileReport, CodacyCoverageReport}

class CoberturaParser(val coverageReport: File, val rootProject: File) extends CoverageParser {

  val rootProjectDir = rootProject.getAbsolutePath + File.separator

  override def isValidReport: Boolean = {
    (xml \\ "coverage").length > 0
  }

  override def generateReport(): CodacyCoverageReport = {
    val total = (xml \\ "coverage" \ "@line-rate").headOption.map {
      total =>
        (total.text.toFloat * 100).toInt
    }.getOrElse(0)

    val files = (xml \\ "class" \\ "@filename").map(_.text).toSet

    val filesCoverage = files.map {
      file =>
        lineCoverage(file)
    }.toSeq

    CodacyCoverageReport(total, filesCoverage)
  }

  private def lineCoverage(sourceFilename: String): CodacyCoverageFileReport = {
    val file = (xml \\ "class").filter {
      n =>
        (n \\ "@filename").text == sourceFilename
    }

    val classHit = (file \\ "@line-rate").map {
      total =>
        (total.text.toFloat * 100).toInt
    }

    val fileHit = classHit.sum / classHit.length

    val lineHitMap = file.map {
      n =>
        (n \\ "line").map {
          line =>
            (line \ "@number").text.toInt -> (line \ "@hits").text.toInt
        }
    }.flatten.toMap.collect {
      case (key, value) if value > 0 =>
        key -> value
    }

    CodacyCoverageFileReport(sourceFilename.stripPrefix(rootProjectDir), fileHit, lineHitMap)
  }

}
