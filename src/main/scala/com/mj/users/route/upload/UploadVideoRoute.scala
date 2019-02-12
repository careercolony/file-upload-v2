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
import com.mj.users.model.{responseMessage, _}
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success}

trait UploadVideoRoute {
  val uploadVideoUserLog = LoggerFactory.getLogger(this.getClass.getName)


  def uploadVideo(system: ActorSystem , materializer : ActorMaterializer): Route = {

    val uploadVideoProcessor = system.actorSelection("/*/uploadVideoProcessor")
    implicit val timeout = Timeout(20, TimeUnit.SECONDS)

    path("video" / "upload" / "file"/ {Segment}) {  videoType =>
      (post & entity(as[Multipart.FormData])) { fileData =>

            val userResponse = uploadVideoProcessor ? ( fileData , materializer , videoType)
            onComplete(userResponse) {
              case Success(resp) =>
                resp match {
                  case s: uploadVideoResponse => {
                    complete(HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                  }
                  case s: responseMessage =>
                    complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, s.toJson.toString)))
                  case _ => complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", resp.toString, "").toJson.toString)))
                }
              case Failure(error) =>
                uploadVideoUserLog.error("Error is: " + error.getMessage)
                complete(HttpResponse(status = BadRequest, entity = HttpEntity(MediaTypes.`application/json`, responseMessage("", error.getMessage, "").toJson.toString)))
            }

          }
        }
      }


}
