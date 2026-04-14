package com.shvarsman.news.domain.usecase

import com.shvarsman.news.domain.entity.Language
import com.shvarsman.news.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(language: Language) {
        settingsRepository.updateLanguage(language)
    }
}
