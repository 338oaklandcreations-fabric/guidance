/*

    Copyright (C) 2016 Mauricio Bustos (m@bustos.org) & 338.oakland creations

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package com._338oaklandcreations.fabric.guidance

import javax.net.ssl.{X509TrustManager, SSLContext}
import java.security.cert.X509Certificate

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.LoggerFactory
import spray.can.Http
import spray.http._
import spray.httpx.ResponseTransformation._
import spray.io.ClientSSLEngineProvider

import scala.concurrent._
import scala.concurrent.duration._

object MachineryAPI {
  val CallTimeout = 10 seconds
}

class MachineryAPI extends Actor with ActorLogging {

  import MachineryAPI._

  implicit val system = ActorSystem()
  implicit val timeout: Timeout = Timeout(5 minutes)

  val logger = LoggerFactory.getLogger(getClass)

  implicit val trustfulSslContext: SSLContext = {
    // Used to ignore self-signed certificate acceptance issues
    class IgnoreX509TrustManager extends X509TrustManager {
      def checkClientTrusted(chain: Array[X509Certificate], authType: String) = {}
      def checkServerTrusted(chain: Array[X509Certificate], authType: String) = {}
      def getAcceptedIssuers = null
    }
    val context = SSLContext.getInstance("TLS")
    context.init(null, Array(new IgnoreX509TrustManager), null)
    context
  }

  implicit val clientSSLEngineProvider =
    ClientSSLEngineProvider {
      _ =>
        val engine = trustfulSslContext.createSSLEngine()
        engine.setUseClientMode(true)
        engine
    }

  val backendServerPort = scala.util.Properties.envOrElse("FABRIC_MACHINERY_URL_PORT", "8110").toInt
  val backendHost = scala.util.Properties.envOrElse("FABRIC_MACHINERY_HOST", "localhost")
  val backendServer = "https://" + backendHost + ":" + backendServerPort

  val hostConnector = Await.result (IO(Http) ? Http.HostConnectorSetup(backendHost, port=backendServerPort, sslEncryption = true), CallTimeout) match {
    case Http.HostConnectorInfo(hostConnector, _) => hostConnector
  }

  def receive = {
    case x: HttpRequest =>
      logger.debug ("Request: " + x.method + " " + x.uri.path.toString + " " + x.entity.data)
      val request = HttpRequest(x.method, backendServer + x.uri.path.toString, x.headers.filter(_.name == "Cookie"), entity = x.entity)
      val response = Await.result ((hostConnector ? request).mapTo[HttpResponse], CallTimeout) ~> unmarshal[String]
      sender ! response
      logger.debug ("Response: " + response.getClass + " " + response.length)
    case x => sender ! "UNKNOWN REQUEST TYPE: " + x.toString
  }
}
