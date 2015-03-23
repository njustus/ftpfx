package ftp.client.sharemanager

import ftp.client.FtpClient
import java.nio.file.Path
import ftp.client.filesystem.FileDescriptor

/**
 * Defines either upload or download messages.
 */
abstract class Transfer[T](protected val files: List[T]) {
  /**
   * Returns the list of files which should be transferred.
   * @return a list of files
   */
  def getFiles() = files
}

/**
 * Defines upload transfer messages.
 */
case class Upload(override protected val files: List[Path]) extends Transfer(files) {

}
/**
 * Defines download transfer  messages.
 */
case class Download(override protected val files: List[FileDescriptor], val dest: String) extends Transfer(files) {

}
/**
 * Defines exit-class for the TransferManager.
 * This message-class simply stops the manager.
 */
case class Exit() {

}
