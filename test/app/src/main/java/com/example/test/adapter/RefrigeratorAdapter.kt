package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.refrigerator.Refrigerator
import java.io.File

class RefrigeratorAdapter(
    private val onEdit: (Refrigerator) -> Unit
) : RecyclerView.Adapter<RefrigeratorAdapter.VH>() {

    private val items = mutableListOf<Refrigerator>()

    fun submit(list: List<Refrigerator>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgCover)
        val name: TextView = v.findViewById(R.id.tvName)
        val memo: TextView = v.findViewById(R.id.tvMemo)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_refrigerator, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.memo.text = item.memo ?: ""

        val raw = item.imageUrl
        if (raw.isNullOrBlank()) {
            holder.img.setImageResource(R.drawable.img_kitchen1)
        } else {
            val model: Any = when {
                raw.startsWith("http", true) || raw.startsWith("content://", true) || raw.startsWith("file://", true) -> raw
                raw.startsWith("/") -> java.io.File(raw)
                else -> raw
            }
            Glide.with(holder.img).load(model).placeholder(R.drawable.img_kitchen1).into(holder.img)
        }



        holder.btnEdit.setOnClickListener { onEdit(item) }
    }


    override fun getItemCount(): Int = items.size
}
