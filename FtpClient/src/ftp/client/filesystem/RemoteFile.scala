package ftp.client.filesystem

/**
 * Describes remote files.
 *
 * Implementation for [[ftp.client.filesystem.FileDescriptor]].
 *
 */
class RemoteFile(private val name: String, private val isdir: Boolean = false) extends FileDescriptor {
  def getAbsoluteFilename(): String = name
  def isDirectory(): Boolean = isdir

  override def toString() = {
    if (name == "/") name
    else {
      val splitted = name.split("/")
      splitted(splitted.length - 1)
    }
  }
}
