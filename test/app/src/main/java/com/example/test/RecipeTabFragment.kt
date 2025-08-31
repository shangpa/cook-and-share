package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.MyWriteRecipeAdapter
import com.example.test.model.recipeDetail.MyWriteRecipe
import com.example.test.model.recipeDetail.MyWriteRecipeResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeTabFragment : Fragment() {

    companion object {
        private const val ARG_USER_ID = "arg_user_id"

        // ① 내 레시피용 (기존과 동일)
        fun newInstance() = RecipeTabFragment()

        // ② 타인 레시피용
        fun newInstance(userId: Int) = RecipeTabFragment().apply {
            arguments = Bundle().apply { putInt(ARG_USER_ID, userId) }
        }
    }

    private var targetUserId: Int = -1

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MyWriteRecipeAdapter
    private var recipeList: List<MyWriteRecipe> = emptyList()

    private lateinit var latestLayout: LinearLayout
    private lateinit var popularityLayout: LinearLayout
    private lateinit var dateLayout: LinearLayout
    private lateinit var latestText: TextView
    private lateinit var popularityText: TextView
    private lateinit var dateText: TextView

    private enum class Sort { LATEST, POPULARITY, DATE }
    private var currentSort = Sort.LATEST

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_recipe_tab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.writeRecipeRecyclerView)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyWriteRecipeAdapter(recipeList) { item ->
            Toast.makeText(requireContext(), "${item.title} 클릭", Toast.LENGTH_SHORT).show()
        }
        recycler.adapter = adapter

        // 정렬 버튼
        latestLayout = view.findViewById(R.id.latest)
        popularityLayout = view.findViewById(R.id.popularity)
        dateLayout = view.findViewById(R.id.date)
        latestText = view.findViewById(R.id.latest_text)
        popularityText = view.findViewById(R.id.popularity_text)
        dateText = view.findViewById(R.id.date_text)

        latestLayout.setOnClickListener { onSortSelected(Sort.LATEST) }
        popularityLayout.setOnClickListener { onSortSelected(Sort.POPULARITY) }
        dateLayout.setOnClickListener { onSortSelected(Sort.DATE) }
        targetUserId = arguments?.getInt(ARG_USER_ID, -1) ?: -1

        applySortUI(Sort.LATEST)
        fetchMyRecipes("latest")
    }

    private fun onSortSelected(sort: Sort) {
        if (currentSort == sort) return
        currentSort = sort

        val sortKey = when (sort) {
            Sort.LATEST -> "latest"
            Sort.POPULARITY -> "views"
            Sort.DATE -> "date"
        }

        applySortUI(sort)
        fetchMyRecipes(sortKey)
    }

    private fun applySortUI(sort: Sort) {
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = 0xFF8A8F9C.toInt()
        val bgSelected = ContextCompat.getDrawable(requireContext(), R.drawable.btn_fridge_ct_ck)
        val bgUnselected = ContextCompat.getDrawable(requireContext(), R.drawable.btn_fridge_ct)

        // 최신순
        if (sort == Sort.LATEST) {
            latestLayout.background = bgSelected
            latestText.setTextColor(selectedTextColor)
        } else {
            latestLayout.background = bgUnselected
            latestText.setTextColor(unselectedTextColor)
        }

        // 인기순
        if (sort == Sort.POPULARITY) {
            popularityLayout.background = bgSelected
            popularityText.setTextColor(selectedTextColor)
        } else {
            popularityLayout.background = bgUnselected
            popularityText.setTextColor(unselectedTextColor)
        }

        // 날짜순
        if (sort == Sort.DATE) {
            dateLayout.background = bgSelected
            dateText.setTextColor(selectedTextColor)
        } else {
            dateLayout.background = bgUnselected
            dateText.setTextColor(unselectedTextColor)
        }
    }
    private fun fetchRecipes(sort: String) {
        val rawToken = App.prefs.token
        if (rawToken.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val token = "Bearer $rawToken"

        // 액티비티가 넣어준 대상 유저 ID (없거나 <=0 이면 null)
        val targetId: Int? = activity
            ?.intent
            ?.getIntExtra("targetUserId", -1)
            ?.takeIf { it > 0 }

        RetrofitInstance.apiService.getMyRecipes(
            token,
            sort = sort,
            categories = emptyList(),
            userId = targetId              // ← 내 프로필이면 null, 타인이면 해당 ID
        ).enqueue(object : Callback<MyWriteRecipeResponse> {
            override fun onResponse(
                call: Call<MyWriteRecipeResponse>,
                response: Response<MyWriteRecipeResponse>
            ) {
                if (!isAdded) return
                if (response.isSuccessful) {
                    val body = response.body() ?: return
                    recipeList = body.recipes
                    adapter = MyWriteRecipeAdapter(recipeList) { item ->
                        Toast.makeText(requireContext(), "${item.title} 클릭", Toast.LENGTH_SHORT).show()
                    }
                    recycler.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyWriteRecipeResponse>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchMyRecipes(sort: String) {
        val token = App.prefs.token
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.apiService.getMyRecipes("Bearer $token", sort, emptyList())
            .enqueue(object : Callback<MyWriteRecipeResponse> {
                override fun onResponse(
                    call: Call<MyWriteRecipeResponse>,
                    response: Response<MyWriteRecipeResponse>
                ) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val body = response.body() ?: return
                        recipeList = body.recipes
                        adapter = MyWriteRecipeAdapter(recipeList) { item ->
                            Toast.makeText(requireContext(), "${item.title} 클릭", Toast.LENGTH_SHORT).show()
                        }
                        recycler.adapter = adapter
                    } else {
                        Toast.makeText(requireContext(), "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyWriteRecipeResponse>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
