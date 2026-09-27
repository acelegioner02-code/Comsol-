# Tungi avtonom ish hisoboti

Boshlanish: 2026-09-28 (mahalliy vaqt, sessiya boshlanishi). Vazifa matni: `TUNGI_VAZIFA.md`.

Bu fayl har bir bosqichdan keyin yangilanadi. Eng so'nggi holat pastda.

## Holat jadvali

| Vazifa | Holat |
|---|---|
| 0. Sozlash (vazifa/hisobot fayllari) | BOSHLANDI |
| 1. Model1 T_amb sweep + N1-N6 | KUTILMOQDA |
| 2. Model2_FET.java | KUTILMOQDA |
| 3. Model3_Pressure.java (N7) | KUTILMOQDA |
| 4. N8 sezgirlik | KUTILMOQDA |

## Jurnalning boshlanishi

- `models/TUNGI_VAZIFA.md` va `models/TUNGI_HISOBOT.md` yaratildi.
- Oldingi sessiyada erishilgan holat (kontekst uchun): Model1_Vertical.java 1-3-bosqichlarni
  o'z ichiga oladi (S1 Stationary kalibrovka, S2 Heat Transfer, S3 Global ODE Stationary tekshiruvi,
  S4 Time Dependent bitta tsikl T_amb=300K, 3 davr, 0-30ms). Oxirgi tasdiqlangan natija: sig_off va
  sig_on kalibrlangan (R_OFF=99.99 kOhm, R_ON=6.99 kOhm), V_SET=3.513V, V_RESET=-3.278V (Biolek
  oynasi, tanh bilan silliqlashtirilgan, E_s=1e7 V/m). Bitta 3-davrli tsikl hisoblash vaqti ~50-52
  daqiqa (T_amb=300K).
- MUHIM: bitta tsikl (3 davr, 30ms) T_amb=300K uchun ~50 daqiqa vaqt oladi. Tungi vazifada har bir
  T_amb nuqtasi uchun FAQAT 1 davr (0-10ms) so'ralgan - bu vaqtni ~3x qisqartirishi kutiladi
  (~15-20 daqiqa/nuqta), 4 nuqta jami ~60-80 daqiqa.

Davomi pastda, bosqichma-bosqich yoziladi.
