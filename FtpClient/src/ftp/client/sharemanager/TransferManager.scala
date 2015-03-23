package ftp.client.sharemanager

import scala.actors.Actor
import java.nio.file.Path
import ftp.client.filesystem.FileDescriptor
import ftp.client.FtpClient

class TransferManager(private val ftpClient: FtpClient) extends Actor {
  def act(): Unit = {
    react {
      case msg: Upload => {
        if (ftpClient != null)
          msg.files.map(x => x.getFileName()).foreach { x => println(x) }
      }
      case msg: Download => {
        if (ftpClient != null)
          msg.files.map(x => x.getFilename()).foreach { x => println(x) }
      }
      case msg: Exit => this.exit()
    }
  }
}