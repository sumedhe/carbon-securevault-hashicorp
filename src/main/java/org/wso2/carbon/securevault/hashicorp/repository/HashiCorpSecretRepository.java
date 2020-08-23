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
package org.wso2.carbon.securevault.hashicorp.repository;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.securevault.hashicorp.config.HashiCorpVaultConfig;
import org.wso2.securevault.keystore.IdentityKeyStoreWrapper;
import org.wso2.securevault.keystore.TrustKeyStoreWrapper;
import org.wso2.securevault.secret.SecretRepository;

import java.util.Properties;

import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.TOKEN_PARAMETER;
import static org.wso2.carbon.securevault.hashicorp.common.HashiCorpVaultConstants.VALUE_PARAMETER;

/**
 * HashiCorp Secret Repository.
 */
public class HashiCorpSecretRepository implements SecretRepository {

    private static final Log LOG = LogFactory.getLog(HashiCorpSecretRepository.class);
    private static final String SLASH = "/";

    private SecretRepository parentRepository;
    private IdentityKeyStoreWrapper identityKeyStoreWrapper;
    private TrustKeyStoreWrapper trustKeyStoreWrapper;
    private String token;

    public HashiCorpSecretRepository(IdentityKeyStoreWrapper identityKeyStoreWrapper,
                                     TrustKeyStoreWrapper trustKeyStoreWrapper) {

        this.identityKeyStoreWrapper = identityKeyStoreWrapper;
        this.trustKeyStoreWrapper = trustKeyStoreWrapper;
    }

    /**
     * Initializes the repository based on provided properties.
     *
     * @param properties Configuration properties
     * @param id         Identifier to identify properties related to the corresponding repository
     */
    @Override
    public void init(Properties properties, String id) {

        LOG.info("Initializing HashiCorp Secure Vault");

        token = System.getenv(TOKEN_PARAMETER);
        if (token == null || token.isEmpty()) {
            LOG.error("VAULT_TOKEN environment variable is not set");
        }

        if (HashiCorpVaultConfig.getEngineVersion() != 2) {
            LOG.error("Supported engine version: 2");
        }
    }

    /**
     * Get Secret from the Secret Repository
     *
     * @param alias Alias name for look up a secret
     * @return Secret if there is any, otherwise, alias itself
     * @see SecretRepository
     */
    @Override
    public String getSecret(String alias) {

        if (StringUtils.isEmpty(alias)) {
            return alias;
        }

        StringBuilder sb = new StringBuilder()
                .append(HashiCorpVaultConfig.getEnginePath())
                .append(SLASH)
                .append(alias);

        String secret = null;
        try {

            final VaultConfig config = new VaultConfig()
                    .address(HashiCorpVaultConfig.getAddress())
                    .token(token)
                    .engineVersion(HashiCorpVaultConfig.getEngineVersion())
                    .build();

            Vault vault = new Vault(config);
            Logical logical = vault.logical();
            if (HashiCorpVaultConfig.getNamespace() != null) {
                logical = logical.withNameSpace(HashiCorpVaultConfig.getNamespace());
            }
            secret = logical.read(sb.toString()).getData()
                    .get(VALUE_PARAMETER);

            if (secret == null) {
                LOG.error("Cannot read the vault secret from the HashiCorp vault. " +
                        "Check whether the VAULT_TOKEN is correct and the secret path is available: " + sb.toString());
            }
        } catch (VaultException e) {
            LOG.error("Error while reading the vault secret value for key: " + sb.toString(), e);
        }

        return secret;
    }

    /**
     * Get Encrypted data.
     *
     * @param alias Alias of the secret
     * @return
     */
    @Override
    public String getEncryptedData(String alias) {

        throw new UnsupportedOperationException();
    }

    /**
     * Set parent repository.
     *
     * @param parent Parent secret repository
     */
    @Override
    public void setParent(SecretRepository parent) {

        this.parentRepository = parent;
    }

    /**
     * Get parent repository.
     *
     * @return
     */
    @Override
    public SecretRepository getParent() {

        return this.parentRepository;
    }
}
