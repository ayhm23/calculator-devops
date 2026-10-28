package com.spe.calculator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"operation\":\"add\",\"a\":2,\"b\":3,\"result\":5}"));
    }

    @Test
    void subtractReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/subtract").param("a", "10").param("b", "4"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"operation\":\"subtract\",\"a\":10,\"b\":4,\"result\":6}"));
    }

    @Test
    void multiplyReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/multiply").param("a", "6").param("b", "7"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"operation\":\"multiply\",\"a\":6,\"b\":7,\"result\":42}"));
    }

    @Test
    void divideReturnsResult() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "7").param("b", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"operation\":\"divide\",\"a\":7,\"b\":2,\"result\":3.5}"));
    }

    @Test
    void divideByZeroReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "5").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"))
                .andExpect(jsonPath("$.path").value("/api/v1/divide"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void missingParamReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Required parameter 'b' is missing"))
                .andExpect(jsonPath("$.path").value("/api/v1/add"));
    }

    @Test
    void nonNumericParamReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "abc").param("b", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parameter 'a' must be a number, got 'abc'"))
                .andExpect(jsonPath("$.path").value("/api/v1/add"));
    }

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
