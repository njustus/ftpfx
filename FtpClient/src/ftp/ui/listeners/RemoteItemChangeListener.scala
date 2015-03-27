package ftp.ui.listeners

import ftp.client.filesystem.FileDescriptor
import javafx.scene.control.TreeItem
import ftp.ui.FtpGui
import ftp.ui.DummyItems

class RemoteItemChangeListener(private val dummy: TreeItem[FileDescriptor] = DummyItems.remoteFs, private val gui: FtpGui) extends TreeListener[FileDescriptor] {
  def onChanged(item: TreeItem[FileDescriptor]): Unit = {
    /*
     * TODO define a interface that returns a List[FileDescriptor] from the ftpclient FtpClient#ls()
     */
    ???
  }
}