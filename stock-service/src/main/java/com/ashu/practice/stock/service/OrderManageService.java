package com.ashu.practice.stock.service;

import com.ashu.practice.common.Constants;
import com.ashu.practice.common.model.Order;
import com.ashu.practice.common.model.OrderKey;
import com.ashu.practice.stock.domain.Product;
import com.ashu.practice.stock.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public record OrderManageService(ProductRepository repository,
                                 KafkaTemplate<OrderKey, Order> template) {

    private static final String SOURCE = "stock";

    @PostConstruct
    public void ini(){
        template.setObservationEnabled(true);
    }

    public void reserve(Order order) {
        Product product = repository.findById(order.getProductId()).orElseThrow();
        log.info("Found: {}", product);
        if (order.getStatus().equals("NEW")) {
            if (order.getProductCount() < product.getAvailableItems()) {
                product.setReservedItems(product.getReservedItems() + order.getProductCount());
                product.setAvailableItems(product.getAvailableItems() - order.getProductCount());
                order.setStatus("ACCEPT");
                repository.save(product);
            } else {
                order.setStatus("REJECT");
            }
            template.send(Constants.TOPIC_ORDERS_STOCK, new OrderKey(order.getId()), order);
            log.info("Sent: {}", order);
        }
    }

    public void confirm(Order order) {
        Product product = repository.findById(order.getProductId()).orElseThrow();
        log.info("Found: {}", product);
        if (order.getStatus().equals("CONFIRMED")) {
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            repository.save(product);
        } else if (order.getStatus().equals("ROLLBACK") && !order.getSource().equals(SOURCE)) {
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            product.setAvailableItems(product.getAvailableItems() + order.getProductCount());
            repository.save(product);
        }
    }

}
