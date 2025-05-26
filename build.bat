cd gateway; mvn package -DskipTests; cd ..;
cd inventory-service; mvn package -DskipTests; cd ..;
cd order-service; mvn package -DskipTests; cd ..;
cd product-service; mvn package -DskipTests; cd ..;
cd payment-service; mvn package -DskipTests; cd ..;
cd eureka-server; mvn package -DskipTests; cd ..;