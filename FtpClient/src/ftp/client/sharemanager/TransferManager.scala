package ftp.client.sharemanager

import scala.actors.Actor
import java.nio.file.Path
import ftp.client.filesystem.FileDescriptor
import ftp.client.FtpClient
import java.nio.file.Files
import ftp.response.Receivable

/**
 * This manager transfers files to the ftpserver and downloads files from the ftpserver.
 * It uses Upload-/Download-Messages for the files that should be transfered.
 * If the client is null this actor does nothing by default.
 *
 * @param ftpClient - the  FtpClient-Connection
 */
class TransferManager(private val ftpClient: FtpClient, private val rc: Receivable) extends Actor {
  def act(): Unit = loop {
    react {
      case msg: Upload if (ftpClient != null) => {
        println("upload")
        msg.files.foreach {
          _ match {
            case x if (Files.isDirectory(x))    => rc.status("Skipping directory: " + x + ". Can't send directorys.")
            case x if (Files.isRegularFile(x))  => ftpClient.sendFile(x.toAbsolutePath().toString())
            case x if (!Files.isRegularFile(x)) => rc.status("Skipping: " + x + ". Is not a regular file.")
            case _                              => rc.error("Skipping: unknown file format.")
          }
        }
      }
      case msg: Download if (ftpClient != null) => {
        println("download")
        msg.files.foreach {
          _ match {
            case x if (x.isDirectory()) => rc.status("Skipping directory: " + x + ". Can't receive directorys.")
            case x if (x.isFile())      => ftpClient.receiveFile(x.getFilename, msg.dest)
            case _                      => rc.error("Skipping: unknown file format.")
          }
        }
      }
      case msg: Exit => this.exit()
    }
  }
}
