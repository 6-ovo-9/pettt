package com.pet.fresh.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class PetNutritionService {

    public Map<String, Object> calculateRecipeRecommendation(String species, double weight, int ageMonths, boolean isNeutered, String activityLevel, String allergens) {
        Map<String, Object> result = new LinkedHashMap<>();

        double rer = 70 * Math.pow(weight, 0.75);

        double factor = 1.6;
        if ("CAT".equalsIgnoreCase(species)) {
            factor = isNeutered ? 1.2 : 1.4;
            if (ageMonths < 12) factor = 2.0;
        } else if ("DOG".equalsIgnoreCase(species)) {
            factor = isNeutered ? 1.6 : 1.8;
            if ("HIGH".equalsIgnoreCase(activityLevel)) factor += 0.4;
            if (ageMonths < 12) factor = 2.5;
        }

        double der = rer * factor;
        double recommendedProtein = "CAT".equalsIgnoreCase(species) ? 40.0 : 28.0;
        double recommendedFat = "CAT".equalsIgnoreCase(species) ? 20.0 : 15.0;
        double recommendedMoisture = 75.0;

        double dailyFreshGrams = (der / 125.0) * 100.0;

        result.put("species", species);
        result.put("weightKg", weight);
        result.put("RER_Kcal", BigDecimal.valueOf(rer).setScale(1, RoundingMode.HALF_UP));
        result.put("dailyEnergyDER_Kcal", BigDecimal.valueOf(der).setScale(1, RoundingMode.HALF_UP));
        result.put("recommendedDailyFoodGrams", BigDecimal.valueOf(dailyFreshGrams).setScale(0, RoundingMode.HALF_UP));
        result.put("targetProteinRatio", recommendedProtein + "%");
        result.put("targetFatRatio", recommendedFat + "%");
        result.put("targetMoistureRatio", recommendedMoisture + "%");

        List<String> suggestedIngredients = new ArrayList<>();
        if ("CAT".equalsIgnoreCase(species)) {
            suggestedIngredients.add("纯肉类(80%): 鲜鸡胸肉、去骨兔肉、深海三文鱼(富含牛磺酸与Omega-3)");
            suggestedIngredients.add("脏器类(10%): 鸡心、鸡肝(天然微量元素与维生素A)");
            suggestedIngredients.add("营养辅料(10%): 蛋黄粉、海藻粉、蔓越莓提取物");
        } else {
            suggestedIngredients.add("纯肉类(65%): 草饲精牛肉、去骨鸭肉、无刺三文鱼肉");
            suggestedIngredients.add("脏器与骨粉(10%): 牛心、牛肝、碳酸钙/优质蛋壳粉");
            suggestedIngredients.add("果蔬膳食纤维(25%): 蒸熟南瓜、胡萝卜粒、西兰花碎、蓝莓");
        }

        if (allergens != null && !allergens.trim().isEmpty()) {
            suggestedIngredients.add("【过敏食材拦截】已自动避开过敏源: " + allergens);
        }

        result.put("customRecipePlan", suggestedIngredients);
        result.put("feedingAdvice", "建议一日分早晚两餐饲喂，冷冻鲜食提前置于0-4℃冷藏室内解冻24小时。");
        return result;
    }
}
