package ftp.client

import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket
import java.util.Scanner

import ftp.response.Receivable

/**
 * This factory is used for creating Ftp-Clients.
 * All methods returning an Object with the type FtpClient.
 */
object ClientFactory {
  /**
   * Creates a simple base client for standard usage.
   * It uses the given servername and port for connecting to the server.
   * The receivable-object gets all incoming (may be error) messages.
   *
   * @param serverName the servername or the ip address
   * @param port the portnumber for the control socket
   * @param rc the receivable object which acceppts and interogates with all messages
   * @throws IOException something went wrong while creating the objects for input-/output streams
   * @throws SocketException something went wrong while connecting the socket
   */
  def newBaseClient(serverName: String, port: Int, rc: Receivable): FtpClient = {
    var sckt = new Socket(InetAddress.getByName(serverName), port)
    var scanner = new Scanner(sckt.getInputStream)
    var writer = new PrintWriter(sckt.getOutputStream, true)

    rc.status("Socket connect: " + scanner.nextLine)
    return new BaseClient(sckt, writer, scanner, rc)
  }

  /**
   * Creates a new simple base client for standard usage.
   * This method uses the <B>standard control port (21)</B> for the control socket.
   *
   * @implnode This method simply calls newBaseClient(serverName, 21, rc)
   *
   * @param serverName the servername or the ip address
   * @param rc the receivable object which acceppts and interogates with all messages
   * @throws IOException something went wrong while creating the objects for input-/output streams
   * @throws SocketException something went wrong while connecting the socket
   */
  def newBaseClient(serverName: String, rc: Receivable): FtpClient = newBaseClient(serverName, 21, rc)
}