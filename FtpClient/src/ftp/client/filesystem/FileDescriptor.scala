package ftp.client.filesystem

/**
 * This trait describes file-informations.
 */
trait FileDescriptor {
  /**
   * Tests wether the file is a directory or not.
   *
   * @return true if it's a directory, false otherwise
   */
  def isDirectory(): Boolean
  /**
   * Tests wether the file is a actual file or not.
   *
   * @return true if it's a file, false otherwise
   */
  def isFile(): Boolean = !isDirectory()
  /**
   * Returns the filename.
   *
   * @return the filename
   */
  def getFilename(): String
}
