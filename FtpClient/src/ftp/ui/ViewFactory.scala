package ftp.ui

import javafx.scene.control.TreeItem
import java.io.File
import javafx.scene.control.TreeView
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.cell.CheckBoxTreeCell
import ftp.ui.filewalker.GenerateTree
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths

import scala.collection.JavaConversions._

object ViewFactory {

  /**
   * Generates a new TreeView from the given file.
   * @param file the file
   */
  def newView(file: Path): CheckBoxTreeItem[Path] = {
    val root = new CheckBoxTreeItem[Path](file)

    val fileWalker = new GenerateTree(root)
    Files.walkFileTree(file, fileWalker)
    return fileWalker.getView
  }

  /**
   * Generates a temporary view, without any sub-elements. They needs to lazily generated.
   *
   */
  def newLazyView(file: Path): CheckBoxTreeItem[Path] = {
    val root = new CheckBoxTreeItem[Path](file)

    //get all entrys without hiddenfiles
    Files.newDirectoryStream(file).filterNot { x => Files.isHidden(x) }.
      foreach { x => root.getChildren.add(new CheckBoxTreeItem[Path](x)) }

    return root
  }

  /**
   * Generates a new (sub)-view for the given directory within the content.
   * Especially used for the response from the server for ls()-commands.
   * @param dir the actual root-directory
   * @param content the content of the directory
   */
  def newSubView(dir: String, content: List[String]): CheckBoxTreeItem[Path] = {
    val root = new CheckBoxTreeItem[Path](Paths.get(dir))

    //generate directory content
    content.foreach { f => root.getChildren.add(new CheckBoxTreeItem[Path](Paths.get(f))) }

    root.setExpanded(true)
    return root
  }

}