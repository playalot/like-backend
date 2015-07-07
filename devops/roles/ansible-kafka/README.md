#Kafka
Installs [kafka](https://kafka.apache.org/)

##Dependency
- ansible-zookeeper
- ansible-java

##Requirements
- zookeeper_hosts - comma separated list of host:port pairs.

##Optional
- kafka_listen_address - defines a specifc address for kafka to listen on.
- If running as a cluster set `kafka_id` for each node.

##License
Apache

