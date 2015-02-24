package ftp.ui

import javafx.scene.control.TreeItem
import java.io.File
import javafx.scene.control.TreeView
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.cell.CheckBoxTreeCell

object ViewFactory {

  /**
   * Generates a new TreeView from the given file.
   * @param file the file
   */
  def newView(file: File): TreeView[File] = {

    def subs(f: File): TreeItem[File] = {
      if (f.isDirectory()) {
        val directory = new CheckBoxTreeItem[File](f)

        f.listFiles().foreach { child => if (!child.isHidden()) directory.getChildren.add(subs(child)) }
        return directory
      } else return new CheckBoxTreeItem[File](f)
    }
    
    val root = subs(file)
    root.setExpanded(true)
    val tree = new TreeView[File](root)
    tree.setCellFactory(CheckBoxTreeCell.forTreeView())
    return tree
  }
  
  /**
   * Generates a new (sub)-view for the given directory within the content.
   * Especially used for the response from the server for ls()-commands.
   * @param dir the actual root-directory
   * @param content the content of the directory
   */
  def newSubView(dir: String, content:List[String]) : CheckBoxTreeItem[File] = {
    val root = new CheckBoxTreeItem[File](new File(dir))
       
    //generate directory content
      //TODO remove filesystem informations, mark directorys with a subitem "loading.."
    content.foreach { f => root.getChildren.add(new CheckBoxTreeItem[File](new File(f))) }
    
    root.setExpanded(true)
    return root
  }

}