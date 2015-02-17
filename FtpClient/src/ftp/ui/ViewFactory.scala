package ftp.ui

import javafx.scene.control.TreeItem
import java.io.File
import javafx.scene.control.TreeView

object ViewFactory { 
  
  /**
   * Generates a new TreeView from the given file.
   * @param file the file 
   */
  def newView(file : File) : TreeView[File] = {    
    
    def subs(f: File) : TreeItem[File] = {
      if(f.isDirectory()) {
        val directory = new TreeItem[File](f)        
        
        f.listFiles().foreach { child => directory.getChildren.add(subs(child)) }
        return directory
      }else return new TreeItem[File](f)        
    }

    return new TreeView[File](subs(file))
  }
  
}