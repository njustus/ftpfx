package ftp.ui

import java.io.File
import java.net.SocketException
import java.net.ConnectException
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Background
import javafx.scene.text.Text
import javafx.stage.Stage
import ftp.client.ClientFactory
import ftp.client.FtpClient
import ftp.response.Receivable
import ftp.ui.FxEventHandlerImplicits._
import javafx.scene.layout.VBox
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.cell.CheckBoxTreeCell
import java.nio.file.Path
import java.nio.file.Paths
import ftp.client.filesystem.FileDescriptor

/**
 * This class is used for the FX-GUI.
 */
class FtpGui extends Application {
  private var ftpClient: FtpClient = null
  private val receiver: Receivable = new ReceiveHandler

  //menue
  private val menueBar = new MenuBar()
  private val fileMenue = new Menu("File")
  private val helpMenue = new Menu("Help")

  //Connection
  private val txtServer = new TextField()
  private val txtPort = new TextField("21")
  private val txtUsername = new TextField()
  private val txtPassword = new PasswordField()
  private val btnConnect = new Button("Connect")
  private val btnDisconnect = new Button("Disconnect")
  //Logs
  private val txaLog = new TextArea()
  private val txaLoads = new TextArea()
  //Filesystems
  private var localFs: TreeView[Path] = null
  private var remoteFs: TreeView[Path] = null

  //Down-/Uploads
  private val btnUpload = new Button("Upload")
  private val btnDownload = new Button("Download")

  override def start(primStage: Stage) = {
    val vboxContainer = new VBox()
    val root = new BorderPane()
    root.setId("rootPane")
    val top = new GridPane()
    top.setId("topGrid")
    root.setTop(top)
    val scene = new Scene(vboxContainer, 800, 700)
    scene.getStylesheets().add(getClass.getResource("style/FtpGui.css").toExternalForm())

    //Menues
    //Help menue
    val clientInfoMnItem = new MenuItem("Client information")
    val serverInfoMnItem = new MenuItem("Server information")
    val aboutInfoMnItem = new MenuItem("About...")
    clientInfoMnItem.setOnAction((ev: ActionEvent) => showClientInformation())
    serverInfoMnItem.setOnAction((ev: ActionEvent) => showServerInformation())
    aboutInfoMnItem.setOnAction((ev: ActionEvent) => showAbout())
    helpMenue.getItems.addAll(clientInfoMnItem, serverInfoMnItem, aboutInfoMnItem)

    //Add menues to the menuebar, add the menuebar
    menueBar.getMenus.addAll(fileMenue, helpMenue)

    btnConnect.setId("green")
    btnConnect.setOnAction((ev: ActionEvent) => connect())
    btnDisconnect.setId("red")
    btnDisconnect.setOnAction((ev: ActionEvent) => if (ftpClient != null) ftpClient.disconnect())
    btnUpload.setOnAction((ev: ActionEvent) => shareFiles(Upload, localFs))
    btnDownload.setOnAction((ev: ActionEvent) => shareFiles(Download, localFs))

    txtPort.setMaxWidth(50)

    top.add(newBoldText("Servername"), 0, 0)
    top.add(txtServer, 1, 0)
    top.add(newBoldText("Port"), 2, 0)
    top.add(txtPort, 3, 0)
    top.add(newBoldText("Usename"), 0, 1)
    top.add(txtUsername, 1, 1)
    top.add(newBoldText("Password"), 2, 1)
    top.add(txtPassword, 3, 1)
    top.add(btnConnect, 4, 1)
    top.add(btnDisconnect, 4, 0)
    top.add(btnUpload, 5, 0)
    top.add(btnDownload, 5, 1)

    root.setCenter(genFileSystemView())

    //log area
    val pane = new TabPane();
    pane.setId("bottomPane")
    pane.setSide(Side.BOTTOM)
    val loadsTab = new Tab("Up-/Downloads")
    val logTab = new Tab("Log")
    txaLoads.setEditable(false)
    txaLoads.setEditable(false)
    loadsTab.setContent(txaLoads)
    logTab.setContent(txaLog)
    pane.getTabs.addAll(loadsTab, logTab)

    root.setBottom(pane)

    vboxContainer.getChildren.addAll(menueBar, root)
    primStage.setTitle("NJ's FTP")
    primStage.setScene(scene)
    primStage.show()
  }

  private def genFileSystemView(): Pane = {
    val fsRoot = new GridPane()
    fsRoot.setId("fsGrid")

    fsRoot.add(newBoldText("Local Filesystem"), 0, 0)
    fsRoot.add(newBoldText("Remote Filesystem"), 1, 0)
    localFs = genLocalFs()
    localFs.setMinSize(370, 300)
    remoteFs = genRemoteFs()
    remoteFs.setMinSize(370, 300)

    fsRoot.add(localFs, 0, 1)
    fsRoot.add(remoteFs, 1, 1)
    return fsRoot
  }

  private def newBoldText(s: String): Text = {
    val text = new Text(s)
    text.setId("bold-text")
    return text
  }

  private def genLocalFs(): TreeView[Path] = {
    val next = Paths.get(System.getProperty("user.home"))
    val root = ViewFactory.newLazyView(next)
    val view = new TreeView[Path](root)
    view.setCellFactory(CheckBoxTreeCell.forTreeView())
    return view
  }

  private def genRemoteFs(): TreeView[Path] = {
    val tree = new TreeView[Path](new CheckBoxTreeItem[Path](Paths.get("Not Connected.")))
    tree.setCellFactory(CheckBoxTreeCell.forTreeView())
    return tree
  }

  /**
   * Generates the new initialized remote-view.
   */
  private def genRemoteFs(dir: String, content: List[FileDescriptor]) =
    remoteFs.setRoot(ViewFactory.newSubView(dir, content))

  /*
   * ------------- EventHandlers -------------------- 
   * Each button gets an own function
   * -----------------------------------------------
   */

  private def connect() = {
    val servername = txtServer.getText
    val port = txtPort.getText.toInt
    val username = txtUsername.getText
    val password = txtPassword.getText
    var userDir = List[FileDescriptor]()
    var actualDir = ""

    if (servername.isEmpty() || txtPort.getText.isEmpty()) receiver.error("Specify Server & Port.")
    else {
      try {
        ftpClient = ClientFactory.newBaseClient(servername, port, receiver)
        ftpClient.connect(username, password)
        actualDir = ftpClient.pwd()
        userDir = ftpClient.ls()
        genRemoteFs(actualDir, userDir)
      } catch {
        case ex: Throwable => handleException(ex)
      }
    }

  } //connect

  /**
   * Handler for exceptions
   */
  private def handleException(e: Throwable) = {
    /**
     * TODO implement wright error handling..
     *  --> right it to the log and informate the user.
     */
    e match {
      case (_: java.net.ConnectException | _: java.net.SocketException) => receiver.error(e.getMessage)
      case ex: java.net.UnknownHostException => receiver.error("Unknown Host: " + txtServer.getText)
      case _ => receiver.error(e.toString)
    }
  }

  /*Handler for the logs*/
  private class ReceiveHandler extends Receivable {
    def error(msg: String): Unit = {
      txaLog.appendText(s"ERROR: $msg")
      /*
       * TODO show an error-box when they released with jdk8_40..
       * => march 2015
       * alternative implement them on your own
       */
    }
    def newMsg(msg: String): Unit = txaLog.appendText(msg)
    def status(msg: String): Unit = txaLog.appendText(msg)
  } //class ReceiveHandler

  private def showServerInformation() = {
    ???
  }

  private def showClientInformation() = {
    //TODO show an information-dialog
    txaLoads.appendText(ftpClient.getClientInformation());
  }

  private def showAbout() = {
    ???
  }

  /**
   * Handles the file transfers.
   */
  private def shareFiles(t: Transfer, view: TreeView[Path]) = {
    t match {
      case Upload => {
        println("Upload")
      }
      case Download => {
        println("Download")
      }
    }
  }
}

object FtpGui {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[FtpGui], args: _*)
  }
}