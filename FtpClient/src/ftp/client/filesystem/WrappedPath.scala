package ftp.client.filesystem

import java.nio.file.Path

/**
 * Wrapper for [[java.nio.file.Path]].
 *
 * This wrapper is needed to get only the filename as a string-representation (toString()) for the given path.
 * This wrapper only contains the re-implemented toString method. For every other function use the path-object.
 *
 * @example Using inside a TreeItem:
 * {{{
 * val wrappedPath = WrappedPath(Paths.get("/tmp/linux")
 * val item = new TreeItem[WrappedPath](wrappedPath)
 * }}}
 */
case class WrappedPath(val path: Path) {
  override def toString() = {
    if (path.toString == "/") path.toString
    else {
      val splitted = path.toString().split("/")
      splitted(splitted.length - 1)
    }
  }
}
