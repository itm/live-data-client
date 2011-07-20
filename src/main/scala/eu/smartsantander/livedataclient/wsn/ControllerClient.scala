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

import de.uniluebeck.itm.wisebed.cmdlineclient.protobuf.ProtobufControllerClient
import de.uniluebeck.itm.wisebed.cmdlineclient.wrapper.WSNAsyncWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetAddress
import eu.wisebed.api.sm.UnknownReservationIdException_Exception
import eu.wisebed.api.sm.ExperimentNotRunningException_Exception
import eu.wisebed.api.sm.SessionManagement
import eu.wisebed.api.wsn.WSN
import eu.wisebed.testbed.api.wsn.WSNServiceHelper
import eu.wisebed.api.sm.SecretReservationKey
import scala.collection.mutable.HashMap
import de.uniluebeck.itm.wisebed.cmdlineclient.{BeanShellHelper, DelegatingController}
import java.util.List

/**
 * @author Soenke Nommensen
 */
class ControllerClient(listener: Listener, configuration: HashMap[String, String]) {

  secretReservationKeys = BeanShellHelper.parseSecretReservationKeys(configuration("secretReservationKeys"))
  pccHost = configuration("pccHost")
  pccPort = (configuration("pccPort")).toInt
  sessionManagementEndpoint = configuration("smEndpointurl")

  def setupProtobufClient() {
    val sessionManagement: SessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpoint)
    var wsnEndpointURL: String = null
    try {
      wsnEndpointURL = sessionManagement.getInstance(secretReservationKeys, "NONE")
    }
    catch {
      case e: UnknownReservationIdException_Exception => {
        log.warn("There was not reservation found with the given secret reservation key. Exiting.")
        System.exit(1)
      }
      case e: ExperimentNotRunningException_Exception => {
        log.error(e.getMessage, e)
      }
    }
    log.info("Got a WSN instance URL, endpoint is: {}", wsnEndpointURL)
    val wsnService: WSN = WSNServiceHelper.getWSNService(wsnEndpointURL)
    wsn = WSNAsyncWrapper.of(wsnService)
    val pcc = ProtobufControllerClient.create(pccHost, 8885, secretReservationKeys)
    if (pcc == null) return
    pcc.addListener(listener)
    pcc.connect()
  }

  def setupWebServiceClient() {
    val localControllerEndpointURL: String = "http://" + InetAddress.getLocalHost.getCanonicalHostName + ":8091/controller"
    val sessionManagement: SessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpoint)
    log.info("Using the following parameters for calling getInstance(): {}, {}", secretReservationKeys, localControllerEndpointURL)
    val delegator: DelegatingController = new DelegatingController(listener)
    delegator.publish(localControllerEndpointURL)
    log.info("Local controller published on url: {}", localControllerEndpointURL)
    try {
      val wsnEndpointURL: String = sessionManagement.getInstance(secretReservationKeys, localControllerEndpointURL)
      log.info("Got a WSN instance URL, endpoint is: {}", wsnEndpointURL)
      val wsnService: WSN = WSNServiceHelper.getWSNService(wsnEndpointURL)
      wsn = WSNAsyncWrapper.of(wsnService)
    }
    catch {
      case e: ExceptionInInitializerError => {
        log.error("Failed to initialize wsnEnpoint, maybe secretReservationKeys are invalid!", e)
      }
    }
  }

  def getSessionManagementEndpoint: String = {
    sessionManagementEndpoint
  }

  def getSecretReservationKeys: List[SecretReservationKey] = {
    secretReservationKeys
  }

  def getWsn: WSNAsyncWrapper = {
    wsn
  }

  private var sessionManagementEndpoint: String = _

  private var pccHost: String = _

  private var pccPort: Int = _

  private var wsn: WSNAsyncWrapper = null

  private var secretReservationKeys: List[SecretReservationKey] = _

  val log: Logger = LoggerFactory.getLogger(classOf[ControllerClient])
}