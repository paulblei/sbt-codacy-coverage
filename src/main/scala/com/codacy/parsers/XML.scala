package com.codacy.parsers

import scala.xml._
import scala.xml.factory.XMLLoader

object XML extends XMLLoader[Elem] {
  override def parser: SAXParser = {
    val f = javax.xml.parsers.SAXParserFactory.newInstance()
    f.setNamespaceAware(false)
    f.setValidating(false)
    f.setFeature("http://xml.org/sax/features/namespaces", false)
    f.setFeature("http://xml.org/sax/features/validation", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    f.newSAXParser()
  }
}
