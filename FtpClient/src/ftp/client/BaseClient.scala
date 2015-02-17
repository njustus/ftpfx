package ftp.client

import java.net.Socket
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.io._
import java.util.Scanner
import java.util.Arrays
import ftp.response.Receivable
import ftp.client.exceptions._
import java.util.StringTokenizer
import scala.runtime.ScalaRunTime._
import java.io.BufferedWriter
import java.net.SocketException

/**
 * Defines a simple ftpclient.
 */
class BaseClient private[client] (private val socket: Socket, private val output: PrintWriter, private val incoming: Scanner, private val receiver: Receivable) extends FtpClient {
  /*
   * Achtung passive-ports wechseln ständig.. Evtl. immer wieder neuen socket connecten
   */
  private var passiveMode: Boolean = true //true if in passive mode, false otherwise
  private var dataSocket: Socket = null //used for the data-connection
  private var dataInput: Scanner = null
  private var dataOutput: PrintWriter = null
  private var actualDir: String = "/" //holds the path to the actual directory

  /**
   * Method chaining for simply use nextLine instead of incoming.nextLine.
   */
  private def nextLine(): String = incoming.nextLine
  /**
   * Method chaining for simply use writeLine instead of output.println.
   */
  private def writeLine(msg: String): Unit = output.println(msg)
  /**
   * Connects the datasocket to an (new) serversocket
   */
  private def setupPassiveConnection(): Unit = {
    writeLine("PASV")
    var resp = nextLine

    if (!resp.startsWith("227")) receiver.error(resp)
    else receiver.status(resp)

    //response is (ip,ip,ip,ip,portSub1,portSub2) --> ip = ip.ip.ip.ip | port = portSub1*256+portSub2
    //get the ip and port with regex
    var matcher = "\\(.*\\)".r
    resp = matcher.findFirstIn(resp).get
    resp = resp.substring(1, resp.length - 1)

    //match the ip-segments & port
    val ip = resp.split(",").dropRight(2).reduce((res, current) => res + "." + current)
    val port = resp.split(",").drop(4).map(s => s.toInt).reduce((res, current) => res * 256 + current)
    receiver.status("Trying to connect to: " + ip + " on Port: " + port)

    dataSocket = new Socket(InetAddress.getByName(ip), port)
    dataInput = new Scanner(dataSocket.getInputStream)
    dataOutput = new PrintWriter(dataSocket.getOutputStream(), true)
  }

  /*
   * Closes the dataSocket and all dataStreams.
   */
  private def closeDataSocket(): Unit = {
    try {
      dataSocket.getOutputStream.flush
      dataSocket.getOutputStream.close
      dataSocket.getInputStream.close
      dataSocket.close
    } catch {
      case e: SocketException => {}
    }

    dataSocket = null
  }

  override def connect(username: String, password: String) = {
    var resp = ""
    println("USER " + username)
    writeLine("USER " + username)

    resp = nextLine
    if (!resp.startsWith("331")) receiver.error(resp)
    else receiver.status(resp)

    writeLine("PASS " + password)

    resp = nextLine
    if (!resp.startsWith("230")) {      
      receiver.error(resp)
      throw new ConnectException("Coudln't authenticate as "+username+"\nUsername or password wrong!")
    }
    else receiver.status(resp)

  }
  override def disconnect() = {
    var resp = ""
    writeLine("QUIT")

    resp = nextLine
    if (!resp.startsWith("221")){
      receiver.error(resp)
      throw new DisconnectException("Can't disconnect from the server. Try again.")
    }
    else receiver.status(resp)

    output.close()
    incoming.close()
    socket.close()
  }
  override def cd(path: String) = {
    var resp = ""
    writeLine("CWD " + path)

    resp = nextLine
    if (!resp.startsWith("250")) {
      receiver.error(resp)
      if(resp.contains("permission"))
          throw new CDException("Can't switch to "+path+" permission denied.")
      else    
        throw new CDException("Can't switch to "+path)
    }
    else {
      receiver.status(resp)
      actualDir = if (path.startsWith("/")) path else actualDir.concat("/" + path)
    }
  }
  override def ls() = {
    if (dataSocket == null)
      changeMode(false)

    writeLine("LIST")
    var respCtrl = nextLine
    if (!respCtrl.startsWith("150")) receiver.error(respCtrl)
    else receiver.status(respCtrl)

    var response = new StringBuilder()
    while (dataInput.hasNext)
      response.append(dataInput.nextLine).append("\n")

    respCtrl = nextLine
    if (!respCtrl.startsWith("226")) receiver.error(respCtrl)
    else {
      receiver.status(respCtrl)
      receiver.newMsg(response.toString)
    }

    closeDataSocket()
  }
  override def pwd() = {
    var resp = ""
    writeLine("PWD")
    resp = nextLine

    if (!resp.startsWith("257")) receiver.error(resp)
    else {
      receiver.status(resp)
      val matcher = "\\\".*\\\"".r
      var path = matcher.findFirstIn(resp).get
      path = path.substring(1, path.length - 1)
      receiver.newMsg(path)
    }
  }
  override def sendFile(filename: String) = {
    if (dataSocket == null)
      changeMode(false)

    val outputStream = new BufferedOutputStream(dataSocket.getOutputStream)
    val file = Paths.get(filename)
    val fileStream = Files.newInputStream(file)

    if (!file.toFile().exists())
      receiver.error("File " + file.toString() + " doesn't exist")

    writeLine("STOR " + file.getFileName.toString)
    var resp = nextLine

    if (!resp.startsWith("150")) receiver.error(resp)
    else receiver.status(resp)

    var buffer = new Array[Byte](BUFFER_SIZE)
    var length: Int = 0
    while (length != -1) {
      length = fileStream.read(buffer)
      outputStream.write(buffer)
    }
    outputStream.flush
    outputStream.close
    fileStream.close

    resp = nextLine
    if (!resp.startsWith("226")) {
      receiver.error(resp + "\nCan't upload " + file.getFileName.toString)
    } else {
      receiver.status(resp)
      receiver.status("Upload of " + file.getFileName.toString + " was successfull.")
    }

    closeDataSocket()
  }
  override def receiveFile(filename: String, dest: String) = {
    if (dataSocket == null)
      changeMode(false)

    val canonicalFilename = if (filename.startsWith("/")) filename else actualDir.concat("/" + filename)
    val localFile = Paths.get(dest)
    val incomingStream = dataSocket.getInputStream
    val localStream = new FileOutputStream(localFile.toFile())

    writeLine("RETR " + filename)
    var resp = nextLine
    if (!resp.startsWith("150")) receiver.error(resp)
    else receiver.status(resp)

    var length: Int = 0

    while (length != -1) {
      length = incomingStream.read()
      localStream.write(length)
    }

    localStream.flush
    localStream.close

    resp = nextLine
    if (!resp.startsWith("226")) receiver.error(resp)
    else receiver.status(resp)

    closeDataSocket()
  }
  override def getServerInformation(): String = {
    throw new NotImplementedError()
  }
  override def getClientInformation(): String = {
    throw new NotImplementedError()
  }
  override def changeMode(active: Boolean): Boolean = {
    if (active) {

    } else setupPassiveConnection()

    passiveMode = active
    return passiveMode
  }

  override def renameFile(oldPath: String, newPath: String) = {
    var op = if (oldPath.startsWith("/")) oldPath else actualDir.concat(oldPath)
    var np = if (newPath.startsWith("/")) newPath else actualDir.concat(newPath)

    /**
     * TODO test and implement errors
     */
    writeLine("RNFR " + op)
    var resp = nextLine
    if (resp.startsWith("350")) receiver.status(resp)
    else receiver.error(resp)

    writeLine("RNTO " + np)
    resp = nextLine
    if (resp.startsWith("250")) receiver.status(resp)
    else receiver.error(resp)
  }

  override def deleteFile(path: String) = {
    val p = if (path.startsWith("/")) path else actualDir.concat(path)

    /**
     * TODO test and implement errors
     */
    writeLine("DELE" + p)
    val resp = nextLine
    if (resp.startsWith("250")) receiver.status(resp)
    else receiver.error(resp)
  }

  override def deleteDir(path: String) = {
    val p = if (path.startsWith("/")) path else actualDir.concat(path)

    /**
     * TODO test and implement errors
     */
    writeLine("RMD" + p)
    val resp = nextLine
    if (resp.startsWith("250")) receiver.status(resp)
    else receiver.error(resp)
  }

  override def mkdir(path: String) = {
    val p = if (path.startsWith("/")) path else actualDir.concat(path)

    /**
     * TODO test and implement errors
     */
    writeLine("MKD" + p)
    val resp = nextLine
    if (resp.startsWith("250")) receiver.status(resp)
    else receiver.error(resp)
  }
  
  override def quit() = {
    writeLine("ABOR")
    val resp = nextLine
    /**
     * TODO test the error
     */
  }
}