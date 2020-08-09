/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.securevault.hashicorp.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.securevault.hashicorp.exception.HashiCorpVaultException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.ADDRESS_PARAMETER;
import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.CONFIG_FILE_PATH;
import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.DEFAULT_ENGINE_VERSION;
import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.ENGINE_VERSION_PARAMETER;
import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.NAMESPACE_PARAMETER;
import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.ENGINE_PATH_PARAMETER;


/**
 * Configuration Loader for Vault Configurations.
 */
public class HashiCorpVaultConfig {

    private static final Log log = LogFactory.getLog(HashiCorpVaultConfig.class);

    private static String address;

    private static String namespace;

    private static String enginePath;

    private static int engineVersion;

    static {
        load();
    }

    /**
     * Load configurations.
     */
    public static void load() {

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(inputStream);
        } catch (IOException e) {
            log.error("Error while loading configurations from " + CONFIG_FILE_PATH, e);
        }

        try {
            address = getProperty(ADDRESS_PARAMETER, properties, true);
            namespace = getProperty(NAMESPACE_PARAMETER, properties, false);
            enginePath = getProperty(ENGINE_PATH_PARAMETER, properties, true);

            String version = getProperty(ENGINE_VERSION_PARAMETER, properties, false);
            engineVersion = version != null ? Integer.parseInt(version) : DEFAULT_ENGINE_VERSION;
        } catch (HashiCorpVaultException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * Get vault address.
     *
     * @return
     */
    public static String getAddress() {

        return address;
    }

    /**
     * Get namespace.
     *
     * @return
     */
    public static String getNamespace() {

        return namespace;
    }

    /**
     * Get engine path.
     *
     * @return
     */
    public static String getEnginePath() {

        return enginePath;
    }

    /**
     * Get vault engine version.
     *
     * @return
     */
    public static int getEngineVersion() {

        return engineVersion;
    }

    /**
     * Check existence and get property
     *
     * @param key Key
     * @param properties Properties
     * @param required Property is required
     * @return
     * @throws HashiCorpVaultException
     */
    private static String getProperty(String key, Properties properties, boolean required) throws HashiCorpVaultException {

        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        } else {
            if (!required) {
                return null;
            } else {
                throw new HashiCorpVaultException("Required configuration is not found: " + key);
            }
        }
    }
}
