package com.mj.users.config

import com.typesafe.config.{Config, ConfigFactory}
import reactivemongo.api.{MongoConnection, MongoDriver}

import scala.concurrent.Future

object Application {
  val config: Config = ConfigFactory.load("application.conf")

  val configServer = config.getConfig("server")
  val hostName = configServer.getString("hostName")
  val port = configServer.getString("port").toInt
  val akkaPort = configServer.getString("akkaPort").toInt
  val seedNodes = configServer.getString("seedNodes")
  val poolSize= config.getInt("poolSize")
  val filePath = config.getString("filePath")

}
