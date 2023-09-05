package com.example.springboot.controller;

import com.example.springboot.model.Inventory;
import com.example.springboot.model.Recipe;
import com.example.springboot.service.CloudKitchenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CloudKitchenController {

    @Autowired
    private CloudKitchenService cloudKitchenService;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping("/clear")
    public void clearKitchen() {
        cloudKitchenService.clearEntries();
    }

    @PostMapping("/inventory/fill")
    public void addInventory(@RequestBody List<Inventory> inventories) {
        cloudKitchenService.addInventory(inventories);
    }

    @GetMapping("/inventory")
    public List<Inventory> getInventory() {
        return cloudKitchenService.getInventory();
    }

    @PostMapping("/recipes/create")
    public Recipe createRecipe(@RequestBody Recipe recipe) {
        return cloudKitchenService.createRecipe(recipe);
    }

    @GetMapping("/recipes/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable("id") Integer id) {
        return cloudKitchenService.getRecipeWithId(id);
    }

    @GetMapping("/recipes")
    public List<Recipe> getRecipes() {
        return cloudKitchenService.getRecipes();
    }

    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<HttpStatus> deleteRecipe(@PathVariable Integer id) {
        return cloudKitchenService.deleteRecipeWithId(id);
    }

    @PatchMapping("/recipes/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Integer id, @RequestBody Recipe recipe) {
        return cloudKitchenService.updateRecipeWithId(id, recipe);
    }

    @PostMapping("/recipes/{id}/make")
    public ResponseEntity<String> makeRecipe(@PathVariable Integer id) {
        return cloudKitchenService.makeRecipe(id);
    }
}


