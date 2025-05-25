package com.example.brokagefirmchallenge.service;

import com.example.brokagefirmchallenge.model.Asset;
import com.example.brokagefirmchallenge.model.Order;
import com.example.brokagefirmchallenge.model.enums.OrderSide;
import com.example.brokagefirmchallenge.model.enums.Status;
import com.example.brokagefirmchallenge.repository.AssetRepository;
import com.example.brokagefirmchallenge.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private static final String CURRENCY_TRY = "TRY";

    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    public OrderService(AssetRepository assetRepository, OrderRepository orderRepository) {
        this.assetRepository = assetRepository;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Order order) {
        order.setStatus(Status.PENDING);
        order.setCreateDate(LocalDateTime.now());

        String assetName = order.getAssetName();
        Long customerId = order.getCustomerId();
        BigDecimal totalCost = order.getPrice().multiply(order.getSize());

        if(order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = assetRepository
                    .findByCustomerIdAndAssetName(customerId, CURRENCY_TRY)
                    .orElseThrow(() -> new RuntimeException("Customer does not have TRY asset"));

            if (tryAsset.getUsableSize().compareTo(totalCost) < 0) throw new RuntimeException("Insufficient TRY asset");

            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));
            assetRepository.save(tryAsset);
        }

        if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = assetRepository
                    .findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new RuntimeException("Customer does not have " + assetName + " asset"));

            if (asset.getUsableSize().compareTo(order.getSize()) < 0) throw new RuntimeException("Insufficient " + assetName + "to SELL order");

            asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
            assetRepository.save(asset);
        }

        return orderRepository.save(order);
    }

    public List<Order> listOrders(Long customerId, LocalDateTime from, LocalDateTime to) {
        if(from != null && to != null) {
            return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, from, to);
        } else {
            return orderRepository.findByCustomerId(customerId);
        }
    }

    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (order.getStatus() != Status.PENDING) throw new RuntimeException("Only PENDING orders can be deleted");

        if (order.getStatus() == Status.PENDING) {
            String assetName = order.getAssetName();
            Long customerId = order.getCustomerId();
            BigDecimal size = order.getSize();
            BigDecimal price = order.getPrice();
            BigDecimal restoredAmount = size.multiply(price);

            if (order.getOrderSide() == OrderSide.BUY) {
                Asset tryAsset = assetRepository
                        .findByCustomerIdAndAssetName(customerId, CURRENCY_TRY)
                        .orElseThrow(() -> new RuntimeException("Customer does not have TRY asset"));
                tryAsset.setUsableSize(tryAsset.getUsableSize().add(restoredAmount));
                assetRepository.save(tryAsset);
            }

            if (order.getOrderSide() == OrderSide.SELL) {
                Asset asset = assetRepository
                        .findByCustomerIdAndAssetName(customerId, assetName)
                        .orElseThrow(() -> new RuntimeException("Customer does not have " + assetName + " asset"));
                asset.setUsableSize(asset.getUsableSize().add(size));
                assetRepository.save(asset);
            }

            orderRepository.deleteById(id);
        }
    }
}
