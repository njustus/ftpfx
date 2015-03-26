package ftp.util

import org.junit.Test
import org.junit.Assert._
import org.junit.runner.RunWith

class ConfigObjTest {
  @Test
  def test() = {
    assertEquals(Some("1.0"), ConfigObj.getC("version"))
    assertEquals(Some("this"), ConfigObj.getC("config-file"))
    assertEquals(Some("en"), ConfigObj.getC("language"))

    assertEquals(None, ConfigObj.getC("blup"))
    assertEquals(None, ConfigObj.getC("dat"))

    assertEquals(Some("Upload"), ConfigObj.getL("upload-btn"))
    assertEquals(Some("Log"), ConfigObj.getL("log-tab"))
    assertEquals(Some("Up-/Downloads"), ConfigObj.getL("loads-tab"))

    assertEquals(None, ConfigObj.getL("blup"))
    assertEquals(None, ConfigObj.getL("dat"))

    //stylesheet tests
    val styleRsc = ConfigObj.getCss()
    //1. test if != null
    assertNotEquals(null, styleRsc)

    //2. test if the relative path is correct
    val pathExp = ".*/style/FtpGui.css".r
    assertTrue(pathExp.findFirstIn(styleRsc).isDefined)
  }
}