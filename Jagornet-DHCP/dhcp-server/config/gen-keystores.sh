#!/bin/sh

set -euxo pipefail

# Generate server keystore and certificate
keytool -genkeypair -alias server \
                    -keyalg RSA \
                    -keysize 2048 \
                    -keystore jagornet_server_keystore.p12 \
                    -storetype PKCS12 \
                    -ext san=dns:jagornet-server
keytool -exportcert -alias server -keystore jagornet_server_keystore.p12 -file server.cer

# Generate client keystore and certificate
keytool -genkeypair -alias client \
                    -keyalg RSA \
                    -keysize 2048 \
                    -keystore jagornet_client_keystore.p12 \
                    -storetype PKCS12 \
                    -ext san=dns:jagornet-client
keytool -exportcert -alias client -keystore jagornet_client_keystore.p12 -file client.cer

# Import client certificate into server's truststore
keytool -importcert -alias client -keystore jagornet_server_truststore.p12 -storetype PKCS12 -file client.cer

# Import server certificate into client's truststore
keytool -importcert -alias server -keystore jagornet_client_truststore.p12 -storetype PKCS12 -file server.cer