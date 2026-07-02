# NewsAppDemo

Учебное Android-приложение для работы с [NewsAPI](https://newsapi.org/).

Построено на Clean Architecture (data / domain / presentation), MVVM, Hilt DI, Room,
Retrofit + kotlinx.serialization, Jetpack Compose, WorkManager, DataStore, Coil.

## Запуск проекта

Для успешной сборки и запуска проекта необходимо создать в корне проекта файл `keystore.properties`  
и добавить в него ключ API в следующем формате:

```properties
NEWS_API_KEY="YOUR_API_KEY"
```

---

## QA / Тестирование

Этот проект дополнительно используется как демонстрация подхода к обеспечению качества
Android-приложения — от ручного анализа и документации до многоуровневой автоматизации.

### Документация
- **[TEST_PLAN.md](./TEST_PLAN.md)** — стратегия тестирования: объём, уровни, инструменты, риски
- **[TEST_CASES.md](./TEST_CASES.md)** — детальные тест-кейсы с приоритизацией (P0–P2) и трассировкой к автотестам
- **[BUG_REPORTS.md](./BUG_REPORTS.md)** — 5 реальных багов (найденных как код-ревью, так и в процессе написания тестов), с шагами воспроизведения, root cause и regression-тестами

### Автоматизация
| Уровень | Что покрыто | Инструменты |
|---|---|---|
| Unit-тесты | Мапперы, Use Case'ы, ViewModel'и | JUnit4, MockK, kotlinx-coroutines-test, Turbine, Truth |
| Интеграционные тесты | Room DAO (реальная in-memory БД, foreign keys, каскадное удаление) | Robolectric |
| Интеграционные тесты | Repository (обработка сетевых ошибок, дедупликация) | MockK, MockWebServer |
| UI-тесты | Ключевые сценарии экранов Subscriptions и Settings | Jetpack Compose UI Testing |
| Smoke-тест | Запуск приложения без падений | AndroidX Test / Espresso |

Локальный запуск:

```bash
./gradlew testDebugUnitTest        # unit + Robolectric-интеграционные тесты
./gradlew jacocoTestReport         # HTML/XML отчёт покрытия -> app/build/reports/jacoco
./gradlew connectedDebugAndroidTest # UI-тесты и smoke-тест (нужен эмулятор/устройство)
```

### CI
GitHub Actions (`.github/workflows/android-firebase-testlab.yml`) на каждый push/PR:
1. **test-and-build** — прогон unit-тестов, генерация и выгрузка JaCoCo-отчёта, сборка APK
2. **instrumented-tests** — Compose UI-тесты и smoke-тест на Android-эмуляторе (API 33), поднимается прямо на раннере GitHub Actions, без GCP-биллинга
3. **firebase-test-lab** — опциональный прогон на матрице реальных устройств Firebase Test Lab (выключен по умолчанию, требует настройки GCP)

