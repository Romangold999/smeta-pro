# Временная выдача PDF для MAX

Сервис нужен только для кнопки «Печать / PDF» в MAX Mini App.

## Как работает

1. Приложение формирует PDF локально в браузере телефона.
2. PDF по HTTPS отправляется в Cloudflare Worker.
3. Worker шифрует файл AES-GCM и загружает временный зашифрованный asset в GitHub Release `remontmaster-temp-pdf`.
4. MAX скачивает PDF через `window.WebApp.downloadFile()`.
5. Worker получает зашифрованный asset, расшифровывает его в памяти и сразу удаляет asset из GitHub.
6. Плановая очистка Worker удаляет оставшиеся assets старше часа, например если скачивание отменили.

PDF не добавляются в исходники, коммиты или историю Git. В GitHub Release лежат только зашифрованные временные файлы.

## Однократная настройка

### 1. Подготовить GitHub token

Создать Fine-grained personal access token для одного репозитория `Romangold999/smeta-pro`:

- Repository access: Only select repositories → `Romangold999/smeta-pro`
- Repository permissions → Contents: Read and write

Добавить токен в репозиторий GitHub:
Settings → Secrets and variables → Actions → New repository secret

Имя секрета: `GH_RELEASE_TOKEN`.

### 2. Подготовить Cloudflare

Создать Cloudflare API Token с правами:

- Account → Workers Scripts → Edit
- Account → Account Settings → Read

Скопировать Account ID из Cloudflare Dashboard.

Добавить в GitHub Actions secrets:

- `CF_API_TOKEN` — Cloudflare API token
- `CF_ACCOUNT_ID` — Cloudflare Account ID

### 3. Развернуть Worker

GitHub → Actions → **Deploy temporary PDF gateway** → Run workflow.

После успешного запуска в логе Wrangler будет адрес Worker примерно такого вида:

`https://remont-master-pdf.<workers.dev-subdomain>.workers.dev`

### 4. Подключить адрес к приложению

Добавить ещё один GitHub Actions secret:

- `RM_PDF_API_URL` — полный HTTPS-адрес Worker без завершающего слеша.

Затем GitHub → Actions → **Deploy RemontMaster to GitHub Pages** → Run workflow.

## Важные ограничения

- Максимальный размер одного PDF: 20 МБ.
- Ссылка на скачивание действует 1 час.
- Неиспользованные файлы удаляются плановой очисткой не позднее следующего запуска cron (каждые 15 минут после достижения часового срока).
- В случае успешного получения PDF Worker удаляет asset из GitHub сразу после расшифровки.
- Для работы требуется бесплатный Workers-доступ Cloudflare и GitHub Release в репозитории. Никаких платных функций включать не нужно.
- Не публиковать `GH_RELEASE_TOKEN` и не вставлять его в HTML или исходники.
