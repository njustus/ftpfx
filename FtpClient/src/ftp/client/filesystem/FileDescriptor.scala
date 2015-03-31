package ftp.client.filesystem

/**
 * This trait describes file-informations about remote-files.
 * @see [[ftp.client.filesystem.RemoteFile]]
 */
trait FileDescriptor extends Comparable[FileDescriptor] {
  /**
   * Tests wether this is a directory or not.
   *
   * @return true if it's a directory, false otherwise
   */
  def isDirectory(): Boolean
  /**
   * Tests wether this is a actual file or not.
   *
   * @return true if it's a file, false otherwise
   */
  def isFile(): Boolean = !isDirectory()
  /**
   * Returns the absolute path to the file.
   *
   * The return-value is something like this:
   * {{{ /tmp/linux/testFile.txt }}}
   * @return the path with the filename
   */
  def getAbsoluteFilename(): String
  /**
   * Returns only the filename without any path-informations.
   *
   * The return-value is something like this:
   * {{{ testFile.txt }}}
   * @return the filename
   */
  def getFilename() = {
    val splitted = this.getAbsoluteFilename().split("/")
    splitted(splitted.length - 1)
  }

  override def toString() = getAbsoluteFilename()

  override def compareTo(other: FileDescriptor) =
    this.getFilename().compareTo(other.getFilename())

}
