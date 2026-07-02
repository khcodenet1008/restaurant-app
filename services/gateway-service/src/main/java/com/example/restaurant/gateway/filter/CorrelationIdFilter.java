package com.example.restaurant.gateway.filter;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private final String headerName;

    public CorrelationIdFilter(
            @Value("${CORRELATION_HEADER_NAME:X-Correlation-Id}") String headerName) {
        this.headerName = headerName;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(headerName);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        ServerHttpRequest updatedRequest = exchange.getRequest()
                .mutate()
                .header(headerName, correlationId)
                .build();

        ServerWebExchange updatedExchange = exchange.mutate()
                .request(updatedRequest)
                .build();

        updatedExchange.getResponse().getHeaders().set(headerName, correlationId);
        return chain.filter(updatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
