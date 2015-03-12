package com.codacy.io

import java.io.{File, PrintWriter}

import scala.io.Source
import scala.util.Try

object FileUtils {

  def read(filePath: String): Option[String] = {
    Try {
      val source = Source.fromFile(filePath)
      val repoToken = source.mkString.trim
      source.close()
      repoToken
    }.toOption
  }

  def write(file: File, content: String): Unit = {
    Try {
      if (!file.exists()) {
        file.getParentFile.mkdirs()
      }

      val printer = new PrintWriter(file)
      printer.println(content)
      printer.close()
    }.toOption
  }

}