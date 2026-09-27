# 1-model (vertikal, 2D o'q-simmetrik): parametrlar, farazlar, ishga tushirish

Manba maqola: Troyan & Doronin, ICCS 2020, LNNS 186, 427–433 (2021), doi:10.1007/978-3-030-66093-2_41.
Maqolada o'lchamlar, material parametrlari va bosim qiymati **berilmagan**. Shuning uchun quyidagi
qiymatlarning ko'pchiligi taxmin yoki fitting natijasi.

Belgilar: **M** — maqoladan (Fig. 1a, grafikdan taxminan o'qilgan); **T** — taxmin; **F** — fitting;
**H** — hisoblangan; **TEKSHIRILSIN** — adabiyot qiymati, lekin aniq manbani o'zingiz tasdiqlang.

## Parametrlar jadvali

| Nomi | Qiymati | Birligi | Manba / asos |
|---|---|---|---|
| R_dev | 50 | nm | T (topshiriq) |
| t_be, t_te | 10, 10 | nm | T (elektrodlar, natijaga deyarli ta'sir qilmaydi) |
| t_ST | 20 | nm | T (topshiriq) |
| t_int | 1.5 | nm | T (bitta vdW bo'shlig'i + qo'shni Te qatlamlari tartibi) |
| t_GT | 20 | nm | T (topshiriq) |
| r_f | 5 | nm | T (topshiriq) |
| sig_m | 5e6 | S/m | T, TiN (ρ ≈ 20 µΩ·cm) — TEKSHIRILSIN |
| sig_ST | 1e5 | S/m | T, degenerat p-Sb2Te3 (ρ ~ 1e-3…1e-4 Ω·cm) — TEKSHIRILSIN |
| sig_GT | 1e5 | S/m | T, degenerat p-GeTe (ρ ~ 1e-3…1e-4 Ω·cm) — TEKSHIRILSIN |
| sig_off | 1.9 (boshl.) | S/m | F → R_OFF = 100 kΩ. H: t_int/(R_OFF·πR_dev²) = 1.5e-9/(1e5·7.85e-15) ≈ 1.9 |
| sig_on | 3e3 (boshl.) | S/m | F → R_ON = 7 kΩ. H: filament tarmog'i ≈ 7.5 kΩ, undan ≈ 1 kΩ tarqalish qarshiligi (≈ 1/(4σa) har tomonda) ayriladi → σ_on ≈ t_int/(6.5 kΩ·πr_f²) ≈ 2.9e3 |
| epsr_ST, epsr_GT, epsr_int, epsr_m | 50, 30, 10, 1 | 1 | T; stationar EC natijasiga ta'sir qilmaydi — TEKSHIRILSIN |
| k_m | 20 | W/(m·K) | T, yupqa TiN plyonka — TEKSHIRILSIN |
| k_ST | 1.0 | W/(m·K) | T, Sb2Te3 c-o'qi bo'yicha — TEKSHIRILSIN |
| k_GT | 2.0 | W/(m·K) | T — TEKSHIRILSIN |
| k_int | 0.5 | W/(m·K) | T (vdW bo'shliq, termik chegara qarshiligini ham o'z ichiga oladi) |
| rho_m, rho_ST, rho_GT, rho_int | 5220, 6500, 6140, 6300 | kg/m³ | TEKSHIRILSIN (rho_int — ST va GT o'rtachasi, faraz) |
| Cp_ST | 200 | J/(kg·K) | H, Dyulong–Pti: 5·3R/M, M = 626 g/mol |
| Cp_GT | 250 | J/(kg·K) | H, Dyulong–Pti: 2·3R/M, M = 200 g/mol |
| Cp_m, Cp_int | 600, 220 | J/(kg·K) | H (TiN: 37 J/(mol·K)/61.9 g/mol); faraz |
| Tm_GT | 998 (≈725 °C) | K | TEKSHIRILSIN |
| Tm_ST | 891 (≈618 °C) | K | TEKSHIRILSIN |
| T_amb | 300 | K | topshiriq |
| k0 | 1e13 | 1/s | T, fonon (urinish) chastotasi tartibi |
| Ea | 0.9 | eV | F boshlang'ich qiymati (quyidagi baho) |
| a_hop | 0.3 | nm | F boshlang'ich qiymati (Te–Te masofasi tartibi) |
| tau_rel | 1e3 | s | T; ≫ 30 ms → nonvolatil |
| p_win | 2 | 1 | T (Joglekar/Biolek oynasi) |
| V_read | 0.1 | V | T (S1 chiziqli, qiymat R ga ta'sir qilmaydi) |
| Vamp, f0 | 4.5, 100 | V, Hz | M/topshiriq |
| R_ON_t | 7 | kΩ | M (0.43 mA, 3 V) |
| R_OFF_t | 100 | kΩ | M (~, tartib bo'yicha) |
| V_SET_t, V_RESET_t | +3.5, −3.5 | V | M |
| h_int, h_glob | 0.5, 2 | nm | topshiriq / T |

### Kinetika uchun boshlang'ich qiymatlarning asosi (3-bosqichda ishlatiladi)

OFF holatda kuchlanishning deyarli hammasi interfeysga tushadi: |Ez| ≈ 3.5 V / 1.5 nm ≈ 2.3e9 V/m.
a = 0.3 nm, T = 300 K da sinh argumenti q·a·E/(2kBT) ≈ 13.5, ya'ni sinh ≈ 3.6e5.
Uchburchak signalda dV/dt = 4·Vamp·f0 = 1800 V/s, V_SET atrofidagi ±0.2 V oraliqda ~0.2 ms o'tadi,
demak V_SET da tezlik ~5e3 1/s bo'lishi kerak. Unda k0·exp(−Ea/kBT) ≈ 1.4e-2 1/s, k0 = 1e13 1/s da
Ea ≈ 0.89 eV. Joul isishi bu bahoni o'zgartiradi, shuning uchun Ea va a keyinroq fitting qilinadi.

## Farazlar va cheklovlar

1. Model **fenomenologik**. Weyl/Dirac fazalari, Fermi yoylari va topologik holatlar modellashtirilmaydi va model ularni isbotlamaydi.
2. "Filament" (GeSbTe4) — radiusi r_f bo'lgan va interfeys qatlami ichidagi silindr. Uning hajmi va shakli qat'iy, faqat o'tkazuvchanligi x orqali o'zgaradi.
3. σ_int = σ_off^(1−x)·σ_on^x (log-chiziqli interpolyatsiya). Filamentdan tashqaridagi halqa doim σ_off.
4. Real element lateral bo'lishi mumkin (FET varianti), lekin 1-model vertikal. Barcha qarshilik interfeysda deb qaraladi, kontakt qarshiliklari hisobga olinmaydi (R_ON va R_OFF ga fitting orqali "singib ketadi").
5. Elektrod materiali noma'lum, TiN deb olingan. Maqolada "metall elektrodlar" deyilgan, xolos.
6. GeTe/Sb2Te3 o'tkazuvchanligi izotrop va haroratga bog'liq emas (1-bosqich).
7. Ferroelektrik maydon aniq modellashtirilmaydi. Uning barqarorlashtiruvchi roli τ_rel orqali fenomenologik beriladi (katta τ_rel → xotira).
8. Fig. 1 qiymatlari grafikdan ko'z bilan o'qilgan (±20 % noaniqlik).
9. Material qiymatlari yupqa plyonka uchun emas, hajmiy material uchun (TEKSHIRILSIN belgili qiymatlar).

## Ishga tushirish (Windows, COMSOL 6.0)

```bat
cd <Model1_Vertical.java joylashgan papka>
"C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64\comsolcompile.exe" Model1_Vertical.java
"C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64\comsolbatch.exe" -inputfile Model1_Vertical.class -outputfile Model1_Vertical.mph > log_S1.txt 2>&1
```

Chiqish fayllari: `Model1_Vertical.mph`, `S1_calibration.csv`, `S1_R_on_off.csv`, `S1_V_axis.csv` va `log_S1.txt`.
