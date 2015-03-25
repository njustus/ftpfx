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
      if (!checkConfig(conf)) {
        DefaultValues.defaultConfKeys.foreach { case (key, value) => conf.setProperty(key, value) }
        conf.store(Files.newOutputStream(configPath), defaultConfDescription)
      }

      //TODO if lanuage != en --> load the specified file
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
      if (!checkLanguage(prop)) {
        DefaultValues.defaultLangKeys.foreach { case (key, value) => prop.setProperty(key, value) }
        prop.store(Files.newOutputStream(langPath), defaultLangDescription)
      }
    }

    return prop
  }

  /**
   * Gets the config value from the given key.
   */
  def getC(key: String) = config.getProperty(key) match {
    case null                             => None
    case x if (x.equals("software-name")) => Some(DefaultValues.swName)
    case x if (x.equals("version"))       => Some(DefaultValues.swVersion)
    case x if (x.equals("port"))          => Some(DefaultValues.port)
    case x: String                        => Some(x)
  }
  /**
   * Gets the language value from the given key.
   */
  def getL(key: String) = language.getProperty(key) match {
    case null                             => None
    case x if (x.equals("software-name")) => Some(DefaultValues.swName)
    case x: String                        => Some(x)
  }

  def getCss() =
    if (getC("theme").get.equals("default"))
      getClass.getResource("style/FtpGui.css").toExternalForm()
    else
      getClass.getResource(getC("theme").get).toExternalForm()

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
  //== Values that shouldn't be translated
  val swName = "NJ's FTP"
  val swVersion = "1.0"
  val port = "Port"
  private val defaultLocalDir = System.getProperty("user.home")
  private val defaultDownloadDir = defaultLocalDir + "/Downloads"

  /*
   * The value this/default is used for default-values
   */
  val defaultConfKeys: Map[String, String] = Map(
    "config-file" -> "this",
    "language-file" -> "default",
    "language" -> "en",
    "theme" -> "default",
    "local-start-dir" -> defaultLocalDir,
    "download-dir" -> defaultDownloadDir)

  val defaultLangKeys: Map[String, String] = Map(
    //menues
    "file-menue" -> "File",
    "help-menue" -> "Help",
    //-- Filemenue
    "local-root" -> "Set local root...",
    "remote-root" -> "Set remote root...",
    "local-root-chooser-title" -> "Set local root directory",
    "exit" -> "Exit",
    //-- Helpmenue
    "client-information-item" -> "Client information",
    "server-information-item" -> "Server information",
    "about-item" -> "About...",
    //connect-header
    "servername" -> "Servername",
    "username" -> "Username",
    "password" -> "Password",
    "connect-btn" -> "Connect",
    "disconnect-btn" -> "Disconnect",
    "upload-btn" -> "Upload",
    "download-btn" -> "Download",
    //filesystem-view
    "local-filesystem-title" -> "Local Filesystem",
    "remote-filesystem-title" -> "Remote Filesystem",
    //filesystem treeview-entrys
    "default-remote-entry" -> "Not Connected.",
    //download-directory
    "download-dir" -> "Download directory:",
    "download-choose-entry" -> "Choose..",
    "download-chooser-title" -> "Set download directory",
    //Log-tabbar
    "loads-tab" -> "Up-/Downloads",
    "log-tab" -> "Log")
}
//==================================================================