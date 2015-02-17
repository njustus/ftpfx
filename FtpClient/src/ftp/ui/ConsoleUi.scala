package ftp.ui

import ftp.client.BaseClient
import ftp.client.ClientFactory
import ftp.response.ConsoleReceiver

import java.util._
import java.net._
import java.io._

/**
 * Tests for the ftpclient.
 */
object ConsoleUi {

  def main(args: Array[String]): Unit = {
    val rec = new ConsoleReceiver
    val bs = ClientFactory.newBaseClient("localhost", rec)
    bs.connect("nico", "f3dora")
    bs.cd("/home/nico/Downloads")
    bs.pwd()

    //bs.sendFile("/home/nico/Bilder/TLOU/Ellie-Winter-Yuiphone-The-Last-Of-Us-1920x1080-HD-Wallpapers.jpg")
    bs.ls
    //bs.receiveFile("/home/nico/Downloads/muster-lebenslauf.pdf", "/home/nico/Downloads/muster-lebenslauf.pdf")
    bs.disconnect()
  }
}