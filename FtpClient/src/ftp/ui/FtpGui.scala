package ftp.ui

import javafx.application.Application
import javafx.geometry.Insets
import javafx.stage.Stage
import javafx.scene._
import javafx.scene.layout._
import javafx.scene.text._
import javafx.scene.control._
import javafx.event._
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.FileSystems
import java.io.File
import ftp.client.FtpClient
import ftp.client.ClientFactory
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.JavaConverters._


class FtpGui extends Application {
  private var ftpClient : FtpClient = null;
  private val txtServer = new TextField()
  private val txtPort = new TextField("21")
  private val txtUsername = new TextField()
  private val txtPassword = new PasswordField()
  /*
   //TODO this 2 val's will hold the filesystems, like the treenodes in swing
  private val localFs
  private val remoteFs
  */
  
  override def start(primStage: Stage) = {
    val root = new BorderPane()
    root.setPadding(new Insets(10,15,25,15))
    val top = new GridPane()
    top.setHgap(10)
    top.setVgap(10)
    root.setTop(top)
    val scene = new Scene(root, 600, 400)
    scene.getStylesheets().add(getClass.getResource("style/FtpGui.css").toExternalForm())
    val btnConnect = new Button("Connect")
    btnConnect.setId("green")
    
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
    
    root.setCenter(genFileSystemView)
    
    primStage.setTitle("NJ's FTP")
    primStage.setScene(scene)
    primStage.show()
  }
  
  private def genFileSystemView : Pane = {
    val fsRoot = new GridPane()
    fsRoot.add(newBoldText("Local Filesystem"), 0, 0)
    fsRoot.add(genLocalFs(), 0, 1)
    
    return fsRoot
    
  } 

  private def newBoldText(s: String): Text = {
    val text = new Text(s)
    text.setId("bold-text")
    return text
  }

  private def genLocalFs() : TreeView[Path] = {
      //lists all root entry points from the filesystem
    //val rootEntrys = FileSystems.getDefault().getRootDirectories()
    /*
    
    def genSubDirs(r : TreeItem[Path]) : TreeItem[Path] = {
      println(r.getValue)
      val attrs = Files.readAttributes(r.getValue, classOf[BasicFileAttributes] )
      if(attrs.isDirectory()) {
        
        r.getValue.asScala.foreach { child => 
          val childItem = new TreeItem[Path](child)
          
          genSubDirs(childItem)
          r.getChildren().add(childItem)
        }        
        return r;
      }else return r;
    }
    
    */
    val next = Paths.get(System.getProperty("user.home"))
    println(next)
     val root = new TreeItem[Path](next)
   
    return new TreeView[Path](root)
  }
}

object FtpGui {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[FtpGui], args: _*)
  }
}