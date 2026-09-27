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

## 1-vazifa: Model1 T_amb sweep + N1-N6 - ISHGA TUSHIRILDI

Model1_Vertical.java ga qo'shildi:
- std6 (S5): T_amb sweep (300/350/400/450K), har biri 1 davr (0-10ms).
- N1 (I-V chiziqli+log), N3 (x/V/I(t), 3 alohida PNG), N4 (T xaritasi SET paytida + T_max(t) bilan
  Tm_ST/Tm_GT chiziqlari), N5 (|E|,|J| SET dan oldin/keyin, 4 PNG) - hammasi FAQAT T_amb=300K sweep
  nuqtasi uchun (dset6 keyingi T_amb qiymatlariga qayta yozilib ketgani uchun, shu bosqichda
  saqlab qolinadi).
- N6_table.csv: T_amb bo'yicha R_OFF/R_ON (DOIMIY, S1 dan - bu modelda T_amb ga bog'liq emas,
  faqat kinetika Arrhenius orqali bog'liq) + V_SET/V_RESET (sweep dan).
- Eksport: iv_Tamb_sweep.csv, Tmax_t.csv, N6_table.csv.

TEKSHIRILSIN yangi joylar: "looplevel" (dataset'da vaqt nuqtasini tanlash), "sol6" (solver tag nomi),
"ec.normE" (|E| o'zgaruvchisi). Natija tekshirilmoqda (jarayon fonda ishlamoqda).

Kompilyatsiya: MUVAFFAQIYATLI (xatosiz). Ishga tushirish: JARAYONDA.

## 2-vazifa: Model2_FET.java - YOZILDI (hali test qilinmagan)

To'liq yozildi: geometriya (lateral, Kartezian, Sb2Te3 kanali + faol soha + GeTe/Al2O3/zatvor
qatlamlari faqat issiqlik uchun), S1 kalibrovka (Ugate=0), sig_on_v kalibrovkasi (Ugate=-1.1V,
I(4V)~0.04mA), S2 issiqlik tekshiruvi, S4 Time Dependent Ugate sweep (0,-0.5,-0.9,-1.1,-1.5V),
Fig.1d ketma-ketligi (0->-1.1->0V), N2 I-V grafik.

Hali TEST QILINMAGAN (Model1 jarayoni band bo'lgani uchun, "bir vaqtda bitta comsolbatch" qoidasi).
Kutilgan TEKSHIRILSIN nuqtalar: "ec.Ex" (lateral maydon komponenti), physics("ec").selection()
orqali domenni cheklash, "sel_ec" bilan emh1 cheklash.

## 3-vazifa: Model3_Pressure.java - YOZILDI (hali test qilinmagan)

To'liq yozildi: Model1 geometriyasi + Solid Mechanics (SolidMechanics fizikasi, Fixed/Roller/
BoundaryLoad chegaralar), ketma-ket Stationary (1-qadam Solid Mechanics, 2-qadam Electric Currents,
"usesol"/"notsolmethod"/"notstudy" orqali bog'lash), d(p)=t_int*(1+aveop_int(solid.eZZ)),
sig_on(p)=sig_on0*exp(beta*(t_int-d)/t_int). beta_mem hali KALIBRLANMAGAN (R_ON(2GPa)/R_ON(0) =
1e-2...1e-3 maqsadiga qarab qo'lda/avtomatik sozlash kerak bo'ladi - hozircha beta_mem=10 boshlang'ich
taxmin, TEKSHIRILMAGAN).

Hali TEST QILINMAGAN. Kutilgan TEKSHIRILSIN nuqtalar: "SolidMechanics" interfeys nomi, "Fixed"/
"Roller"/"BoundaryLoad" feature nomlari, "LoadType"/"Pressure" xossalari, "solid.eZZ" o'zgaruvchisi,
ikkinchi Stationary qadamni birinchisining yechimidan boshlash mexanizmi ("usesol" va h.k.).

## Keyingi qadam

Model1 jarayoni tugashini kutish (log har 3-5 daqiqada tekshirilmoqda), keyin Model2_FET.java ni
kompilyatsiya+test qilish navbatda.

## Optimallashtirish: bazaviy tsiklni qayta hisoblashdan saqlanish

Birinchi to'liq ishga tushirish (~21 daqiqa, jarayon boshida) 300K, 3-davrli bazaviy tsiklni QAYTA
hisoblab yuborayotganini payqadim - bu natija allaqachon (kechqurungi sessiyadan) saqlangan va
tasdiqlangan edi (V_SET=3.513V, V_RESET=-3.278V). Jarayonni to'xtatib,
`RUN_BASELINE_300K_3CYCLE=false` bayrog'ini qo'shdim (Model1_Vertical.java) - bu std5 (bazaviy
tsikl) yaratilishi/ishga tushirilishini butunlay o'tkazib yuboradi, faqat "ge1" tenglamasi va
"term1.V0" ni Time Dependent uchun kerakli holatga o'rnatadi (bular std6 uchun ham zarur).

## MUHIM TOPILMA: dset/sol raqamlanishi

Qayta ishga tushirilgandan keyin (~21 daqiqa, T_amb=300K nuqtasining o'zi muvaffaqiyatli yechildi),
"Unknown dataset: dset6" xatosi chiqdi. Sabab: dset/sol raqamlanishi STUDY TEGI nomiga (masalan
"std6") EMAS, balki HAQIQIY YARATILGAN yechimlar SONIGA asoslanadi. std5 (bazaviy tsikl)
yaratilmagani uchun std6 aslida 5-YARATILGAN yechim bo'lib, "dset5"/"sol5" nomini oldi. Barcha
N1/N3/N4/N5 kod qismlaridagi "dset6"/"sol6" murojaatlari "dset5"/"sol5" ga tuzatildi. Bu QIMMATLI
METODOLOGIK SABOQ: COMSOL Java API da avtomatik nomlanish RAQAMLI TARTIB (creation order) asosida
ishlaydi, siz bergan TEG NOMI (masalan "std6") bilan hech qanday bog'liqligi yo'q.

Hozircha 3-marta ishga tushirish kerak bo'ldi (birinchisi bazaviy tsiklni behuda qayta hisobladi
~21 daqiqa, ikkinchisi dset6/sol6 xatosi bilan yana ~21 daqiqa behuda ketdi post-processing
bosqichida). Endi tuzatilgan holda qayta ishga tushirilmoqda.

## 1-vazifa YAKUNLANDI: barcha raqamli natijalar va PNG'lar tayyor

To'liq sweep (4 T_amb nuqta, dset5/sol5 tuzatilgan) muvaffaqiyatli yakunlandi (~66 daqiqa,
3979s). N6_table.csv toza monoton tendentsiya bilan:

| T_amb (K) | V_SET (V) | V_RESET (V) | R_OFF/R_ON |
|---|---|---|---|
| 300 | 3.513 | -3.279 | 14.31 |
| 350 | 2.570 | -2.429 | 14.31 |
| 400 | 1.630 | -1.529 | 14.31 |
| 450 | 0.778 | -0.739 | 14.31 |

Kutilgan tendensiya ("T_amb oshgani sari V_SET va |V_RESET| kamayadi") TO'LIQ TASDIQLANDI, aniq
monoton kamayish bilan. R_OFF/R_ON T_amb ga bog'liq emas (modelda kutilganidek - faqat sig_off/
sig_on orqali aniqlanadi, kinetika esa alohida T ga bog'liq).

PNG eksportida ikkita qo'shimcha COMSOL API muammosi topildi va TUZATILDI:
1. Image eksport uchun xossa nomi "pngfilename" (nisbiy yo'l bilan "Failed_to_create_directory"
   berdi) - ABSOLYUT yo'l ("C:/comsol_ish/models/...") kerak ekan.
2. Vaqt-kesimi (SET oldidan/keyin) tanlash uchun alohida "Solution" dataset yaratib "solnum"/
   "looplevel" qo'yish ISHLAMAYDI ("Unknown_property"). To'g'ri yechim: asosiy dataset
   to'g'ridan-to'g'ri ishlatilib, "solnum" (INTEGER) PLOT FEATURE (surf1)ning o'ziga qo'yiladi.

Barcha 11 PNG (N1 x2: chiziqli+log I-V, N3 x3: x(t)/V(t)/I(t), N4 x2: T xaritasi SET paytida +
T_max(t) bilan Tm chiziqlari, N5 x4: |E|/|J| SET dan oldin/keyin) muvaffaqiyatli yaratildi va
tasdiqlandi (T_amb=300K tezkor tekshiruvda). Hozir to'liq 4-nuqtali production yakuniy marta
ishga tushirilmoqda (~66-90 daqiqa kutilmoqda) - CSV va PNG fayllarni bitta izchil to'plamda olish
uchun. Tugagach commit+push qilinadi va 2-vazifaga (Model2_FET.java test) o'tiladi.
