cd Jagornet-DHCP
mvn clean install -DskipTests
mvn test -pl dhcp-server -Dtest=TestSubnetSelection
