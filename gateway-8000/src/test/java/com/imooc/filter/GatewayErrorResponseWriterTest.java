package com.imooc.filter;

import com.google.gson.Gson;
import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.utils.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayErrorResponseWriterTest {

    @Test
    void writesGraceJsonErrorResponse() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/limited").build());
        GatewayErrorResponseWriter writer = new GatewayErrorResponseWriter();

        writer.write(exchange, ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP).block();

        Map<?, ?> body = responseBody(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exchange.getResponse().getHeaders().getContentType().toString()).isEqualTo("application/json");
        assertThat(((Double) body.get("status")).intValue()).isEqualTo(ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP.status());
        assertThat(body.get("msg")).isEqualTo(ResponseStatusEnum.SYSTEM_ERROR_BLACK_IP.msg());
        assertThat(body.get("success")).isEqualTo(false);
    }

    @Test
    void securityFilterWritesJwtExpiredResponse() {
        SecurityFilterJWT filter = new SecurityFilterJWT();
        ExcludeUrlProperties excludeUrlProperties = new ExcludeUrlProperties();
        excludeUrlProperties.setUrls(Collections.emptyList());
        excludeUrlProperties.setFileStart("");
        JWTUtils jwtUtils = mock(JWTUtils.class);
        when(jwtUtils.checkJWT("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        ReflectionTestUtils.setField(filter, "excludeUrlProperties", excludeUrlProperties);
        ReflectionTestUtils.setField(filter, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(filter, "gatewayErrorResponseWriter", new GatewayErrorResponseWriter());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/protected")
                        .header(SecurityFilterJWT.HEADER_USER_TOKEN,
                                BaseInfoProperties.TOKEN_SAAS_PREFIX + JWTUtils.at + "expired-token")
                        .build());

        filter.filter(exchange, ignored -> Mono.empty()).block();

        Map<?, ?> body = responseBody(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(((Double) body.get("status")).intValue()).isEqualTo(ResponseStatusEnum.JWT_EXPIRE_ERROR.status());
        assertThat(body.get("msg")).isEqualTo(ResponseStatusEnum.JWT_EXPIRE_ERROR.msg());
        assertThat(body.get("success")).isEqualTo(false);
    }

    private static Map<?, ?> responseBody(MockServerWebExchange exchange) {
        String response = exchange.getResponse().getBodyAsString().block();
        return new Gson().fromJson(response, Map.class);
    }
}
