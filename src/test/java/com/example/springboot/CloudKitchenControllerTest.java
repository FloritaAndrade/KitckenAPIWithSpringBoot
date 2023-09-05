package com.example.springboot;

import com.example.springboot.model.Ingredient;
import com.example.springboot.model.Inventory;
import com.example.springboot.model.Recipe;
import com.example.springboot.service.CloudKitchenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CloudKitchenControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CloudKitchenService cloudKitchenService;

    @Test
    public void checkIfTheServerIsActive() throws Exception {
        mockMvc.perform(get("/ping").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pong")));
    }

    @Test
    public void testGetInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setName("Flour");
        inventory.setQuantity(5);

        when(cloudKitchenService.getInventory()).thenReturn(List.of(inventory));
        mockMvc.perform(get("/inventory").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpectAll(jsonPath("$[0].name", is("Flour")), jsonPath("$[0].quantity", is(5)));
    }

    public Recipe getRecipeTestData() {
        Recipe recipe = new Recipe();
        recipe.setName("Chocolate Doughnuts");
        recipe.setId(1);
        recipe.setInstructions("Melt chocolate, dip the Doughnut");
        Ingredient ingredient1 = new Ingredient("Chocolate", 4);
        Ingredient ingredient2 = new Ingredient("Doughnut", 4);
        recipe.setIngredients(List.of(ingredient1, ingredient2));

        return recipe;
    }

    @Test
    public void testGetRecipeWithValidId() throws Exception {
        when(cloudKitchenService.getRecipeWithId(1)).thenReturn(new ResponseEntity<>(getRecipeTestData(), HttpStatus.OK));

        mockMvc.perform(get("/recipes/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredients", hasSize(2)))
                .andExpectAll(jsonPath("$.id", is(1)), jsonPath("$.ingredients[0].name", is("Chocolate")));
    }

    @Test
    public void testGetRecipeWithIdNotPresent() throws Exception {
        when(cloudKitchenService.getRecipeWithId(4)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/recipes/{id}", "4"))
                .andExpect(status().isNotFound());

    }

    @Test
    public void testGetRecipes() throws Exception {
        Recipe recipe1 = getRecipeTestData();

        Recipe recipe2 = new Recipe();
        recipe2.setName("Cinnamon Bun");
        recipe2.setId(2);
        recipe2.setInstructions("Put cinnamon on the bun");
        Ingredient ingredient21 = new Ingredient("Cinnamon", 2);
        Ingredient ingredient22 = new Ingredient("Bun", 2);
        Ingredient ingredient23 = new Ingredient("Sugar", 2);
        recipe2.setIngredients(List.of(ingredient21, ingredient22, ingredient23));

        when(cloudKitchenService.getRecipes()).thenReturn(List.of(recipe1, recipe2));

        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ingredients", hasSize(2)))
                .andExpect(jsonPath("$[1].ingredients", hasSize(3)))
                .andExpectAll(jsonPath("$[0].id", is(1)), jsonPath("$[0].name", is("Chocolate Doughnuts")), jsonPath("$[0].ingredients[0].name", is("Chocolate")))
                .andExpectAll(jsonPath("$[1].id", is(2)), jsonPath("$[1].name", is("Cinnamon Bun")), jsonPath("$[1].ingredients[0].name", is("Cinnamon")));
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAddInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setName("Flour");
        inventory.setQuantity(5);

        mockMvc.perform(post("/inventory/fill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(List.of(inventory)).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateRecipe() throws Exception {
        Recipe recipeCreated = getRecipeTestData();

        when(cloudKitchenService.createRecipe(Mockito.any(Recipe.class))).thenReturn(recipeCreated);
        mockMvc.perform(post("/recipes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(recipeCreated).getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    public void testDeleteRecipe() throws Exception {
        when(cloudKitchenService.deleteRecipeWithId(1)).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
        mockMvc.perform(delete("/recipes/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
