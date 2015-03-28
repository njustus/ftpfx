package ftp.client.sharemanager

import scala.actors.Actor
import java.nio.file.Path
import ftp.client.filesystem.FileDescriptor
import ftp.client.FtpClient
import java.nio.file.Files
import ftp.response.Receivable

/**
 * This manager transfers files to the ftpserver and downloads files from the ftpserver.
 *
 * It uses Upload-/Download-Messages for the files that should be transfered.<br/><br/>
 * <b>If the client is null this actor does nothing by default.</b>
 *
 * @param ftpClient - the  FtpClient-Connection
 */
class TransferManager(private val ftpClient: FtpClient, private val rc: Receivable) extends Actor {
  def act(): Unit = loop {
    react {
      case msg: Upload if (ftpClient != null) => {
        msg.getFiles.foreach {
          _ match {
            case x if (Files.isDirectory(x)) => rc.status("Upload: Skipping directory: " + x + ". Can't send directorys.")
            case x if (Files.isRegularFile(x)) => {
              rc.status("Upload: " + x.toString())
              ftpClient.sendFile(x.toAbsolutePath().toString())
            }
            case x if (!Files.isRegularFile(x)) => rc.status("Upload: Skipping: " + x + ". Is not a regular file.")
            case _                              => rc.error("Skipping: unknown file format.")
          }
        }
      }
      case msg: Download if (ftpClient != null) => {
        msg.getFiles.foreach {
          _ match {
            case x if (x.isDirectory()) => rc.status("Download: Skipping directory: " + x + ". Can't receive directorys.")
            case x if (x.isFile()) => {
              val dest = msg.dest + "/" + x.getFilename()
              rc.status("Download: src: " + x.getAbsoluteFilename + " dest: " + dest)
              ftpClient.receiveFile(x.getAbsoluteFilename, dest)
            }
            case _ => rc.error("Skipping: unknown file format.")
          }
        }
      }
      case msg: Exit => this.exit()
    }
  }
}
