package ftp.client.sharemanager

import scala.actors.Actor
import java.nio.file.Path
import ftp.client.filesystem.FileDescriptor
import ftp.client.FtpClient

/**
 * This manager transfers files to the ftpserver and downloads files from the ftpserver.
 * It uses Upload-/Download-Messages for the files that should be transfered.
 * If the client is null this actor does nothing by default.
 *
 * @param ftpClient - the  FtpClient-Connection
 */
class TransferManager(private val ftpClient: FtpClient) extends Actor {
  def act(): Unit = {
    react {
      case msg: Upload if (ftpClient != null) => {
        msg.files.map(x => x.getFileName()).foreach { x => println(x) }
      }
      case msg: Download if (ftpClient != null) => {
        if (ftpClient != null)
          msg.files.map(x => x.getFilename()).foreach { x => println(x) }
      }
      case msg: Exit => this.exit()
    }
  }
}