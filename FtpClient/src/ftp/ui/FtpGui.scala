package ftp.ui

import java.nio.file.{ Path, Paths }

import javafx.application.{ Application, Platform }
import javafx.collections.{ FXCollections, ObservableList }
import javafx.event.ActionEvent
import javafx.geometry.Side
import javafx.scene.Scene
import javafx.scene.control.{ Button, CheckBox, ComboBox, Menu, MenuBar, MenuItem, PasswordField, SelectionMode, Tab, TabPane, TextArea, TextField, TreeItem, TreeView }
import javafx.scene.input.{ KeyCode, KeyEvent }
import javafx.scene.layout.{ BorderPane, GridPane, HBox, Pane, VBox }
import javafx.scene.text.Text
import javafx.stage.{ DirectoryChooser, Stage }

import scala.collection.JavaConversions.asScalaBuffer

import ftp.client.{ ClientFactory, FtpClient }
import ftp.client.filesystem.{ FileDescriptor, RemoteFile, WrappedPath }
import ftp.client.sharemanager.{ Download, Exit, TransferManager, Upload }
import ftp.response.MessageHandler
import ftp.ui.FxEventHandlerImplicits.{ actionEvent2EventHandler, keyEvent2EventHandler }
import ftp.ui.errorhandle.{ ErrorHandle, ExceptionHandler }
import ftp.ui.listeners.RemoteItemChangeListener
import ftp.util.ConfigObj
import ftp.util.ImplicitConversions.funToRunnable

/**
 * Used for the FX-GUI.
 */
class FtpGui extends Application {
  private var ftpClient: FtpClient = null
  private val receiver: MessageHandler = new ReceiveHandler
  private val exh: ErrorHandle = new ExceptionHandler(receiver)
  private var primaryStage: Stage = null
  //menue
  private val menueBar = new MenuBar()
  private val fileMenue = new Menu(lang("file-menue"))
  private val helpMenue = new Menu(lang("help-menue"))

  //Connection
  private val txtServer = new TextField()
  private val txtPort = new TextField("21")
  private val txtUsername = new TextField()
  private val txtPassword = new PasswordField()
  private val btnConnect = new Button(lang("connect-btn"))
  private val btnDisconnect = new Button(lang("disconnect-btn"))
  private val cbAnon = new CheckBox(lang("anonymous-login"))
  //Logs
  private val txaLog = new TextArea()
  private val txaLoads = new TextArea()
  private val tabLog = new Tab(lang("log-tab"))
  private val tabLoads = new Tab(lang("loads-tab"))

  //Filesystems
  private var localFs: TreeView[WrappedPath] = null
  private var remoteFs: TreeView[FileDescriptor] = null

  //Down-/Uploads
  //added in genFileSystemView() together with the download-directory-chooser
  private val btnUpload = new Button(lang("upload-btn"))
  private val btnDownload = new Button(lang("download-btn"))
  private val btnChangeDownloadDir = new Button(lang("download-choose-entry"))
  //transfermanager for the up-/downloads
  private var trManager: TransferManager = null
  //Download-directory
  private val downloadDir = new ComboBox[Path]()

  /**
   * Gets the specified config-value from the config-object.
   * @param the key for the value
   * @return the value or "not defined"
   */
  private def conf(key: String): String = ConfigObj.getC(key) match {
    case Some(x) => x
    case None =>
      receiver.status(s"The config value for: $key doesn't exist")
      "not defined"
  }

  /**
   * Gets the specified language-value from the config-object.
   * @param the key for the value
   * @return the value or "not defined"
   */
  private def lang(key: String): String = ConfigObj.getL(key) match {
    case Some(x) => x
    case None =>
      receiver.status(s"The language value for: $key doesn't exist")
      "not defined"
  }

  override def start(primStage: Stage) = {
    primaryStage = primStage
    val vboxContainer = new VBox()
    val root = new BorderPane()
    root.setId("rootPane")
    val top = new GridPane()
    top.setId("topGrid")
    root.setTop(top)
    val scene = new Scene(vboxContainer, 800, 700)
    scene.getStylesheets().add(ConfigObj.getCss())

    //Menues
    //File menue
    val chLocalMnItem = new MenuItem(lang("local-root"))
    val chRemoteMnItem = new MenuItem(lang("remote-root"))
    val exitMnItem = new MenuItem(lang("exit"))
    //changes the local root view
    chLocalMnItem.setOnAction((ev: ActionEvent) => changeLocalRootDir())
    //changes the remote's root directory
    chRemoteMnItem.setOnAction((ev: ActionEvent) => changeRemoteRootDir())
    exitMnItem.setOnAction((ev: ActionEvent) => primStage.close())
    fileMenue.getItems.addAll(chLocalMnItem, chRemoteMnItem, exitMnItem)

    //Help menue
    val clientInfoMnItem = new MenuItem(lang("client-information-item"))
    val serverInfoMnItem = new MenuItem(lang("server-information-item"))
    val aboutInfoMnItem = new MenuItem(lang("about-item"))
    clientInfoMnItem.setOnAction((ev: ActionEvent) => showClientInformation())
    serverInfoMnItem.setOnAction((ev: ActionEvent) => showServerInformation())
    aboutInfoMnItem.setOnAction((ev: ActionEvent) => showAbout())
    helpMenue.getItems.addAll(clientInfoMnItem, serverInfoMnItem, aboutInfoMnItem)

    //Add menues to the menuebar, add the menuebar
    menueBar.getMenus.addAll(fileMenue, helpMenue)

    btnConnect.setId("connect-btn")
    btnConnect.setOnAction((ev: ActionEvent) => connect())
    btnDisconnect.setId("disconnect-btn")
    btnDisconnect.setOnAction((ev: ActionEvent) => disconnect())
    btnUpload.setOnAction((ev: ActionEvent) => shareFiles(ev))
    btnDownload.setOnAction((ev: ActionEvent) => shareFiles(ev))
    btnUpload.setId("upload-btn")
    btnDownload.setId("download-btn")

    txtPort.setMaxWidth(50)
    txtPassword.setOnKeyPressed((ev: KeyEvent) => if (ev.getCode == KeyCode.ENTER) connect())

    cbAnon.setSelected(false)
    //handler for tick-untick "login anonnymous
    cbAnon.setOnAction((ev: ActionEvent) => if (cbAnon.isSelected) {
      txtUsername.setDisable(true)
      txtPassword.setDisable(true)
    } else {
      txtUsername.setDisable(false)
      txtPassword.setDisable(false)
    })

    top.add(newBoldText(lang("servername")), 0, 0)
    top.add(txtServer, 1, 0)
    top.add(newBoldText(lang("port")), 2, 0)
    top.add(txtPort, 3, 0)
    top.add(newBoldText(lang("username")), 0, 1)
    top.add(txtUsername, 1, 1)
    top.add(newBoldText(lang("password")), 2, 1)
    top.add(txtPassword, 3, 1)
    top.add(btnConnect, 4, 1)
    top.add(btnDisconnect, 4, 0)
    top.add(cbAnon, 5, 1)

    root.setCenter(genFileSystemView())

    //log area
    val pane = new TabPane();
    pane.setId("bottomPane")
    pane.setSide(Side.BOTTOM)
    txaLoads.setEditable(false)
    txaLoads.setEditable(false)
    tabLoads.setContent(txaLoads)
    tabLog.setContent(txaLog)
    tabLoads.setClosable(false)
    tabLog.setClosable(false)
    pane.getTabs.addAll(tabLoads, tabLog)

    root.setBottom(pane)
    vboxContainer.getChildren.addAll(menueBar, root)
    primStage.setTitle(conf("software-name"))
    primStage.setScene(scene)
    primStage.sizeToScene()
    primStage.show()
  }
  /**
   * Method invoked when the last window is closed or the application is stopped.
   */
  override def stop() = {
    disconnect()
  }

  /**
   * Generates the centered panels with the local and remote filesystem-treeviews.
   *
   * Also adds:
   *  <li>the directory-chooser for the downloads</li>
   *  <li>the upload-button</li>
   *  <li>the download-button</li>
   */
  private def genFileSystemView(): Pane = {
    val fsRoot = new GridPane()
    fsRoot.setId("fsGrid")

    fsRoot.add(newBoldText(lang("local-filesystem-title")), 0, 0)
    fsRoot.add(newBoldText(lang("remote-filesystem-title")), 1, 0)
    localFs = genLocalFs()
    localFs.setMinSize(370, 300)
    remoteFs = genRemoteFs()
    remoteFs.setMinSize(370, 300)

    fsRoot.add(localFs, 0, 1)
    fsRoot.add(remoteFs, 1, 1)

    //download directory
    val downloadPane = new HBox()
    val l: ObservableList[Path] = FXCollections.observableArrayList(Paths.get(conf("download-dir")), Paths.get(conf("local-start-dir")));
    downloadPane.setId("downloadPane")
    downloadDir.setItems(l)
    downloadDir.getSelectionModel().selectFirst()
    downloadDir.setMinWidth(150)
    //handler for showing the directory-chooser
    btnChangeDownloadDir.setOnAction((ev: ActionEvent) => {
      val chooser = new DirectoryChooser()
      chooser.setTitle(lang("download-chooser-title"))
      val file = chooser.showDialog(primaryStage)
      if (file != null) {
        val path = file.toPath()
        downloadDir.getItems.add(0, path)
        downloadDir.getSelectionModel().selectFirst()
      }
    })

    downloadPane.getChildren.addAll(newBoldText(lang("download-dir")),
      downloadDir, btnChangeDownloadDir, btnUpload, btnDownload)

    //only needed for setup the download-directory below the fs-view
    val root = new VBox()
    root.setId("centeredView")
    root.getChildren.addAll(fsRoot, downloadPane)
    return root
  }

  private def newBoldText(s: String): Text = {
    val text = new Text(s)
    text.setId("bold-text")
    return text
  }

  /**
   * Generates the view for the local filesystem.
   *
   * This method uses the factory for generating the view.
   */
  private def genLocalFs(): TreeView[WrappedPath] = {
    val next = Paths.get(conf("local-start-dir"))
    val root = ViewFactory.newLazyView(next)
    val view = new TreeView[WrappedPath](root)

    view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    return view
  }

  /**
   * Generates the standard-view for the remote filesystem.
   *
   * <li>This method is normally only used at start-time for generating a TreeView.</li>
   * <li>This method uses the factory for generating the view.</li>
   */
  private def genRemoteFs(): TreeView[FileDescriptor] = {
    val tree = new TreeView[FileDescriptor](new TreeItem[FileDescriptor](new RemoteFile(lang("default-remote-entry"))))
    tree.setDisable(true) //set to disable because without login it's not usable
    tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    return tree
  }

  /**
   * Generates the new remote-view after login and also after changing the directorys.
   *
   * This method is used at runtime.
   */
  private def genRemoteFs(dir: String, content: List[FileDescriptor]) = {
    val root = ViewFactory.newSubView(dir, content)

    /**
     * Returns either None or Some() with the listing of the new directory.
     */
    def listRemoteFiles(parent: FileDescriptor): Option[List[FileDescriptor]] =
      ftpClient match {
        case null => None
        case _    => ftpClient.cd(parent.getAbsoluteFilename()); Some(ftpClient.ls())
      }

    val listener = new RemoteItemChangeListener(listRemoteFiles)

    //add EventHalders to all child's that aren't leafs ( subRoots are folders ;) )
    root.getChildren.filter(!_.isLeaf()).foreach { x => x.expandedProperty().addListener(listener) }

    remoteFs.setDisable(false) //activate because now it's usable
    remoteFs.setRoot(root)
  }

  /*
   * ------------- EventHandlers --------------------
   * -----------------------------------------------
   */

  /**
   * Connects the client by using the top-textfields.
   */
  private def connect() = {
    val servername = txtServer.getText
    val port = txtPort.getText.toInt
    var username = new String()
    var password = new String()
    var userDir = List[FileDescriptor]()
    var actualDir = new String()
    //anonymous login
    if (cbAnon.isSelected()) {
      username = conf("anon-username")
      password = conf("anon-password")
    } else { //authenticated login
      username = txtUsername.getText
      password = txtPassword.getText
    }

    if (servername.isEmpty() || txtPort.getText.isEmpty()) receiver.error("Specify Server & Port.")
    else if (username.isEmpty() || password.isEmpty()) receiver.error("Specify username/password.")
    else {
      exh.catching {
        ftpClient = ClientFactory.newBaseClient(servername, port, receiver)
        if (ftpClient.connect(username, password)) {
          actualDir = ftpClient.pwd()
          userDir = ftpClient.ls()
          genRemoteFs(actualDir, userDir)
          //setup the transfer-manager
          if (trManager != null) trManager ! Exit()
          trManager = new TransferManager(ftpClient, receiver, exh)
          trManager.start()
        }
      }
    }

  } //connect

  /**
   * Disconnects the client and resets the object.
   * (Also stops the transfer-actor.)
   */
  private def disconnect() = {
    if (trManager != null)
      trManager ! Exit() //stop the actor

    if (ftpClient != null) {
      ftpClient.disconnect()
      ftpClient = null
    }

    remoteFs.setRoot(new TreeItem[FileDescriptor](new RemoteFile(lang("default-remote-entry"))))
    remoteFs.setDisable(true)
  }

  /**
   * Changes the local root dir.
   */
  private def changeLocalRootDir() = {
    val chooser = new DirectoryChooser()
    chooser.setTitle(lang("local-root-chooser-title"))

    val file = chooser.showDialog(primaryStage)
    if (file != null) {
      val path = file.toPath()
      localFs.setRoot(ViewFactory.newLazyView(path))
    }
  }

  /**
   * Changes the remote root dir.
   */
  private def changeRemoteRootDir() = if (ftpClient != null) {
    //show input-dialog and set the root
    val dialog = ViewFactory.newChangeRemoteRootDialog()
    val optResult = dialog.showAndWait()
    if (optResult.isPresent) {
      val path = optResult.get
      ftpClient.cd(path)
      val content = ftpClient.list()
      genRemoteFs(path, content)
    }
  } else receiver.error("Please connect to the server first.")

  private def showServerInformation() = if (ftpClient != null) {
    val infos = ftpClient.getServerInformationAsMap()
    val dialog = ViewFactory.newSystemsInfo(lang("server-information-title"), lang("server-information-header"), lang("server-information-content"), infos)
    dialog.showAndWait()
  } else receiver.error("Please connect to the server first!")

  private def showClientInformation() = if (ftpClient != null) {
    val infos = ftpClient.getClientInformationAsMap()
    val dialog = ViewFactory.newSystemsInfo(lang("client-information-title"), lang("client-information-header"), lang("client-information-content"), infos)
    dialog.showAndWait()
  } else receiver.error("Please connect to the server first!")

  private def showAbout() = {
    ???
  }

  /**
   * Handles the file transfers.
   */
  private def shareFiles(ev: ActionEvent) = if (ev.getSource == btnUpload && ftpClient != null) {
    val selectedElements = this.localFs.getSelectionModel.getSelectedItems.map(_.getValue.path).toList

    trManager ! Upload(selectedElements)
  } else if (ev.getSource == btnDownload && ftpClient != null) {
    val selectedElements = this.remoteFs.getSelectionModel.getSelectedItems.map(_.getValue).toList
    //get the active element from the download-ComboBox
    //transform it into an absolute path
    val destination = downloadDir.getSelectionModel.getSelectedItem.toAbsolutePath()

    trManager ! Download(selectedElements, destination.toString())
  } else receiver.error("Please connect to the server before starting a transfer.")

  /**
   * Observer-/Handler for the logs.
   */
  private class ReceiveHandler extends MessageHandler {
    /*
     * Implementation-info:
     * All methods need to modify UI-Components,
     * so every method-body is executed inside the JavaFX-EDT
     */

    def error(msg: String): Unit = Platform.runLater(() => {
      txaLog.appendText(s"ERROR: $msg\n")
      tabLog.getTabPane.getSelectionModel.select(tabLog)

      val dialog = ViewFactory.newErrorDialog(msg = msg)
      //cause runnables can't return values.. java... -.-
      val opt = dialog.showAndWait()
    })
    def newMsg(msg: String): Unit = Platform.runLater(() => txaLog.appendText(msg + "\n"))
    def status(msg: String): Unit = Platform.runLater(() => {
      //text starts with Upload/Download, so append it to the transferlog
      if (msg.startsWith("Download") || msg.startsWith("Upload:")) txaLoads.appendText(msg + "\n")
      else txaLog.appendText(msg + "\n")
    })

    def newException(ex: Exception): Unit = Platform.runLater(() => {
      txaLog.appendText("Exception occured: " + ex.toString)
      tabLog.getTabPane.getSelectionModel.select(tabLog)

      val dialog = ViewFactory.newExceptionDialog(msg = "You found a bug.", ex = ex)
      //cause runnables can't return values.. java... -.-
      val opt = dialog.showAndWait()
    })
  } //class ReceiveHandler
}

object FtpGui {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[FtpGui], args: _*)
  }
}
