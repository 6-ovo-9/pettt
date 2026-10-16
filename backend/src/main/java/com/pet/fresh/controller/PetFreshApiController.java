package com.pet.fresh.controller;

import com.pet.fresh.service.BatchInventoryService;
import com.pet.fresh.service.PetNutritionService;
import com.pet.fresh.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. 登录
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginReq) {
        String username = loginReq.get("username");
        String password = loginReq.get("password");

        try {
            Map<String, Object> user = jdbcTemplate.queryForMap(
                "SELECT id, username, password, nickname, role, status FROM sys_user WHERE username = ?", username);

            if (user == null || !password.equals(user.get("password"))) {
                return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户名或密码错误"));
            }
            if ((int) user.get("status") == 0) {
                return ResponseEntity.status(403).body(Map.of("code", 403, "message", "该账户已被管理员禁用"));
            }

            String token = jwtUtil.generateToken(username, (String) user.get("role"), (String) user.get("nickname"));
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "token", token,
                "userId", user.get("id"),
                "username", username,
                "nickname", user.get("nickname"),
                "role", user.get("role")
            ));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(401).body(Map.of("code", 401, "message", "用户不存在，请先注册！"));
        }
    }

    // 2. 注册
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> regReq) {
        String username = regReq.get("username");
        String password = regReq.get("password");
        String nickname = regReq.get("nickname");
        String phone = regReq.get("phone");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "用户名和密码不能为空"));
        }

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM sys_user WHERE username = ?", Integer.class, username);
        if (count != null && count > 0) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "该用户名已被注册，请更换！"));
        }

        jdbcTemplate.update("INSERT INTO sys_user (username, password, nickname, phone, role) VALUES (?, ?, ?, ?, 'ROLE_USER')",
                username, password, (nickname == null || nickname.isEmpty()) ? "宠主" + username : nickname, phone);

        return ResponseEntity.ok(Map.of("code", 200, "message", "注册成功！请使用新账号登录。"));
    }

    // 3. AI 测算
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

    // 4. 用户新建/保存宠物健康档案
    @PostMapping("/pet/profile/save")
    public ResponseEntity<?> savePetProfile(@RequestBody Map<String, Object> req) {
        Long userId = req.get("userId") != null ? Long.valueOf(req.get("userId").toString()) : 2L;
        String petName = (String) req.get("petName");
        String species = (String) req.get("species");
        String breed = (String) req.get("breed");
        int ageMonths = req.get("ageMonths") != null ? ((Number) req.get("ageMonths")).intValue() : 12;
        double weight = req.get("weight") != null ? ((Number) req.get("weight")).doubleValue() : 5.0;
        String activityLevel = req.get("activityLevel") != null ? (String) req.get("activityLevel") : "NORMAL";
        boolean isNeutered = req.get("isNeutered") != null && Boolean.parseBoolean(req.get("isNeutered").toString());
        String allergens = (String) req.get("allergens");

        jdbcTemplate.update("INSERT INTO pet_profile (user_id, pet_name, species, breed, age_months, weight, activity_level, is_neutered, allergens) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId, petName, species, breed, ageMonths, weight, activityLevel, isNeutered ? 1 : 0, allergens);

        return ResponseEntity.ok(Map.of("code", 200, "message", "宠物健康档案创建成功！"));
    }

    // 5. 获取宠物档案列表 (支持权限隔离：管理员查看全部宠主建档并联查所属人，宠主仅看自己的)
    @GetMapping("/pet/profile/list")
    public ResponseEntity<?> getPetProfiles(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "ROLE_USER") String role) {
        if ("ROLE_ADMIN".equals(role)) {
            // 管理员端：查询全平台所有用户录入的宠物建档，并展示所属用户昵称与电话
            String adminSql = "SELECT p.*, COALESCE(u.nickname, u.username, '散客') AS owner_name, u.phone AS owner_phone " +
                              "FROM pet_profile p LEFT JOIN sys_user u ON p.user_id = u.id ORDER BY p.id DESC";
            return ResponseEntity.ok(jdbcTemplate.queryForList(adminSql));
        } else {
            // 宠主端：严格按当前登录用户 ID 过滤
            Long targetUserId = userId != null ? userId : 2L;
            String userSql = "SELECT p.*, '我' AS owner_name FROM pet_profile p WHERE p.user_id = ? ORDER BY p.id DESC";
            return ResponseEntity.ok(jdbcTemplate.queryForList(userSql, targetUserId));
        }
    }

    // 6. 获取鲜食商品列表
    @GetMapping("/goods/list")
    public ResponseEntity<?> getGoodsList() {
        return ResponseEntity.ok(inventoryService.getAllGoods());
    }

    // 7. FIFO 出库
    @PostMapping("/inventory/deduct")
    public ResponseEntity<?> deductStock(@RequestParam Long goodsId, @RequestParam int quantity) {
        try {
            Map<String, Object> result = inventoryService.deductStockFIFO(goodsId, quantity);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 8. 库存大屏
    @GetMapping("/inventory/dashboard")
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok(inventoryService.getInventoryDashboardStats());
    }
}
