package ftp.ui.filewalker

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

import javafx.scene.control.CheckBoxTreeItem

class GenerateTree(private val root: CheckBoxTreeItem[Path]) extends FileVisitor[Path] {
  private var actual = root

  def postVisitDirectory(file: Path, exc: IOException): FileVisitResult = {
    actual = actual.getParent().asInstanceOf[CheckBoxTreeItem[Path]]

    return FileVisitResult.CONTINUE
  }

  def preVisitDirectory(file: Path, attr: BasicFileAttributes): FileVisitResult = {
    if (file == root.getValue) return FileVisitResult.CONTINUE
    else if (!Files.isHidden(file)) {
      val sub = new CheckBoxTreeItem[Path](file)
      actual.getChildren.add(sub)
      actual = sub
      return FileVisitResult.CONTINUE
    } else return FileVisitResult.SKIP_SUBTREE
  }

  def visitFile(file: Path, attr: BasicFileAttributes): FileVisitResult = {
    if (attr.isRegularFile() && !Files.isHidden(file))
      actual.getChildren.add(new CheckBoxTreeItem[Path](file))

    return FileVisitResult.CONTINUE
  }

  def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
    return FileVisitResult.TERMINATE
  }

  def getView(): CheckBoxTreeItem[Path] = return root
}