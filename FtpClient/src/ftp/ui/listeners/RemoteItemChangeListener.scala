package ftp.ui.listeners

import ftp.client.filesystem.FileDescriptor
import javafx.scene.control.TreeItem
import ftp.ui.FtpGui
import ftp.ui.DummyItems
import ftp.ui.ViewFactory

class RemoteItemChangeListener(ls: (FileDescriptor) => Option[List[FileDescriptor]], dummy: TreeItem[FileDescriptor] = DummyItems.remoteFs) extends TreeListener[FileDescriptor] {
  def onChanged(item: TreeItem[FileDescriptor]): Unit = {
    //set new subpath for the given directory if it's not created yet
    if (item.getChildren.contains(dummy)) {
      //remove the dummy and replace the childrens
      item.getChildren.remove(dummy)
      val path = item.getValue

      ls(path) match {
        case None => { /*do nothing because of no content*/ }
        case Some(x: List[FileDescriptor]) =>
          //generate the subview from the changed element and add it to the tree
          val subview = ViewFactory.newSubView(path.getFilename(), x)
          item.getChildren.addAll(subview.getChildren)
      }

    }
  }
}
