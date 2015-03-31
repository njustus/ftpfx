package ftp.ui.errorhandle

/**
 * Describes error-/exception-handlers.
 */
trait ErrorHandle {
  /**
   * Trys to execute the given function and handles possible exceptions.
   *
   * If the execution fails it returns None, else it returns Some(..) with the return-value from the given function.
   *
   * @param f the function that throws possibly an exception
   * @tparam A the return-type of the given function
   * @return None if the execution fails, Some(x:[A]) otherwise
   */
  def catching[A](f: => A): Option[A]
}
