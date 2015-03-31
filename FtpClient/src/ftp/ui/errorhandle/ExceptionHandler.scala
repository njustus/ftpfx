package ftp.ui.errorhandle

import ftp.response.Receivable
import ftp.response.MessageHandler

/**
 * Objects from this class can handle exceptions.
 */
class ExceptionHandler(val receiver: MessageHandler) extends ErrorHandle {
  override def catching[A](f: => A): Option[A] = {
    try {
      val result: A = f
      return Some(result)
    } catch {
      case ex @ (_: java.net.ConnectException | _: java.net.SocketException) => receiver.error("Can't connect to the server. \n Verify servername & port.")
      case ex: java.net.UnknownHostException                                 => receiver.error("Can't find the server. \n Unknown hostname or ip-address.")
      case ex: java.io.FileNotFoundException                                 => receiver.error("Can't access file: \n" + ex.getMessage)
      case ex: Exception =>
        receiver.newException(ex);
        None
    }
    return None
  }
}
