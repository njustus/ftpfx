package ftp.ui

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import javafx.scene.control.TreeItem

object ViewFactory {
  def newFileItem(file : Path) : TreeItem[Path] = {
    val attrs = Files.readAttributes(file, classOf[BasicFileAttributes] )
    
    if(attrs.isDirectory()) throw new IllegalArgumentException("Path is a directory")
    
    val item = new TreeItem[Path](file.getFileName())
    return item;
  }
  
  def newDirItem(file : Path) : TreeItem[Path] = {
    val attrs = Files.readAttributes(file, classOf[BasicFileAttributes] )
    
    if(!attrs.isDirectory()) throw new IllegalArgumentException("Path is a file")
    
    val item = new TreeItem[Path](file.getFileName())
    return item;
  }
}