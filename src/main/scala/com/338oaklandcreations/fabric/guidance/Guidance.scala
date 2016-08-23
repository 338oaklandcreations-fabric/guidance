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

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.io.Tcp.CommandFailed
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Properties.envOrElse

object Guidance extends App {

  def doMain = {

    implicit val system = ActorSystem("guidanceSystem")
    implicit val timeout = Timeout(DurationInt(30).seconds)

    val config = ConfigFactory.load
    val port = envOrElse("PORT", config.getString("server.port"))

    val server = system.actorOf(Props[GuidanceServiceActor], "guidanceServiceActor")

    val bindResult = {
      if (args.length > 0) IO(Http) ? Http.Bind(server, "0.0.0.0", args(0).toInt)
      else IO(Http) ? Http.Bind(server, "0.0.0.0", port.toInt)
    }
    println("test")
    Await.result(bindResult, timeout.duration) match {
      case CommandFailed(cmd) => System.exit(1)
      case _ =>
    }
  }

  doMain

}
