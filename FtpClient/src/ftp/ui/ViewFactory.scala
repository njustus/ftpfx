package ftp.ui

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.CheckBoxTreeItem
import scala.collection.JavaConversions.iterableAsScalaIterable
import ftp.ui.filewalker.GenerateTree
import ftp.ui.listeners._
import javafx.scene.control.TreeItem
import javafx.event.EventHandler
import javafx.event.ActionEvent
import javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent
import javafx.beans.property.BooleanProperty
import ftp.client.filesystem.FileDescriptor
import ftp.client.filesystem.RemoteFile

object ViewFactory {
  /**
   * Generates a new TreeView from the given file.
   *
   * This method shouldn't be used anymore. The lazy-view-generation (newLazyView()) is better.
   * @param file the file
   */
  @deprecated def newView(file: Path): CheckBoxTreeItem[Path] = {
    val root = new CheckBoxTreeItem[Path](file)

    val fileWalker = new GenerateTree(root)
    Files.walkFileTree(file, fileWalker)
    return fileWalker.getView
  }

  /**
   * Generates a temporary view, without any sub-elements. They needs to lazily generated.
   *
   * @param file the (actual) rootpath
   */
  def newLazyView(file: Path): CheckBoxTreeItem[Path] = {
    val root = new CheckBoxTreeItem[Path](file)
    val listener: ChangeListener[java.lang.Boolean] = new LocalItemChangeListener()

    //! this is absolutely ugly ! (thanks to the generics)
    def generateItem(x: Path): CheckBoxTreeItem[Path] = {
      val item = new CheckBoxTreeItem[Path](x)

      item.expandedProperty().addListener(listener)
      return item
    }

    //get all entrys without hiddenfiles
    Files.newDirectoryStream(file).filterNot { x => Files.isHidden(x) }.
      foreach {
        _ match {
          case x if (Files.isDirectory(x)) =>
            //Add a dummy-children for identifying later in the lazy generation
            val xItem = generateItem(x)
            xItem.getChildren.add(DummyItems.localFs)
            root.getChildren.add(xItem)
          case x if (!Files.isDirectory(x)) => root.getChildren.add(new CheckBoxTreeItem[Path](x))
        }
      }

    root.setExpanded(true)
    return root
  }

  /**
   * Generates a new (sub)-view for the given directory within the content.
   *
   * Especially used for the response from the server for ls()-commands.
   * @param dir the actual root-directory
   * @param content the content of the directory
   */
  def newSubView(dir: String, content: List[FileDescriptor]): CheckBoxTreeItem[FileDescriptor] = {
    val root = new CheckBoxTreeItem[FileDescriptor](new RemoteFile(dir, true))

    //generate directory content
    content.foreach {
      _ match {
        case f if (f.isDirectory()) =>
          //Add a dummy-children for identifying later in the lazy generation
          val xItem = new CheckBoxTreeItem[FileDescriptor](f)
          xItem.getChildren.add(DummyItems.remoteFs)
          root.getChildren.add(xItem)
        case f if (f.isFile()) =>
          root.getChildren.add(new CheckBoxTreeItem[FileDescriptor](f))
      }
    }

    root.setExpanded(true)
    return root
  }

  private class ItemChangeListener extends ChangeListener[java.lang.Boolean] {
    override def changed(obVal: ObservableValue[_ <: java.lang.Boolean], oldVal: java.lang.Boolean, newVal: java.lang.Boolean): Unit = {
      /*
       * newVal = new state of the component (true if expanded, false otherwise)
       * obVal = the "observed" item
       * bb.getBean holds the actual Item
       */
      if (newVal) {
        //System.out.println("newValue = " + newVal);
        val bb = obVal.asInstanceOf[BooleanProperty];
        //System.out.println("bb.getBean() = " + bb.getBean());
        val t = bb.getBean.asInstanceOf[TreeItem[Path]];
        val path = t.getValue

        //set new subpath for the given directory if it's not created yet
        if (t.getChildren.contains(DummyItems.localFs)) {
          //remove the dummy and replace the childrens
          t.getChildren.remove(DummyItems.localFs)
          val subview = newLazyView(path)
          t.getChildren.addAll(subview.getChildren)
        }
      }
    }
  } //class ItemChangeListener
}
