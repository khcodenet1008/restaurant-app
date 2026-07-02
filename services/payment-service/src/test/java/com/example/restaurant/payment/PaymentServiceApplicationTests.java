package com.example.restaurant.payment;

import com.example.restaurant.payment.service.PaymentEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.example.restaurant.payment.repository.PaymentAuthorizationRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class PaymentServiceApplicationTests {

    @MockBean
    private PaymentAuthorizationRepository paymentAuthorizationRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        PaymentEventPublisher paymentEventPublisher() {
            return new PaymentEventPublisher(null, new ObjectMapper(), "test.payment.events") {
                @Override
                public void publish(
                        String orderId,
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
