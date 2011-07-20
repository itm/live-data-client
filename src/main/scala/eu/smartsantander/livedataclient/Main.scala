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
package eu.smartsantander.livedataclient

import org.slf4j.{LoggerFactory, Logger}
import wsn.{Listener, ControllerClient}
import collection.mutable.HashMap
import scala.xml._
import redis.clients.jedis.Jedis

/**
 *
 * @author Soenke Nommensen 
 */

object Main {

  def main(args: Array[String]) {
    val listener: Listener = new Listener(jedis)
    val configuration = loadConfiguration()
    val controller: ControllerClient = new ControllerClient(listener, configuration)
    controller.setupProtobufClient()

    log.info("Server started...")
  }

  def loadConfiguration(): HashMap[String, String] = {
    val configuration = new HashMap[String, String]
    val configXml = XML.loadFile("src/main/resources/config.xml") // TODO Load from arbitrary location.
    (configXml \\ "config" \ "_").foreach(c => configuration += c.label -> c.text)
    configuration
  }

  val redisHost = "localhost"

  val jedis = new Jedis(redisHost)

  val log: Logger = LoggerFactory.getLogger(classOf[Main])

}

class Main
