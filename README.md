# home-server

![Build home-server](https://github.com/bassages/home-server/workflows/Build%20home-server/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=coverage)](https://sonarcloud.io/dashboard?id=home-server)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=code_smells)](https://sonarcloud.io/dashboard?id=home-server)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=bugs)](https://sonarcloud.io/dashboard?id=home-server)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=home-server&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=home-server)

## Features
- Provides a RESTful API to store and query data related to a [smart meter](https://en.wikipedia.org/wiki/Smart_meter)
- Provides a RESTful API to store and query data related to environmental sensor readings (temperature and humidity)
- Ability to send gas meter readings to [mindergas.nl](https://mindergas.nl) on a daily basis

## Links
* [home-sensors](https://github.com/bassages/home-sensors)
* [home-frontend](https://github.com/bassages/home-frontend)

## Technical notes

### SSL Certificates / https

#### Create keystore with a self-signed certificate

```
keytool -genkeypair -alias home-server-localhost -dname "cn=localhost, ou=home-server, o=home-server, l=Deventer, s=Overijssel, c=NL" -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore home-server-localhost-keystore.p12 -validity 3650 -ext san=dns:localhost,ip:127.0.0.1,ip:::1
```
When prompted for a password, supply a strong one, you could for example generate one on https://passwordsgenerator.net/.

For more information about **keytool**, see [keytool tutorial by Oracle](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html).

If you omit the `-ext san=`... part, Chrome Developer Tools complains with a message like ```Invalid self signed SSL cert``` and ```Subject Alternative Name Missing```

#### Export public key
```
keytool -export -alias home-server-localhost -keystore home-server-localhost-keystore.p12 -rfc -file home-server-localhost.cer
```

This command effectively:
1. Reads from the keystore file named `home-server-localhost-keystore.p12`.
2. Looks in that file for the alias named `home-server-localhost`.
3. Exports the public key to the new file named `home-server-localhost.cer`.

For more information about the content of the `-dname` parameter, see:
[Certificate Attributes tutorial by Oracle](https://docs.oracle.com/cd/E24191_01/common/tutorials/authz_cert_attributes.html).

#### Configure Spring Boot to use the keystore on the server

Add to `application.properties`:

```
# The format used for the keystore. It could be set to JKS in case it is a JKS file
server.ssl.key-store-type=PKCS12
# The path to the keystore containing the certificate
server.ssl.key-store=classpath:home-server-localhost-keystore.p12
# The password used to generate the certificate
server.ssl.key-store-password=<replace with password that was created in step "Create keystore with a self-signed certificate">
# The alias mapped to the certificate
server.ssl.key-alias=home-server-localhost
```

Futhermore:

1. Make sure to require a secure channel:
```
    public void configure(final HttpSecurity http) throws Exception {
        http
            .requiresChannel()
            .anyRequest().requiresSecure()
            .and()
```

2. Change the server port, 443 is the default port for https, example `application.properties`:
```
server.port=8443
```

#### Create a truststore to be used by java clients that make requests to the server
```
keytool -importcert -file home-server-localhost.cer -keystore home-sensors-truststore-localhost.p12 -alias "home-server-localhost"
```
When prompted for a password, supply a strong one, you could for example generate one on https://passwordsgenerator.net/.
Use a different password than the one used for the keystore (see "Create keystore with a self-signed certificate").

#### Use truststore from client
Add VM options:
`-Djavax.net.ssl.trustStore=<absolute-path-to->home-sensors-truststore-rpi.p12
-Djavax.net.ssl.trustStorePassword=<password, see "Create a truststore">`

When connection fails, add the following VM option and retry: `-Djavax.net.debug=all`. Inspect the logging for hints about what is wrong.
Furthermore read the following acticle [Handshake failure scenarios on Baeldung](https://www.baeldung.com/java-ssl-handshake-failures#handshake_failure_scenarios)

### Tools

KeyStore Explorer is an open source GUI replacement for the Java command-line utilities keytool and jarsigner. 
https://keystore-explorer.org/

## Common development tasks

### Update versions

#### Gradle
For this, you can use the following command: `gradlew wrapper --gradle-version <new version>`

### Dependencies
For this, you can use the following command: `gradlew dependencyUpdates`.
Inspect the output of the command, update build.gradle manually and test if the application still builds and executes correctly.

### JDK
On the Raspberry PI, the *Liberica JDK* distribution is used.
See the [downloads page](https://bell-sw.com/pages/downloads/) and look for "Linux" :arrow_right: "ARM" :arrow_right: ".tar.gz".
On the RPI:
- download it: `cd /opt`, `sudo wget <URL of the file to be downloaded>`
- unpack it (`sudo tar xvfz <name of the file>.tar.gz`)
- update [./etc/script/start-home-server.sh]

## Compact database file
1. Login in to the application
2. Go to the H2 console, eg:
   ```
   http://home:9090/h2-console/
   ```
3. Connect with the following connection string
   ```
   jdbc:h2:~/home-server/database/home-server-database;DB_CLOSE_ON_EXIT=FALSE
   ```
4. Execute the following SQL statement:
   ```   
   SHUTDOWN COMPACT
   ```