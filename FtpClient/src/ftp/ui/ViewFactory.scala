package ftp.ui

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.TreeItem
import scala.collection.JavaConversions.iterableAsScalaIterable
import ftp.ui.filewalker.GenerateTree
import ftp.ui.listeners._
import javafx.scene.control.TreeItem
import javafx.event.EventHandler
import javafx.event.ActionEvent
import javafx.scene.control.TreeItem.TreeModificationEvent
import javafx.beans.property.BooleanProperty
import ftp.client.filesystem.FileDescriptor
import ftp.client.filesystem.RemoteFile
import ftp.client.filesystem.WrappedPath
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Alert
import javafx.scene.layout.GridPane
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import java.io.StringWriter
import java.io.PrintWriter
import javafx.scene.layout.Priority
import javafx.scene.control.TextInputDialog
import ftp.util.ConfigObj

/**
 * Creates JavaFx-components.
 *
 * For further informations about Dialogues:
 * [[http://code.makery.ch/blog/javafx-dialogs-official/]]
 */
object ViewFactory {
  /**
   * Generates a new TreeView from the given file.
   *
   * This method shouldn't be used anymore. The lazy-view-generation (newLazyView()) is better.
   * @param file the file
   */
  @deprecated def newView(file: Path): TreeItem[Path] = {
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
  def newLazyView(file: Path): TreeItem[WrappedPath] = {
    val root = new TreeItem[WrappedPath](WrappedPath(file))
    val listener: ChangeListener[java.lang.Boolean] = new LocalItemChangeListener()

    //! this is absolutely ugly ! (thanks to the generics)
    def generateItem(x: Path): TreeItem[WrappedPath] = {
      val item = new TreeItem[WrappedPath](WrappedPath(x))

      item.expandedProperty().addListener(listener)
      return item
    }

    //get all entrys without hiddenfiles
    val files = Files.newDirectoryStream(file).filterNot(Files.isHidden(_)).toList
    //sort them by compareTo() method & add them to the view
    files.sortWith((x, y) => compareByName(x, y) < 0).foreach(
      _ match {
        case x if (Files.isDirectory(x)) =>
          //Add a dummy-children for identifying later in the lazy generation
          val xItem = generateItem(x)
          xItem.getChildren.add(DummyItems.localFs)
          root.getChildren.add(xItem)
        case x if (!Files.isDirectory(x)) => root.getChildren.add(new TreeItem[WrappedPath](WrappedPath(x)))
      })

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
  def newSubView(dir: String, content: List[FileDescriptor]): TreeItem[FileDescriptor] = {
    val root = new TreeItem[FileDescriptor](new RemoteFile(dir, true))

    //generate directory content
    content.sortWith((x, y) => compareByName(x, y) < 0).foreach {
      _ match {
        case f if (f.isDirectory()) =>
          //Add a dummy-children for identifying later in the lazy generation
          val xItem = new TreeItem[FileDescriptor](f)
          xItem.getChildren.add(DummyItems.remoteFs)
          root.getChildren.add(xItem)
        case f if (f.isFile()) =>
          root.getChildren.add(new TreeItem[FileDescriptor](f))
      }
    }

    root.setExpanded(true)
    return root
  }

  /**
   * Compares the given elements by their string-representation.
   *
   * This method <b>ignores</b> uper- or lowercase characters.
   */
  private def compareByName[T <: Comparable[T]](x: T, y: T) =
    x.toString.toLowerCase().compareTo(y.toString.toLowerCase())

  /**
   * Creates a new error-dialgue with the given content.
   *
   * This method uses the [[javafx.scene.control.Alert]] from JavaFX.
   * @param title The title of the dialogue-box
   * @param header The header-line of the dialogue-box
   * @param msg The actual message inside the dialogue-box
   * @return an Alert-Dialogue
   */
  def newErrorDialog(title: String = "Error", header: String = "An error occured!", msg: String) = {
    val dialog = new Alert(AlertType.ERROR)
    dialog.setTitle(title)
    dialog.setHeaderText(header)
    dialog.setContentText(msg)

    dialog
  }

  /**
   * Creates a new warning-dialgue with the given content.
   *
   * This method uses the [[javafx.scene.control.Alert]] from JavaFX.
   * @param title The title of the dialogue-box
   * @param header The header-line of the dialogue-box
   * @param msg The actual message inside the dialogue-box
   * @return an Alert-Dialogue
   */
  def newWarningDialog(title: String = "Warning", header: String = "Attention", msg: String) = {
    val dialog = new Alert(AlertType.WARNING)
    dialog.setTitle(title)
    dialog.setHeaderText(header)
    dialog.setContentText(msg)

    dialog
  }

  /**
   * Creates a new information-dialgue with the given content.
   *
   * This method uses the [[javafx.scene.control.Alert]] from JavaFX.
   * @param title The title of the dialogue-box
   * @param header The header-line of the dialogue-box
   * @param msg The actual message inside the dialogue-box
   * @return an Alert-Dialogue
   */
  def newInformationDialog(title: String = "Information", header: String = "Information:", msg: String) = {
    val dialog = new Alert(AlertType.INFORMATION)
    dialog.setTitle(title)
    dialog.setHeaderText(header)
    dialog.setContentText(msg)

    dialog
  }

  /**
   * Creates a new <b>exception-dialgue</b> with the given content.
   *
   * This method uses the [[javafx.scene.control.Alert]] from JavaFX.
   * @param title The title of the dialogue-box
   * @param header The header-line of the dialogue-box
   * @param msg The actual message inside the dialogue-box
   * @param ex The exception that occured
   * @return an Alert-Dialogue
   */
  def newExceptionDialog(title: String = "EXCEPTION - ERROR", header: String = "Oups that shouldn't happen:", msg: String, ex: Exception) = {
    val dialog = new Alert(AlertType.ERROR)
    dialog.setTitle(title)
    dialog.setHeaderText(header)
    dialog.setContentText(msg)

    //write the stacktrace into a string
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    ex.printStackTrace(pw)
    val exceptionText = sw.toString

    val label = new Label("The exception stacktrace was:");
    val textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);
    textArea.setMaxSize(Double.MaxValue, Double.MaxValue)

    //let the text grow
    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)

    val pane = new GridPane()
    pane.setMaxWidth(Double.MaxValue)
    pane.add(label, 0, 0)
    pane.add(textArea, 0, 1)

    dialog.getDialogPane().setMinSize(400, 400)
    dialog.getDialogPane().setExpandableContent(pane)

    dialog
  }

  /**
   * Creates a dialog for setting the remote-root directory.
   */
  def newChangeRemoteRootDialog() = {
    /*
     * method for getting the specified keys from the ConfigObj.
     * This method is a shortcut.
     */
    def getL(key: String) = ConfigObj.getL(key) match {
      case None    => "not defined"
      case Some(x) => x
    }

    //setup the dialog with the language-keys
    val dialog = new TextInputDialog("/")
    dialog.setTitle(getL("remote-root-chooser-title"))
    dialog.setHeaderText(getL("remote-root-chooser-header"))
    dialog.setContentText(getL("remote-root-chooser-content"))
    dialog
  }

  @deprecated
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
        val t = bb.getBean.asInstanceOf[TreeItem[WrappedPath]];
        val path = t.getValue.path

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
