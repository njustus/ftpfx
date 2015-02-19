package ftp.client

import ftp.response.Receivable

/**
 * This class generates the logs for the Ftp-connection.
 */
class Log(private val rc: Receivable) {

  private var lastResult: Boolean = false;

  /**
   * Gets a new message and either writes a status- or error message depending on the test from x.
   * @param line the Message
   * @param x the coressponding test
   */
  def newMsg(line: String, x: String => Boolean): Unit = {
    lastResult = x.apply(line)
    if (lastResult) {
      rc.status(line)
    } else rc.error(line)
  }

  /**
   * Writes new status-messages.
   * This method is always writing status!
   * @param line the Message
   */
  def newMsg(line: String): Unit = {
    newMsg(line, x => true)
  }

  /**
   * Writes new error-messages.
   * This method is always writing errors!
   * @param line the Message
   */
  def newError(line: String): Unit = {
    newMsg(line, x => false)
  }

  /**
   * Returns the result of the last executed test.
   */
  def getLastResult: Boolean = return lastResult;
}