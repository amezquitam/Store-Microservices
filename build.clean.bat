cd gateway; mvn clean package -DskipTests; cd ..;
cd inventory-service; mvn clean package -DskipTests; cd ..;
cd order-service; mvn clean package -DskipTests; cd ..;
cd product-service; mvn clean package -DskipTests; cd ..;
cd payment-service; mvn clean package -DskipTests; cd ..;
cd eureka-server; mvn clean package -DskipTests; cd ..;