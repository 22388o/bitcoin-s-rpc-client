package org.bitcoins.rpc.util

import java.io.PrintWriter

import akka.http.scaladsl.model.Uri
import org.bitcoins.core.config.RegTest
import org.bitcoins.rpc.RPCClient
import org.bitcoins.rpc.auth.AuthCredentials
import org.bitcoins.rpc.config.DaemonInstance

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by chris on 5/2/17.
  */
trait TestUtil {

  def randomDirName: String = 0.until(5).map(_ => scala.util.Random.alphanumeric.head).mkString

  /** Creates a datadir and places the username/password combo
    * in the bitcoin.conf in the datadir */
  def authCredentials(uri: Uri, rpcUri: Uri): AuthCredentials  = {
    val d = "/tmp/" + randomDirName
    val f = new java.io.File(d)
    f.mkdir()
    val conf = new java.io.File(f.getAbsolutePath + "/bitcoin.conf")
    conf.createNewFile()
    val username = "random_user_name"
    val pass = randomDirName
    val pw = new PrintWriter(conf)
    pw.write("rpcuser=" + username + "\n")
    pw.write("rpcpassword=" + pass + "\n")
    pw.write("rpcbind=" + rpcUri.toString + "\n")
    pw.write("rpcport=" + rpcUri.effectivePort + "\n")
    pw.write("port=" + uri.effectivePort + "\n")
    pw.write("listen=1\n")
    pw.close()
    AuthCredentials(username,pass,d)
  }

  lazy val network = RegTest

  /** Test instance for [[DaemonInstance]] -- this is connected to RegTest on your machine */
  def instance(port: Int, rpcPort: Int): DaemonInstance = {
    val uri = Uri("http://localhost:" + port)
    val rpcUri = Uri("http://localhost:" + rpcPort)
    DaemonInstance(network,uri,rpcUri,authCredentials(uri,rpcUri))
  }

  def startNodes(clients: Seq[RPCClient]): Unit = {
    clients.map(_.start)
    Thread.sleep(45000)
  }

  def stopNodes(clients: Seq[RPCClient])(implicit ec: ExecutionContext): Future[Unit] = {
    Future.sequence(clients.map(_.stop)).map(_.head)
  }
}

object TestUtil extends TestUtil
