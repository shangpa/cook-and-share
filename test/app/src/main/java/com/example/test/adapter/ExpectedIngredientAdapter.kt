package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.recipeDetail.ExpectedIngredient
import com.example.test.model.Fridge.UsedIngredientRequest

class ExpectedIngredientAdapter(
    private val itemList: List<ExpectedIngredient>,
    private var servings: Int = 1 ) :
    RecyclerView.Adapter<ExpectedIngredientAdapter.ViewHolder>() {

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

        val amountInRecipe = parseDouble(item.amountInRecipe)  // 문자열 정리 후 double
        val amountInFridge = parseDouble(item.amountInFridge)

        val needed = amountInRecipe * servings

        holder.name.text = item.name
        holder.amountNeeded.text = "필요: ${needed.toInt()}${item.unit ?: ""}"
        holder.totalAmount.text = "보유: ${amountInFridge.toInt()}${item.unit ?: ""}"
        holder.date.text = "구매일자: ${item.date}"
    }

    fun updateServings(newServings: Int) {
        servings = newServings
        notifyDataSetChanged()
    }

    override fun getItemCount() = itemList.size

    private fun parseDouble(value: String?): Double {
        return value
            ?.replace("[^\\d.]".toRegex(), "")  // 숫자와 소수점만 남김
            ?.toDoubleOrNull() ?: 0.0
    }

    fun getUsedIngredients(): List<UsedIngredientRequest> {
        return itemList.map {
            UsedIngredientRequest(
                name = it.name,
                amount = parseDouble(it.amountInRecipe) * servings
            )
        }
    }

}
