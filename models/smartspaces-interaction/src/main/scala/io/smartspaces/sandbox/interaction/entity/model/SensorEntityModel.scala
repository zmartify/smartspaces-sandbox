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

package io.smartspaces.sandbox.interaction.entity.model

import io.smartspaces.sandbox.interaction.entity.SensorEntityDescription

/**
 * The model of a sensor.
 * 
 * <p>
 * The sensor is considered online or offline by whether or not the sensor has sent either a
 * heartbeat or a sensor message within a specified rime window. The time window for a given
 * sensor is set in the sensor description.
 *
 * @author Keith M. Hughes
 */
trait SensorEntityModel {

  /**
   * The sensor entity description for the model.
   */
  val sensorEntityDescription: SensorEntityDescription

  /**
   * Get the sensed entity model collection this model is in.
   */
  val allModels: CompleteSensedEntityModel

  /**
   * The model that is being sensed by this sensor.
   */
  var sensedEntityModel: Option[SensedEntityModel]
  
  /**
   * Is the sensor online?
   */
  var online: Boolean

  /**
   * Get the value of a sensed property by its type ID.
   *
   * @param valueTypeId
   *          the ID of the value type
   *
   * @return the sensed value with the specified value type
   */
  def getSensedValue(valueTypeId: String): Option[SensedValue[Any]]

  /**
   * Get all sensed values for this entity.
   *
   * @return all sensed values
   */
  def getAllSensedValues(): List[SensedValue[Any]]

  /**
   * Update a sensed value.
   *
   * @param value
   *          the value being updated
   * @param updateTime
   * 		      the time of this update
   */
  def updateSensedValue[T <: Any](value: SensedValue[T], updateTime: Long): Unit

  /**
   * Get the last update for the model.
   *
   * @return the last time
   */
  def getLastUpdate(): Long
}