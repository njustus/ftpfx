package ftp.ui

import java.nio.file.Paths
import javafx.scene.control.TreeItem
import java.nio.file.Path
import ftp.client.filesystem.FileDescriptor
import ftp.client.filesystem.RemoteFile
import ftp.client.filesystem.WrappedPath

/**
 * Holds the path-entrys for dummyitems in a [[TreeItem]].
 */
object DummyItems {
  /**
   * Entry for the local filesystem.
   */
  val localFs: TreeItem[WrappedPath] = new TreeItem[WrappedPath](WrappedPath(Paths.get(".")))
  /**
   * Entry for the remote filesystem.
   */
  val remoteFs: TreeItem[FileDescriptor] = new TreeItem[FileDescriptor](new RemoteFile(".", false))
}