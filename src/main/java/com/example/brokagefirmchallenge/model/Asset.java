package com.example.brokagefirmchallenge.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private String assetName;
    @Column(precision = 19, scale = 2)
    private BigDecimal size;
    @Column(precision = 19, scale = 2)
    private BigDecimal usableSize;

    public Asset() {}

    public Asset(Long customerId, String assetName, BigDecimal size, BigDecimal usableSize) {
        this.customerId = customerId;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public BigDecimal getUsableSize() {
        return usableSize;
    }

    public void setUsableSize(BigDecimal usableSize) {
        this.usableSize = usableSize;
    }
}
