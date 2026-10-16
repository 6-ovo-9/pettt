package com.pet.fresh.controller;

import com.pet.fresh.service.BatchInventoryService;
import com.pet.fresh.service.PetNutritionService;
import com.pet.fresh.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PetFreshApiController {

    @Autowired
    private PetNutritionService nutritionService;

    @Autowired
    private BatchInventoryService inventoryService;

    @Autowired
    private JwtUtil jwtUtil;

    // 1. 用户/管理员登录授权
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginReq) {
        String username = loginReq.get("username");
        String password = loginReq.get("password");

        if ("admin".equals(username) && "123456".equals(password)) {
            String token = jwtUtil.generateToken("admin", "ROLE_ADMIN");
            return ResponseEntity.ok(Map.of("code", 200, "token", token, "role", "ROLE_ADMIN", "username", "admin"));
        } else if ("petlover".equals(username) && "123456".equals(password)) {
            String token = jwtUtil.generateToken("petlover", "ROLE_USER");
            return ResponseEntity.ok(Map.of("code", 200, "token", token, "role", "ROLE_USER", "username", "petlover"));
        }
        return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户名或密码错误(测试账号: admin/123456 或 petlover/123456)"));
    }

    // 2. AI 营养配比与健康测算接口
    @GetMapping("/nutrition/calculate")
    public ResponseEntity<?> calculateNutrition(
            @RequestParam String species,
            @RequestParam double weight,
            @RequestParam int ageMonths,
            @RequestParam(defaultValue = "false") boolean isNeutered,
            @RequestParam(defaultValue = "NORMAL") String activityLevel,
            @RequestParam(required = false) String allergens) {
        Map<String, Object> result = nutritionService.calculateRecipeRecommendation(species, weight, ageMonths, isNeutered, activityLevel, allergens);
        return ResponseEntity.ok(result);
    }

    // 3. FIFO 先进先出扣减库存接口
    @PostMapping("/inventory/deduct")
    public ResponseEntity<?> deductStock(@RequestParam Long goodsId, @RequestParam int quantity) {
        try {
            Map<String, Object> result = inventoryService.deductStockFIFO(goodsId, quantity);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 4. 库存大屏与临期预警数据接口
    @GetMapping("/inventory/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(inventoryService.getInventoryDashboardStats());
    }
}
