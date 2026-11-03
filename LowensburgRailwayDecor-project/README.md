# Lowensburg Railway Decor (Fabric 1.20.1)

## Как получить .jar (без установки чего-либо)
1. Зайдите на github.com, создайте репозиторий (New repository).
2. Add file -> Upload files: загрузите ВСЁ содержимое этой папки, включая скрытую папку `.github`.
   (Если папка `.github` не загрузилась: Add file -> Create new file, имя `.github/workflows/build.yml`, вставьте содержимое файла.)
3. Вкладка Actions -> Build mod -> Run workflow (сборка стартует и сама при загрузке).
4. Через 2-4 минуты откройте завершённый запуск, внизу в Artifacts скачайте `LowensburgRailwayDecor` (zip с .jar).
5. Положите .jar в `.minecraft/mods`. Нужны Fabric Loader 0.15+ и Fabric API 0.92.2 для 1.20.1.

Если сборка упала, пришлите текст ошибки из лога.
