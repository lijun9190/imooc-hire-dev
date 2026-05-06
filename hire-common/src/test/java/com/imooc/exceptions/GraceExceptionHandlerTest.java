package com.imooc.exceptions;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GraceExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GraceExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void returnsCustomExceptionStatus() throws Exception {
        mockMvc.perform(get("/custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatusEnum.USER_NOT_EXIST_ERROR.status()))
                .andExpect(jsonPath("$.msg").value(ResponseStatusEnum.USER_NOT_EXIST_ERROR.msg()))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void returnsJwtExpiredStatus() throws Exception {
        mockMvc.perform(get("/expired-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatusEnum.JWT_EXPIRE_ERROR.status()))
                .andExpect(jsonPath("$.msg").value(ResponseStatusEnum.JWT_EXPIRE_ERROR.msg()))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void returnsJwtSignatureStatusForMalformedToken() throws Exception {
        mockMvc.perform(get("/malformed-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatusEnum.JWT_SIGNATURE_ERROR.status()))
                .andExpect(jsonPath("$.msg").value(ResponseStatusEnum.JWT_SIGNATURE_ERROR.msg()))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void returnsFieldErrorsForInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatusEnum.FAILED.status()))
                .andExpect(jsonPath("$.data.name").value("名称不能为空"));
    }

    @Test
    void returnsSystemErrorForUnexpectedException() throws Exception {
        mockMvc.perform(get("/unexpected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ResponseStatusEnum.SYSTEM_ERROR.status()))
                .andExpect(jsonPath("$.msg").value(ResponseStatusEnum.SYSTEM_ERROR.msg()))
                .andExpect(jsonPath("$.success").value(false));
    }

    @RestController
    static class TestController {

        @GetMapping("/custom")
        GraceJSONResult custom() {
            throw new MyCustomException(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        @GetMapping("/expired-jwt")
        GraceJSONResult expiredJwt() {
            throw new ExpiredJwtException(null, null, "expired");
        }

        @GetMapping("/malformed-jwt")
        GraceJSONResult malformedJwt() {
            throw new MalformedJwtException("malformed");
        }

        @PostMapping("/valid")
        GraceJSONResult valid(@Valid @RequestBody ValidRequest request) {
            return GraceJSONResult.ok(request);
        }

        @GetMapping("/unexpected")
        GraceJSONResult unexpected() {
            throw new RuntimeException("boom");
        }
    }

    static class ValidRequest {
        @NotBlank(message = "名称不能为空")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
