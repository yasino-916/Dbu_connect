package com.dbuconnect.data.api

import com.dbuconnect.BuildConfig

object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()
    
    // Loaded dynamically from local.properties environment configurations
    val resendApiKey: String = BuildConfig.RESEND_API_KEY.trim()

    val isConfigured: Boolean
        get() = url.startsWith("https://") && anonKey.isNotBlank()
}
