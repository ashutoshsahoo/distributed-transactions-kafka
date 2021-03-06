package com.ashu.practice.order;

import com.ashu.practice.common.Constants;
import com.ashu.practice.common.model.Order;
import com.ashu.practice.order.service.OrderManagementService;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableKafkaStreams
@EnableAsync
@Slf4j
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public NewTopic topicOrder() {
        return TopicBuilder.name(Constants.TOPIC_ORDERS).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic topicOrderPayment() {
        return TopicBuilder.name(Constants.TOPIC_ORDERS_PAYMENT).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic topicOrderStock() {
        return TopicBuilder.name(Constants.TOPIC_ORDERS_STOCK).partitions(6).replicas(3).build();
    }

    @Value("${spring.kafka.properties.schema.registry.url:http://localhost:8081}")
    private String schemaRegistryUrl;

    @Autowired
    private OrderManagementService orderManagementService;

    private Serde<Order> orderValueSerde = null;


    @PostConstruct
    public void initialize() {
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
                schemaRegistryUrl);
        orderValueSerde = new SpecificAvroSerde<>();
        orderValueSerde.configure(serdeConfig, false);
    }

    @Bean
    public KStream<Long, Order> stream(StreamsBuilder builder) {
        KStream<Long, Order> stream = builder
                .stream(Constants.TOPIC_ORDERS_PAYMENT, Consumed.with(Serdes.Long(), orderValueSerde));

        stream.join(builder.stream(Constants.TOPIC_ORDERS_STOCK),
                        orderManagementService::confirm,
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(10)),
                        StreamJoined.with(Serdes.Long(), orderValueSerde, orderValueSerde))
                .peek((k, o) -> log.info("Output: {}", o))
                .to(Constants.TOPIC_ORDERS);
        return stream;
    }

    @Bean
    public KTable<Long, Order> table(StreamsBuilder builder) {
        KeyValueBytesStoreSupplier store =
                Stores.persistentKeyValueStore("orders");
        KStream<Long, Order> stream = builder
                .stream(Constants.TOPIC_ORDERS, Consumed.with(Serdes.Long(), orderValueSerde));
        return stream.toTable(Materialized.<Long, Order>as(store)
                .withKeySerde(Serdes.Long())
                .withValueSerde(orderValueSerde));
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("kafkaSender-");
        executor.initialize();
        return executor;
    }

}
