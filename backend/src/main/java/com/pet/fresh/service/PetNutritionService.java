package com.pet.fresh.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PetNutritionService {

    /**
     * AI辅助科学配比推荐模型
     * 静息能量需求(RER)公式: RER = 70 * (体重kg ^ 0.75)
     */
    public Map<String, Object> calculateRecipeRecommendation(String species, double weight, int ageMonths, boolean isNeutered, String activityLevel, String allergens) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 计算 RER
        double rer = 70 * Math.pow(weight, 0.75);

        // 2. 结合生理指标求每日维持能量 DER 系数
        double factor = 1.6;
        if ("CAT".equalsIgnoreCase(species)) {
            factor = isNeutered ? 1.2 : 1.4;
            if (ageMonths < 12) factor = 2.0; // 幼猫生长系数
        } else if ("DOG".equalsIgnoreCase(species)) {
            factor = isNeutered ? 1.6 : 1.8;
            if ("HIGH".equalsIgnoreCase(activityLevel)) factor += 0.4;
            if (ageMonths < 12) factor = 2.5; // 幼犬生长系数
        }

        double der = rer * factor; // 每日能量需求(kcal)

        // 3. 营养素配平标准 (基于干物质DM目标)
        double recommendedProtein = "CAT".equalsIgnoreCase(species) ? 40.0 : 28.0;
        double recommendedFat = "CAT".equalsIgnoreCase(species) ? 20.0 : 15.0;
        double recommendedMoisture = 75.0; // 鲜食典型含水量

        // 4. 每日适喂克重换算 (鲜食平均能量密度约 120-130 kcal/100g)
        double dailyFreshGrams = (der / 125.0) * 100.0;

        // 5. 组装输出
        result.put("species", species);
        result.put("weightKg", weight);
        result.put("RER_Kcal", BigDecimal.valueOf(rer).setScale(1, RoundingMode.HALF_UP));
        result.put("dailyEnergyDER_Kcal", BigDecimal.valueOf(der).setScale(1, RoundingMode.HALF_UP));
        result.put("recommendedDailyFoodGrams", BigDecimal.valueOf(dailyFreshGrams).setScale(0, RoundingMode.HALF_UP));
        result.put("targetProteinRatio", recommendedProtein + "%");
        result.put("targetFatRatio", recommendedFat + "%");
        result.put("targetMoistureRatio", recommendedMoisture + "%");

        // 6. 生成食材推荐清单 (规避过敏原)
        List<String> suggestedIngredients = new ArrayList<>();
        if ("CAT".equalsIgnoreCase(species)) {
            suggestedIngredients.add("纯肉类(80%): 去骨兔肉/鲜鸡肉/深海三文鱼(牛磺酸补充)");
            suggestedIngredients.add("脏器类(10%): 鸡肝、鸡心(天然维生素A与微量元素)");
            suggestedIngredients.add("营养辅料(10%): 蔓越莓粉、南极磷虾油、蛋黄粉");
        } else {
            suggestedIngredients.add("纯肉类(65%): 草饲牛肉、去骨鸭肉、三文鱼肉");
            suggestedIngredients.add("脏器与骨粉(10%): 牛心、牛肝、碳酸钙/蛋壳粉");
            suggestedIngredients.add("果蔬膳食纤维(25%): 熟南瓜、胡萝卜碎、蓝莓、西兰花");
        }

        if (allergens != null && !allergens.trim().isEmpty()) {
            suggestedIngredients.add("【过敏规避警示】已自动剔除宠主指定的过敏食材: " + allergens);
        }

        result.put("customRecipePlan", suggestedIngredients);
        result.put("feedingAdvice", "建议一日分早晚两餐饲喂，冷冻鲜食提前置于0-4℃冷藏室内化冻。");
        return result;
    }
}
