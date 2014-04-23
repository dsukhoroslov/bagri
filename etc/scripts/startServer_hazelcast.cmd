cd ..\..\bagri-xdm-cache\bagri-xdm-cache-hazelcast
start "SERVER NODE HAZELCAST" mvn -Drun-cache -Ptest -DskipTests=true --offline package
cd ..\..\etc\scripts
