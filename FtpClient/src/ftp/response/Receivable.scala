package ftp.response

/**
 * This trait describes methods that a receiver for the ftp-client needs.
 */
trait Receivable {
  /**
   * Gets a new message from the server.
   * That's like the actual working directory.
   */
  def newMsg(msg : String) : Unit;
  /**
   * Gets status-infomrations from the server.
   * @param msg the status-message
   */
  def status(msg : String) : Unit;
  /**
   * Gets <B>error</B>-messages from the server.
   * If this method is used, the messages are critical. The best way of handling this is to close the connection.
   */
  def error(msg : String) : Unit;
}