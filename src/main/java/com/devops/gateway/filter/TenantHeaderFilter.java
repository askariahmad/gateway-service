package com.devops.gateway.filter;

import com.devops.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TenantHeaderFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public TenantHeaderFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    String tenantId = jwtUtil.getTenantIdFromToken(token);
                    if (tenantId != null) {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-Tenant-Id", tenantId)
                                .build();
                        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                        return chain.filter(mutatedExchange);
                    }
                }
            } catch (Exception e) {
                // Ignore token errors here, SecurityConfig handles authorization rejection.
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Run before routing
    }
}
