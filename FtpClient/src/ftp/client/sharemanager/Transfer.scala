package ftp.client.sharemanager

import ftp.client.FtpClient
import java.nio.file.Path
import ftp.client.filesystem.FileDescriptor

/**
 * Defines either upload or download messages.
 */
abstract class Transfer[T](val files: List[T]) {

}

/**
 * Defines upload transfer messages.
 */
case class Upload(override val files: List[Path]) extends Transfer(files) {

}
/**
 * Defines download transfer  messages.
 */
case class Download(override val files: List[FileDescriptor]) extends Transfer(files) {

}
/**
 * Defines exit-class for the TransferManager.
 * This message-class simply stops the manager.
 */
case class Exit() {

}