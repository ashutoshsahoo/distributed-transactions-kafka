package com.ashu.practice.stock;

import com.ashu.practice.stock.domain.Product;
import com.ashu.practice.stock.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;


import java.util.Random;

@SpringBootApplication
@EnableKafka
public class StockServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockServiceApplication.class, args);
	}

	@Autowired
	private ProductRepository repository;

	@PostConstruct
	public void generateData() {
		final Random secureRandom = new Random();
		for (int i = 0; i < 1000; i++) {
			int count = secureRandom.nextInt(1000);
			Product p = new Product(null, "Product" + i, count, 0);
			repository.save(p);
		}
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		//The following code enable observation in the consumer listener
		factory.getContainerProperties().setObservationEnabled(true);
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}
}
