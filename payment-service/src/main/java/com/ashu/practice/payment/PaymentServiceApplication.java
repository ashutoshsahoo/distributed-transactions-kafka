package com.ashu.practice.payment;

import com.ashu.practice.payment.domain.Customer;
import com.ashu.practice.payment.repository.CustomerRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

import javax.annotation.PostConstruct;
import java.util.Random;

@SpringBootApplication
@EnableKafka
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

    @Autowired
    private CustomerRepository repository;

    @PostConstruct
    public void generateData() {
        final Random secureRandom = new Random();
        Faker faker = new Faker();
        for (int i = 0; i < 100; i++) {
            int count = secureRandom.nextInt(1000);
            Customer c = new Customer(null, faker.name().fullName(), count, 0);
            repository.save(c);
        }
    }
}
