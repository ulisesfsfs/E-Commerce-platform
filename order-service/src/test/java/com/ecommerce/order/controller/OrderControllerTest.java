package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrderById_whenUserIsOwner_shouldReturn200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(5L)
                .userId("42")
                .build();

        when(orderService.getOrderById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/5")
                        .header("X-User-Id", "42")
                        .header("X-User-Roles", "ROLE_USER"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_whenUserIsDifferent_shouldReturn403() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(5L)
                .userId("42")
                .build();

        when(orderService.getOrderById(5L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/5")
                        .header("X-User-Id", "99")
                        .header("X-User-Roles", "ROLE_USER"))
                .andExpect(status().isForbidden());
    }
}