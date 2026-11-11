# 📦 Overstacked

[![en](https://img.shields.io/badge/lang-English-blue.svg)](#english) [![ru](https://img.shields.io/badge/lang-Русский-green.svg)](#русский)

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1_--_26.3-2E8B57?style=for-the-badge&logo=minecraft&logoColor=white)
![Mod Loader](https://img.shields.io/badge/Fabric-0.16.0+-D32F2F?style=for-the-badge)
![API](https://img.shields.io/badge/Fabric_API-Required-1976D2?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-FBC02D?style=for-the-badge)

---

<a name="english"></a>
## 🇬🇧 English

> **Overstacked** is the ultimate stack-size overhaul mod for Minecraft (1.20.1 – 26.3). Break free from vanilla limits and stack items up to **1,000,000,000 (1 Billion)** per slot with intelligent UI formatting, zero-lag physics, and a powerful XML/JSON configuration engine!

### ✨ Key Features

- **🚀 Extreme Stack Limits:** Increase stack sizes up to `1,000,000,000` items per slot without triggering vanilla Data Component or Codec validation errors.
- **🎨 Smart UI Number Formatting:** No more overlapping text! Large numbers are automatically abbreviated to fit cleanly inside standard 16x16 inventory icons:
  | Item Count | UI Display | Example |
  | :--- | :---: | :--- |
  | `< 10,000` | Standard Integer | `9999` |
  | `10,000` – `999,999` | Thousand Suffix (`K`) | `10K`, `500K` |
  | `1,000,000` – `999,999,999` | Million Suffix (`M`) | `1M`, `250M` |
  | `>= 1,000,000,000` | Billion Suffix (`B`) | `1B` |
- **🛡️ Zero-Lag Container Destruction:** Breaking a chest with 1 billion cobblestone won't freeze your game or spawn millions of dropped entities. Overstacked patches vanilla item scattering so massive stacks drop as **one clean item entity**.
- **⚡ Instant Safe Splitting:** Right-clicking or shift-clicking huge stacks calculates splits instantly using direct math (`count / 2`) instead of laggy item-by-item loops.
- **🌐 Client & Server Sync:** Install on both client and server `mods/` folders. Custom rules and limits automatically synchronize over the network.

### 🛠️ Configuration & Setup

Overstacked features a flexible hybrid configuration architecture designed for both casual players and modpack creators:

1. **In-Game Quick Setup:** Open Mods → Overstacked → Configure through Mod Menu and Cloth Config. Changing server settings requires operator permission level 2.
2. **XML Rules Engine:** For advanced tuning, manually edit your stack rules using clean XML markup inside `run/config/overstacked-rules.xml`.
3. **JSON Template Overrides:** Force-stack normally unstackable items (e.g., Totems of Undying, Ender Pearls, Potions, custom modded items, or `#tags`) by adding them directly into `overstacked-template-overrides.json`.

### 📦 Requirements & Installation

1. Install [Fabric Loader](https://fabricmc.net/) `>= 0.16.0` for Minecraft `1.21.1`.
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Place the `Overstacked.jar` file into your `mods/` folder on **both** client and server:
   * **Windows:** `%appdata%\.minecraft\mods`
   * **Mac:** `~/Library/Application Support/minecraft/mods`
   * **Linux:** `~/.minecraft/mods`

---

<a name="русский"></a>
## 🇷🇺 Русский

> **Overstacked** — это глобальный мод на переработку размеров стаков для Minecraft (1.20.1 – 26.3). Забудьте про ванильное ограничение в 64 предмета! Стакайте лут вплоть до **1 000 000 000 (1 миллиарда)** штук в один слот с аккуратной отрисовкой чисел, защитой от лагов и мощной системой настройки через XML/JSON!

### ✨ Главные особенности

- **🚀 Гигантские стаки:** Увеличивайте вместимость ячеек вплоть до `1 000 000 000` предметов без крашей и ошибок ванильной проверки данных (Codecs).
- **🎨 Умное сокращение чисел в инвентаре:** Огромные числа больше не вылезают за границы иконок! Интерфейс автоматически форматирует текст в ячейках 16x16:
  | Количество предметов | Отображение в игре | Пример |
  | :--- | :---: | :--- |
  | `< 10 000` | Обычное число | `9999` |
  | `10 000` – `999 999` | Тысячи (суффикс `K`) | `10K`, `500K` |
  | `1 000 000` – `999 999 999` | Миллионы (суффикс `M`) | `1M`, `250M` |
  | `>= 1 000 000 000` | Миллиарды (суффикс `B`) | `1B` |
- **🛡️ Защита от лагов при разрушении сундуков:** Если вы сломаете сундук с миллиардом булыжников, игра не зависнет от попытки заспавнить миллионы кучек по 64 штуки. Мод изменяет физику выпадения — весь огромный стак аккуратно выпадает **одной лежащей кучкой**.
- **⚡ Мгновенное разделение:** Клик правой кнопкой мыши или Shift-клик по стаку из миллиарда предметов делит его ровно пополам мгновенно, без зависаний сервера.
- **🌐 Синхронизация Клиент-Сервер:** Работает как в одиночной игре, так и на серверах. Установите мод в папки `mods/` на сервере и клиенте — все правила и лимиты синхронизируются автоматически.

### 🛠️ Настройка и управление

Overstacked использует гибкую гибридную систему конфигурации, удобную как для обычных игроков, так и для авторов сборкок:

1. **Быстрая настройка в игре:** Откройте Mods → Overstacked → Configure через Mod Menu и Cloth Config. Изменение серверных настроек требует прав оператора уровня 2.
2. **Движок правил XML:** Для тонкой ручной настройки редактируйте параметры стаков в файле `run/config/overstacked-rules.xml`.
3. **Исключения через JSON (Overrides):** Чтобы сделать стакаемыми нестандартные предметы (Тотемы бессмертия, зелья, эндер-жемчуг, лут из других модов или целые теги `#tags`), просто добавьте их в файл `overstacked-template-overrides.json`.

### 📦 Требования и установка

1. Установите [Fabric Loader](https://fabricmc.net/) `>= 0.16.0` для Minecraft `1.21.1`.
2. Скачайте и установите [Fabric API](https://modrinth.com/mod/fabric-api).
3. Поместите файл `Overstacked.jar` в папку `mods/` **как на клиенте, так и на сервере**:
   * **Windows:** `%appdata%\.minecraft\mods`
   * **Mac:** `~/Library/Application Support/minecraft/mods`
   * **Linux:** `~/.minecraft/mods`

---

## 📄 License / Лицензия

This project is licensed under the **MIT License** — feel free to use it in your modpacks!  
Проект распространяется под лицензией **MIT** — вы можете свободно использовать его в своих сборках модов!
