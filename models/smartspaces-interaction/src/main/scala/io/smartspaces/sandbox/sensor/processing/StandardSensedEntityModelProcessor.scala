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

import io.smartspaces.util.data.dynamic.DynamicObject
import io.smartspaces.sandbox.sensor.entity.model.SensedEntityModel
import io.smartspaces.sandbox.sensor.entity.model.SensorEntityModel
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sandbox.sensor.entity.model.CompleteSensedEntityModel

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

/**
 * A sensor processor that will update sensed entity models.
 *
 * @author Keith M. Hughes
 */
class StandardSensedEntityModelProcessor(private val completeSensedEntityModel: CompleteSensedEntityModel,
  private val log: ExtendedLog)
    extends SensedEntityModelProcessor with SensedEntitySensorListener {

  /**
   * The map of sensor types to sensor processors.
   */
  private val sensorValuesProcessors: Map[String, SensorValueProcessor] = new HashMap

  /**
   * The context for sensor value processors.
   */
  val processorContext = new SensorValueProcessorContext(completeSensedEntityModel, log)

  override def addSensorValueProcessor(processor: SensorValueProcessor): SensedEntityModelProcessor = {
    log.formatInfo("Adding sensor processor for %s", processor.sensorValueType)

    val previous = sensorValuesProcessors.put(processor.sensorValueType, processor)
    if (previous.isDefined) {
      log.formatWarn("A sensor processor for %s has just been replaced", processor.sensorValueType)
    }

    this
  }

  override def handleSensorData(handler: SensedEntitySensorHandler, timestamp: Long,
    sensor: SensorEntityModel, sensedEntity: SensedEntityModel, data: DynamicObject): Unit = {
    log.formatInfo("Updating model for entity %s", sensedEntity)

    // Go into the data fields.
    data.down(SensorMessages.SENSOR_MESSAGE_FIELD_NAME_DATA)

    val sensorDetail = sensor.sensorEntityDescription.sensorDetail
    if (sensorDetail.isDefined) {
      // Go through every property in the data set, find its type, and then create
      // appropriate values.
      data.getObjectEntries().foreach((entry) => {
        val channelName = entry.getProperty()

        val sensedMeasurementType = sensorDetail.get.getSensorChannelDetail(channelName).get.measurementType
        val sensorValueProcessor = sensorValuesProcessors.get(sensedMeasurementType.externalId)
        if (sensorValueProcessor.isDefined) {
          entry.down()
          sensorValueProcessor.get.processData(timestamp, sensor, sensedEntity, processorContext,
            data);
        } else {
          log.formatWarn("Got unknown sensed type with no apparent processor %s", sensedMeasurementType)
        }

      })
    } else {
      log.formatWarn("Got sensor with no sensor detail %s", sensor.sensorEntityDescription)
    }
  }
}
