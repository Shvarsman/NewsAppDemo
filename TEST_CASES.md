# Test Cases — NewsAppDemo

Легенда приоритетов: **P0** — Critical, **P1** — High, **P2** — Medium.
Статус отражает результат ручной проверки логики / соответствующего автотеста.
Ссылки `[TC-XXX]` используются в `BUG_REPORTS.md` для трассировки от бага к кейсу.

---

## 1. Подписки (Subscriptions Screen)

| ID | Название | Приоритет | Предусловия | Шаги | Ожидаемый результат | Статус | Автотест |
|---|---|---|---|---|---|---|---|
| TC-001 | Экран подписок пуст при первом запуске | P1 | Подписок нет | Открыть экран Subscriptions | Отображается сообщение "No subscriptions" | Pass | `SubscriptionsScreenTest.emptyState_showsNoSubscriptionsMessage` |
| TC-002 | Кнопка "Добавить" неактивна для пустого поля ввода | P1 | Поле ввода темы пусто | Открыть экран Subscriptions | Кнопка "Add" задизейблена | Pass | `SubscriptionsScreenTest.addSubscriptionButton_isDisabledForEmptyQuery` |
| TC-003 | Кнопка "Добавить" неактивна для строки из одних пробелов | P2 | — | Ввести в поле "   " (только пробелы) | Кнопка "Add" остаётся задизейбленной | Pass | `SubscriptionsViewModelTest.subscribe button is disabled for a blank query` |
| TC-004 | Успешное добавление новой подписки | P0 | Поле ввода пусто | 1. Ввести "kotlin" 2. Нажать "Add" | Подписка "kotlin" добавляется в список, поле ввода очищается | Pass | `SubscriptionsScreenTest.typingTopicAndClickingAdd_callsAddSubscriptionUseCase` |
| TC-005 | Пробелы по краям темы обрезаются при добавлении | P2 | — | Ввести "  kotlin  ", нажать "Add" | В подписки добавляется ровно "kotlin", без пробелов | Pass | `SubscriptionsViewModelTest.ClickSubscribe adds a trimmed topic...` |
| TC-006 | Новая подписка появляется в списке с выбранным по умолчанию флагом | P1 | — | Добавить подписку "kotlin" | Подписка отображается как "включена" (её статьи попадают в ленту) | Pass | `SubscriptionsViewModelTest.newly observed subscriptions are selected by default` |
| TC-007 | Отключение отображения подписки (без удаления) | P1 | Есть активная подписка "kotlin" | Тапнуть по чипу "kotlin", чтобы снять выделение | Статьи по "kotlin" пропадают из ленты, но подписка остаётся в списке | Pass | `SubscriptionsViewModelTest.ToggleTopicSelection flips the selection flag...` |
| TC-008 | Удаление подписки полностью | P0 | Есть подписка "kotlin" | Удалить подписку "kotlin" (например, свайпом/кнопкой) | Подписка исчезает из списка, её статьи больше не подгружаются | Pass | `SubscriptionsViewModelTest.RemoveSubscription delegates...` |
| TC-009 | Очистка статей только по выбранным темам | P1 | Подписки "kotlin" (выбрана) и "android" (снята) | Нажать "Clear articles" | Удаляются статьи только по "kotlin"; статьи "android" не трогаются | Pass | `SubscriptionsViewModelTest.ClearArticles clears only the currently selected topics` |
| TC-010 | Ручное обновление ленты (pull-to-refresh / кнопка) | P1 | Есть активные подписки | Инициировать обновление | Вызывается загрузка новых статей по всем подпискам с текущим языком | Pass | `SubscriptionsViewModelTest.RefreshData triggers...` |
| TC-011 | Переход на экран настроек по иконке | P2 | — | Нажать на иконку настроек в топбаре | Открывается экран Settings | Pass | `SubscriptionsScreenTest.settingsIcon_navigatesToSettings` |
| TC-012 | Список статей реактивно обновляется при смене набора выбранных тем | P1 | Подписка "kotlin" с одной статьей | Появление подписки "kotlin" в потоке subscriptions | В `state.articles` появляется статья по "kotlin" | Pass | `SubscriptionsViewModelTest.articles are refreshed when the set of selected topics changes` |

---

## 2. Настройки (Settings Screen)

| ID | Название | Приоритет | Предусловия | Шаги | Ожидаемый результат | Статус | Автотест |
|---|---|---|---|---|---|---|---|
| TC-020 | Экран отображает текущие сохранённые настройки | P1 | Settings: EN, 15 минут | Открыть Settings | Виден выбранный язык "English" и интервал "15 minutes" | Pass | `SettingsScreenTest.settingsScreen_showsCurrentLanguageAndInterval` |
| TC-021 | Смена языка через выпадающий список | P0 | Текущий язык — English | Открыть список языков, выбрать "Русский" | Настройка сохраняется, `UpdateLanguageUseCase` вызывается с `Language.RUSSIAN` | Pass | `SettingsScreenTest.selectingLanguageFromDropdown_updatesSettingsViaUseCase` |
| TC-022 | Смена интервала обновления | P0 | Текущий интервал — 15 минут | Выбрать "1 hour" в списке интервалов | `UpdateIntervalUseCase` вызывается с `Interval.HOUR_1` | Pass | `SettingsScreenTest.selectingIntervalFromDropdown_updatesSettingsViaUseCase` |
| TC-023 | Переключение "только через Wi-Fi" | P1 | wifiOnly = true | Тапнуть переключатель Wi-Fi Only | Значение переключается на false и сохраняется | Pass | `SettingsScreenTest.togglingWifiOnlySwitch_updatesSettingsViaUseCase` |
| TC-024 | Переключение уведомлений о новых статьях | P1 | notificationsEnabled = false | Тапнуть переключатель уведомлений | Значение переключается на true и сохраняется | Pass | `SettingsViewModelTest.SetNotificationsEnabled command delegates...` |
| TC-025 | Кнопка "Назад" возвращает на предыдущий экран | P2 | — | Нажать иконку "Назад" | Вызывается колбэк `onBackClick` | Pass | `SettingsScreenTest.backButton_invokesOnBackClickCallback` |
| TC-026 | Состояние экрана реагирует на внешнее изменение настроек | P2 | — | Изменить `Settings` в источнике данных напрямую (например, из другого экрана) | UI обновляется без перезапуска экрана | Pass | `SettingsViewModelTest.state updates reactively when settings change` |

---

## 3. Локальное хранилище / Room (Data Layer)

| ID | Название | Приоритет | Предусловия | Шаги | Ожидаемый результат | Статус | Автотест |
|---|---|---|---|---|---|---|---|
| TC-040 | Повторное добавление одинаковой подписки не создаёт дубликат | P1 | — | Добавить подписку "kotlin" дважды | В таблице подписок ровно одна запись | Pass | `NewsDaoTest.adding the same subscription twice is silently ignored` |
| TC-041 | Удаление подписки каскадно удаляет её статьи | P0 | Подписка "kotlin" со статьёй | Удалить подписку "kotlin" | Связанные статьи удаляются автоматически (foreign key CASCADE) | Pass | `NewsDaoTest.deleting a subscription cascades to its articles` |
| TC-042 | Нельзя сохранить статью для несуществующей подписки | P1 | Подписки "no-such-subscription" нет | Попытаться вставить статью с `topic = "no-such-subscription"` | Room выбрасывает исключение (нарушение foreign key) | Pass | `NewsDaoTest.inserting an article for an unknown topic violates the foreign key constraint` |
| TC-043 | Запрос статей по теме не возвращает статьи других тем | P0 | Подписки "kotlin" и "android" со статьями | Запросить статьи по `["kotlin"]` | В результате только статьи с `topic == "kotlin"` | Pass | `NewsDaoTest.articles for unrequested topics are not returned` |
| TC-044 | Статьи отсортированы от новых к старым | P1 | Две статьи с разным `publishedAt` | Запросить статьи по теме | Список отсортирован по убыванию `publishedAt` | Pass | `NewsDaoTest.articles are ordered by publishedAt descending` |
| TC-045 | Удаление статей по списку тем не затрагивает остальные темы | P1 | Статьи по "kotlin" и "android" | Вызвать удаление статей для `["kotlin"]` | Остаются только статьи "android" | Pass | `NewsDaoTest.deleteArticlesByTopics removes only matching topics` |

---

## 4. Синхронизация с сетью (Repository Layer)

| ID | Название | Приоритет | Предусловия | Шаги | Ожидаемый результат | Статус | Автотест |
|---|---|---|---|---|---|---|---|
| TC-060 | Обновление темы возвращает `true`, если появились новые статьи | P1 | API вернул статью, которой не было в БД | Вызвать `updateArticlesForTopic` | Метод возвращает `true` | Pass | `NewsRepositoryImplTest.updateArticlesForTopic returns true...` |
| TC-061 | Обновление темы возвращает `false`, если все статьи уже известны | P1 | Все статьи из ответа API уже есть в БД (конфликт INSERT IGNORE) | Вызвать `updateArticlesForTopic` | Метод возвращает `false` | Pass | `NewsRepositoryImplTest.updateArticlesForTopic returns false...` |
| TC-062 | Сбой сети при обновлении не приводит к падению приложения | P0 | NewsApiService выбрасывает `IOException` | Вызвать `updateArticlesForTopic` | Исключение перехватывается, метод возвращает `false`, приложение не падает | Pass | `NewsRepositoryImplTest.updateArticlesForTopic does not crash when the API call fails` |
| TC-063 | Массовое обновление возвращает только реально обновившиеся темы | P1 | "kotlin" получил новую статью, "android" — нет | Вызвать `updateArticlesForAllSubscriptions` | Результат содержит только "kotlin" | Pass | `NewsRepositoryImplTest.updateArticlesForAllSubscriptions returns only topics...` |
| TC-064 | Постановка фоновой задачи обновления в WorkManager | P1 | — | Вызвать `startBackgroundRefresh` с конфигом | В `WorkManager` ставится уникальная периодическая задача с политикой `CANCEL_AND_REENQUEUE` | Pass | `NewsRepositoryImplTest.startBackgroundRefresh enqueues a unique periodic work request` |

---

## 5. Маппинг данных (Mappers)

| ID | Название | Приоритет | Предусловия | Шаги | Ожидаемый результат | Статус | Автотест |
|---|---|---|---|---|---|---|---|
| TC-080 | Корректный маппинг языка в параметр запроса API | P1 | — | Вызвать `Language.X.toQueryParam()` для всех значений | Каждому языку соответствует правильный ISO-код (en/ru/fr/de) | Pass | `NewsMapperTest.each language maps to its correct ISO query param` |
| TC-081 | Корректный маппинг известного значения минут в Interval | P1 | — | Вызвать `15.toInterval()`, `60.toInterval()`, `1440.toInterval()` | Возвращаются соответствующие значения `Interval` | Pass | `NewsMapperTest.toInterval maps known minute values correctly` |
| TC-082 | **[Bug regression]** Неизвестное значение минут приводит к падению | P0 | — | Вызвать `42.toInterval()` | Ожидается **graceful fallback**, а не падение — см. BUG-001 | **Fail (баг подтверждён)** | `NewsMapperTest.BUG-001: toInterval throws on unknown minute value...` |
| TC-083 | Маппинг статьи из DTO в DB-модель | P1 | — | Смаппить `ArticleDto` с заполненными полями | Все поля (`title`, `description`, `url`, `imageUrl`, `sourceName`, `topic`) перенесены верно | Pass | `NewsMapperTest.toDbModels maps remote articles to db models...` |
| TC-084 | **[Bug regression]** Неразбираемая дата публикации ломает маппинг вместо fallback | P1 | `publishedAt = "not-a-date"` | Смаппить статью | Ожидается graceful fallback на текущее время — см. BUG-005 | **Fail (баг подтверждён)** | `NewsMapperTest.BUG-005 - toDbModels throws ParseException...` |
| TC-085 | **[Bug regression]** Дата публикации парсится в локальном, а не UTC поясе | P1 | Устройство с таймзоной ≠ UTC | Смаппить статью с `publishedAt = "2024-05-01T10:15:00Z"` | Ожидается timestamp, соответствующий UTC — см. BUG-002 | **Fail (баг подтверждён)** | `NewsMapperTest.BUG-002 - publishedAt is parsed using the local timezone...` |
| TC-086 | Дедупликация полностью идентичных статей (разные подписки, тот же контент) | P2 | Одна и та же статья сохранена под двумя topic | Смаппить в `List<Article>` для UI | В результирующем списке для UI остаётся одна карточка статьи | Pass | `NewsMapperTest.toEntities removes fully duplicate articles` |
| TC-087 | Пустой ответ API маппится в пустой список без ошибок | P2 | `articles = emptyList()` | Смаппить пустой ответ | Результат — пустой список | Pass | `NewsMapperTest.toDbModels returns empty list for empty response` |
| TC-088 | **[Bug regression]** Одна статья с "битой" датой не должна ронять всю партию статей темы | P1 | В ответе API валидная + невалидная статья | Вызвать `updateArticlesForTopic` | Ожидается: сохраняется валидная статья, невалидная пропускается — см. BUG-005 | **Fail (баг подтверждён)** | `NewsRepositoryImplTest.BUG-005 consequence - a single malformed article discards the whole topic batch...` |

---

## 6. Общая работоспособность (Smoke)

| ID | Название | Приоритет | Предусловия | Шаги | Ожидаемый результат | Статус | Автотест |
|---|---|---|---|---|---|---|---|
| TC-100 | Приложение запускается без падений | P0 | Чистая установка | Запустить MainActivity | Activity достигает состояния `RESUMED` | Pass | `AppLaunchSmokeTest.mainActivity_launches_and_reaches_resumed_state` |

---

## Сводка по найденным дефектам (см. `BUG_REPORTS.md` для деталей)

| Bug ID | Связанный TC | Критичность |
|---|---|---|
| BUG-001 | TC-082 | Critical |
| BUG-002 | TC-085 | Medium |
| BUG-003 | — (не покрыт unit-тестом, требует stress/race-condition теста; см. отчёт) | Medium |
| BUG-004 | — (архитектурный риск, не воспроизводится unit-тестом) | Low |
| BUG-005 | TC-084, TC-088 | Medium |
