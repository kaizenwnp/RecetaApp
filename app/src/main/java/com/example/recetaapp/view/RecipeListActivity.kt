package com.example.recetaapp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recetaapp.Adapter.RecipeAdapter
import com.example.recetaapp.R
import com.example.recetaapp.model.Recipe
import com.example.recetaapp.model.RecipeTypes
import com.example.recetaapp.viewmodel.RecipeViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class RecipeListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeTypeSpinner: Spinner
    private lateinit var addRecipeButton: Button
    private lateinit var searchView: androidx.appcompat.widget.SearchView

    private val viewModel: RecipeViewModel by viewModels()

    private var allRecipes: MutableList<Recipe> = mutableListOf()
    private var recipeTypeNames: MutableList<String> = mutableListOf("Show All")

    private var currentSearchQuery: String = ""

    private val recipeDetailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val updatedRecipe = result.data?.getSerializableExtra("UPDATED_RECIPE") as? Recipe
            if (updatedRecipe != null) {
                updateRecipeInList(updatedRecipe)
            }

            val deletedRecipeId = result.data?.getIntExtra("DELETE_RECIPE_ID", -1) ?: -1
            if (deletedRecipeId != -1) {
                deleteRecipeFromList(deletedRecipeId)
            }
        }
    }

    private val addRecipeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val newRecipe = Recipe(
                    id = allRecipes.size + 1,
                    name = data.getStringExtra("NEW_RECIPE_NAME") ?: "",
                    type = data.getStringExtra("NEW_RECIPE_TYPE") ?: "",
                    ingredients = data.getStringArrayListExtra("NEW_RECIPE_INGREDIENTS") ?: listOf(),
                    steps = data.getStringArrayListExtra("NEW_RECIPE_STEPS") ?: listOf(),
                    imageUrl = data.getStringExtra("NEW_RECIPE_IMAGE_URL") // Corrected key for image file name
                )
                allRecipes.add(newRecipe)
                saveRecipesToInternalStorage(allRecipes)
                updateRecipeListBasedOnSelection()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        recyclerView = findViewById(R.id.recipe_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No acción especial al enviar búsqueda
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText?.trim() ?: ""
                updateRecipeListBasedOnSelection()
                return true
            }
        })

        recipeTypeSpinner = findViewById(R.id.recipe_type_spinner)
        addRecipeButton = findViewById(R.id.add_recipe_button)

        addRecipeButton.setOnClickListener {
            val filteredRecipeTypes = recipeTypeNames.filter { it != "Show All" }
            val intent = Intent(this, AddRecipeActivity::class.java)
            intent.putStringArrayListExtra("RECIPE_TYPES", ArrayList(filteredRecipeTypes))
            addRecipeLauncher.launch(intent)
        }

        setSupportActionBar(findViewById(R.id.toolbar))

        viewModel.recipes.observe(this, Observer { recipes ->
            allRecipes.clear()
            allRecipes.addAll(recipes)
            updateRecipeListBasedOnSelection()
        })

        loadRecipes()
        setupRecipeTypeSpinner()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_recipe_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection.
        return when (item.itemId) {
            R.id.add_recipe_menu -> {
                val filteredRecipeTypes = recipeTypeNames.filter { it != "Show All" }
                val intent = Intent(this, AddRecipeActivity::class.java)
                intent.putStringArrayListExtra("RECIPE_TYPES", ArrayList(filteredRecipeTypes))
                addRecipeLauncher.launch(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecipeTypeSpinner() {
        val recipeTypes: List<RecipeTypes> = loadRecipeTypesFromAssets()

        recipeTypeNames.addAll(recipeTypes.map { recipeType: RecipeTypes -> recipeType.type })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTypeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recipeTypeSpinner.adapter = adapter

        recipeTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateRecipeListBasedOnSelection()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateRecipeListBasedOnSelection() {
        val selectedType = recipeTypeSpinner.selectedItem?.toString() ?: "Show All"
        val filteredByType = if (selectedType == "Show All") {
            allRecipes
        } else {
            allRecipes.filter { it.type == selectedType }
        }

        val filteredRecipes = if (currentSearchQuery.isEmpty()) {
            filteredByType
        } else {
            filteredByType.filter { it.name.contains(currentSearchQuery, ignoreCase = true) }
        }

        recyclerView.adapter = RecipeAdapter(filteredRecipes, this).apply {
            setOnItemClickListener { recipe ->
                val intent = Intent(this@RecipeListActivity, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_DETAIL", recipe)
                recipeDetailLauncher.launch(intent)
            }
        }
    }

    private fun loadRecipes() {
        val recipesFile = File(filesDir, "recipes.json")
        if (recipesFile.exists()) {
            val json = recipesFile.bufferedReader().use { it.readText() }
            val recipeListType = object : TypeToken<List<Recipe>>() {}.type
            allRecipes = Gson().fromJson(json, recipeListType)
        } else {
            allRecipes = loadRecipesFromAssets().toMutableList()
            saveRecipesToInternalStorage(allRecipes)
        }
        updateRecipeListBasedOnSelection()
    }

    private fun saveRecipesToInternalStorage(recipes: List<Recipe>) {
        val file = File(filesDir, "recipes.json")
        val json = Gson().toJson(recipes)
        file.writeText(json)
    }

    private fun updateRecipeInList(updatedRecipe: Recipe) {
        val index = allRecipes.indexOfFirst { it.id == updatedRecipe.id }
        if (index != -1) {
            allRecipes[index] = updatedRecipe
            saveRecipesToInternalStorage(allRecipes)
            updateRecipeListBasedOnSelection()
        }
    }

    private fun deleteRecipeFromList(recipeId: Int) {
        val index = allRecipes.indexOfFirst { it.id == recipeId }
        if (index != -1) {
            allRecipes.removeAt(index)
            saveRecipesToInternalStorage(allRecipes)
            updateRecipeListBasedOnSelection()
        }
    }

    private fun loadRecipesFromAssets(): List<Recipe> {
        val json = assets.open("recipes.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(json, object : TypeToken<List<Recipe>>() {}.type)
    }

    private fun loadRecipeTypesFromAssets(): List<RecipeTypes> {
        val json = assets.open("recipetypes.json").bufferedReader().use { it.readText() }
        return Gson().fromJson(json, object : TypeToken<List<RecipeTypes>>() {}.type)
    }
}
