cd Jagornet-DHCP
mvn clean install -DskipTests
mvn test -Dtest=TestSubnetSelection
