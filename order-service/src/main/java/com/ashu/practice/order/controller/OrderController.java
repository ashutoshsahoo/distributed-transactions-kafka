package com.ashu.practice.order.controller;

import com.ashu.practice.common.Constants;
import com.ashu.practice.common.model.Order;
import com.ashu.practice.common.model.OrderKey;
import com.ashu.practice.order.dto.OrderDto;
import com.ashu.practice.order.service.OrderGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/orders")
@Slf4j
public record OrderController(KafkaTemplate<OrderKey, Order> template,
                              StreamsBuilderFactoryBean kafkaStreamsFactory,
                              OrderGeneratorService orderGeneratorService) {

    private static final AtomicLong id = new AtomicLong();

    @PostMapping
    public Order create(@RequestBody Order order) {
        order.setId(id.incrementAndGet());
        template.send(Constants.TOPIC_ORDERS, new OrderKey(order.getId()), order);
        log.info("Sent: {}", order);
        return order;
    }

    @PostMapping("/generate")
    public void create() {
        orderGeneratorService.generate();
    }

    @GetMapping
    public List<OrderDto> all() {
        List<OrderDto> orders = new ArrayList<>();
        ReadOnlyKeyValueStore<OrderKey, Order> store = kafkaStreamsFactory
                .getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(
                        "orders",
                        QueryableStoreTypes.keyValueStore()));
        KeyValueIterator<OrderKey, Order> it = store.all();

        while (it.hasNext()) {
            KeyValue<OrderKey, Order> next = it.next();
            Order order = next.value;
//            log.info("Order in store: key={},value={}", next.key, order);
            OrderDto orderDto = OrderDto.builder()
                    .id(order.getId())
                    .customerId(order.getCustomerId())
                    .productId(order.getProductId())
                    .productCount(order.getProductCount())
                    .price(order.getPrice())
                    .status(order.getStatus())
                    .source(order.getSource())
                    .build();
            orders.add(orderDto);
        }

        return orders;
    }

}
