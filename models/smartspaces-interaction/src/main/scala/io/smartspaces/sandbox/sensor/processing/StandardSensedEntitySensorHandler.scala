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
import io.smartspaces.sandbox.sensor.entity.EntityMapper
import io.smartspaces.sandbox.sensor.entity.MemoryEntityMapper
import io.smartspaces.sandbox.sensor.entity.SensedEntityDescription
import io.smartspaces.sandbox.sensor.entity.SensorEntityDescription
import io.smartspaces.sandbox.sensor.entity.model.CompleteSensedEntityModel
import io.smartspaces.sandbox.sensor.entity.model.SensedEntityModel
import io.smartspaces.sandbox.sensor.entity.model.SensorEntityModel
import io.smartspaces.util.data.dynamic.DynamicObject

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

/**
 * The standard implementation of a sensed entity sensor handler.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntitySensorHandler(private val completeSensedEntityModel: CompleteSensedEntityModel, private val unknownSensedEntityHandler: UnknownSensedEntityHandler,
    val log: ExtendedLog) extends SensedEntitySensorHandler {

  /**
   * The mapping from sensor to sensed entity.
   */
  private val sensorToSensedEntity: EntityMapper = new MemoryEntityMapper

  /**
   * The sensors being handled, keyed by their ID.
   */
  private val sensors: Map[String, SensorEntityModel] = new HashMap

  /**
   * The entities being sensed, keyed by their ID.
   */
  private val sensedEntities: Map[String, SensedEntityModel] = new HashMap

  /**
   * The sensor processor the sensor input is running under.
   */
  var sensorProcessor: SensorProcessor = null

  /**
   * The listeners for physical based sensor events.
   */
  private val sensedEntitySensorListeners: ArrayBuffer[SensedEntitySensorListener] =
    new ArrayBuffer

  override def startup(): Unit = {
    // Nothing to do.
  }

  override def shutdown(): Unit = {
    // Nothing to do.
  }

  override def addSensedEntitySensorListener(listener: SensedEntitySensorListener): SensedEntitySensorHandler = {
    sensedEntitySensorListeners += listener

    this
  }

  override def associateSensorWithEntity(sensor: SensorEntityDescription,
    sensedEntity: SensedEntityDescription): SensedEntitySensorHandler = {

    val sensorModel = completeSensedEntityModel.getSensorEntityModel(sensor.externalId)
    sensors.put(sensor.externalId, sensorModel.get)

    val sensedModel = completeSensedEntityModel.getSensedEntityModel(sensedEntity.externalId)
    sensedEntities.put(sensedEntity.externalId, sensedModel.get)
    sensorToSensedEntity.put(sensor.externalId, sensedEntity.externalId)

    this
  }

  override def handleSensorData(timestamp: Long, data: DynamicObject): Unit = {
    val sensorId = data.getString(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_SENSOR)

    if (sensorId == null) {
      log.warn("Got data from unknown sensor, the sensor ID is missing")
      return
    }

    val sensor = sensors.get(sensorId)
    if (sensor.isEmpty) {
      log.formatWarn("Got data from unregistered sensor %s, the data is %s", sensorId,
        data.asMap())
      unknownSensedEntityHandler.handleUnknownSensor(sensorId)

      return
    }

    if (!sensor.get.sensorEntityDescription.active) {
      return
    }

    val sensedEntityId = sensorToSensedEntity.get(sensorId)
    if (sensedEntityId.isEmpty) {
      log.formatWarn("Got data from sensor %s with no registered sensed entity: %s", sensorId,
        data.asMap())
      return
    }

    // No need to confirm sensed entity, we would not have a sensed entity ID
    // unless there was an entity registered.
    val sensedEntity = sensedEntities.get(sensedEntityId.get)

    if (log.isDebugEnabled()) {
      log.formatDebug("Got data from sensor %s for sensed entity %s: %s", sensor, sensedEntity,
        data.asMap());
    }

    completeSensedEntityModel.doVoidWriteTransaction { () =>
      sensedEntitySensorListeners.foreach((listener) => {
        try {
          listener.handleSensorData(this, timestamp, sensor.get, sensedEntity.get, data);
        } catch {
          case e: Throwable =>
            log.formatError(e, "Error during listener processing of physical based sensor data");
        }
      })
    }
  }
}
