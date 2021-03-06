# Intergrate HashiCorp Vault with WSO2 Identity Server

## Setting up

### Step 1: Setup HashiCorp Vault

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

### Step 2: Configure HashiCorp Vault extension

1. Build the HashiCorp Vault Integration OSGI bundle using `mvn clean install` and copy
the `target/org.wso2.carbon.securevault.hashicorp-1.0.jar` file to `<IS_HOME>/repository/components/dropin/`
directory.

2. Add **HashiCorp Vault Java Driver** (Eg: `vault-java-driver-5.1.0.jar`) to the
`<IS_HOME>/repository/components/lib/` directory.

3. Open `/repository/conf/security/secret-conf.properties` file and set following configurations.
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

4. Add following lines to the `<IS_HOME>/repository/conf/log4j2.properties` file
    ```
    logger.org-wso2-carbon-securevault-hashicorp.name=org.wso2.carbon.securevault.hashicorp
    logger.org-wso2-carbon-securevault-hashicorp.level=INFO
    logger.org-wso2-carbon-securevault-hashicorp.additivity=false
    logger.org-wso2-carbon-securevault-hashicorp.appenderRef.CARBON_CONSOLE.ref = CARBON_CONSOLE
    ```
   Then append `org-wso2-carbon-securevault-hashicorp` to the `loggers` list in the same file as follows.
   ```
   loggers = AUDIT_LOG, trace-messages, ... ,org-wso2-carbon-securevault-hashicorp
   ```

### Step 3: Update passwords with their aliases
1. Open the `deployment.toml` file in the `<IS_HOME>/repository/conf/` directory and add
   the `[secrets]` configuration section **at the bottom of the file** as shown below.
   Give an alias for the passwords and put the value as blank (`""`).

    ```toml
    [secrets]
    admin_password = ""
    keystore_password = ""
    database_password = ""
    ```
   
2. Add the encrypted password alias to the relevant sections in the `deployment.toml`
   file by using a place holder: `$secret{alias}`. For example:

    ```toml
    [super_admin]
    username="admin"
    password="$secret{admin_password}"
    
    [keystore.primary]
    file_name = "wso2carbon.jks"
    password = "$secret{keystore_password}" 
    
    [database.identity_db]
    type = "h2"
    url = "jdbc:h2:./repository/database/WSO2IDENTITY_DB;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=60000"
    username = "wso2carbon"
    password = "$secret{database_password}"
    ```

### Step 4: Start the Server

1. Set the `VAULT_TOKEN` environment variable before starting the Identity Server.
    ```
    export VAULT_TOKEN='<token>'
    ```
   
2. Start the WSO2 Identity Server and enter the keystore password at the startup when prompted.
   ```
   [Enter KeyStore and Private Key Password :] wso2carbon
   ```
