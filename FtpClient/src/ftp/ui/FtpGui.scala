package ftp.ui

import java.io.File
import ftp.client.ClientFactory
import ftp.client.FtpClient
import ftp.response.Receivable
import ftp.ui.FxEventHandlerImplicits._
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.scene.control.CheckBox
import javafx.scene.control.TextArea
import javafx.scene.control.TabPane
import javafx.scene.control.Tab
import javafx.geometry.Side
import javafx.scene.input.MouseEvent


class FtpGui extends Application with EventHandler[ActionEvent]{
  private var ftpClient : FtpClient = null
  private val receiver : Receivable = new ReceiveHandler
  
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
  private var localFs : TreeView[File] = null
  private var remoteFs : TreeView[File] = null

  
  override def start(primStage: Stage) = {
    val root = new BorderPane()
    root.setId("rootPane")
    //root.setPadding(new Insets(10,15,25,15))
    val top = new GridPane()
    top.setId("topGrid")
    root.setTop(top)
    val scene = new Scene(root, 800, 600)
    scene.getStylesheets().add(getClass.getResource("style/FtpGui.css").toExternalForm())
    
    btnConnect.setId("green")
    btnConnect.setOnAction(this)
    btnDisconnect.setId("red")
    btnDisconnect.setOnAction( (ev:ActionEvent) => if(ftpClient != null) ftpClient.disconnect() )

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
    top.add(btnDisconnect, 4,0)
    
    root.setCenter(genFileSystemView() )
    
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
    
    primStage.setTitle("NJ's FTP")
    primStage.setScene(scene)
    primStage.show()
  }
  
  private def genFileSystemView() : Pane = {
    val fsRoot = new GridPane()
    fsRoot.setId("fsGrid")
    fsRoot.add(newBoldText("Local Filesystem"), 0, 0)
    fsRoot.add(newBoldText("Remote Filesystem"), 1, 0)
    localFs = genLocalFs()
    remoteFs = genRemoteFs()
    fsRoot.add(localFs, 0, 1)
    fsRoot.add(remoteFs, 1, 1)
    return fsRoot    
  } 

  private def newBoldText(s: String): Text = {
    val text = new Text(s)
    text.setId("bold-text")
    return text
  }  

  private def genLocalFs() : TreeView[File] = {
    val next = new File (System.getProperty("user.home"))
    val root = ViewFactory.newView(next)

    return root
  }
  
  private def genRemoteFs() : TreeView[File] = 
    new TreeView[File](new TreeItem[File](new File("Not Connected.")))

  
  /*------------- EventHandlers -------------------- */
  override def handle(ev: ActionEvent): Unit = {
    if(ev.getSource() == btnConnect) {
       val servername = txtServer.getText
       val port = txtPort.getText.toInt
       val username = txtUsername.getText
       val password = txtPassword.getText
       var userDir = List[String]()
       
       try {
       ftpClient = ClientFactory.newBaseClient(servername, port, receiver )
       ftpClient.connect(username, password)
       userDir = ftpClient.ls()
       }catch {
         case e:Throwable => handleException(e)
         case _ => receiver.error("An unknown error was thrown");
       }      
    }
  }
  
  /**
   * Handler for exceptions
   */
  private def handleException(e : Throwable) = {
    /**
     * TODO implement wright error handling..
     *  --> right it to the log and informate the user.
     */
    e.printStackTrace()
  }
  
  /*Handler for the logs*/
  private class ReceiveHandler extends Receivable {
    def error(msg: String): Unit = {
     ???
    }

    def newMsg(msg: String): Unit = {
      ???
    }

    def status(msg: String): Unit = {
     println("Status: "+msg)
    }
  }//class ReceiveHandler
}

object FtpGui {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[FtpGui], args: _*)
  }
}