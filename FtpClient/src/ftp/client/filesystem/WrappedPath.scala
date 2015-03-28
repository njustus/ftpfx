package ftp.client.filesystem

import java.nio.file.Path

case class WrappedPath(val path: Path) {
  override def toString() = {
    val splitted = path.toString().split("/")
    splitted(splitted.length - 1)
  }
}