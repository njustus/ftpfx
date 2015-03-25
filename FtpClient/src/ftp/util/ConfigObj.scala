package ftp.util

import java.nio.file.Paths
import java.nio.file.Files

object ConfigObj {
  private val defaultDescription = "ftpfx's default configuration file."
  private val configPath = Paths.get(getClass.getResource("rsc/conf/ftpfxDefault.conf").toURI())
  private val langPath = Paths.get(getClass.getResource("rsc/lang/ftpfx-en.conf").toURI())
  private val config: java.util.Properties = loadConfig()
  //TODO needs to be defined
  private val language: java.util.Properties = loadLanguage()

  private def loadConfig(): java.util.Properties = {
    val conf: java.util.Properties = new java.util.Properties()
    if (!Files.exists(configPath)) {
      defaultConfKeys.foreach { case (key, value) => conf.setProperty(key, value) }
      conf.store(Files.newOutputStream(configPath), defaultDescription)
    } else conf.load(Files.newInputStream(configPath))
    return conf
  }

  private def loadLanguage(): java.util.Properties = {
    val prop: java.util.Properties = new java.util.Properties()
    if (!Files.exists(langPath)) {
      defaultLangKeys.foreach { case (key, value) => prop.setProperty(key, value) }
      prop.store(Files.newOutputStream(langPath), defaultDescription)
    } else prop.load(Files.newInputStream(langPath))

    return prop
  }

  /**
   * Gets the config value from the given key.
   */
  def getC(key: String) = config.getProperty(key) match {
    case null      => None
    case x: String => Some(x)
  }
  /**
   * Gets the language value from the given key.
   */
  def getL(key: String) = language.getProperty(key) match {
    case null      => None
    case x: String => Some(x)
  }

  //==================================================================
  //default key-values, if the file doesn't exsts
  private val defaultConfKeys: Map[String, String] = Map(
    "config-file" -> "this",
    "version" -> "1.0",
    "language" -> "en")
  private val defaultLangKeys: Map[String, String] = Map()
}