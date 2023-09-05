package com.example.springboot.repository;

import com.example.springboot.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    Recipe findRecipeByName(String name);
}
