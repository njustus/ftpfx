package ftp.ui.listeners

import ftp.client.filesystem.FileDescriptor
import javafx.scene.control.TreeItem
import ftp.ui.FtpGui

class RemoteItemChangeListener(private val dummy: TreeItem[FileDescriptor], private val gui: FtpGui) extends TreeListener[FileDescriptor] {
  def onChanged(item: TreeItem[FileDescriptor]): Unit = {
    ???
  }
}