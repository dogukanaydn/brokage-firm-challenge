package com.example.brokagefirmchallenge.service;

import com.example.brokagefirmchallenge.model.Asset;
import com.example.brokagefirmchallenge.model.Order;
import com.example.brokagefirmchallenge.model.enums.OrderSide;
import com.example.brokagefirmchallenge.model.enums.Status;
import com.example.brokagefirmchallenge.repository.AssetRepository;
import com.example.brokagefirmchallenge.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

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
        double totalCost = order.getPrice() * order.getSize();

        if(order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = assetRepository
                    .findByCustomerIdAndAssetName(customerId, "TRY")
                    .orElseThrow(() -> new RuntimeException("Customer does not have TRY asset"));

            if (tryAsset.getUsableSize() < totalCost) {
                throw new RuntimeException("Insufficient TRY asset");
            }

            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost);
            assetRepository.save(tryAsset);
        }

        if (order.getOrderSide() == OrderSide.SELL) {
            Asset asset = assetRepository
                    .findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new RuntimeException("Customer does not have " + assetName + " asset"));

            if (asset.getUsableSize() < order.getSize()) {
                throw new RuntimeException("Insufficient " + assetName + "to SELL order");
            }

            asset.setUsableSize(asset.getUsableSize() - order.getSize());
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
        Order order = orderRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (order.getStatus() == Status.PENDING) {
            String assetName = order.getAssetName();
            Long customerId = order.getCustomerId();
            double size = order.getSize();
            double price = order.getPrice();

            if (order.getOrderSide() == OrderSide.BUY) {
                Asset tryAsset = assetRepository
                        .findByCustomerIdAndAssetName(customerId, "TRY")
                        .orElseThrow(() -> new RuntimeException("Customer does not have TRY asset"));
                tryAsset.setUsableSize(tryAsset.getUsableSize() + (size * price));
                assetRepository.save(tryAsset);
            }

            if (order.getOrderSide() == OrderSide.SELL) {
                Asset asset = assetRepository
                        .findByCustomerIdAndAssetName(customerId, assetName)
                        .orElseThrow(() -> new RuntimeException("Customer does not have " + assetName + " asset"));
                asset.setUsableSize(asset.getUsableSize() + size);
                assetRepository.save(asset);
            }

            orderRepository.deleteById(id);
        }
    }
}
