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

package io.smartspaces.sandbox.interaction.entity;

/**
 * A registry of known sensors and entities that are being sensed.
 *
 * @author Keith M. Hughes
 */
trait SensorRegistry {

  /**
   * Register a sensor with the registry.
   *
   * @param sensor
   *          the sensor to add
   *
   * @return this registry
   */
  def registerSensor(sensor: SensorEntityDescription): SensorRegistry

  /**
   * Get the sensor description associated with a given ID.
   *
   * @param id
   *          the sensor ID
   *
   * @return the description
   */
  def getSensor(id: String): Option[SensorEntityDescription]

  /**
   * Register a marker with the registry.
   *
   * @param marker
   *          the marker to add
   *
   * @return this registry
   */
  def registerMarker(marker: MarkerEntityDescription): SensorRegistry

  /**
   * Get the marker description associated with a given ID.
   *
   * @param id
   *          the marker ID
   *
   * @return the description
   */
  def getMarker(id: String): Option[MarkerEntityDescription]

  /**
   * Get the markable entity description associated with a given ID.
   *
   * @param id
   *          the markable entity ID
   *
   * @return the description
   */
  def getMarkableEntity(id: String): Option[MarkableEntityDescription]

  /**
   * Get the marker entity description associated with a given marker ID.
   *
   * <p>
   * The {@code markerId} is the value of
   * {@link MarkerEntityDescription.getMarkerId()}.
   *
   * @param markerId
   *          the marker ID
   *
   * @return the description
   */
  def getMarkerEntityByMarkerId(markerId: String): Option[MarkerEntityDescription]

  /**
   * Get the markable entity description associated with a given marker ID.
   *
   * <p>
   * The {@code markerId} is the value of
   * {@link MarkerEntityDescription.getMarkerId()}.
   *
   * @param markerId
   *          the marker ID
   *
   * @return the description
   */
  def getMarkableEntityByMarkerId(markerId: String): Option[MarkableEntityDescription]

  /**
   * Register a sensed entity with the registry.
   *
   * @param sensedEntity
   *          the sensed entity to add
   *
   * @return this registry
   */
  def registerSensedEntity(sensor: SensedEntityDescription): SensorRegistry

  /**
   * Get the sensed entity description associated with a given ID.
   *
   * @param id
   *          the sensed entity ID
   *
   * @return the description
   */
  def getSensedEntity(id: String): Option[SensedEntityDescription]

  /**
   * Get all the sensed entities in the registry.
   *
   * @return a collection of the entities
   */
  def getAllSensedEntities(): List[SensedEntityDescription]

  /**
   * Associate a marker with its marked entity.
   *
   * @param markerId
   *          the ID of the marker
   * @param markedEntityId
   *          the ID of the marked entity
   *
   * @returns this registry
   */
  def associateMarkerWithMarkedEntity(markerId: String, markedEntityId: String): SensorRegistry

  /**
   * Associate a marker with its marked entity.
   *
   * @param marker
   *          the marker
   * @param markableEntity
   *          the markable entity
   *
   * @returns this registry
   */
  def associateMarkerWithMarkedEntity(marker: MarkerEntityDescription,
    markableEntity: MarkableEntityDescription): SensorRegistry

  /**
   * Get the associations between markers and their marked entities.
   *
   * @return the associations as an unmodifiable list
   */
  def getMarkerMarkedEntityAssociations(): List[MarkerMarkedEntityAssociation]

  /**
   * Associate a sensor with its sensed entity.
   *
   * @param sensorId
   *          the ID of the sensor
   * @param sensedEntityId
   *          the ID of the sensed entity
   *
   * @returns this registry
   */
  def associateSensorWithSensedEntity(sensorId: String, sensedEntityId: String): SensorRegistry

  /**
   * Get the associations between sensors and their sensed entities.
   *
   * @return the associations as an unmodifiable list
   */
  def getSensorSensedEntityAssociations(): List[SimpleSensorSensedEntityAssociation]

  /**
   * Add in configuration data for a given entity.
   *
   * <p>
   * This data will be merged with any previous data.
   *
   * @param entityId
   *          the ID of the entity
   * @param configurationData
   *          the configuration data to add
   *
   * @return this registry
   */
  def addConfigurationData(entityId: String, configurationData: Map[String, AnyRef]): SensorRegistry

  /**
   * Get the configuration data for the given entity.
   *
   * @param entityId
   *          the entity ID
   *
   * @return the configuration data known for the entity
   */
  def getConfigurationData(entityId: String): Map[String, AnyRef]
}