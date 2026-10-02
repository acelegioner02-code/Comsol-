# Konferensiya afishasi — 6 ta dizayn varianti

«Innovatsion pedagogika: tabiiy va aniq fanlarni o‘qitishda global tajriba va mahalliy yechimlar»
Urganch, 2026-yil 12-oktabr · UrDPI

| # | Fayl | Uslub |
|---|------|-------|
| 1 | `png/v1_klassik.png` | Klassik Xorazm — to‘q ko‘k + oltin, girih naqsh, Xiva siluyeti (rasmiy) |
| 2 | `png/v2_formula.png` | Ilmiy formula — blueprint to‘r, atom, formulalar, yo‘nalish chiplari (zamonaviy) |
| 3 | `png/v3_minimal.png` | Minimal (Swiss) — oq fon, katta tipografiya, 4 fan plitkasi |
| 4 | `png/v4_foto.png` | Foto + gradient — asl rasm sahnasi, firuza fon, sariq aksent |
| 5 | `png/v5_majolika.png` | Xiva majolikasi — kobalt/firuza koshin naqsh, peshtoq arkasi |
| 6 | `png/v6_global.png` | Global & mahalliy — tong gradienti, globus tarmog‘i + Xiva siluyeti |

`png/00_umumiy_korinish.png` — barchasi bitta sahifada.

PNG o‘lchami: 2160×3240 px (2:3). Matnni tahrirlash: `src/*.html` ni o‘zgartirib,
`NODE_PATH=$(npm root -g) node build.js` ni ishga tushiring (Playwright kerak).

## Tiniq variantlar (`tiniq/`)

Asl afisha asosida: fon rasmi Real-ESRGAN bilan 4x kattalashtirilgan (`assets/bg_up.jpg`, 3412×5120),
logo (`parts/logo_urspi.svg`), matn, naqshlar va havolalar esa vektor ko'rinishida qayta terilgan.

| Fayl | Tavsif | O'lcham |
|------|--------|---------|
| `tiniq/out/A_asl_tiniq.*` | Asl dizayn, tiniq, pastda ko'k havolalar qatori | 3240×4860 (+PDF) |
| `tiniq/out/B_kok_sarlavha.*` | Yuqori qismi ko'k-oltin, sahna pastda | 3240×4860 (+PDF) |
| `tiniq/out/C_oltin_ramka.*` | Oltin ramka, ko'k urg'u, sana alohida blokda | 3240×4860 (+PDF) |
| `tiniq/out/D_banner_16x9.*` | Ekran / LED / taqdimot uchun banner | 3840×2160 |
| `tiniq/out/E_kvadrat_instagram.*` | Instagram / Telegram uchun kvadrat | 2160×2160 |

Qayta yig'ish: `NODE_PATH=$(npm root -g) node tiniq/build.js [filtr] [masshtab]`

Ingliz tilidagi nusxalar: `tiniq/out/*_EN.*` (A, B, C — PNG/JPG/PDF; D, E — PNG/JPG).
Ijtimoiy tarmoq ikonkalari asl brend ranglarida (Facebook, Instagram, Telegram).
