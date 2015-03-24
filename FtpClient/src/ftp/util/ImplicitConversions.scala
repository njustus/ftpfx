package ftp.util

object ImplicitConversions {
  implicit def funToRunnable(fun: () => Unit) = new Runnable() { def run() = fun() }
}
