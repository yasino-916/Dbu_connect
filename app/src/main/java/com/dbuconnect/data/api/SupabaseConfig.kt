package com.dbuconnect.data.api

import com.dbuconnect.BuildConfig

object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    val isConfigured: Boolean
        get() = url.startsWith("https://") && anonKey.isNotBlank()
}
