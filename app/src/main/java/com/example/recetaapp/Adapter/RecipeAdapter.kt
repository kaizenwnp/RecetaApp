package com.example.recetaapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recetaapp.R
import com.example.recetaapp.model.Recipe
import java.io.File

class RecipeAdapter(private val recipes: List<Recipe>, private val context: Context) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private var onItemClickListener: ((Recipe) -> Unit)? = null

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeName: TextView = view.findViewById(R.id.recipe_name)
        val recipeType: TextView = view.findViewById(R.id.recipe_type)
        val recipeImage: ImageView = view.findViewById(R.id.recipe_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeName.text = recipe.name
        holder.recipeType.text = recipe.type


        loadImageFromInternalStorage(recipe.imageUrl, holder.recipeImage)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun setOnItemClickListener(listener: (Recipe) -> Unit) {
        onItemClickListener = listener
    }

    // Load image from internal storage
    private fun loadImageFromInternalStorage(imageName: String?, imageView: ImageView) {
        if (imageName.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.error)
            return
        }

        val imageFile = File(context.filesDir, imageName)
        if (imageFile.exists()) {
            Glide.with(imageView.context)
                .load(imageFile)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.error)
        }
    }
}