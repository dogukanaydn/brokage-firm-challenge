package com.example.brokagefirmchallenge;


import com.example.brokagefirmchallenge.model.Asset;
import com.example.brokagefirmchallenge.model.Order;
import com.example.brokagefirmchallenge.model.enums.OrderSide;
import com.example.brokagefirmchallenge.model.enums.Status;
import com.example.brokagefirmchallenge.repository.AssetRepository;
import com.example.brokagefirmchallenge.repository.OrderRepository;
import com.example.brokagefirmchallenge.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createBuyOrder_withSufficientTRY_shouldSucceed() {
        Order order = new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"), new BigDecimal("100"), null, null);
        Asset tryAsset = new Asset(1L, "TRY", new BigDecimal("2000"), new BigDecimal("2000"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(Mockito.any())).thenReturn(order);

        Order savedOrder = orderService.createOrder(order);

        assertEquals(Status.PENDING, savedOrder.getStatus());
        assertEquals(new BigDecimal("1000"), tryAsset.getUsableSize());
        assertEquals(new BigDecimal("2000"), tryAsset.getSize());
        verify(assetRepository).save(tryAsset);
    }

    @Test
    void createBuyOrder_withInsufficientTRY_shouldThrow() {
        Order order = new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("10"), new BigDecimal("200"), null, null);
        Asset tryAsset = new Asset(1L, "TRY", new BigDecimal("1500"), new BigDecimal("1500"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
    }

    @Test
    void createSellOrder_withSufficientAsset_shouldSucceed() {
        Order order = new Order(2L, "TSLA", OrderSide.SELL, new BigDecimal("5"), new BigDecimal("100"), null, null);
        Asset tslaAsset = new Asset(2L, "TSLA", new BigDecimal("10"), new BigDecimal("10"));

        when(assetRepository.findByCustomerIdAndAssetName(2L, "TSLA")).thenReturn(Optional.of(tslaAsset));
        when(orderRepository.save(Mockito.any())).thenReturn(order);

        Order savedOrder = orderService.createOrder(order);

        assertEquals(new BigDecimal("5"), tslaAsset.getUsableSize());
        assertEquals(new BigDecimal("10"), tslaAsset.getSize());
        assertEquals(Status.PENDING, savedOrder.getStatus());
        verify(assetRepository).save(tslaAsset);
        verify(orderRepository).save(savedOrder);
    }

    @Test
    void createSellOrder_withInsufficientAsset() {
        Order order = new Order(2L, "TSLA", OrderSide.SELL, new BigDecimal("6"), new BigDecimal("100"), null, null);
        Asset tslaAsset = new Asset(2L, "TSLA", new BigDecimal("10"), new BigDecimal("5"));

        when(assetRepository.findByCustomerIdAndAssetName(2L, "TSLA")).thenReturn(Optional.of(tslaAsset));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
        assertEquals("Insufficient TSLA to SELL order", ex.getMessage());
    }


    @Test
    void deletePendingOrder_shouldRestoreAssets() {
        Order order = new Order(1L, "AAPL", OrderSide.SELL, new BigDecimal("2"), new BigDecimal("100"), Status.PENDING, LocalDateTime.now());
        order.setId(10L);
        Asset asset = new Asset(1L, "AAPL", new BigDecimal("10"), new BigDecimal("5"));

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL")).thenReturn(Optional.of(asset));

        orderService.deleteOrder(10L);

        assertEquals(new BigDecimal("7"), asset.getUsableSize());
        assertEquals(new BigDecimal("10"), asset.getSize());
        verify(orderRepository).deleteById(10L);
    }

    @Test
    void deleteBuyOrder_restoresTRY() {
        Order order = new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("3"), new BigDecimal("100"), Status.PENDING, LocalDateTime.now());
        order.setId(20L);
        Asset tryAsset = new Asset(1L, "TRY", new BigDecimal("2000"), new BigDecimal("1000"));

        when(orderRepository.findById(20L)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY")).thenReturn(Optional.of(tryAsset));

        orderService.deleteOrder(20L);

        assertEquals(new BigDecimal("1300"), tryAsset.getUsableSize());
        assertEquals(new BigDecimal("2000"), tryAsset.getSize());
        verify(orderRepository).deleteById(20L);
    }


    @Test
    void deleteOrder_nonPending_throwsException() {
        Order order = new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("1"), new BigDecimal("100"), Status.MATCHED, LocalDateTime.now());
        when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.deleteOrder(99L));
        assertEquals("Only PENDING orders can be deleted", ex.getMessage());
    }

    @Test
    void listOrders_byCustomerId_only() {
        List<Order> mockOrders = List.of(
                new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("5"), new BigDecimal("200"), Status.PENDING, LocalDateTime.now())
        );

        when(orderRepository.findByCustomerId(1L)).thenReturn(mockOrders);

        List<Order> result = orderService.listOrders(1L, null, null);

        assertEquals(1, result.size());
        verify(orderRepository).findByCustomerId(1L);
    }

    @Test
    void listOrders_byCustomerId_andDateRange() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59);
        List<Order> mockOrders = List.of(
                new Order(1L, "TSLA", OrderSide.SELL, new BigDecimal("2"), new BigDecimal("300"), Status.MATCHED, LocalDateTime.of(2024, 5, 1, 10, 0))
        );

        when(orderRepository.findByCustomerIdAndCreateDateBetween(1L, from, to)).thenReturn(mockOrders);

        List<Order> result = orderService.listOrders(1L, from, to);

        assertEquals(1, result.size());
        verify(orderRepository).findByCustomerIdAndCreateDateBetween(1L, from, to);
    }

    @Test
    void listAssets_byCustomerId() {
        List<Asset> mockAssets = List.of(
                new Asset(1L, "TRY", new BigDecimal("1000"), new BigDecimal("800")),
                new Asset(1L, "AAPL", new BigDecimal("10"), new BigDecimal("10"))
        );

        when(assetRepository.findByCustomerId(1L)).thenReturn(mockAssets);

        List<Asset> result = assetRepository.findByCustomerId(1L);

        assertEquals(2, result.size());
        assertEquals("TRY", result.get(0).getAssetName());
        verify(assetRepository).findByCustomerId(1L);
    }

    @Test
    void matchOrder_shouldUpdateAssetForBuyOrder() {
        Order buyOrder = new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("5"), new BigDecimal("100"), Status.PENDING, LocalDateTime.now());
        buyOrder.setId(100L);

        Asset existingAsset = new Asset(1L, "AAPL", new BigDecimal("10"), new BigDecimal("10"));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(buyOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL")).thenReturn(Optional.of(existingAsset));
        when(orderRepository.save(Mockito.any())).thenReturn(buyOrder);


        Order matched = orderService.matchOrder(100L);

        assertEquals(Status.MATCHED, matched.getStatus());
        assertEquals(new BigDecimal("15"), existingAsset.getSize());
        assertEquals(new BigDecimal("15"), existingAsset.getUsableSize());
        verify(assetRepository).save(existingAsset);
    }

    @Test
    void matchOrder_shouldUpdateTRYAndReduceSellAssetForSellOrder() {
        Order sellOrder = new Order(2L, "TSLA", OrderSide.SELL, new BigDecimal("3"), new BigDecimal("200"), Status.PENDING, LocalDateTime.now());
        sellOrder.setId(101L);

        Asset tryAsset = new Asset(2L, "TRY", new BigDecimal("1000"), new BigDecimal("1000"));
        Asset soldAsset = new Asset(2L, "TSLA", new BigDecimal("5"), new BigDecimal("5"));

        when(orderRepository.findById(101L)).thenReturn(Optional.of(sellOrder));
        when(assetRepository.findByCustomerIdAndAssetName(2L, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetRepository.findByCustomerIdAndAssetName(2L, "TSLA")).thenReturn(Optional.of(soldAsset));
        when(orderRepository.save(Mockito.any())).thenReturn(sellOrder);

        Order matched = orderService.matchOrder(101L);

        assertEquals(Status.MATCHED, matched.getStatus());
        assertEquals(new BigDecimal("1600"), tryAsset.getSize());
        assertEquals(new BigDecimal("1600"), tryAsset.getUsableSize());
        assertEquals(new BigDecimal("2"), soldAsset.getSize());

        verify(assetRepository).save(tryAsset);
        verify(assetRepository).save(soldAsset);
        verify(orderRepository).save(sellOrder);
    }


    @Test
    void matchOrder_shouldThrowIfNotPending() {
        Order order = new Order(1L, "AAPL", OrderSide.BUY, new BigDecimal("5"), new BigDecimal("100"), Status.MATCHED, LocalDateTime.now());
        order.setId(102L);

        when(orderRepository.findById(102L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.matchOrder(102L));
        assertEquals("Only PENDING orders can be matched", ex.getMessage());
    }

    @Test
    void matchOrder_shouldThrowIfOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.matchOrder(999L));
        assertEquals("Order not found with id: 999", ex.getMessage());
    }

}
