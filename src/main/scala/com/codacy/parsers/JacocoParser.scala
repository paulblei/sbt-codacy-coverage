package com.codacy.parsers

import java.io.File

import com.codacy.api.{CodacyCoverageFileReport, CodacyCoverageReport}

import scala.xml.Node

class JacocoParser(val coverageReport: File, val rootProject: File) extends CoverageParser {

  val rootProjectDir = rootProject.getAbsolutePath + File.separator

  override def isValidReport: Boolean = {
    (xml \\ "report").length > 0
  }

  override def generateReport(): CodacyCoverageReport = {
    val total = (xml \\ "report" \ "counter").collectFirst {
      case counter if (counter \ "@type").text == "LINE" =>
        val covered = (counter \ "@covered").text.toFloat
        val missed = (counter \ "@missed").text.toFloat
        ((covered / (covered + missed)) * 100).toInt
    }.getOrElse(0)

    val filesCoverage = (xml \\ "package").flatMap {
      `package` =>
        val packageName = (`package` \ "@name").text
        (`package` \\ "sourcefile").map {
          file =>
            val filename = (file \ "@name").text
            lineCoverage(packageName + File.separator + filename, file)
        }
    }

    CodacyCoverageReport(total, filesCoverage)
  }

  private def lineCoverage(sourceFilename: String, file: Node): CodacyCoverageFileReport = {
    val classHit = (file \\ "counter").collect {
      case counter if (counter \ "@type").text == "LINE" =>
        val covered = (counter \ "@covered").text.toFloat
        val missed = (counter \ "@missed").text.toFloat
        ((covered / (covered + missed)) * 100).toInt
    }

    val fileHit = classHit.sum / classHit.length

    val lineHitMap = (file \\ "line").map {
      line =>
        (line \ "@nr").text.toInt -> (line \ "@ci").text.toInt
    }.toMap.collect {
      case (key, value) if value > 0 =>
        key -> 1
    }

    CodacyCoverageFileReport(sourceFilename.stripPrefix(rootProjectDir), fileHit, lineHitMap)
  }

}
