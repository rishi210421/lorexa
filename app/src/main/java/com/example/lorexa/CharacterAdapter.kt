package com.example.lorexa

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class CharacterAdapter(private val list: List<Character>) :
    RecyclerView.Adapter<CharacterAdapter.ViewHolder>() {
    var onItemClick: ((Character) -> Unit)? = null
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.characterImage)
        val name: TextView = view.findViewById(R.id.characterName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        holder.image.setImageResource(item.image)

        holder.itemView.setOnClickListener {
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).duration = 100

                val context = it.context
                val intent = Intent(context, CharacterDetailActivity::class.java)
                intent.putExtra("name", item.name)
                intent.putExtra("image", item.image)
                intent.putExtra("description", item.description)
                context.startActivity(intent)
            }
        }

    }
 }