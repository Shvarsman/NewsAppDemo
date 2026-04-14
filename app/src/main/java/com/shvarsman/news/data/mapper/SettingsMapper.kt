package com.shvarsman.news.data.mapper

import com.shvarsman.news.domain.entity.RefreshConfig
import com.shvarsman.news.domain.entity.Settings

fun Settings.toRefreshConfig(): RefreshConfig {
    return RefreshConfig(language, interval, wifiOnly)
}