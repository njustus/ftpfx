package ftp.ui.listeners

import javafx.scene.control.TreeItem
import java.nio.file.Path
import java.nio.file.Paths
import ftp.ui.ViewFactory
import ftp.ui.DummyItems

class LocalItemChangeListener(private val dummy: TreeItem[Path] = DummyItems.localFs) extends TreeListener[Path] {
  def onChanged(item: TreeItem[Path]): Unit = {
    //set new subpath for the given directory if it's not created yet
    if (item.getChildren.contains(dummy)) {
      //remove the dummy and replace the childrens
      item.getChildren.remove(dummy)
      val path = item.getValue

      //generate the subview from the changed element and add it to the tree
      val subview = ViewFactory.newLazyView(path)
      item.getChildren.addAll(subview.getChildren)
    }
  }
}