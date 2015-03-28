package ftp.response

trait MessageHandler extends Receivable {
  /**
   * Is called if an exception occured that can't be matched to any specified error-handling
   *
   * @param ex the exception that occured
   */
  def newException(ex: Exception): Unit
}
