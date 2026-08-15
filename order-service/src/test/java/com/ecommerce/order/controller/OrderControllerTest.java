package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrderById_whenUserIsOwner_shouldReturn200() throws Exception {
        OrderResponse response = mock(OrderResponse.class);
        when(response.getUserId()).thenReturn("42");

        when(orderService.getOrderById(eq(5L), eq("42"), eq("ROLE_USER"))).thenReturn(response);
        // for other requester (99) simulate forbidden
        when(orderService.getOrderById(eq(5L), eq("99"), eq("ROLE_USER")))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied"));

        mockMvc.perform(get("/api/orders/5")
                        .header("X-User-Id", "42")
                        .header("X-User-Roles", "ROLE_USER"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_whenUserIsDifferent_shouldReturn403() throws Exception {
        OrderResponse response = mock(OrderResponse.class);
        when(response.getUserId()).thenReturn("42");

        when(orderService.getOrderById(eq(5L), eq("42"), eq("ROLE_USER"))).thenReturn(response);
        when(orderService.getOrderById(eq(5L), eq("99"), eq("ROLE_USER")))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied"));

        mockMvc.perform(get("/api/orders/5")
                        .header("X-User-Id", "99")
                        .header("X-User-Roles", "ROLE_USER"))
                .andExpect(status().isForbidden());
    }
}