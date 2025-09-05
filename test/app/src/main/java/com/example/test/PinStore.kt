package com.example.test

import android.content.Context

object PinStore {
    private const val PREF = "material_prefs"
    private const val KEY = "pinned_ids" // 최근 고정한 순서를 유지하고 싶으면 리스트로 저장

    // 최근 고정한 게 맨 위로 오게: 앞에 추가
    fun pin(ctx: Context, id: Long) {
        val list = getPinnedOrder(ctx).toMutableList()
        list.remove(id)         // 중복 제거
        list.add(0, id)         // 맨 앞(최상단)으로
        save(ctx, list)
    }

    fun unpin(ctx: Context, id: Long) {
        val list = getPinnedOrder(ctx).toMutableList()
        list.remove(id)
        save(ctx, list)
    }

    fun isPinned(ctx: Context, id: Long): Boolean =
        getPinnedOrder(ctx).contains(id)

    fun getPinnedOrder(ctx: Context): List<Long> {
        val s = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "") ?: ""
        if (s.isBlank()) return emptyList()
        return s.split(",").mapNotNull { it.toLongOrNull() }
    }

    private fun save(ctx: Context, list: List<Long>) {
        val s = list.joinToString(",")
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY, s).apply()
    }
}
