package com.ashu.practice.order.service;

import com.ashu.practice.common.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderManagementService {

    public static final String SOURCE = "ORDER";
    public static final String CONSTANT_REJECT = "REJECT";

    public Order confirm(Order orderPayment, Order orderStock) {
        Order o = Order.newBuilder()
                .setId(orderPayment.getId())
                .setCustomerId(orderPayment.getCustomerId())
                .setProductId(orderPayment.getProductId())
                .setProductCount(orderPayment.getProductCount())
                .setPrice(orderPayment.getPrice())
                .setSource(SOURCE)
                .setStatus(orderPayment.getStatus())
                .build();
        if (orderPayment.getStatus().equals("ACCEPT") &&
                orderStock.getStatus().equals("ACCEPT")) {
            o.setStatus("CONFIRMED");
        } else if (orderPayment.getStatus().equals(CONSTANT_REJECT) &&
                orderStock.getStatus().equals(CONSTANT_REJECT)) {
            o.setStatus(CONSTANT_REJECT);
        } else if (orderPayment.getStatus().equals(CONSTANT_REJECT) ||
                orderStock.getStatus().equals(CONSTANT_REJECT)) {
            String source = orderPayment.getStatus().equals(CONSTANT_REJECT)
                    ? "PAYMENT" : "STOCK";
            o.setStatus("ROLLBACK");
            o.setSource(source);
        }
        return o;
    }
}
