/usr/bin/kafka-topics --create --topic "fleet-management.trip" --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
/usr/bin/kafka-topics --create --topic "fleet-management.car.position" --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
/usr/bin/kafka-topics --create --topic "fleet-management.driver.penalty-points" --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
