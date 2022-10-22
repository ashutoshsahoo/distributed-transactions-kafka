package com.ashu.practice.order.service;

import com.ashu.practice.common.Constants;
import com.ashu.practice.common.model.Order;
import com.ashu.practice.common.model.OrderKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderGeneratorService {

    public static final String SOURCE = "order";
    private final KafkaTemplate<OrderKey, Order> template;

    @Async
    public void generate() {
        final Random secureRandom = new Random();
        final AtomicLong id = new AtomicLong();
        for (int i = 0; i < 10000; i++) {
            int x = secureRandom.nextInt(5) + 1;
            Order o = Order.newBuilder()
                    .setId(id.incrementAndGet())
                    .setCustomerId(secureRandom.nextLong(100) + 1)
                    .setProductId(secureRandom.nextLong(100) + 1)
                    .setStatus("NEW")
                    .setPrice(100 * x)
                    .setProductCount(x)
                    .setSource(SOURCE)
                    .build();
            log.info("Generated order:{}",o);
            template.send(Constants.TOPIC_ORDERS, new OrderKey(o.getId()), o);
        }
    }
}
