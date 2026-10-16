package com.pet.fresh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetFreshApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetFreshApplication.class, args);
        System.out.println("=================================================================");
        System.out.println("AI辅助宠物营养配比推荐与鲜食库存管理系统 后端启动成功！端口: 8080");
        System.out.println("=================================================================");
    }
}
