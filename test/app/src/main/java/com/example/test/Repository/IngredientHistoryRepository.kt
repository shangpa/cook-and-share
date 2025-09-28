package com.example.test.Repository

import com.example.test.model.pantry.IngredientHistoryUi
import com.example.test.model.pantry.toUi
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IngredientHistoryRepository {
    suspend fun loadByPantry(
        bearer: String,
        pantryId: Long,
        ingredientKeyword: String? = null
    ): Result<List<IngredientHistoryUi>> = runCatching {
        withContext(Dispatchers.IO) {
            RetrofitInstance.pantryApi
                .listPantryHistory(bearer, pantryId, ingredientKeyword)
                .map { it.toUi() }
        }
    }
}
