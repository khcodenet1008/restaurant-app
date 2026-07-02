package com.example.restaurant.order;

import com.example.restaurant.order.repository.CustomerOrderRepository;
import com.example.restaurant.order.repository.OrderItemRepository;
import com.example.restaurant.order.repository.SagaStateRepository;
import com.example.restaurant.order.service.OrderEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "spring.kafka.listener.auto-startup=false"
})
class OrderServiceApplicationTests {

    @MockBean
    private CustomerOrderRepository customerOrderRepository;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @MockBean
    private SagaStateRepository sagaStateRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        OrderEventPublisher orderEventPublisher() {
            return new OrderEventPublisher(null, new ObjectMapper(), "test.order.events", 1000L) {
                @Override
                public void publish(
                        String orderId,
                        String eventId,
                        String eventType,
                        String sagaId,
                        String traceId,
                        Map<String, Object> payload) {
                    // No-op publisher for context loading tests.
                }
            };
        }
    }
}
