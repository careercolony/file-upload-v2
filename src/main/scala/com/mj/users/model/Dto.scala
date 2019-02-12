package com.mj.users.model


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}



case class UploadImageResponse ( fileName  : String )

case class uploadVideoResponse ( fileName  : String )
//Response format for all apis
case class responseMessage(uid: String, errmsg: String, successmsg: String)

object JsonRepo extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val errorMessageDtoFormats: RootJsonFormat[responseMessage] = jsonFormat3(responseMessage)
  implicit val uploadImageResponseFormats: RootJsonFormat[UploadImageResponse] = jsonFormat1(UploadImageResponse)
  implicit val uploadVideoResponseFormats: RootJsonFormat[uploadVideoResponse] = jsonFormat1(uploadVideoResponse)
}
