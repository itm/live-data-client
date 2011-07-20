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

  val sessionManagementEndpoint: String = configuration("smEndpointurl")

  val pccHost: String = configuration("pccHost")

  val pccPort: Int = (configuration("pccPort")).toInt

  var wsn: WSNAsyncWrapper = null

  private var secretReservationKeys: List[SecretReservationKey] =
    BeanShellHelper.parseSecretReservationKeys(configuration("secretReservationKeys"))

  def setupProtobufClient() {
    val sessionManagement: SessionManagement = WSNServiceHelper.getSessionManagementService(sessionManagementEndpoint)
    var wsnEndpointURL: String = null
    try {
      wsnEndpointURL = sessionManagement.getInstance(secretReservationKeys, "NONE")
    }
    catch {
      case e: UnknownReservationIdException_Exception => {
        println("There was not reservation found with the given secret reservation key. Exiting.")
        sys.exit(1)
      }
      case e: ExperimentNotRunningException_Exception => {
        println(e.getMessage)
        sys.exit(1)
      }
    }
    println("Got a WSN instance URL, endpoint is: %s".format(wsnEndpointURL))
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
    println("Using the following parameters for calling getInstance(): {}, {}", secretReservationKeys, localControllerEndpointURL)
    val delegator: DelegatingController = new DelegatingController(listener)
    delegator.publish(localControllerEndpointURL)
    println("Local controller published on url: {}", localControllerEndpointURL)
    try {
      val wsnEndpointURL: String = sessionManagement.getInstance(secretReservationKeys, localControllerEndpointURL)
      println("Got a WSN instance URL, endpoint is: {}", wsnEndpointURL)
      val wsnService: WSN = WSNServiceHelper.getWSNService(wsnEndpointURL)
      wsn = WSNAsyncWrapper.of(wsnService)
    }
    catch {
      case e: ExceptionInInitializerError => {
        println("Failed to initialize wsnEnpoint, maybe secretReservationKeys are invalid!")
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
}