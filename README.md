# Intergrate HashiCorp Vault with WSO2 Identity Server

## Setting up

1. Start HashiCorp vault server and create a new **kv engine**.

   Enter a Path name when creating the kv engine (Eg: `wso2is`).
  
   Following commands can be used to add secrets with the HashiCorp vault.
   
   ```
   # Create a new kv engine
   vault secrets enable -path=wso2is -version=2 kv
   
   # Add new secret
   vault kv put wso2is/keystore_password value=wso2carbon
   
   # Get a secret (To check)
   vault kv get -field=value wso2is/keystore_password
   ```
   
2. Open the `deployment.toml` file in the `<IS_HOME>/repository/conf/` directory and add
   the `[secrets]` configuration section **at the bottom of the file** as shown below.
   Give an alias for the password type followed by the actual password. The following example
   lists the most common passwords in configuration files.

    ```toml
    [secrets]
    admin_password = ""
    keystore_password = ""
    key_password = ""
    truststrore_password = ""
    "log4j.appender.LOGEVENT.password" = ""
    ```

3. Refer the encrypted passwords in the relevant configuration files:
The `deployment.toml` file.

   You can add the encrypted password to the relevant sections in the `deployment.toml` file by using 
a place holder: `$secret{alias}`. For example:

    ```toml
    [super_admin]
    username="admin"
    password="$secret{admin_password}"
    
    [keystore.tls]
    password = "$secret{keystore_password}" 
    alias = "$secret{keystore_password}" 
    key_password = "$secret{key_password}"  
    
    [truststore]                  
    password = "$secret{keystore_password}" 
    ```

4. Build the HashiCorp Vault Integration OSGI bundle using `mvn clean install` and copy
the `target/org.wso2.carbon.securevault.hashicorp-1.0.jar` file to `<IS_HOME>/repository/components/dropin/`
directory.

5. Add **HashiCorp Vault Java Driver** (Eg: `vault-java-driver-5.1.0.jar`) to the
`<IS_HOME>/repository/components/lib/` directory.

6. Open `/repository/conf/security/secret-conf.properties` file and set following configurations.
    ```
    keystore.identity.location=repository/resources/security/wso2carbon.jks
    keystore.identity.type=JKS
    keystore.identity.store.password=identity.store.password
    keystore.identity.store.secretProvider=org.wso2.carbon.securevault.DefaultSecretCallbackHandler
    keystore.identity.key.password=identity.key.password
    keystore.identity.key.secretProvider=org.wso2.carbon.securevault.DefaultSecretCallbackHandler
    carbon.secretProvider=org.wso2.securevault.secret.handler.SecretManagerSecretCallbackHandler
    
    secVault.enabled=true
    secretRepositories=vault
    secretRepositories.vault.provider=org.wso2.carbon.securevault.hashicorp.repository.HashiCorpSecretRepositoryProvider
    secretRepositories.vault.properties.address=http://127.0.0.1:8200
    secretRepositories.vault.properties.namespace=ns1
    secretRepositories.vault.properties.enginePath=wso2is
    secretRepositories.vault.properties.engineVersion=2
    ```

7. Add following lines to the `<IS_HOME>/repository/conf/log4j2.properties` file
    ```
    logger.org-wso2-carbon-securevault-hashicorp.name=org.wso2.carbon.securevault.hashicorp
    logger.org-wso2-carbon-securevault-hashicorp.level=INFO
    logger.org-wso2-carbon-securevault-hashicorp.additivity=false
    logger.org-wso2-carbon-securevault-hashicorp.appenderRef.CARBON_CONSOLE.ref = CARBON_CONSOLE
    ```
   Then append `org-wso2-carbon-securevault-hashicorp` to the `loggers` list in the same file as follows.
   <pre>
   loggers = AUDIT_LOG, trace-messages, ... ,<b>org-wso2-carbon-securevault-hashicorp<b>
   </pre>

   
8. Start the WSO2 Identity Server and enter the keystore password at the startup when prompted.
   ```
   [Enter KeyStore and Private Key Password :] wso2carbon
   ```
