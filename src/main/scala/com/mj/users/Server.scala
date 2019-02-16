package com.mj.users

import java.net.InetAddress

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import com.mj.users.config.Application
import com.mj.users.config.Application._
import com.mj.users.processor.upload.UploadVideoProcessor
import com.mj.users.tools.CommonUtils._
import com.mj.users.tools.RouteUtils
import com.typesafe.config.ConfigFactory

object Server extends App {
  val seedNodesStr = seedNodes
    .split(",")
    .map(s => s""" "akka.tcp://users-cluster@$s" """)
    .mkString(",")

  val inetAddress = InetAddress.getLocalHost
  var configCluster = Application.config.withFallback(
    ConfigFactory.parseString(s"akka.cluster.seed-nodes=[$seedNodesStr]"))

  configCluster = configCluster
    .withFallback(
      ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname=$hostName"))
    .withFallback(
      ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$akkaPort"))

  implicit val system: ActorSystem = ActorSystem("users-cluster", configCluster)
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  val uploadImageProcessor = system.actorOf(RoundRobinPool(poolSize).props(Props[processor.upload.UploadImageProcessor]), "uploadImageProcessor")
  val uploadVideoProcessor = system.actorOf(RoundRobinPool(poolSize).props(Props[processor.upload.UploadVideoProcessor]), "uploadVideoProcessor")
  val uploadAvatarProcessor = system.actorOf(RoundRobinPool(poolSize).props(Props[processor.upload.UploadAvatarProcessor]), "uploadAvatarProcessor")
  val uploadProfileBGProcessor = system.actorOf(RoundRobinPool(poolSize).props(Props[processor.upload.UploadProfileBGProcessor]), "uploadProfileBGProcessor")

  import system.dispatcher

  Http().bindAndHandle(RouteUtils.logRoute, "0.0.0.0", port)

  consoleLog("INFO",
    s"User server started! Access url: https://$hostName:$port/")
}
