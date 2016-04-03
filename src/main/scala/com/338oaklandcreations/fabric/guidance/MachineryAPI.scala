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

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.LoggerFactory
import spray.can.Http
import spray.http._
import spray.httpx.ResponseTransformation._

import scala.concurrent._
import scala.concurrent.duration._

class MachineryAPI extends Actor with ActorLogging {

  implicit val system = ActorSystem()
  implicit val timeout: Timeout = Timeout(5 minutes)

  val logger = LoggerFactory.getLogger(getClass)

  val backendServer = {
    scala.util.Properties.envOrElse("FABRIC_MACHINERY_URL", "http://localhost:8111")
  }

  def receive = {
    case x: HttpRequest =>
      logger.info (x.method + " " + x.uri.path.toString + " " + x.entity.data)
      val request = HttpRequest(x.method, backendServer + x.uri.path.toString, entity = x.entity)
      val response = Await.result ((IO(Http) ? request).mapTo[HttpResponse], 3 seconds) ~> unmarshal[String]
      sender ! response
      logger.info ("Response: " + response.getClass + " " + response.length)
    case x => sender ! "UNKNOWN REQUEST TYPE: " + x.toString
  }
}
