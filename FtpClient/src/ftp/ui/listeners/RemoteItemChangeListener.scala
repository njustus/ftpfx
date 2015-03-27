package ftp.ui.listeners

import ftp.client.filesystem.FileDescriptor
import javafx.scene.control.TreeItem
import ftp.ui.FtpGui
import ftp.ui.DummyItems

class RemoteItemChangeListener(ls: (FileDescriptor) => Option[List[FileDescriptor]], dummy: TreeItem[FileDescriptor] = DummyItems.remoteFs) extends TreeListener[FileDescriptor] {
  def onChanged(item: TreeItem[FileDescriptor]): Unit = {
    ???
  }
}