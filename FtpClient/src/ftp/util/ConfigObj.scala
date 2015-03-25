package ftp.util

import java.nio.file.Paths
import java.nio.file.Files

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object ConfigObj {
  private val defaultConfDescription = "ftpfx's default configuration file."
  private val defaultLangDescription = "ftpfx's default language file."
  private val configPath = Paths.get("rsc/conf/ftpfxDefault.conf")
  private val langPath = Paths.get("rsc/lang/ftpfx-en.conf")
  private val config: java.util.Properties = loadConfig()
  //TODO needs to be defined
  private val language: java.util.Properties = loadLanguage()

  private def loadConfig(): java.util.Properties = {
    val conf: java.util.Properties = new java.util.Properties()
    if (!Files.exists(configPath)) {
      DefaultValues.defaultConfKeys.foreach { case (key, value) => conf.setProperty(key, value) }
      conf.store(Files.newOutputStream(configPath), defaultConfDescription)
    } else {
      conf.load(Files.newInputStream(configPath))
      //the loaded file doesn't contain all keys => use the default-keyset
      if (!checkConfig(conf))
        DefaultValues.defaultConfKeys.foreach { case (key, value) => conf.setProperty(key, value) }

      //TODO if lanuage != en --> load -de.conf file
    }

    return conf
  }

  private def loadLanguage(): java.util.Properties = {
    val prop: java.util.Properties = new java.util.Properties()
    if (!Files.exists(langPath)) {
      DefaultValues.defaultLangKeys.foreach { case (key, value) => prop.setProperty(key, value) }
      prop.store(Files.newOutputStream(langPath), defaultLangDescription)
    } else {
      prop.load(Files.newInputStream(langPath))
      //the loaded file doesn't contain all keys => use the default-keyset
      if (!checkLanguage(prop))
        DefaultValues.defaultLangKeys.foreach { case (key, value) => prop.setProperty(key, value) }
    }

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

  private def checkConfig(conf: java.util.Properties): Boolean = {
    val origKeys = DefaultValues.defaultConfKeys.keySet
    val extractedKeys = conf.keySet().filter(x => x.isInstanceOf[String])
    return origKeys.forall { x => extractedKeys.contains(x) }
  }

  private def checkLanguage(prop: java.util.Properties): Boolean = {
    val origKeys = DefaultValues.defaultLangKeys.keySet
    val extractedKeys = prop.keySet().filter(x => x.isInstanceOf[String])
    return origKeys.forall { x => extractedKeys.contains(x) }
  }
}

//==================================================================
//default key-values, if the file doesn't exsts
private object DefaultValues {
  /*
   * The value this/default is used for default-values
   */
  val defaultConfKeys: Map[String, String] = Map(
    "config-file" -> "this",
    "language-file" -> "default",
    "version" -> "1.0",
    "language" -> "en",
    "theme" -> "default")
  val defaultLangKeys: Map[String, String] = Map(
    "upload-btn-lbl" -> "Upload",
    "download-btn-lbl" -> "Download",
    "loads-tab-lbl" -> "Up-/Downloads",
    "log-tab-lbl" -> "Log",
    "file-menue-lbl" -> "File",
    "help-menue-lbl" -> "Help")
}
//==================================================================