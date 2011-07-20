/**
 * Copyright (C) 2011 Universität zu Lübeck, Institut für Telematik (ITM)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.smartsantander.livedataclient.wsn

import scala.collection.JavaConversions._

import de.uniluebeck.itm.tr.util.StringUtils
import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerClientListener
import eu.wisebed.api.common.Message
import eu.wisebed.api.controller.RequestStatus
import javax.jws.WebParam
import redis.clients.jedis.Jedis
import java.util.{LinkedList, List, Queue}

/**
 * @author Soenke Nommensen
 */
class Listener(jedis: Jedis) extends ProtobufControllerClientListener {

  val messageQueue: Queue[Message] = new LinkedList[Message]

  val requestStatusQueue: Queue[RequestStatus] = new LinkedList[RequestStatus]

  val notificationQueue: Queue[String] = new LinkedList[String]

  val channelName = "sensor_readings"

  def onConnectionClosed() {
    println("Connection closed")
  }

  def onConnectionEstablished() {
    println("Connection established")
  }

  def experimentEnded() {
    println("Experiment ended")
  }

  class Reading(sender: String, reading: String, readingType: String, timestamp: Long) {
    val jsonFormat = "{ \"sender\": \"%s\", \"reading\": \"%s\", \"readingType\": \"%s\", \"timestamp\": %s }"

    def getReadingType = readingType

    def toJson = jsonFormat.format(sender, reading, readingType, timestamp)
  }

  def receive(@WebParam(name = "msg", targetNamespace = "") messages: List[Message]) {
    for (msg: Message <- messages) {

      //log.info(StringUtils.replaceNonPrintableAsciiCharacters(new String(msg.getBinaryData)))

      val msg_array: Array[String] = StringUtils.replaceNonPrintableAsciiCharacters(new String(msg.getBinaryData)).split(";")

      if (msg_array(0).compareTo("h[NUL]wiseml") == 0) {
        try {
          val msgType: Int = Integer.valueOf(msg_array(1))
          val sender: Int = Integer.parseInt(msg_array(2).substring(2), 16)
          var reading: Reading = new Reading("", "", "", 0)
          val timestamp = new java.util.Date()
          msgType match {
            case 0 =>
              val receiver: Int = Integer.parseInt(msg_array(3).substring(2), 16)
              val rssi: Int = Integer.parseInt(msg_array(4))
              val lqi: Int = Integer.parseInt(msg_array(5))
              reading = new Reading(sender.toString, receiver.toString, "none", timestamp.getTime)
            case 1 =>
              val temp: Double = (msg_array(3).replace(',', '.')).toDouble
              reading = new Reading(sender.toString, (temp/2).toString, "temp", timestamp.getTime)
            case 2 =>
              val lum: Int = Integer.valueOf(msg_array(3))
              reading = new Reading(sender.toString, lum.toString, "lum", timestamp.getTime)
            case 3 =>
              val irda: Int = Integer.valueOf(msg_array(3))
              reading = new Reading(sender.toString, irda.toString, "irda", timestamp.getTime)
            case 6 =>
              val hum: Int = Integer.valueOf(msg_array(3))
              reading = new Reading(sender.toString, hum.toString, "hum", timestamp.getTime)
          }

          // Publish data channel using Redis
          if (reading.getReadingType != "none") {
            println(reading.toJson)
            jedis.publish(channelName, reading.toJson)
          }

        }
        catch {
          case e: NumberFormatException => {
            println(e.getMessage)
          }
          case e: Exception => {
            println(e.getMessage)
            sys.exit(1)
          }
        }
      }
      else {
        println("Unknown message:" + StringUtils.replaceNonPrintableAsciiCharacters(new String(msg.getBinaryData)))
      }
    }
  }

  def receiveNotification(@WebParam(name = "msg", targetNamespace = "") notifications: List[String]) {
    for (notification <- notifications) {
      println("[Notification]:" + notification)
      notificationQueue.add(notification)
    }
  }

  def receiveStatus(@WebParam(name = "status", targetNamespace = "") requestStatuses: List[RequestStatus]) {
    // TODO
  }

  def getMessageQueue: Queue[Message] = {
    messageQueue
  }

  def getRequestStatusQueue: Queue[RequestStatus] = {
    requestStatusQueue
  }

  def getNotificationQueue: Queue[String] = {
    notificationQueue
  }
}