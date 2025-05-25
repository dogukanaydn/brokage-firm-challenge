package com.example.brokagefirmchallenge.controller;

import com.example.brokagefirmchallenge.model.Asset;
import com.example.brokagefirmchallenge.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/list")
    ResponseEntity<List<Asset>> listAssets(@RequestParam Long customerId) {
        return ResponseEntity.ok(assetService.listAssets(customerId));
    }
}
