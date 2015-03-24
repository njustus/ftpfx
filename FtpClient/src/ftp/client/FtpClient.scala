package ftp.client

import ftp.client.filesystem.FileDescriptor
import java.nio.file.Path

/**
 * This trait describes Ftp-Clients.
 * FTP is a synchronous protocol so you can use send & receive methods.
 * For more information about FTP:
 * @see FtpClient/FtpDescription-RFC959.txt
 * @see <a href="https://www.ietf.org/rfc/rfc959.txt"> https://www.ietf.org/rfc/rfc959.txt</a>
 */
trait FtpClient {
  /**
   * Size of the output-buffer in bytes.
   */
  protected val BUFFER_SIZE: Int = 1024 * 64

  /**
   * Connects the client to the sever.
   * @param username the username for authentication
   * @param password the password for authentication
   */
  def connect(username: String, password: String): Boolean
  /**
   * Disconnects the client from the sever.
   */
  def disconnect(): Boolean
  /**
   * Changes the directory to the given path.
   * @param path the path
   */
  def cd(path: String): String
  def changeDirectory(path: String): String = cd(path)

  /**
   * Lists the directory content.
   */
  def ls(): List[FileDescriptor]
  /**
   * Prints out the actual working directory
   */
  def pwd(): String
  /**
   * Sends the given file to the server.
   * @param filename the filename
   */
  def sendFile(filename: String): Boolean
  def sendFile(file: Path): Boolean = sendFile(file.toAbsolutePath.toString)
  /**
   * Receives a file from the server.
   * @param filename the file which should be downloaded
   */
  def receiveFile(filename: String, dest: String): Boolean
  def receiveFile(file: Path, dest: Path): Boolean = receiveFile(file.toAbsolutePath.toString, dest.toAbsolutePath.toString)
  /**
   * Gets information about the connected server.
   * The informations are separated by \n and the key : value by :.
   * @return the serverinformations
   */
  def getServerInformation(): String
  /**
   * Gets information about the used client.
   * The informations are separated by \n and the key : value by :.
   * @return the informations about the client
   */
  def getClientInformation(): String
  /**
   * Changes the transfer-mode. Either to active or passive.
   * @pararm active true if active mode, false if passive-mode
   */
  def changeMode(active: Boolean): Boolean
  /**
   * Renames the given file to the new name.<BR/>
   * The names should be the <B>canonical path</B>.
   *
   * <P>This method uses RNFR <oldPath>, RNTO <newPath></P>
   */
  def renameFile(oldPath: String, newPath: String): Boolean
  /**
   * Deletes the given <B>file</B>.<BR/>
   * <P>Uses: DELE <path></P>
   */
  def deleteFile(path: String): Boolean
  /**
   * Deletes the given <B>directory</B>.<BR/>
   * <P>Uses: RMD <path></P>
   */
  def deleteDir(path: String): Boolean
  /**
   * Creates a new directory.<BR/>
   * <P>Uses: MKD <path></P>
   */
  def mkdir(path: String): Boolean

  /**
   * Stops the actual process and quits it.
   */
  def quit()
}