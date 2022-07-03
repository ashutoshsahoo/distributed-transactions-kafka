package com.ashu.practice.payment.processor;

import com.ashu.practice.common.Constants;
import com.ashu.practice.common.model.Order;
import com.ashu.practice.payment.service.OrderManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public record OrderProcessor(OrderManagementService orderManagementService) {

    @KafkaListener(id = "orders", topics = Constants.TOPIC_ORDERS, groupId = "${spring.application.name:payment}")
    public void onEvent(Order o) {
        log.info("Received: {}", o);
        if (o.getStatus().equals("NEW"))
            orderManagementService.reserve(o);
        else
            orderManagementService.confirm(o);
    }
}
