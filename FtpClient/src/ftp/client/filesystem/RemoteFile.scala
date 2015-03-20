package ftp.client.filesystem

/**
 * This class describes remote (ftp) files.
 */
class RemoteFile(private val name: String, private val isdir: Boolean = false) extends FileDescriptor {

  def getFilename(): String = name
  def isDirectory(): Boolean = isdir
}