# Distributed Transactions in Microservices with Kafka Streams and Spring Boot

## Run application

* Use docker to deploy kafka with registry (Use reference project to download docker compose yml file).

```shell
docker compose -f zk-single-kafka-multiple-schema-registry-ui.yml up -d
```

Open UI for Apache Kafka at [http://localhost:8084/](http://localhost:8084/).

* Run gradle build to generate avro source files

```shell
cd common-lib
gradle clean build
```

* Run application
    * order-service

  ```shell
  cd order-service
  gradle clean bootRun
  ```
    * payment-service

  ```shell
  cd payment-service
  gradle clean bootRun
  ```
    * stock-service

  ```shell
  cd stock-service
  gradle clean bootRun
  ```

* Use following request to generate orders

```shell
curl --location --request POST 'http://localhost:8080/orders/generate'
```

* Use following request to check orders status

```shell
curl --location --request GET 'http://localhost:8080/orders'
```

## Stop application

* Use ctrl+c to cancel running applications
* Use following command to stop and remove kafka containers

```shell
docker compose -f zk-single-kafka-multiple-schema-registry-ui.yml down
```

## Reference

* [Spring Boot Kafka](https://github.com/ashutoshsahoo/gs-spring-boot-kafka)
* [Distributed Transactions in Microservices with Kafka Streams and Spring Boot](https://piotrminkowski.com/2022/01/24/distributed-transactions-in-microservices-with-kafka-streams-and-spring-boot/)
* [Kafka Streams Data Types and Serialization](https://docs.confluent.io/platform/current/streams/developer-guide/datatypes.html)
