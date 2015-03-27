package ftp.ui.listeners

import javafx.scene.control.TreeItem
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import ftp.client.filesystem.FileDescriptor
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
          //add a cd-handler to the new generated subview
          subview.getChildren.asScala.filter(_.getValue().isDirectory()).foreach { x => x.expandedProperty().addListener(this) }
          //add the subview to it's root
          item.getChildren.addAll(subview.getChildren)
      }

    }
  }
}
