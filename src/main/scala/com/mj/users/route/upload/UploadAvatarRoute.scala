package com.mj.users.route.upload

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, Multipart}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.mj.users.model.JsonRepo._
import com.mj.users.model.{responseMessage, uploadAvatarResponse}
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success}

trait UploadAvatarRoute {
  val uploadAvatarUserLog = LoggerFactory.getLogger(this.getClass.getName)


  def uploadAvatar(system: ActorSystem , materializer : ActorMaterializer): Route = {

    val uploadAvatarProcessor = system.actorSelection("/*/uploadAvatarProcessor")
    implicit val timeout = Timeout(20, TimeUnit.SECONDS)

    path("upload" / "avatar" / "memberID"/ Segment / "type" / Segment) { (memberID, imageType) =>
      (post & entity(as[Multipart.FormData])) { fileData =>

            val userResponse = uploadAvatarProcessor ? ( fileData , materializer , memberID , imageType)
            onComplete(userResponse) {
              case Success(resp) =>
                resp match {
                  case s: uploadAvatarResponse => {
                    complete(HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                  }
                  case s: responseMessage =>
                    complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                  case _ => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", resp.toString, "").toJson.toString)))
                }
              case Failure(error) =>
                uploadAvatarUserLog.error("Error is: " + error.getMessage)
                complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", error.getMessage, "").toJson.toString)))
            }

          }
        }
      }


}
