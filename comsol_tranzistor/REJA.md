# Reja: Kii & Nouchi (2025) natijasini COMSOL 6.0 da olish

Maqsad: 4b-rasmdagi V_on(V_D) ni (92.5 V/V), 4c-rasmdagi to'g'rilash (0.03) va gisterezisni (87%),
shuningdek 2-rasmdagi qiymatlarni COMSOL Semiconductor Module modelida kalibrlash orqali olish.
Qabul chegaralari: `PROMPT.md`, 2-bo'lim (T1–T8).

| # | Bosqich | Kim | Vaqt | Tekshiruv sharti |
|---|---|---|---|---|
| 0 | Maqola tahlili, kompakt model, qo'llanma | bulut (bajarildi) | — | `compact_model.py` T1–T7 ni takrorlaydi |
| 1 | `C:\comsol_tranzistor` va repo klon | siz / skript | 5 daq | `ish\PROMPT.md` mavjud |
| B0 | Muhit: comsolcompile, Python | lokal Claude | 15 daq | test model ishladi |
| B1 | COMSOL 6.0 Application Library'dan Java API nomlarini olish | lokal Claude | 45 daq | `API_ESLATMA.md` |
| B2 | To'liq geometriya (`GEOMETRIYA.md`: Si/SiO2/MoS2/Cr/Au/F4TCNQ/suv/havo) + Schottky kontaktlar | lokal Claude | 1.5 soat | yaqinlashadi, I_D > 0 |
| B3 | Transfer sweep, V_on | lokal Claude | 1 soat | V_on(V_D) chiqadi |
| B4 | Kalibrlash: D_it → SS, Phi_B0 → V_on, beta → qiyalik | lokal Claude | 1.5 soat | T1, T2, T3, T8 |
| B5 | Time Dependent chiqish xarakteristikasi (Global ODE ionlar) | lokal Claude | 1.5 soat | T4, T5, T6 |
| B6 | 2-rasm qurilmasi | lokal Claude | 1 soat | T7 |
| B6b | B modeli: ion transporti (ixtiyoriy) | lokal Claude | vaqt qolsa | c0, D qiymatlari |
| B7 | Grafiklar (maqola nuqtalari + model), `HISOBOT.md` | lokal Claude | 45 daq | hisobot bor |
| B8 | Natijalarni repo'ga commit va push | lokal Claude | 5 daq | commit |

Muhim: hisobotdagi har bir raqam haqiqiy COMSOL hisobidan kelishi shart. Maqsadga yetilmasa, farqi
ochiq yoziladi (`PROMPT.md`, 0-bo'lim).
