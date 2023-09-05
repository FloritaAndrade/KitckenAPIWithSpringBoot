package com.example.springboot.service;

import com.example.springboot.model.Ingredient;
import com.example.springboot.model.Inventory;
import com.example.springboot.model.Recipe;
import com.example.springboot.repository.IngredientRepository;
import com.example.springboot.repository.InventoryRepository;
import com.example.springboot.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CloudKitchenService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    /*Add items to the inventory table.
    * 1. If the item already exist, increase the quantity of the item.
    * 2. If the item is new, make a new entry to the inventory table.
    * */
    public void addInventory(List<Inventory> inventories) {
        if (!inventories.isEmpty()) {
            List<Inventory> existingInventory = getInventory();
            List<Inventory> newInventory = inventories;
            existingInventory.stream().forEach(existingItem -> {
                newInventory.stream().forEach(newItem -> {
                    if (existingItem.getName().equals(newItem.getName()))
                        newItem.setQuantity(newItem.getQuantity() + existingItem.getQuantity());
                });

            });
            inventoryRepository.saveAll(newInventory);
        }
    }

    /* Returns all the inventory present in the inventory table */
    public List<Inventory> getInventory() {
        return inventoryRepository.findAll();
    }

    public List<Recipe> getRecipes() {
        return recipeRepository.findAll();
    }

    /*Deletes all the entries in the database*/
    public void clearEntries() {
        inventoryRepository.deleteAll();
        recipeRepository.deleteAll();
    }

    public boolean isRecipeFound(Recipe recipe) {
        Recipe presentRecipe = recipeRepository.findRecipeByName(recipe.getName());
        AtomicBoolean isRecipeFound = new AtomicBoolean(true);
        if (presentRecipe != null) {
            if (recipe.getInstructions().equals(presentRecipe.getInstructions()) && recipe.getIngredients().size() == presentRecipe.getIngredients().size()) {
                List<Ingredient> ingredients = presentRecipe.getIngredients();
                recipe.getIngredients().stream().forEach(newIngredient -> {
                    for (int i = 0; i < ingredients.size(); i++) {
                        if (ingredients.get(i).getName().equals(newIngredient.getName()) && ingredients.get(i).getQuantity() == newIngredient.getQuantity()) {
                            break;
                        } else if (i == ingredients.size() - 1)
                            isRecipeFound.set(false);
                    }
                });
            } else
                isRecipeFound.set(false);
        } else
            isRecipeFound.set(false);
        return isRecipeFound.get();
    }

    public Ingredient isIngredientFound(Ingredient newItem) {
        List<Ingredient> ingredientsList = ingredientRepository.findIngredientsByName(newItem.getName());
        Ingredient ingredientFound = null;
        if (!ingredientsList.isEmpty()) {
            for (int i = 0; i < ingredientsList.size(); i++) {
                if (ingredientsList.get(i).getQuantity() == newItem.getQuantity()) {
                    ingredientFound = ingredientsList.get(i);
                    break;
                }
            }
        }
        return ingredientFound;
    }

    /*Create new recipe. If the recipe already exists, return the existing recipe*/
    public Recipe createRecipe(Recipe recipe) {
        Recipe presentRecipe = recipeRepository.findRecipeByName(recipe.getName());
        if (presentRecipe != null)
            return recipeRepository.findRecipeByName(recipe.getName());
        else {
            List<Ingredient> ingredients = recipe.getIngredients();
            List<Ingredient> newIngredients = new ArrayList<>();
            List<Ingredient> existingIngredients = new ArrayList<>();

            ingredients.stream().forEach(newItem -> {
                Ingredient ingredientFound = isIngredientFound(newItem);
                if (ingredientFound != null)
                    existingIngredients.add(ingredientFound);
                else
                    newIngredients.add(newItem);
            });

            recipeRepository.save(new Recipe(recipe.getName(), recipe.getInstructions(), newIngredients));
            Recipe createdRecipe = recipeRepository.findRecipeByName(recipe.getName());
            if (!existingIngredients.isEmpty()) {
                for (Ingredient existing : existingIngredients) {
                    createdRecipe.addIngredient(existing);
                    recipeRepository.save(createdRecipe);
                }
            }
            return recipeRepository.findRecipeByName(createdRecipe.getName());
        }

    }

    public Recipe findCreatedRecipe(Recipe recipe) {

        List<Recipe> recipes = recipeRepository.findAll();
        final Recipe[] newlyCreatedRecipe = {null};
        recipes.stream().forEach(newRecipe ->
        {
            if (recipe.getName().equals(newRecipe.getName())) {
                newlyCreatedRecipe[0] = newRecipe;
            }
        });
        return newlyCreatedRecipe[0];
    }

    public Optional<Recipe> getRecipe(Integer id) {
        return recipeRepository.findById(id);
    }

    public ResponseEntity<Recipe> getRecipeWithId(Integer id) {
        Optional<Recipe> recipe = getRecipe(id);

        if (recipe.isPresent()) {
            return new ResponseEntity<>(recipe.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<HttpStatus> deleteRecipeWithId(Integer id) {
        recipeRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Recipe> updateRecipeWithId(Integer id, Recipe recipe) {
        if (recipe.getId() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (getRecipe(id).isPresent()) {
            Recipe existingRecipe = getRecipe(id).get();
            if (recipe.getName() != null) existingRecipe.setName(recipe.getName());
            if (recipe.getInstructions() != null) existingRecipe.setInstructions(recipe.getInstructions());

            List<Ingredient> ingredients = recipe.getIngredients();
            List<Ingredient> newIngredients = new ArrayList<>();
            List<Ingredient> existingIngredients = new ArrayList<>();
            if (ingredients != null) {
                ingredients.stream().forEach(newItem -> {
                    Ingredient ingredientFound = isIngredientFound(newItem);
                    if (ingredientFound != null)
                        existingIngredients.add(ingredientFound);
                    else
                        newIngredients.add(newItem);
                });
                existingRecipe.setIngredients(newIngredients);
            }
            recipeRepository.save(existingRecipe);
            Recipe createdRecipe = getRecipe(id).get();
            if (!existingIngredients.isEmpty()) {
                for (Ingredient existing : existingIngredients) {
                    createdRecipe.addIngredient(existing);
                    recipeRepository.save(createdRecipe);
                }
            }
            return new ResponseEntity<>(getRecipe(id).get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /*If the ingredients are available in the inventory, the recipe can be made and returns "Yummy!" as a response. */
    public ResponseEntity<String> makeRecipe(Integer id) {
        if (getRecipe(id).isPresent()) {
            Recipe recipe = getRecipe(id).get();

            List<Ingredient> ingredientsRequired = recipe.getIngredients();

            List<Inventory> inventoryList = getInventory();
            AtomicReference<Boolean> isRecipePossible = new AtomicReference<>(true);

            ingredientsRequired.stream().forEach(ingredient -> {
                for (int i = 0; i <= inventoryList.size(); i++) {
                    if (inventoryList.get(i).getName() == ingredient.getName() && inventoryList.get(i).getQuantity() >= ingredient.getQuantity()) {
                        break;
                    } else if (i == inventoryList.size() - 1) {
                        isRecipePossible.set(false);
                        break;
                    }
                }
            });
            if (isRecipePossible.get()) {
                updateInventory(ingredientsRequired);
                return new ResponseEntity<>("Yummy!", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public void updateInventory(List<Ingredient> ingredients) {
        ingredients.stream().forEach(ingredient -> {
            Optional<Inventory> inventoryPresent = inventoryRepository.findById(ingredient.getName());
            Integer quantity = inventoryPresent.get().getQuantity() - ingredient.getQuantity();
            if (quantity <= 0) {
                inventoryRepository.delete(inventoryPresent.get());
            } else {
                inventoryPresent.get().setQuantity(quantity);
                inventoryRepository.save(inventoryPresent.get());
            }
        });
    }

}
