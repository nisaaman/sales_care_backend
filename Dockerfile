#FROM maven:3.6.3 AS maven

#WORKDIR /home/sales-care-app

#COPY . /home/sales-care-app

#RUN mvn package

FROM openjdk:8
WORKDIR /app

#COPY --from=maven /home/sales-care-app/target/sales-care-app.jar /app

EXPOSE 8090
ADD target/sales-care-app.jar sales-care-app.jar
ENTRYPOINT ["java", "-jar", "sales-care-app.jar"]