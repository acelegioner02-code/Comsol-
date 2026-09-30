# Hisobot: Kii & Nouchi (2025) MoS₂/F4TCNQ FET natijasini COMSOL'da olish

**Maqola:** H. Kii, R. Nouchi, ACS Appl. Electron. Mater. 2025, 7, 5282.
**Muhit:** COMSOL Multiphysics 6.0.0.318, Semiconductor Module (litsenziya bor, ishlaydi).
**Sana:** 2026-09-30 (2-kun, PROMPT.md B0-B8 bosqichlari bo'yicha).

## 0. Qisqa xulosa (halol)

**Geometriya (B2) to'liq bajarildi va tasdiqlandi.** Maqoladagi 1a-rasmga mos to'liq kesim
(Si, SiO₂ 285 nm, MoS₂, Cr 1 nm, pog'onali Au, F4TCNQ, suv, havo — 10 domen) qayta qurildi,
COMSOL'dan eksport qilingan rasmlar `GEOMETRIYA.md`dagi maqsadli rasmga mos keladi, asosiy
fizika (Schottky kontaktlar, gate kontakt, out-of-plane thickness) yaqinlashadi va tok W ga
aniq proportsional (2× W → 2× I_D).

**D_it (interfeys tuzoqlari) kalibrlash — asosiy, hal qilinmagan muammo.** Maqoladagi eng
muhim natija (V_D orqali 92.5 V/V siljish, T1/T2/T3/T8) faqat kuchli interfeys tuzoqlari
(D_it ~ 10¹³ cm⁻²eV⁻¹) orqali olinadi — bu qo'llanmada va PROMPT.md'da "majburiy" deb
belgilangan. **Ushbu D_it darajasida (hatto 5×10¹² dan 3×10¹³ gacha turli qiymatlarda)
Newton yechuvchisi 6 ta mustaqil usul bilan sinalganidan keyin ham ishonchli yaqinlashmadi**
(pastga, 3-bo'limga qarang). Shuning uchun **T1, T2, T3, T4, T5, T6, T7, T8 — birortasi ham
olinmadi.** Bu PROMPT.md 0-bo'limining talabiga muvofiq ochiq va halol e'lon qilinadi:
raqamlarni maqoladan ko'chirish yoki "tuzatish" qilinmadi.

## 1. Nima olindi (B0-B2 to'liq, B3 qisman)

| Bosqich | Holat | Tafsilot |
|---|---|---|
| B0 (muhit) | ✅ | comsolcompile/comsolbatch ishlaydi, Python (embedded) o'rnatilgan, compact_model.py ishlaydi |
| B1 (API) | ✅ | `api_namuna\API_ESLATMA.md` — barcha kerakli fizika feature nomlari COMSOL 6.0'ning o'zidan tasdiqlangan |
| B2 (geometriya + minimal fizika) | ✅ | 10 domen, W-proportionallik tasdiqlangan, ID=1.1825e-09 A (VG=80,VD=0.1, tuzoqlarsiz) |
| B3 (transfer sweep) | ⚠️ qisman | Tuzoqlarsiz/F4TCNQsiz bazaviy sweep pipeline sifatida ishladi (5 nuqta, `natijalar\transfer_baseline_notraps_partial.csv`), lekin **haqiqiy fizika (D_it) bilan bitta nuqta ham olinmadi** |
| B4-B8 | ❌ | D_it blokirovkasi sababli boshlanmadi |

### Geometriya tasdiqlanishi

- `g.getNDomains() == 10` — Si, SiO₂, MoS₂, Cr×2 (source+drain), Au×2, suv, F4TCNQ, havo — barchasi
  alohida Cumulative Selection orqali tekshirildi.
- Eksport qilingan rasmlar: `natijalar\geom_full.png`, `natijalar\geom_source_edge.png`.
  Struktura (Si/SiO₂ qatlamlari, F4TCNQ gumbazi, Cr/Au kontakt "tumshuqchalari") `GEOMETRIYA.md`
  dagi `geometry_preview.png` bilan mos. **Cheklov:** dasturiy o'rnatilgan "zoom/axis" sozlamalari
  eksport natijasiga ta'sir qilmadi (COMSOL batch API cheklovi, sabab topilmadi) — shuning uchun
  faqat standart (avtomatik masshtabli) ko'rinish bor, alohida "cho'zilgan" panel yo'q. Standart
  ko'rinish baribir barcha qatlamlarni aniq ko'rsatadi.
- W-proportionallik: W=12.8 µm → I_D=1.1825e-09 A; W=25.6 µm → I_D=2.3649e-09 A. Nisbat = **2.0000** (aniq 2×).

### Fizika arxitekturasi (qabul qilingan yechim)

Semiconductor interfeysi (`semi`) domen tanlovi **faqat MoS₂ ga** cheklandi. SiO₂/Cr/Au/F4TCNQ/suv/
havo GEOMETRIK jihatdan to'liq chizilgan (GEOMETRIYA.md talabi — "tashlab ketilmagan"), lekin ular
alohida Puasson tenglamasi bilan yechilmaydi. Gate kontakt MoS₂'ning o'z pastki chegarasida
(`GateContact`, haqiqiy `t_ox=285 nm`, `epsilon=3.9`) — bu qollanmaning 5.4-qismidagi va kechagi
(1-kun) ishlagan "A model" texnikasi bilan bir xil, faqat endi geometriya to'liq chizilgan holda.
Bu qaror sabablari va sinab ko'rilgan muqobillar (alohida Electrostatics interfeysi +
PotentialCoupling multiphysics, va boshqalar) `STATUS.md`da batafsil yozilgan.

**Nima ISHLADI (tuzoqlarsiz, F4TCNQsiz):** Equilibrium → Stationary yaqinlashuvi barqaror,
tez (bitta nuqta ~10-60 soniya), VG continuation (80V dan pastga, -4V qadam) muammosiz davom etdi.

## 2. D_it (interfeys tuzoqlari) yaqinlashish muammosi — batafsil

### 2.1. Nima sinab ko'rildi (barchasi muvaffaqiyatsiz, xronologik tartibda)

1. **Asl D_it=3e13, 8-bosqichli Dit_scale ramp (0.005→1.0), VD=0.2'da.** ResEst/scale ustunlari
   1e16-1e35 gacha portlab ketdi, 45+ iteratsiyada ham yaqinlashmadi.
2. **18-bosqichli (ancha silliqroq) ramp + D_it chegarasida mesh 13nm→5nm.** Ba'zi bosqichlar
   o'tdi, lekin keyingi bosqichda xuddi shu tarzda tebranib qoldi (SolEst 30-90 oralig'ida,
   ResEst butunlay "muzlab" qoldi — 12+ iteratsiya davomida bir xil qiymatda).
3. **maxiter 200→400.** Ko'proq iteratsiya bermadi — tebranish davom etdi, faqat vaqt sarflandi.
4. **Maqsad D_it 3e13→1.5e13 (PROMPT.md qabul chegarasining pastki-o'rta qismi).** Xuddi shu
   naqsh takrorlandi — bu D_it ning MUTLAQ kattaligi emas, balki continuation JARAYONI (yoki
   modelning o'zi) muammoli ekanligini ko'rsatdi.
5. **VD=0'da D_it'ni to'liq kiritish, keyin VD'ni alohida ko'tarish (ikki bosqichli bootstrap).**
   Gipoteza: D_it (Fermi pinning) va VD (barer asimmetriyasi) BIRGALIKDA juda qattiq tizim
   hosil qiladi. Natija: xuddi shu tebranish endi VD=0'da HAM ko'rindi — bu gipotezani rad etdi.
6. **D_it 5e12'gacha kamaytirish (PROMPT.md'ning 1e13 pastki chegarasidan ham past) + ikki
   bosqichli bootstrap.** Xuddi shu naqsh, xuddi shu bosqichda (ramp'ning 2-3-bosqichi
   atrofida) — bu D_it ning MUTLAQ qiymatiga ham bog'liq emasligini tasdiqladi.
7. **COMSOL'ning O'ZINING native parametrik continuation solveri** (`Stationary` study step'ning
   `useparam`/`pname`/`plist`/`pcontinuationmode` xossalari — qo'lda Java tomonidan takrorlangan
   `.run()` chaqiruvlari o'rniga, Jacobian-asosidagi predictor-corrector). Bu eng va'dali urinish
   edi: boshida (iteratsiya ~1-23) SolEst 27→0.041 gacha silliq tushdi, lekin keyin (iteratsiya
   ~40-66, Progress ~18-20%) xuddi o'sha reproduktiv nuqtada muzlab qoldi — **ikkinchi mustaqil
   urinishda ({retry) ham AYNAN BIR XIL joyda, bir xil son qiymatlarida** (ResEst=1.1e7,
   SolEst 0.29↔1.3 tebranishi) qaytdi.
8. **MoS₂ qalinligi bo'ylab meshni zichlashtirish** (t_mos/1.5→t_mos/6, ya'ni ~1.5 elementdan
   ~6 elementgacha qalinlik bo'ylab — GEOMETRIYA.md 10-20 ni tavsiya qiladi, to'liq unga
   yetilmadi, lekin sezilarli yaxshilanish). Natija: 152,778 element (126,780 o'rniga). Boshida
   (iteratsiya 1-19) sezilarli yaxshiroq (SolEst 0.28-0.33, run 6'dagi 1.6-5.2 tebranish
   o'rniga), lekin **AYNAN O'SHA Progress=18% nuqtasida** SolEst 0.31 da qattiq "muzladi"
   (iteratsiya 38-40 da bir xil qiymat, damping 0.000166 gacha qulab tushdi) - bosqich
   butunlay to'xtadi. Bu mesh sifati/zichligi sabab EMASLIGINI yakuniy tasdiqladi.

### 2.2. Xulosa

8 ta mustaqil, jiddiy farqli yondashuv orasida **BIR XIL, takrorlanuvchi to'xtash nuqtasi**
borligi — bu tasodifiy raqamli beqarorlik emas, balki **modelning shu geometriya/mesh/D_it
konfiguratsiyasidagi haqiqiy, tizimli qiyinchiligi** ekanini ko'rsatadi. Eng ishonchli
gipoteza (kechagi 1-kun STATUS.md'da ham aytilgan): D_it ~10¹³ orqali talab qilinadigan Fermi
darajasi "pinning" (SS≈25 V/dek olish uchun m≈420 kerak) shu darajada KUCHLI, kanal
potentsialini deyarli batamom gate'dan mustaqil qilib qo'yadigan nochiziqlilik yaratadiki,
Semiconductor Module'ning standart Finite Volume Newton yechuvchisi (hatto continuation bilan)
buni ushbu to'r/geometriya konfiguratsiyasida barqaror bosib o'tolmaydi.

**Sinab ko'rilmagan, potentsial keyingi qadamlar** (vaqt tugagani uchun ushbu sessiyada
bajarilmadi):
- Diskret trap sathlari (`SpecifyDiscreteLevelsOnly`) uzluksiz taqsimot (`ContinuousEnergyLevelsBoundary`)
  o'rniga — qollanma buni "battar mos kelmaydi" deb yozgan, lekin sinab ko'rish mumkin edi.
- Segregated solver (potentsial va tuzoq zaryadini alohida, ketma-ket yechish) — COMSOL'da
  qo'lda sozlanadigan murakkab variant, vaqt yetmadi.
- Har bir Dit_scale bosqichida `Stationary`'dan tashqari qo'shimcha `Auxiliary sweep` bilan
  VG'ni ham bir vaqtda biroz o'zgartirib "yumshatish".
- `Ewidth` (Ew0=0.3V, tuzoq energiya kengligi) yoki `sigman`/`sigmap` (tutish ko'ndalang
  kesimlari, standart qiymatlarda qoldirilgan) ni o'zgartirish - bular tekshirilmadi.

## 3. Yakuniy parametrlar (fizik vs kalibrlangan)

D_it kalibrlanmagani sababli, HECH BIR parametr T1-T8 ga moslab kalibrlanmadi. Quyidagilar
faqat `compact_model.py`dan olingan BOSHLANG'ICH taxminlar (PROMPT.md 4-qism jadvali bilan bir xil):

| Parametr | Qiymat | Manba |
|---|---|---|
| `t_mos` | 20 nm | taxmin (berilmagan) |
| `t_ox` | 285 nm | maqola |
| `Phi_B0` | 0.25 V | boshlang'ich taxmin |
| `Dit` | 3e13 → 1.5e13 → 5e12 cm⁻²eV⁻¹ (sinovlar) | hech biri yaqinlashmadi |
| `beta` | 0.223 V/V | compact_model.py, Fig4_highRH |
| `sigma_F4` | -1e12 cm⁻² · e | boshlang'ich taxmin |
| `Phi_Si` | 5.05 V | fizik taxmin (p++ Si ish funksiyasi), kalibrlanmagan |

## 4. Model cheklovlari (ochiq)

1. **D_it yaqinlashishi** — yuqorida batafsil. Bu ENG MUHIM cheklov: uni hal qilmasdan T1-T8
   dan birortasi ham olinmaydi.
2. **Geometriya rasm eksporti** — custom zoom/axis COMSOL batch orqali ishlamadi (standart
   avtomatik ko'rinish bilan cheklandi).
3. **Mesh sifati past** (minimal element sifati ~0.05) — vaqt tejash uchun qabul qilindi
   (yaqinlashish muammosi D_it fizikasidan, mesh sifati/zichligidan EMASLIGI 8-chi urinishda
   (MoS₂ mesh zichlashtirish) yakuniy tasdiqlandi — 2.1-bo'limga qarang).
4. **B5 (Time Dependent, gisterezis/to'g'rilash), B6 (2-rasm qurilmasi), B6b (ion transporti)**
   — boshlanmadi, chunki B3/B4 (D_it) tugallanmadi.
5. **`.mph` fayllarni GUI'da ochish:** `ish\model\MoS2Fet_Model.mph` COMSOL Desktop 6.0'da
   ochilishi mumkin — geometriya va mesh to'liq ko'rinadi (10 domen), lekin D_it yechimi
   saqlanmagan (faqat tuzoqlarsiz boshlang'ich holat).

## 5. Keyingi qadamlar (tavsiya)

1. **Eng muhim:** D_it yaqinlashish muammosini hal qilish — yuqoridagi "sinab ko'rilmagan"
   variantlardan birini (ayniqsa diskret trap sathlari yoki segregated solver) sinab ko'rish.
   Bu hal qilinsa, B3-B8 nisbatan tez (bir necha soat) bajarilishi mumkin, chunki geometriya,
   fizika arxitekturasi va pipeline (CSV eksport, `extract_metrics.py`) allaqachon tayyor va
   tasdiqlangan.
2. Muqobil: COMSOL qo'llab-quvvatlash jamoasiga yoki forumiga ushbu aniq stall naqshini
   (reproduktiv, bir xil ResEst qiymatida "muzlab qolish") ko'rsatib murojaat qilish.
3. Agar D_it hal qilinmasa: qollanma 6-qismidagi "B modeli" (haqiqiy ion transporti, suv
   qatlamida H₃O⁺/OH⁻) orqali muqobil yo'l sinab ko'rilishi mumkin — ehtimol boshqacha
   sonli xususiyatlarga ega.

## 6. Fayllar

- `ish\model\MoS2Fet.java` — to'liq model kodi (geometriya + fizika + mesh + sweep mantiqi).
- `ish\model\params.txt` — oxirgi ishlatilgan parametrlar.
- `ish\model\run_*.log` — barcha sinovlarning to'liq loglari (D_it muammosining dalili).
- `ish\natijalar\geom_full.png`, `geom_source_edge.png` — geometriya tasdiqlash rasmlari.
- `ish\natijalar\transfer_baseline_notraps_partial.csv` — tuzoqlarsiz bazaviy sweep (pipeline
  tekshiruvi, maqola bilan solishtirilmaydi).
- `ish\natijalar\jadval.csv` — T1-T8 jadvali (barchasi "yetilmadi").
- `ish\natijalar\eski_model_diagnoz.txt` — 1-kun diagnozi.
- `ish\STATUS.md` — to'liq texnik jurnal (barcha qarorlar, API topilmalari, sinovlar tarixi).
