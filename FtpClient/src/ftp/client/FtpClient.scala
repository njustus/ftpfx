package ftp.client

import ftp.client.filesystem.FileDescriptor
import java.nio.file.Path

/**
 * Describes methods that Ftp-Clients needs.
 *
 * FTP is a synchronous protocol so you can use send & receive methods.
 *
 * <br/> For more information about the FTP-Protocol:
 *
 *  - [[https://www.ietf.org/rfc/rfc959.txt]]
 */
trait FtpClient {
  /**
   * Size of the output-buffer in bytes.
   */
  protected val BUFFER_SIZE: Int = 1024 * 64

  /**
   * Connects the client to the sever.
   *
   * @param username the username for authentication
   * @param password the password for authentication
   */
  def connect(username: String, password: String): Boolean
  /**
   * Connects the client via anonymous authentication to the server.
   */
  def connectAnonymous() = connect("anonymous", "anon")

  /**
   * Disconnects the client from the sever.
   */
  def disconnect(): Boolean
  /**
   * Changes the directory to the given path.
   *
   * @param path the path
   * @return the new actual path
   */
  def cd(path: String): String
  def changeDirectory(path: String) = cd(_)
  /**
   * Lists the directory content.
   *
   * @see [[ftp.client.filesystem.FileDescriptor]]
   * @return a List[FileDescriptor] of the directory content
   */
  def ls(): List[FileDescriptor]
  def list() = ls()
  def listFiles() = ls()
  /**
   * Gets the actual working directory
   */
  def pwd(): String
  /**
   * Sends the given file to the server.
   *
   * @param filename the filename
   * @return '''true''' if the transmission was successfull, <br/>'''false''' otherwise
   */
  def sendFile(filename: String): Boolean
  def sendFile(file: Path): Boolean = sendFile(file.toAbsolutePath.toString)
  /**
   * Receives a file from the server.
   *
   * @param filename the file which should be downloaded
   * @return '''true''' if the transmission was successfull, <br/>'''false''' otherwise
   */
  def receiveFile(filename: String, dest: String): Boolean
  def receiveFile(file: Path, dest: Path): Boolean = receiveFile(file.toAbsolutePath.toString, dest.toAbsolutePath.toString)
  /**
   * Gets information about the connected server.
   *
   * The informations are separated by \n and the key = value by =.
   * @return the serverinformation in string-representation
   */
  def getServerInformation(): String
  /**
   * Returns the server information as key -> value map.
   * @see [[ftp.client.FtpClient#getServerInformation()]]
   */
  def getServerInformationAsMap(): Map[String, String] =
    getClientInformation().split("\n").flatMap(line =>
      if (line.contains("=")) {
        val pairs = line.split("=")
        Some((pairs(0), pairs(1)))
      } else None).toMap

  /**
   * Gets information about the used client.
   *
   * The informations are separated by \n and the key = value by =.
   * @return the information about the client in string-representation
   */
  def getClientInformation(): String

  /**
   * Returns the client information as key -> value map.
   * @see [[ftp.client.FtpClient#getClientInformation()]]
   */
  def getClientInformationAsMap(): Map[String, String] =
    getClientInformation().split("\n").flatMap(line =>
      if (line.contains("=")) {
        val pairs = line.split("=")
        Some((pairs(0), pairs(1)))
      } else None).toMap

  /**
   * Changes the transfer-mode. Either to active or passive.
   *
   * @pararm active true if active mode, false if passive-mode
   */
  def changeMode(active: Boolean): Boolean
  /**
   * Renames the given file to the new name.<br/>
   *
   * The names should be the '''canonical path'''.
   * <p>This method uses RNFR <oldPath>, RNTO <newPath></p>
   *
   * @param oldPath the oldpath to the file
   * @param newPath the newpath to the file
   */
  def renameFile(oldPath: String, newPath: String): Boolean
  /**
   * Deletes the given '''file'''.
   *
   * <p>'''Uses:''' DELE <path></p>
   * @param path the path to the file
   */
  def deleteFile(path: String): Boolean
  /**
   * Deletes the given '''directory'''.
   *
   * '''Uses:''' RMD <path>
   * @param path the path to the directory
   */
  def deleteDir(path: String): Boolean
  /**
   * Creates a new directory.
   *
   * <p>'''Uses:''' MKD <path></p>
   * @param path the path to the directory
   */
  def mkdir(path: String): Boolean
  /**
   * Creates a new directory.
   * @param path the path to the directory
   * @see [[ftp.client.FtpClient#mkdir()]]
   */
  def createDirectory(path: String) = mkdir(_)
  /**
   * Creates a new directory.
   * @see [[ftp.client.FtpClient#mkdir()]]
   */
  def makeDirectory(path: String) = mkdir(_)
  /**
   * Stops the actual executed process and quits it.
   */
  def quit()
}
