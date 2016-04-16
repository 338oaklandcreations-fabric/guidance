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

import akka.actor._
import akka.pattern.ask
import org.slf4j.LoggerFactory
import spray.http.MediaTypes._
import spray.http.{DateTime, HttpCookie}
import spray.routing._

import scala.util.{Failure, Success}

class GuidanceServiceActor extends HttpServiceActor with ActorLogging {

  override def actorRefFactory = context

  val guidanceRoutes = new GuidanceRoutes {
    def actorRefFactory = context
  }

  def receive = runRoute(
    guidanceRoutes.routes ~
      get {getFromResourceDirectory("webapp")})

}

trait GuidanceRoutes extends HttpService with UserAuthentication {

  import UserAuthentication._

  val logger = LoggerFactory.getLogger(getClass)
  val system = ActorSystem("whoWonSystem")

  import system.dispatcher

  val guidanceData = system.actorOf(Props[GuidanceData], "guidanceData")
  val machineryAPI = system.actorOf(Props[MachineryAPI], "machineryAPI")

  val routes =
    mainPage ~
      getHostCPU ~
      getHostMemory ~
      getHeartbeat ~
      getHostStatistics ~
      ledPower ~
      login

  val authenticationRejection = RejectionHandler {
    case AuthenticationRejection(message) :: _ => getFromResource("webapp/login.html")
  }

  val cookies = cookie("FABRIC_GUIDANCE_SESSION") & cookie("FABRIC_GUIDANCE_USER") & respondWithMediaType(`application/json`)
  def authenticateCookies(sessionId: HttpCookie, username: HttpCookie) = authenticate(authenticateSessionId(sessionId.content, username.content))

  val keyLifespanMillis = 120000 * 1000 // 2000 minutes
  val expiration = DateTime.now + keyLifespanMillis
  val SessionKey = "FABRIC_GUIDANCE_SESSION"
  val UserKey = "FABRIC_GUIDANCE_USER"
  val ResponseTextHeader = "{\"responseText\": "
  val UnauthorizedRequestString = "Unauthorized Request"
  val UnknownCommandResponseString = ResponseTextHeader + "\"Unknown command results\"}"

  def getHostMemory = get {
    path("hostMemory") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          val future = machineryAPI ? ctx.request
          future onComplete {
            case Success(success) => success match {
              case history: String => ctx.complete(history)
              case _ => ctx.complete(400, UnknownCommandResponseString)
            }
            case Failure(failure) => ctx.complete(400, failure.toString)
          }
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def getHostCPU = get {
    path("hostCPU") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          val future = machineryAPI ? ctx.request
          future onComplete {
            case Success(success) => success match {
              case history: String => ctx.complete(history)
              case _ => ctx.complete(400, UnknownCommandResponseString)
            }
            case Failure(failure) => ctx.complete(400, failure.toString)
          }
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def getHeartbeat = get {
    path("heartbeat") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          val future = machineryAPI ? ctx.request
          future onComplete {
            case Success(success) => success match {
              case history: String => ctx.complete(history)
              case _ => ctx.complete(400, UnknownCommandResponseString)
            }
            case Failure(failure) => ctx.complete(400, failure.toString)
          }
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def getHostStatistics = get {
    path("hostStatistics") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          val future = machineryAPI ? ctx.request
          future onComplete {
            case Success(success) => success match {
              case history: String => ctx.complete(history)
              case _ => ctx.complete(400, UnknownCommandResponseString)
            }
            case Failure(failure) => ctx.complete(400, failure.toString)
          }
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def ledPower = post {
    path("ledPower" / """(on|off)""".r) { (select) =>
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          val future = machineryAPI ? ctx.request
          future onComplete {
            case Success(success) => success match {
              case history: String => ctx.complete(history)
              case _ => ctx.complete(400, UnknownCommandResponseString)
            }
            case Failure(failure) => ctx.complete(400, failure.toString)
          }
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def mainPage = get {
    path("" | "main.html") {
      cookie("FABRIC_GUIDANCE_SESSION") { sessionId =>
        cookie("FABRIC_GUIDANCE_USER") { username =>
          authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
            getFromResource("webapp/main.html")
          }
        }
      } ~ getFromResource("webapp/login.html")
    }
  }

  def login = post {
    path("login") {
      formFields('inputName, 'inputPassword) { (inputName, inputPassword) =>
        handleRejections(authenticationRejection) {
          authenticate(authenticateUser(inputName, inputPassword)) { authentication =>
            setCookie(HttpCookie(SessionKey, content = authentication.token, expires = Some(expiration))) {
              setCookie(HttpCookie(UserKey, content = inputName, expires = Some(expiration))) { ctx =>
                ctx.complete("")
              }
            }
          }
        }
      }
    }
  }

}
