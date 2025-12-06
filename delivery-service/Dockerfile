FROM maven:3.9.9-eclipse-temurin-21 AS build  
WORKDIR /project
COPY . /project/
ENV TZ="Europe/Rome"
RUN mvn clean package -DskipTests
RUN mkdir delivery-service
EXPOSE 9002
CMD ["mvn", "exec:java", "-Dexec.mainClass=delivery_service.infrastructure.DeliveryServiceMain"]