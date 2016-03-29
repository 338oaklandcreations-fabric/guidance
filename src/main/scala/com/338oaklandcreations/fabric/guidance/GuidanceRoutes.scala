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
import spray.json._
import spray.routing._

class GuidanceServiceActor extends HttpServiceActor with ActorLogging {

  override def actorRefFactory = context

  val guidanceRoutes = new GuidanceRoutes {
    def actorRefFactory = context
  }

  def receive = runRoute(
    guidanceRoutes.routes ~
      get {getFromResourceDirectory("webapp")} ~
      get {getFromResource("webapp/index.html")})

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

  val routes =
    postBet ~
      bets ~
      postGameResult ~
      gamesRequest ~
      missingGamesRequest ~
      admin ~
      saveTicket ~
      login

  val authenticationRejection = RejectionHandler {
    case AuthenticationRejection(message) :: _ => complete(400, message)
  }

  val authorizationRejection = RejectionHandler {
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
  val SessionKey = "WHOWON_SESSION"
  val UserKey = "WHOWON_USER"
  val ResponseTextHeader = "{\"responseText\": "

  def postBet = post {
    path("bets") {
      cookie("WHOWON_SESSION") { sessionId => {
        cookie("WHOWON_USER") { username => {
          respondWithMediaType(`application/json`) { ctx =>
            val newBet = ctx.request.entity.data.asString.parseJson.convertTo[Bet]
            val future = guidanceData ? newBet
            future onSuccess {
              case BetSubmitted => ctx.complete(200, ResponseTextHeader + "\"Bet Submitted\"}")
              case BetReplaced => ctx.complete(200, ResponseTextHeader + "\"Bet Replaced\"}")
              case UnknownPlayer => ctx.complete(400, ResponseTextHeader + "\"Unknown Player\"}")
              case UnknownBookId => ctx.complete(400, ResponseTextHeader + "\"Unknown Book Id\"}")
            }
          }
        }
        }
      }}
    }
  }

  def bets = get {
    pathPrefix("bets" / """.*""".r / IntNumber) { (userName, year) =>
      respondWithMediaType(`application/json`) { ctx =>
        val future = guidanceData ? BetsRequest(userName, year)
        future onSuccess {
          case Bets(list) => ctx.complete(list.toJson.toString)
          case UnknownPlayer => ctx.complete(400, ResponseTextHeader + "\"Unknown Player\"}")
        }
      }
    }
  }

  def saveTicket = post {
    path("ticket") {
      cookie("WHOWON_SESSION") { sessionId => {
        cookie("WHOWON_USER") { username => {
          handleRejections(authorizationRejection) {
            authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
              respondWithMediaType(`application/json`) { ctx =>
                val future = guidanceData ? TicketImage(username.content, ctx.request.entity.data.toByteString)
                future onSuccess {
                  case location: String => ctx.complete(location)
                  case _ => ctx.complete(400, ResponseTextHeader + "\"Error storing image\"}")
                }
              }}
          }}
        }}
      }} ~ getFromResource("webapp/login.html")
    }

  def postGameResult = post {
    path("games" / IntNumber) { (year) =>
      cookie("WHOWON_SESSION") { sessionId => {
        cookie("WHOWON_USER") { username => {
          respondWithMediaType(`application/json`) { ctx =>
            val newResult = ctx.request.entity.data.asString.parseJson.convertTo[GameResult]
            val future = guidanceData ? newResult
            future onSuccess {
              case ResultSubmitted => ctx.complete(ResponseTextHeader + "\"Submitted\"}")
              case _ => ctx.complete(500, ResponseTextHeader + "\"Problem Submitting\"}")
            }
          }
        }
        }}
      }}
  }

  def gamesRequest = get {
    path("games" / IntNumber) { (year) =>
      respondWithMediaType(`application/json`) { ctx =>
        val future = guidanceData ? GameResultsRequest(year)
        future onSuccess {
          case GameResults(list) => {
            ctx.complete(list.toJson.toString)
          }
        }
      }
    }
  }

  def missingGamesRequest = get {
    path("games" / IntNumber / "missing") { (year) =>
      respondWithMediaType(`application/json`) { ctx =>
        val future = guidanceData ? MissingGameResultsRequest(year)
        future onSuccess {
          case BookIdsResults(list) => {
            ctx.complete(list.toJson.toString)
          }
        }
      }
    }
  }

  def admin = get {
    path("admin") {
      cookie("WHOWON_SESSION") { sessionId => {
        cookie("WHOWON_USER") { username => {
          handleRejections(authorizationRejection) {
            authenticate(authenticateSessionId(sessionId.content, username.content)) { authentication =>
              getFromResource("webapp/admin.html")
            }
          }
        }
        }
      }
      } ~ getFromResource("webapp/login.html")
    }
  }

  def login =
    post {
      path("login") {
        formFields('inputName, 'inputPassword) { (inputName, inputPassword) =>
          handleRejections(authenticationRejection) {
            authenticate(authenticateUser(inputName, inputPassword)) { authentication =>
              setCookie(HttpCookie(SessionKey, content = authentication.token, expires = Some(expiration))) {
                setCookie(HttpCookie(UserKey, content = inputName, expires = Some(expiration))) { ctx =>
                  val future = guidanceData ? PlayerIdRequest(inputName)
                  future onSuccess {
                    case x: Player => ctx.complete("")
                    case x: UnknownPlayer => ctx.complete(400, "Unknown Player")
                  }
                }
              }
            }
          }
        }
      }
    }

}
