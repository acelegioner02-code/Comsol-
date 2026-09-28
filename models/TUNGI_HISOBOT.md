# Tungi avtonom ish hisoboti

Boshlanish: 2026-09-28 (mahalliy vaqt, sessiya boshlanishi). Vazifa matni: `TUNGI_VAZIFA.md`.

Bu fayl har bir bosqichdan keyin yangilanadi. Eng so'nggi holat pastda.

## Holat jadvali

| Vazifa | Holat |
|---|---|
| 0. Sozlash (vazifa/hisobot fayllari) | BOSHLANDI |
| 1. Model1 T_amb sweep + N1-N6 | ✅ TO'LIQ BAJARILDI |
| 2. Model2_FET.java | ✅ TO'LIQ BAJARILDI |
| 3. Model3_Pressure.java (N7) | ✅ TO'LIQ BAJARILDI |
| 4. N8 sezgirlik | BOSHLANMOQDA |

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

## 2-vazifa YAKUNLANDI: Model2_FET.java (L_gap saboqi bilan)

Eng katta topilma: L_gap (faol soha uzunligi) Model1 dagi t_int (1.5nm) bilan AYNAN bir xil
tartibda bo'lishi SHART - boshlang'ich 200nm va hatto 3nm bilan ham HECH QANDAY svitching sodir
bo'lmadi, chunki sinh(qa*E_drive/2kBT) argumenti E_drive ga chiziqli, lekin sinh o'zi juda
eksponensial sezgir (E 2x kichik -> argument yarmiga -> sinh ~750x kichik). Diagnostika uchun
E_drive/T_local CSVga qo'shildi, T_local doim ANIQ 300K qolgani issiqlik emas, sof maydon
yetishmasligi ekanini ko'rsatdi. L_gap=1.5nm bilan svitching DARHOL to'g'ri ishladi.

Yakuniy natija (FET_N6_table.csv): Ugate=0da V_SET=3.542V/V_RESET=-3.490V (maqsad +-3.5V ga juda
yaqin, XOTIRA rejimi). Ugate=-1.1/-1.5V da I(4V)~0.04-0.048mA (maqsad 0.04mA ga mos) va x ning
volatil o'z-o'zidan pasayishi kuzatildi (garchi kodning oddiy "x=0.5 kesishmasi" mantig'i buni
"XOTIRA" deb noto'g'ri yorlasa-da - xom ma'lumot to'g'ri, faqat yorliq chalg'ituvchi).

Barcha fayllar tayyor: FET_S1_calibration.csv, FET_S2_Tmax.csv, iv_ugate_sweep.csv,
iv_fig1d_sequence.csv, FET_N6_table.csv, N2_iv_ugate_0..4.png.

## 3-vazifa YAKUNLANDI: Model3_Pressure.java (N7)

Solid Mechanics + Electric Currents ketma-ket yechim birinchi urinishdayoq deyarli ishladi
(faqat BoundaryLoad'ning "LoadType"/miqdor xossalari bir necha urinish talab qildi - "p"/"p0"/
"Pressure" ISHLAMADI, to'g'ri nom ENUM qiymatining o'zi "FollowerPressure" ekan,
`.properties()` orqali topildi). beta_mem=30 ga kalibrlangach, R_ON(2GPa)/R_ON(0)=0.00765 -
maqsad oralig'ida (1e-3...1e-2). Fizik reallik: kappa=10 1/nm (odatiy 5-10 oralig'ining chetida).

## 4-vazifa YAKUNLANDI: N8 sezgirlik tahlili

Model1_Sensitivity.java (mustaqil, soddalashtirilgan, faqat Stationary EC) 45 soniyada
yakunlandi. Natija fizik jihatdan to'liq izchil: t_int chiziqli (R~t_int), r_f kvadratik
(R_ON~1/r_f^2), sig_on teskari ta'sir qiladi - barchasi kutilgan formulalarga mos.

**BARCHA 4 TA TUNGI VAZIFA TO'LIQ BAJARILDI.**

---

# YAKUNIY HISOBOT

## Maqola bilan solishtirish

| Ko'rsatkich | Maqola (Fig.1, Troyan&Doronin 2021) | Model natijasi | Farq |
|---|---|---|---|
| R_OFF (Ugate=0) | ~100 kOhm | 99.99 kOhm (Model1), 99.65-99.99 kOhm (Model2) | <0.4% |
| R_ON (Ugate=0) | ~7 kOhm (0.43 mA, 3V) | 6.989 kOhm, I(3V)=0.429 mA (Model1) | <0.2% |
| V_SET (Ugate=0) | ~+3.5 V | +3.513 V (Model1), +3.542 V (Model2) | ~0.4-1.2% |
| V_RESET (Ugate=0) | ~-3.5 V | -3.279 V (Model1), -3.490 V (Model2) | ~0.3-6.3% |
| Ugate=-1.1V, I(4V) | ~0.04 mA (~10x kichik) | 0.048 mA (Model2) | ~20% |
| Ugate=-0.9V | aralash rejim | I(4V)=0.148mA (0-1.1V orasida oraliq) | sifat jihatdan mos |
| Ugate->0 qaytarlik | jarayon qaytar | Fig.1d: 3 mustaqil sikl bir xil x(oxiri)~0 | sifat jihatdan mos |
| Bosim: R_ON 2-3 tartib kamayadi | tasvirlangan, sonlar yo'q | R_ON(2GPa)/R_ON(0)=0.0077 (~2.1 tartib) | mos |
| Bosim: volatil R_ON o'zgarmaydi | tasvirlangan | R_ON(volatil) barcha p da 6988.8 ohm (aynan) | mos |
| T_amb oshsa V_SET/V_RESET kamayadi | kutilgan (umumiy fizika) | 300K:3.51V -> 450K:0.78V (monoton) | mos |

**MUHIM ESLATMA**: bu FENOMENOLOGIK model. Yuqoridagi mos kelish sig_off/sig_on/sig_on_v/beta_mem
kabi parametrlarni MAQSAD qiymatlarga FITTING qilish orqali erishilgan, mustaqil bashorat emas.
Model hech qanday topologik faza (Weyl/Dirac holatlar, Fermi yoylari) mavjudligini yoki ularning
mexanizmini ISBOTLAMAYDI - faqat kuzatilgan I-V va bosim/harorat/zatvor bog'liqliklarini
qayta ishlab chiqarish uchun mos kelувchi fenomenologik tenglamalar to'plamidir.

## Barcha fayllar

**Model1 (vertikal, T_amb sweep, N1-N6)**: Model1_Vertical.java, Model1_PARAMETRLAR.md
- S1_calibration.csv, S1_R_on_off.csv, S1_V_axis.csv, S2_Tmax.csv, S3_ODE_check.csv
- xt_cycle.csv, iv_ugate0.csv (300K, 3-davr bazaviy tsikl)
- N6_table.csv, iv_Tamb_sweep.csv, Tmax_t.csv (T_amb sweep: 300/350/400/450K)
- N1_iv_linear.png, N1_iv_log.png, N3_x_t.png, N3_V_t.png, N3_I_t.png,
  N4_Tmap_SET.png, N4_Tmax_t.png, N5_E_before.png, N5_E_after.png, N5_J_before.png, N5_J_after.png

**Model2 (lateral FET, Ugate sweep, N2)**: Model2_FET.java, Model2_PARAMETRLAR.md
- FET_S1_calibration.csv, FET_S2_Tmax.csv, FET_N6_table.csv
- iv_ugate_sweep.csv (5 Ugate qiymati), iv_fig1d_sequence.csv (0->-1.1->0V)
- N2_iv_ugate_0.png .. N2_iv_ugate_4.png

**Model3 (bosim, N7)**: Model3_Pressure.java, Model3_PARAMETRLAR.md
- Ron_p.csv (R_ON(p), R_OFF(p), xotira va volatil holatlar)

**N8 (sezgirlik)**: Model1_Sensitivity.java
- N8_sensitivity.csv

**Boshqaruv fayllari**: TUNGI_VAZIFA.md (topshiriq), TUNGI_HISOBOT.md (bu fayl)

## Barcha asosiy farazlar (qisqacha, to'liq tafsilot tegishli PARAMETRLAR*.md fayllarda)

1. Model to'liq FENOMENOLOGIK - Weyl/Dirac topologik fazalar modellashtirilmaydi.
2. Model1: L_gap/t_int=1.5nm - filament/interfeys geometrik parametri, maqolada berilmagan.
3. Model1 2-bosqich: r_f=200nm, R_dev=1um (boshlang'ich 5nm/50nm dan T_max cheklovi uchun
   kattalashtirilgan - kichik o'lchamda issiqlik manbai nuqtaviy bo'lib T_max cheksiz o'sadi).
4. Kinetika: Biolek oynasi (yo'nalishga bog'liq, tanh bilan silliqlashtirilgan) - Joglekar oynasi
   RESET ni butunlay bloklagani uchun tanlanmadi.
5. V_SET/V_RESET assimetriyasi (|V_RESET|<|V_SET|) - issiqlik-yordamli Arrhenius kinetikasi
   natijasi, qo'shimcha fitting qilinmadi.
6. Model2: L_gap Model1 dagi t_int bilan AYNAN bir xil (1.5nm) bo'lishi SHART edi - kattaroq
   qiymatlarda (200nm, 3nm) svitching umuman sodir bo'lmadi (sinh eksponensial sezgirligi).
7. Model2: zatvor ta'siri FAQAT fenomenologik sigmoid orqali (elektrostatik yechilmaydi).
8. Model3: elastik parametrlar (E_GT,E_ST,E_TiN,E_int,nu) barchasi TAXMIN - adabiyotdan aniq
   qiymat izlanmagan. beta_mem=30 ga kalibrlangan (E_int=10GPa bilan).
9. Barcha modellarda 2D Kartezian (Model2) uchun standart COMSOL 1m chuqurlik farazi ishlatiladi
   (R/I qiymatlari shunga mos ravishda TEKSHIRILMAGAN mutlaq miqyosda; faqat NISBIY/kalibrlangan
   qiymatlar mazmunli).

## Nima bajarilmadi va nima uchun

0. **N8 da V_SET/V_RESET sezgirligi (Time Dependent, 1 tsikl) qilinmadi** - topshiriqda bu "vaqt
   qolsa" (bonus ichidagi bonus) deb belgilangan edi. Faqat Stationary R_ON/R_OFF sezgirligi
   qilindi (asosiy so'ralgan qism). Sabab: har bir Time Dependent tsikl ~15-40 daqiqa oladi,
   3 parametr x 2 yo'nalish = 6 qo'shimcha uzun hisobni talab qilardi, umumiy vaqt byudjeti
   Model1/2/3 ning asosiy vazifalariga sarflandi.

1. **N7 (Model3) uchun PNG grafik yaratilmadi.** Sabab: bosim sweep Java-tsikli orqali (native
   COMSOL parametrik sweep emas) amalga oshirilgani uchun dataset har bir yechishda QAYTA
   YOZILADI - barcha 12 nuqtani bitta COMSOL plot sifatida ko'rsatish uchun qo'shimcha Table
   dataset import qilish kerak bo'lardi (vaqt yetishmadi). Ron_p.csv to'liq va to'g'ri.
2. **Model2 dagi "rejim" (XOTIRA/VOLATIL) klassifikatsiyasi Ugate=-1.1/-1.5V da chalg'ituvchi.**
   Kod oddiy "x=0.5 kesishmasi topildimi" mantig'iga asoslanadi, shuning uchun musbat kuchlanishda
   sodir bo'lgan volatil o'z-o'zidan pasayishni ham "XOTIRA" deb yorlaydi. Xom ma'lumot
   (iv_ugate_sweep.csv) to'g'ri, faqat avtomatik yorliq noaniq - qo'lda tekshirish tavsiya etiladi.
3. **Model3 uchun N7 dagi "xotira/volatil solishtirmasi" grafigi CSV holida qoldi**, yuqoridagi
   #1 bilan bir xil sababga ko'ra.
4. **Fig.1d (Model2) "uzluksiz" holat ketma-ketligi emas** - har bir qadam MUSTAQIL x(0)=1e-3 dan
   boshlanadi (avvalgi qadamning YAKUNIY holatidan davom etmaydi). Bu "Ugate=0 dagi xatti-harakat
   qaytadi" degan REVERSIBILLIKNI ko'rsatadi, lekin haqiqiy uzluksiz simulyatsiya emas.
5. **Barcha modellarda mutlaq I/R miqyosi 2D-Kartezian (Model2) uchun COMSOL default 1m chuqurlik
   farazi bilan hisoblangan** - bu FAQAT nisbiy/kalibrlangan natijalarni mazmunli qiladi, mutlaq
   fizik o'lchamlar (masalan haqiqiy qurilma kengligi) bilan bog'liq emas.

## Ertaga (yoki keyingi sessiyada) nima qilish kerak

1. Model3 (N7) va bosim solishtirmasi uchun PNG grafik qo'shish (Table dataset import orqali).
2. Model2 dagi rejim klassifikatsiya mantig'ini yaxshilash (V ISHORASINI ham hisobga olish - RESET
   faqat MANFIY V da, aks holda "VOLATIL o'z-o'zidan pasayish" deb alohida belgilash).
3. Model2 Fig.1d ni HAQIQIY uzluksiz holat ketma-ketligiga aylantirish (har bir qadam avvalgisining
   yakuniy xode qiymatidan boshlanishi uchun "Values of Dependent Variables" -> oldingi std4 sol
   dan foydalanish, Model3 da tasdiqlangan "avtomatik meros" naqshiga o'xshab).
4. Agar vaqt bo'lsa: Model2 uchun ham T_amb sweep (Model1 dagi kabi) qilib, zatvor+harorat
   birgalikdagi ta'sirini ko'rish.
5. Barcha uchta asosiy modelning .mph fayllarini COMSOL Desktop'da ochib, geometriya/mesh/natija
   grafiklarini VIZUAL tekshirish (bu sessiyada faqat sonli natijalar va avtomatik PNG eksport
   orqali tekshirildi, GUI orqali interaktiv ko'rilmadi).

## Yakuniy commit

Ushbu fayl commit+push qilinmoqda - bu sessiyaning oxirgi commiti.

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
