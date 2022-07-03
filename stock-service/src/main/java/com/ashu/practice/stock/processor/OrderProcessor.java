package com.ashu.practice.stock.processor;

import com.ashu.practice.common.Constants;
import com.ashu.practice.common.model.Order;
import com.ashu.practice.stock.service.OrderManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public record OrderProcessor(OrderManageService orderManageService) {


    @KafkaListener(id = "orders", topics = Constants.TOPIC_ORDERS, groupId = "${spring.application.name:stock}")
    public void onEvent(Order o) {
        log.info("Received: {}", o);
        if (o.getStatus().equals("NEW"))
            orderManageService.reserve(o);
        else
            orderManageService.confirm(o);
    }

}
