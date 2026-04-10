# https://learn.microsoft.com/en-us/azure/application-gateway/self-signed-certificates
set -Eeuo pipefail

rm *.p12

# CA Key & Certificate
echo "CA Setup"
openssl ecparam -out root-ca.key -name prime256v1 -genkey
openssl req -new -sha256 -key root-ca.key -out root-ca.csr \
    -subj "/C=XX/ST=StateName/L=CityName/O=CompanyName/OU=CompanySectionName/CN=certificate-authority"
openssl x509 -req -in root-ca.csr -signkey root-ca.key -out root-ca.crt -days 3000  \
  -extensions v3_ca -extfile openssl.cnf

# Debug:
# openssl x509 -in root-ca.crt -text -noout

# Server intermediary CA
echo "Server Intermediary CA"
openssl ecparam -out server-intermediary.key -name prime256v1 -genkey
openssl req -new -sha256 -key server-intermediary.key -out server-intermediary.csr \
  -subj "/C=XX/ST=StateName/L=CityName/O=CompanyName/OU=CompanySectionName/CN=server-intermediary"
openssl x509 -req -in server-intermediary.csr -sha256 -out server-intermediary.crt -days 3000  \
  -extensions v3_intermediate_ca -extfile openssl.cnf \
  -CA root-ca.crt -CAkey root-ca.key -CAcreateserial \

# server SSL key & CSR
echo "Server Cert"
openssl ecparam -out server.key -name prime256v1 -genkey
openssl req -new -sha256 -key server.key -out server.csr \
  -subj "/C=XX/ST=StateName/L=CityName/O=CompanyName/OU=CompanySectionName/CN=server"
openssl x509 -req -in server.csr -sha256 -out server.crt -days 3000  \
  -extensions server_cert -extfile openssl.cnf \
  -CA server-intermediary.crt -CAkey server-intermediary.key -CAcreateserial \


# Truststore for server certs

echo "Server Truststore"
cat server-intermediary.crt root-ca.crt > server-chain.pem
keytool -importcert -trustcacerts -alias root-ca -file root-ca.crt \
  -keystore server-truststore.p12 -storetype PKCS12 -storepass password -noprompt
keytool -importcert -trustcacerts -alias client-ca -file client-intermediary.crt \
  -keystore server-truststore.p12 -storetype PKCS12 -storepass password -noprompt

echo "Server Keystore"
openssl pkcs12 -export -in server.crt -inkey server.key -chain -CAfile server-chain.pem \
  -out server-keystore.p12 -name "server-keystore" -password pass:password

## Client intermediary CA
echo "Client Intermediary CA"
openssl ecparam -out client-intermediary.key -name prime256v1 -genkey
openssl req -new -sha256 -key client-intermediary.key -out client-intermediary.csr \
  -subj "/C=XX/ST=StateName/L=CityName/O=CompanyName/OU=CompanySectionName/CN=client-intermediary"
openssl x509 -req -in client-intermediary.csr -sha256 -out client-intermediary.crt -days 3000  \
  -extensions v3_intermediate_ca -extfile openssl.cnf \
  -CA root-ca.crt -CAkey root-ca.key -CAcreateserial \

## Client Cert
openssl ecparam -out client.key -name prime256v1 -genkey
openssl req -new -sha256 -key client.key -out client.csr \
  -subj "/C=XX/ST=StateName/L=CityName/O=CompanyName/OU=CompanySectionName/CN=client"
openssl x509 -req -in client.csr -sha256 -out client.crt -days 3000  \
  -CA client-intermediary.crt -CAkey client-intermediary.key -CAcreateserial \


echo "Client Truststore"
# Sadly we need to use keytool in lieu of openssl for storing CA certs
# since this adds some Java specific goodness that openssl doesn't
cat client-intermediary.crt root-ca.crt > client-chain.pem
keytool -importcert -trustcacerts -alias root-ca -file root-ca.crt \
  -keystore client-truststore.p12 -storetype PKCS12 -storepass password -noprompt
keytool -importcert -trustcacerts -alias server-ca -file server-intermediary.crt \
  -keystore client-truststore.p12 -storetype PKCS12 -storepass password -noprompt

echo "Client Keystore"
openssl pkcs12 -export -in client.crt -inkey client.key -chain -CAfile client-chain.pem \
  -out client-keystore.p12 -name "client-keystore" -password pass:password

echo "Delete unused files"
rm *.srl
rm *.csr
