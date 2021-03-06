/*
 * Copyright (C) 2016 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.sandbox.sensor.processing

import io.smartspaces.logging.ExtendedLog
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpoint
import io.smartspaces.service.comm.pubsub.mqtt.MqttCommunicationEndpointService
import io.smartspaces.service.comm.pubsub.mqtt.MqttSubscriberListener
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.util.messaging.mqtt.MqttBrokerDescription

/**
 * A sensor input aggregator for sensor message over MQTT.
 * 
 * @author Keith M. Hughes
 */
class MqttSensorInputAggregator(private val mqttBrokerDescription: MqttBrokerDescription, private val mqttClientId: String,
      private val mqttSensorTopicName:String, private val spaceEnvironment: SmartSpacesEnvironment, private val log: ExtendedLog) extends SensorInput {

  /**
   * The sensor processor the sensor input is running under.
   */
  private var sensorProcessor: SensorProcessor = null

  /**
   * The MQTT client endpoint.
   */
  private var mqttEndpoint: MqttCommunicationEndpoint = null

  /**
   * The codec for translating messages.
   */
  private val codec = new DynamicObjectByteArrayCodec()

  override def startup(): Unit = {
    val service: MqttCommunicationEndpointService = spaceEnvironment.getServiceRegistry()
        .getRequiredService(MqttCommunicationEndpointService.SERVICE_NAME)
    mqttEndpoint = service.newMqttCommunicationEndpoint(mqttBrokerDescription, mqttClientId, log)

    mqttEndpoint.subscribe(mqttSensorTopicName, new MqttSubscriberListener() {
      override def handleMessage( endpoint: MqttCommunicationEndpoint,  topicName: String,
           payload: Array[Byte]): Unit = {
        handleSensorMessage(topicName, payload)
      }
    }, 0, true)
    
    mqttEndpoint.startup()
   }

  override def shutdown(): Unit = {
    if (mqttEndpoint != null) {
      mqttEndpoint.shutdown()
    }
  }

 override def setSensorProcessor(sensorProcessor: SensorProcessor): Unit = {
    this.sensorProcessor = sensorProcessor
  }

  /**
   * Handle an incoming sensor message.
   * 
   * @param topicName
   *          the name of the topic the message cam in on
   * @param payload
   *          the message payload
   */
  private def handleSensorMessage(topicName: String ,payload: Array[Byte]): Unit = {
    val message = codec.decode(payload)
    log.formatDebug("Got message on topic %s", topicName)

    // TODO(keith): Consider also checking message to see if it has a timestamp.
    // If so use it, otherwise use time provider.
    val currentTime = spaceEnvironment.getTimeProvider().getCurrentTime()

    sensorProcessor.processSensorData(currentTime, message)
  }
}
