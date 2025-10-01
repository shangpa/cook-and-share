package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.recipeDetail.ExpectedIngredient
import com.example.test.model.recipeDetail.UsedIngredient
import java.math.BigDecimal

class ExpectedRecipeIngredientsAdapter(
    private val itemList: List<ExpectedIngredient>,
    private var servings: Int = 1
) : RecyclerView.Adapter<ExpectedRecipeIngredientsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val amountNeeded: TextView = view.findViewById(R.id.amountNeeded)
        val totalAmount: TextView = view.findViewById(R.id.totalAmount)
        val date: TextView = view.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expected_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        val amountInRecipe = parseNumber(item.amountInRecipe)
        val amountInFridge = parseNumber(item.amountInFridge)
        val needed = amountInRecipe * servings

        // ✅ 이름이 null/blank면 "재료" 또는 "-" 같은 기본값
        holder.name.text = item.name?.takeIf { it.isNotBlank() } ?: "재료"

        holder.amountNeeded.text = "필요: ${fmt(needed)}${item.unit.orEmpty()}"
        holder.totalAmount.text  = "보유: ${fmt(amountInFridge)}${item.unit.orEmpty()}"

        // ✅ 날짜 표시: 둘 다 비면 GONE, 값이 있으면 "옵션 : 날짜" 형태
        val dateLabel = listOfNotNull(
            item.dateOption?.takeIf { it.isNotBlank() },
            item.date?.takeIf { it.isNotBlank() }
        ).joinToString(" : ")

        if (dateLabel.isBlank()) {
            holder.date.visibility = View.GONE
        } else {
            holder.date.visibility = View.VISIBLE
            holder.date.text = dateLabel
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun updateServings(n: Int) {
        servings = n.coerceAtLeast(1)
        notifyDataSetChanged()
    }

    fun getUsedIngredients(): List<UsedIngredient> {
        return itemList.map {
            val base = parseNumber(it.amountInRecipe)
            UsedIngredient(name = it.name, amount = base * servings, unit = it.unit)
        }
    }

    private fun parseNumber(v: String?): Double =
        v?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0

    private fun fmt(v: Double): String = try {
        BigDecimal(v).stripTrailingZeros().toPlainString()
    } catch (_: Exception) { if (v.toString().endsWith(".0")) v.toInt().toString() else v.toString() }

}
