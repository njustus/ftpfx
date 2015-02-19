package ftp.client

import ftp.response.Receivable

class Log(private val rc: Receivable) {

  private var lastResult: Boolean = false;

  def newMsg(line: String, x: String => Boolean): Unit = {
    lastResult = x.apply(line)
    if (lastResult) {
      rc.status(line)
    } else rc.error(line)
  }

  def newMsg(line: String): Unit = {
    newMsg(line, x => x != null)
  }

  def newError(line: String): Unit = {
    newMsg(line, x => x == null)
  }

  def getLastResult: Boolean = { return lastResult; }
}