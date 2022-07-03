package com.ashu.practice.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private long id;
    private long customerId;
    private long productId;
    private int productCount;
    private int price;
    private String status;
    private String source;
}
