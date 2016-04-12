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
import spray.http.StatusCodes._
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

  import java.net.InetAddress

  import UserAuthentication._
  import GuidanceTables._
  import GuidanceJsonProtocol._

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

  val secureCookies: Boolean = {
    // Don't require HTTPS if running in development
    val hostname = InetAddress.getLocalHost.getHostName
    hostname != "localhost" && !hostname.contains("pro")
  }

  def redirectToHttps: Directive0 = {
    requestUri.flatMap { uri =>
      redirect(uri.copy(scheme = "https"), MovedPermanently)
    }
  }

  val isHttpsRequest: RequestContext => Boolean = { ctx =>
    (ctx.request.uri.scheme == "https" || ctx.request.headers.exists(h => h.is("x-forwarded-proto") && h.value == "https")) && secureCookies
  }

  def enforceHttps: Directive0 = {
    extract(isHttpsRequest).flatMap(
      if (_) pass
      else redirectToHttps
    )
  }

  val keyLifespanMillis = 120000 * 1000 // 2000 minutes
  val expiration = DateTime.now + keyLifespanMillis
  val SessionKey = "FABRIC_GUIDANCE_SESSION"
  val UserKey = "FABRIC_GUIDANCE_USER"
  val ResponseTextHeader = "{\"responseText\": "

  def getHostMemory = get {
    path("hostMemory") {
      cookie("FABRIC_GUIDANCE_SESSION") { sessionId =>
        cookie("FABRIC_GUIDANCE_USER") { username =>
          authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
            respondWithMediaType(`application/json`) { ctx =>
              val future = machineryAPI ? ctx.request
              future onComplete {
                case Success(success) => success match {
                  case history: String => ctx.complete(history)
                  case _ => ctx.complete(400, ResponseTextHeader + "\"Unknown command results\"}")
                }
                case Failure(failure) => ctx.complete(400, failure.toString)
              }
            }
          }
        }
      } ~ complete(401, "")
    }
  }

  def getHostCPU = get {
    path("hostCPU") {
      cookie("FABRIC_GUIDANCE_SESSION") { sessionId =>
        cookie("FABRIC_GUIDANCE_USER") { username =>
          authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
            respondWithMediaType(`application/json`) { ctx =>
              val future = machineryAPI ? ctx.request
              future onComplete {
                case Success(success) => success match {
                  case history: String => ctx.complete(history)
                  case _ => ctx.complete(400, ResponseTextHeader + "\"Unknown command results\"}")
                }
                case Failure(failure) => ctx.complete(400, failure.toString)
              }
            }
          }
        }
      } ~ complete(401, "")
    }
  }

  def getHeartbeat = get {
    path("heartbeat") {
      cookie("FABRIC_GUIDANCE_SESSION") { sessionId =>
        cookie("FABRIC_GUIDANCE_USER") { username =>
          authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
            respondWithMediaType(`application/json`) { ctx =>
              val future = machineryAPI ? ctx.request
              future onComplete {
                case Success(success) => success match {
                  case history: String => ctx.complete(history)
                  case _ => ctx.complete(400, ResponseTextHeader + "\"Unknown command results\"}")
                }
                case Failure(failure) => ctx.complete(400, failure.toString)
              }
            }
          }
        }
      } ~ complete(401, "")
    }
  }

  def getHostStatistics = get {
    path("hostStatistics") {
      cookie("FABRIC_GUIDANCE_SESSION") { sessionId =>
        cookie("FABRIC_GUIDANCE_USER") { username =>
          authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
            respondWithMediaType(`application/json`) { ctx =>
              val future = machineryAPI ? ctx.request
              future onComplete {
                case Success(success) => success match {
                  case history: String => ctx.complete(history)
                  case _ => ctx.complete(400, ResponseTextHeader + "\"Unknown command results\"}")
                }
                case Failure(failure) => ctx.complete(400, failure.toString)
              }
            }
          }
        }
      } ~ complete(401, "")
    }
  }

  def ledPower = post {
    path("ledPower" / """(on|off)""".r) { (select) =>
      cookie("FABRIC_GUIDANCE_SESSION") { sessionId =>
        cookie("FABRIC_GUIDANCE_USER") { username =>
          authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
            respondWithMediaType(`application/json`) { ctx =>
              val future = machineryAPI ? ctx.request
              future onComplete {
                case Success(success) => success match {
                  case history: String => ctx.complete(history)
                  case _ => ctx.complete(400, ResponseTextHeader + "\"Unknown command results\"}")
                }
                case Failure(failure) => ctx.complete(400, failure.toString)
              }
            }
          }
        }
      } ~ complete(401, "")
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
