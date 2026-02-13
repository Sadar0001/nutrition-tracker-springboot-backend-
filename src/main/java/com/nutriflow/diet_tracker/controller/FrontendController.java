package com.nutriflow.diet_tracker.controller;

import com.nutriflow.diet_tracker.dto.FoodLogDto;
import com.nutriflow.diet_tracker.entity.FoodLog;
import com.nutriflow.diet_tracker.service.FrontendService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/user")
public class FrontendController {
    private final FrontendService frontendService;

    public FrontendController(FrontendService frontendService) {
        this.frontendService = frontendService;
    }

    @PostMapping("/search")
    public FoodLog searchItem(@RequestBody Map<String, String> payload,@RequestParam String quantity,@AuthenticationPrincipal Jwt jwt) {
        String item = payload.get("item");
        FoodLog foodLog=frontendService.addByItem(item,quantity,jwt);
        return foodLog;
    }


    @PostMapping("/query")
    public FoodLog searchQuery(@RequestBody Map<String, String> payload, @AuthenticationPrincipal Jwt jwt) {
        String query = payload.get("query");
        FoodLog foodlog=frontendService.getQuery(query,jwt);
        return foodlog;
    }


    @PostMapping("/image")
    public FoodLog imageItem(@RequestBody Map<String, String>payload,@AuthenticationPrincipal Jwt jwt) {
        String base64Img = payload.get("image_base64");
        FoodLog foodLog=frontendService.addByImage(base64Img,jwt);
        return foodLog;
    }

    @GetMapping("/logs")
    public List<FoodLog> getLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal Jwt jwt
            ) {

        List<FoodLog> logList=frontendService.getLogs(from,to,jwt);
        return logList;
    }

    @PostMapping("/logs")
    public FoodLog createLog(@RequestBody FoodLogDto log, @AuthenticationPrincipal Jwt jwt) {
        FoodLog res=frontendService.addLog(log,jwt);
        return res;
    }

    @GetMapping("/check-engine")
    public String checkEngine() {
        return frontendService.checkEngine();
    }

    @DeleteMapping("/delete")
    public void deleteItem(@RequestParam Long id, @AuthenticationPrincipal Jwt jwt) {
        // Pass the Long directly
        frontendService.deleteLog(id, jwt);
    }
}