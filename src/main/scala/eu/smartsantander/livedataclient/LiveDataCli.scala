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
import org.clapper.argot._
import ArgotConverters._

/**
 *
 * @author Soenke Nommensen 
 */

object LiveDataCli {
  val DefaultConfigLocation = "config.xml"
  val ApplicationInfo = "Live Data Client: Version 1.0. Copyright (c) 2011, Institut fuer Telematik."
  val ApplicationName = "ldc-cli"

  def main(args: Array[String]) {
    // Handle command line arguments
    val parser = new ArgotParser(ApplicationName, preUsage = Some(ApplicationInfo))
    var configFile : SingleValueOption[String] = null
    try {
      configFile = parser.option[String](List("f", "config-file"), "<config-file>", "Path to configuration file")
      parser.parse(args)
    }
    catch {
      case e: ArgotUsageException => println(e.getMessage); sys.exit(1)
      case e: Exception => log.error(e.getMessage, e); sys.exit(1)
    }

    // Load the configuration
    val configuration: HashMap[String, String] = configFile.value match {
      case None => loadConfiguration(DefaultConfigLocation)
      case Some(configPath) => loadConfiguration(configPath)
    }

    // Init testbed controller and listener
    val listener: Listener = new Listener(jedis)
    val controller: ControllerClient = new ControllerClient(listener, configuration)
    controller.setupProtobufClient()

    log.info("Server started...")
  }

  def loadConfiguration(configLocation: String): HashMap[String, String] = {
    val configuration = new HashMap[String, String]
    val configXml = XML.loadFile(configLocation)

    (configXml \\ "config" \ "_").foreach(c => configuration += c.label -> c.text)

    configuration
  }

  val redisHost = "localhost"

  val jedis = new Jedis(redisHost)  // TODO Catch error when Redis is not available

  val log: Logger = LoggerFactory.getLogger(classOf[LiveDataCli])
}

class LiveDataCli
