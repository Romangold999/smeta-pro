# Смета Pro

Нативное Android-приложение для составления строительных смет.

## Сборка APK
GitHub Actions собирает debug APK при каждом push в `main`.
Открой вкладку **Actions**, выбери последний запуск **Android APK** и скачай artifact **Smeta-Pro-debug**.

## Технологии
- Kotlin + Jetpack Compose (нативный интерфейс, не WebView)
- Локальное сохранение смет на устройстве
- Экспорт PDF и XLSX
- Минимальная версия Android: 8.0 (API 26)
