package com.nutriflow.diet_tracker.service;


import com.nutriflow.diet_tracker.dto.AiModelResponse;
import com.nutriflow.diet_tracker.dto.FoodLogDto;
import com.nutriflow.diet_tracker.entity.FoodLog;
import com.nutriflow.diet_tracker.entity.User;
import com.nutriflow.diet_tracker.repository.FoodLogRepository;
import com.nutriflow.diet_tracker.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FrontendService {
    private final RestClient restClient;
    private final UserRepository userRepository;
    private final FoodLogRepository foodLogRepository;

    public FrontendService(RestClient.Builder builder,UserRepository userRepository,FoodLogRepository foodLogRepository) {
        this.foodLogRepository = foodLogRepository;
        this.userRepository=userRepository;
        this.restClient = builder
                .baseUrl("http://127.0.0.1:8000")
                .build();
    }

    public String checkEngine(){
        return restClient.get()
                .uri("/health")
                .retrieve()
                .body(String.class);
    }

    public FoodLog addLog(FoodLogDto log, Jwt jwt) {
        User user=getUser(jwt);

        FoodLog foodLog=new FoodLog();
        foodLog.setFoodName(log.getFoodName());
        foodLog.setQuantity(log.getQuantity());
        foodLog.setCarbohydrates(log.getCarbohydrates());
        foodLog.setFiber(log.getFiber());
        foodLog.setProtein(log.getProtein());
        foodLog.setEnergy(log.getEnergy());
        foodLog.setFat(log.getFat());
        foodLog.setUser(user);
        foodLog.setCreatedAt(LocalDateTime.now());

         return foodLogRepository.save(foodLog);
    }

    public List<FoodLog> getLogs(LocalDateTime from, LocalDateTime to,Jwt jwt) {
        User user=getUser(jwt);
        List<FoodLog> foogLog=foodLogRepository.findByCreatedAtBetweenAndUser(from,to,user);
        return foogLog;

    }

//    public User getUser(Jwt jwt) {
//        String clerkId=jwt.getSubject();
//        User user=userRepository.findByClerkId(clerkId).orElseThrow(()-> new UsernameNotFoundException("User Not Found"));
//        return user;
//    }

    public User getUser(Jwt jwt) {
        // TEMPORARY BYPASS: If there is no token, just grab the first user from the database
        if (jwt == null) {
            List<User> allUsers = userRepository.findAll();
            if (allUsers.isEmpty()) {
                throw new RuntimeException("Please create at least one user in the database first!");
            }
            return allUsers.get(0); // Return the first user as a dummy test user
        }

        // Normal flow (when you turn security back on)
        String clerkId = jwt.getSubject();
        return userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }

    public FoodLog addByItem(String item, String quantity,Jwt jwt) {
        String promt="I have "+item+"  "+quantity;
        return getQuery(promt,jwt);
    }

    public FoodLog getQuery(String prompt, Jwt jwt) {

        Map<String, String> map = Map.of("query", prompt);

        AiModelResponse aiModelResponse=restClient.post()
                .uri("/query")
                .body(map)
                .retrieve()
                .body(AiModelResponse.class);

        return addAiModelResAndFoodLog(aiModelResponse,jwt);
    }

    public FoodLog addAiModelResAndFoodLog(AiModelResponse aiModelResponse,Jwt jwt) {
        User user=getUser(jwt);

        FoodLog foodLog=new FoodLog();
        foodLog.setFoodName(aiModelResponse.getTitle());
        foodLog.setQuantity(aiModelResponse.getQuantity());
        foodLog.setCarbohydrates(aiModelResponse.getCarbohydrates());
        foodLog.setFiber(aiModelResponse.getFiber());
        foodLog.setProtein(aiModelResponse.getProtein());
        foodLog.setEnergy(aiModelResponse.getEnergy());
        foodLog.setFat(aiModelResponse.getFat());
        foodLog.setCreatedAt(LocalDateTime.now());
        foodLog.setUser(user);

        return foodLogRepository.save(foodLog);
    }


    public FoodLog addByImage(String base64Img, Jwt jwt) {
        Map<String,String> map=new HashMap<>();
        map.put("image_base64",base64Img);

        AiModelResponse aiModelResponse=restClient.post()
                .uri("/image")
                .body(map)
                .retrieve()
                .body(AiModelResponse.class);

        FoodLog foodLog=addAiModelResAndFoodLog(aiModelResponse,jwt);
        return foodLog;
    }

    public boolean deleteLog(Long id, Jwt jwt) {
        User user = getUser(jwt);

        FoodLog log = foodLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log not found"));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You cannot delete someone else's log");
        }

        foodLogRepository.delete(log);

        return true;
    }
}
