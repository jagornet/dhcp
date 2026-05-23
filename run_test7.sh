cd Jagornet-DHCP
mvn clean test -Dtest=TestSubnetSelection -Dsurefire.failIfNoSpecifiedTests=false -pl dhcp-server -am
