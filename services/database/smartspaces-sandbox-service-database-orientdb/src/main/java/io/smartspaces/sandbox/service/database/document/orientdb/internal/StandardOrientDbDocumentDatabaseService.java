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

package io.smartspaces.sandbox.service.database.document.orientdb.internal;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.sandbox.service.database.document.orientdb.OrientDbDocumentDatabaseEndpoint;
import io.smartspaces.sandbox.service.database.document.orientdb.OrientDbDocumentDatabaseService;
import io.smartspaces.service.BaseSupportedService;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkListenerConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkProtocolConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import org.apache.commons.logging.Log;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The standard implementation of the orientDB Database Service.
 * 
 * @author Oleksandr Kelepko
 * @author Keith M. Hughes
 */
public class StandardOrientDbDocumentDatabaseService extends BaseSupportedService implements OrientDbDocumentDatabaseService {
  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "database.orientdb";

  /**
   * Protocol used by the server.
   */
  public static final String PROTOCOL = "binary";

  /**
   * Name of the class that implements the protocol used by the server.
   */
  public static final String PROTOCOL_IMPLEMENTATION =
      "com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary";

  /**
   * IP address to which server binds.
   */
  public static final String IP_ADDRESS = "0.0.0.0";

  /**
   * Range of ports to which the server tries to bind.
   */
  public static final String PORT_RANGE = "2424-2500";

  /**
   * Username of a server user.
   */
  public static final String SERVER_USER_LOGIN = "root";

  /**
   * Password of the server user.
   */
  public static final String SERVER_USER_PASSWORD = "ThisIsA_TEST";

  /**
   * Resources accessible to the server user.
   */
  public static final String SERVER_USER_RESOURCES = "";

  /**
   * Configuration property for caching static contents. If enabled the files
   * will be kept in memory the first time they are loaded. Changes to the files
   * will be taken on the next restart.
   */
  public static final String CONFIGURATION_CACHE_STATIC_RESOURCES = "server.cache.staticResources";

  /**
   * Configuration property for the logging level of the logger that outputs to
   * the console.
   */
  public static final String CONFIGURATION_LOG_CONSOLE_LEVEL = "log.console.level";

  /**
   * Configuration property for the logging level of the logger that outputs to
   * a file.
   */
  public static final String CONFIGURATION_LOG_FILE_LEVEL = "log.file.level";

  /**
   * Configuration property for attaching dynamic plugins to the server.
   */
  public static final String CONFIGURATION_PLUGIN_DYNAMIC = "plugin.dynamic";

  /**
   * Username of the database user.
   */
  public static final String DEFAULT_LOGIN = "admin";

  /**
   * Password of the database user.
   */
  public static final String DEFAULT_PASSWORD = "admin";

  /**
   * Protocol string to use for local databases.
   */
  public static final String DATABASE_URL_PROTOCOL_PLOCAL = "plocal:";

  /**
   * State of this service.
   */
  private final AtomicBoolean active = new AtomicBoolean();

  /**
   * OrientDB server starter/stopper.
   */
  private final DatabaseServiceStarter control = new DatabaseServiceStarter();

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public void startup() {
    active.compareAndSet(false, true);
  }

  @Override
  public void shutdown() {
    active.compareAndSet(true, false);
    control.shutdown();
  }

  @Override
  public OrientDbDocumentDatabaseEndpoint getOrientDbDocumentDatabaseEndpoint(String databaseName,
      Log log) {
    String databaseUrl = DATABASE_URL_PROTOCOL_PLOCAL
        + getSpaceEnvironment().getFilesystem().getDataDirectory(getName()).getAbsolutePath()
        + File.separator + databaseName;
    return getOrientDbDocumentDatabaseEndpoint(databaseUrl, DEFAULT_LOGIN, DEFAULT_PASSWORD, log);
  }

  @Override
  public OrientDbDocumentDatabaseEndpoint getOrientDbDocumentDatabaseEndpoint(File dbDirectory,
      Log log) {
    String databaseUrl = DATABASE_URL_PROTOCOL_PLOCAL + dbDirectory.getAbsolutePath();
    return getOrientDbDocumentDatabaseEndpoint(databaseUrl, DEFAULT_LOGIN, DEFAULT_PASSWORD, log);
  }

  @Override
  public OrientDbDocumentDatabaseEndpoint getOrientDbDocumentDatabaseEndpoint(String databaseUrl,
      String login, String password, Log log) {
    return new StandardOrientDbDocumentDatabaseEndpoint(this, databaseUrl, login, password, log);
  }

  /**
   * Opens a database with the given URL and credentials. If the database does
   * not exist, it will be created.
   *
   * @param url
   *          path to the database to open
   * @param login
   *          login for the database access
   * @param password
   *          password for the database access
   *
   * @return open database
   */
  ODatabaseDocumentTx createOrOpenDatabase(String url, String login, String password) {
    Preconditions.checkState(active.get());
    control.startup();
    ODatabaseDocumentTx result = new ODatabaseDocumentTx(url);
    if (!result.exists()) {
      result.create();
    } else {
      result.open(login, password);
    }
    return result;
  }

  /**
   * Provide one off startup and shutdown of the database.
   *
   * @author Sasha Kelepko
   * @author Keith M. Hughes
   */
  private static class DatabaseServiceStarter {
    /**
     * The database server.
     */
    private OServer orientDbServer;

    /**
     * Starts the database server.
     */
    public synchronized void startup() {
      if (orientDbServer == null) {
        OServerConfiguration config = new OServerConfiguration();

        config.network = new OServerNetworkConfiguration();

        String implementation = PROTOCOL_IMPLEMENTATION;
        String protocolName = PROTOCOL;
        OServerNetworkProtocolConfiguration protocol =
            new OServerNetworkProtocolConfiguration(protocolName, implementation);
        config.network.protocols = ImmutableList.of(protocol);

        OServerNetworkListenerConfiguration listener = new OServerNetworkListenerConfiguration();
        listener.ipAddress = IP_ADDRESS;
        listener.portRange = PORT_RANGE;
        listener.protocol = protocolName;
        config.network.listeners = ImmutableList.of(listener);

        OServerUserConfiguration user = new OServerUserConfiguration(SERVER_USER_LOGIN,
            SERVER_USER_PASSWORD, SERVER_USER_RESOURCES);
        config.users = new OServerUserConfiguration[] { user };

        config.properties = new OServerEntryConfiguration[] {
            new OServerEntryConfiguration(CONFIGURATION_CACHE_STATIC_RESOURCES, "false"),
            new OServerEntryConfiguration(CONFIGURATION_LOG_CONSOLE_LEVEL, "info"),
            new OServerEntryConfiguration(CONFIGURATION_LOG_FILE_LEVEL, "fine"),
            new OServerEntryConfiguration(CONFIGURATION_PLUGIN_DYNAMIC, "false") };
        try {
          orientDbServer = OServerMain.create();
          orientDbServer.startup(config);
          orientDbServer.activate();
        } catch (Exception e) {
          throw new SmartSpacesException("Could not start up the orientDB server", e);
        }
      }
    }

    /**
     * Shuts down the database server.
     */
    public synchronized void shutdown() {
      if (orientDbServer != null) {
        orientDbServer.shutdown();
        orientDbServer = null;
      }
    }
  }
}
