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
  val system = ActorSystem("guidanceSystem")

  import system.dispatcher

  val guidanceData = system.actorOf(Props[GuidanceData], "guidanceData")
  val machineryAPI = system.actorOf(Props[MachineryAPI], "machineryAPI")

  val routes =
    mainPage ~
      hostCPU ~
      hostMemory ~
      heartbeat ~
      hostStatistics ~
      ledPower ~
      patternNames ~
      patternUpdate ~
      versions ~
      logLevel ~
      setWellLightSettings ~
      wellLightSettings ~
      login

  val authenticationRejection = RejectionHandler {
    case AuthenticationRejection(message) :: _ => getFromResource("webapp/login.html")
  }

  def cookies = cookie("FABRIC_GUIDANCE_SESSION") & cookie("FABRIC_GUIDANCE_USER") & respondWithMediaType(`application/json`)
  def authenticateCookies(sessionId: HttpCookie, username: HttpCookie) = authenticate(authenticateSessionId(sessionId.content, username.content))

  val keyLifespanMillis = 120000 * 1000 // 2000 minutes
  val expiration = DateTime.now + keyLifespanMillis
  val SessionKey = "FABRIC_GUIDANCE_SESSION"
  val UserKey = "FABRIC_GUIDANCE_USER"
  val ResponseTextHeader = "{\"responseText\": "
  val UnauthorizedRequestString = "Unauthorized Request"
  val UnknownCommandResponseString = ResponseTextHeader + "\"Unknown command results\"}"

  def forwardRequest(ctx: RequestContext) = {
    val future = machineryAPI ? ctx.request
    future onComplete {
      case Success(success) => success match {
        case history: String => ctx.complete(history)
        case _ => ctx.complete(400, UnknownCommandResponseString)
      }
      case Failure(failure) => ctx.complete(400, failure.toString)
    }
  }

  def hostCPU = get {
    path("hostCPU") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def hostMemory = get {
    path("hostMemory") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def heartbeat = get {
    path("heartbeat") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def hostStatistics = get {
    path("hostStatistics") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def ledPower = post {
    path("ledPower" / """(on|off)""".r) { (select) =>
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def setWellLightSettings = post {
    path("wellLightSettings") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def wellLightSettings = get {
    path("wellLightSettings") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def patternNames = get {
    path("pattern" / "names") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def patternUpdate = post {
    path("pattern") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def versions = get {
    pathPrefix("version") {
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          forwardRequest(ctx)
        }
      } ~ complete(401, UnauthorizedRequestString)
    }
  }

  def logLevel = post {
    path("logLevel" / """(DEBUG|INFO|WARN)""".r) { (level) =>
      cookies { (sessionId, username) =>
        authenticateCookies(sessionId, username) { authenticated => ctx =>
          import ch.qos.logback.classic.Level
          val root = org.slf4j.LoggerFactory.getLogger("root").asInstanceOf[ch.qos.logback.classic.Logger]
          level match {
            case "DEBUG" => root.setLevel(Level.DEBUG)
            case "INFO" => root.setLevel(Level.INFO)
            case "WARN" => root.setLevel(Level.WARN)
          }
          forwardRequest(ctx)
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
