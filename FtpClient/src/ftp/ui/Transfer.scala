package ftp.ui

/**
 * Defines either upload or download.
 */
private[ui] sealed trait Transfer {

}

/**
 * Defines upload transfer.
 */
private[ui] object Upload extends Transfer {
  
}
/**
 * Defines download transfer.
 */
private[ui] object Download extends Transfer {
  
}