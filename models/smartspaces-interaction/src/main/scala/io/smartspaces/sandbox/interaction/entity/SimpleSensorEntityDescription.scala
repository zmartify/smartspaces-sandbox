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
 * The standard sensor entity description.
 *
 * @author Keith M. Hughes
 */
class SimpleSensorEntityDescription(id: String, displayName: String, description: String, private val sensorDetail: Option[SensorDetail]) extends SimpleEntityDescription(id, displayName, description)
    with SensorEntityDescription {

  override def getSensorDetail(): Option[SensorDetail] = {
    sensorDetail
  }
}
