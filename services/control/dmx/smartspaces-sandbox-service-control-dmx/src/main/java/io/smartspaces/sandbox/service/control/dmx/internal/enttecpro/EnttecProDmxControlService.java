/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.sandbox.service.control.dmx.internal.enttecpro;

import io.smartspaces.sandbox.service.control.dmx.DmxControlEndpoint;
import io.smartspaces.sandbox.service.control.dmx.DmxControlService;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.comm.serial.SerialCommunicationEndpointService;

import org.apache.commons.logging.Log;

/**
 * A DMX control service that works with the Enttec Pro DMX interface.
 *
 * @author Keith M. Hughes
 */
public class EnttecProDmxControlService extends BaseSupportedService implements DmxControlService {

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  /**
   * Get a new DMX control endpoint.
   *
   * @param portName
   *          the port for the endpoint
   * @param log
   *          the logger to use
   *
   * @return the DMX control endpoint
   */
  @Override
  public DmxControlEndpoint newSerialDmxControlEndpoint(String portName, Log log) {
    SerialCommunicationEndpointService serialService =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(
            SerialCommunicationEndpointService.SERVICE_NAME);

    return new EnttecProDmxControlEndpoint(serialService.newSerialEndpoint(portName),
        getSpaceEnvironment().getExecutorService(), log);
  }
}
