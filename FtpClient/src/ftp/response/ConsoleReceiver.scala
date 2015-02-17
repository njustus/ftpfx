package ftp.response

/**
 * This class describes a simple command-line-using receiver for the ftp-client.
 * This class uses the print-methods for printing status and messages. If an error-message is received the program is killed with System.exit()
 */
class ConsoleReceiver extends Receivable {
  override def newMsg(msg: String) : Unit = {
    printf("Message: %s\n", msg)
  }
  override def status(msg: String): Unit = {
    printf("Status: %s\n", msg)
  }
  override def error(msg: String): Unit = {
    System.err.printf("Error: %s\n", msg)
    System.exit(0)
  }
}