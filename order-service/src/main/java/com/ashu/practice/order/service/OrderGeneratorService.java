package com.ashu.practice.order.service;


import com.ashu.practice.common.Constants;
import com.ashu.practice.common.model.Order;
import com.ashu.practice.common.model.OrderKey;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.annotation.PostConstruct;
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
    private final Tracer tracer;
    private static final Random SECURE_RANDOM = new Random();


    @PostConstruct
    public void ini(){
        template.setObservationEnabled(true);
    }

    @Async
    public void generate() {
        final AtomicLong id = new AtomicLong();
        for (int i = 0; i < 1; i++) {
            int x = SECURE_RANDOM.nextInt(5) + 1;
            Order o = Order.newBuilder()
                    .setId(id.incrementAndGet())
                    .setCustomerId(SECURE_RANDOM.nextLong(100) + 1)
                    .setProductId(SECURE_RANDOM.nextLong(100) + 1)
                    .setStatus("NEW")
                    .setPrice(100 * x)
                    .setProductCount(x)
                    .setSource(SOURCE)
                    .build();
            log.info("Generated order:{}",o);

            log.info("I'm in the original span");
            Span newSpan = tracer.nextSpan().name(String.format("orderId-%s",o.getId())).start();
            try (Tracer.SpanInScope ws = tracer.withSpan(newSpan)) {
                log.info("I'm in the new span doing some cool work that needs its own span");
            } finally {
                newSpan.end();
            }
            log.info("I'm in the original span");

            template.send(Constants.TOPIC_ORDERS, new OrderKey(o.getId()), o);
        }
    }

}
