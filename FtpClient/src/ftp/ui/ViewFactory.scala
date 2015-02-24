package ftp.ui

import javafx.scene.control.TreeItem
import java.io.File
import javafx.scene.control.TreeView
import javafx.scene.control.CheckBoxTreeItem

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
      } else return new TreeItem[File](f)
    }
    
    val root = subs(file)
    root.setExpanded(true)

    return new TreeView[File](root)
  }
  
  /**
   * Generates a new (sub)-view for the given directory within the content.
   * Especially used for the response from the server for ls()-commands.
   * @param dir the actual root-directory
   * @param content the content of the directory
   */
  def newSubView(dir: String, content:List[String]) : TreeItem[String] = {
    val root = new TreeItem[String](dir)
       
    //generate directory content
      //TODO remove filesystem informations, mark directorys with a subitem "loading.."
    content.foreach { f => root.getChildren.add(new CheckBoxTreeItem[String](f)) }
    
    root.setExpanded(true)
    return root
  }

}