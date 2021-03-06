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

package io.smartspaces.sandbox.sensor.value.converter

import io.smartspaces.sandbox.sensor.entity.MeasurementUnitDescription

/**
 * Converter for celcius to fahrenheit.
 * 
 * @author Keith M. Hughes
 */
class CelciusToFahrenheitMeasurementValueConverter extends MeasurementValueConverter[Double, Double] {

  override val from: MeasurementUnitDescription = null

  override val to: MeasurementUnitDescription = null
  
  override  def convert(value: Double): Double = {
     value * 9 / 5 + 32
  }
}