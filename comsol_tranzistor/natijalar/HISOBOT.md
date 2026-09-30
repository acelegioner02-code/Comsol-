# Hisobot: Kii & Nouchi (2025) MoS₂/F4TCNQ FET natijasini COMSOL'da olish

**Maqola:** H. Kii, R. Nouchi, ACS Appl. Electron. Mater. 2025, 7, 5282.
**Muhit:** COMSOL Multiphysics 6.0.0.318, Semiconductor Module (litsenziya bor, ishlaydi).
**Sana:** 2026-09-30/10-01 (2-kun va 3-kun, PROMPT.md B0-B8 va 6b-qismi bo'yicha).

## 0. Qisqa xulosa (halol)

**Geometriya (B2) to'liq bajarildi va tasdiqlandi.** Maqoladagi 1a-rasmga mos to'liq kesim
(Si, SiO₂ 285 nm, MoS₂, Cr 1 nm, pog'onali Au, F4TCNQ, suv, havo — 10 domen) qurildi,
COMSOL'dan eksport qilingan rasmlar `GEOMETRIYA.md`dagi maqsadli rasmga mos keladi, asosiy
fizika (Schottky kontaktlar, gate kontakt, out-of-plane thickness) yaqinlashadi va tok W ga
aniq proportsional (2× W → 2× I_D).

**3-kun: bulutdagi tekshiruv ikkita ANIQ dasturiy xatoni topdi, ikkalasi ham tuzatildi:**
1. `MetalContact` "ideal" rejimida COMSOL barerni `Phi_B` emas, balki metall chiqish ishi
   `Phi`dan hisoblar ekan — `Phi` hech qachon berilmagani uchun `Phi_B0` va ion holati `x`
   amalda modelga ULANMAGAN edi. **Tuzatildi va tasdiqlandi** (pastga, 1-tekshiruv).
2. Interfeys tuzoqlari tor-band diskret trap modeli (`ContinuousEnergyLevelsBoundary`) fizik
   jihatdan ham, sonli jihatdan ham noto'g'ri edi. **Tuzatildi**: oddiy `SurfaceChargeDensity`,
   Fermi sathiga chiziqli bog'liq zaryad (`rhoqs = -e_const*Dit*Dit_scale*(semi.Efn -
   (semi.Ec+semi.Ev)/2 - dE0)`), COMSOL'dan tasdiqlangan o'zgaruvchi nomlari bilan.

**Ammo D_it (interfeys tuzoqlari) HALI HAM yaqinlashmaydi — YANGI, chuqurroq sabab bilan.**
Bug 2 tuzatilgandan keyin ham, HATTO ENG KICHIK sinalgan D_it qiymatida (Dit_scale=0.02,
ya'ni D_it=6×10¹¹ cm⁻²eV⁻¹ — PROMPT.md'ning 10¹³ pastki chegarasidan 16 marta kichik!)
Newton yechuvchisi yaqinlashmadi — bu safar solver sozlamalari (reserrfact, initstep, maxiter)
ham ancha oshirilgan/yumshatilgan holda. Demak, muammo endi ramp qadam kattaligi yoki solver
murosasozligi emas, balki yangi `rhoqs` ifodasining o'zi (Fermi sathiga CHEGARALANMAGAN
chiziqli bog'liqlik) ushbu model konfiguratsiyasida son jihatdan beqaror. Batafsil 3-bo'limda.

**Natija: T1, T2, T3, T4, T5, T6, T7, T8 — birortasi ham TO'LIQ olinmadi**, chunki
barchasi bevosita yoki bilvosita D_it'ga bog'liq. Biroq, ikki bug tuzatilgandan keyin
model AVVALGIDAN ancha yaxshi ishlaydi: barer endi haqiqatda ta'sir qiladi (1-tekshiruv),
va B3 transfer sweep (D_it'siz, F4TCNQ bilan) 21 ta haqiqiy nuqta berdi, tok 22.7× kamaydi
(VG=80→0, VD=0.2V) — bu 2-kundagi "deyarli o'zgarmas tok" holatidan katta farq. Bu
PROMPT.md 0-bo'limining talabiga muvofiq ochiq va halol e'lon qilinadi: raqamlarni
maqoladan ko'chirish yoki "tuzatish" qilinmadi.

## 1. Nima olindi

| Bosqich | Holat | Tafsilot |
|---|---|---|
| B0 (muhit) | ✅ | comsolcompile/comsolbatch ishlaydi, Python (embedded) o'rnatilgan |
| B1 (API) | ✅ | `api_namuna\API_ESLATMA.md` — barcha kerakli fizika feature nomlari COMSOL 6.0'ning o'zidan tasdiqlangan |
| B2 (geometriya + asosiy fizika) | ✅ | 10 domen, W-proportsionallik 2.0000x, geometriya rasmlari tasdiqlangan |
| 3-kun Bug 1 (Schottky bareri) | ✅ tuzatildi va tasdiqlandi | Pastga, 1-tekshiruv |
| 3-kun Bug 2 (D_it fizika formulasi) | ✅ to'g'ri joriy qilindi, lekin yaqinlashmaydi | Pastga, 2-bo'lim |
| B3 (transfer sweep) | ⚠️ qisman | VD=0.2V, VG=80→0 (21 nuqta), D_it'siz+F4TCNQ bilan — `model\transfer_b3_vd02_partial.csv` |
| B4-B8 (kalibrlash, gisterezis, 2-rasm) | ❌ | D_it yaqinlashmagani uchun boshlanmadi |

## 2. 3-kun: ikkita bug tuzatilishi (bulutdagi tekshiruv topilmasi)

### Bug 1: Schottky bareri modelga ulanmagan edi

`MetalContact`da `SpecifyBarrierHeight="ideal"` rejimida COMSOL barerni Phi_B = Phi − χ
formulasi bilan, **metall chiqish ishi `Phi`dan** hisoblar ekan — `Phi_B` xossasi shu
rejimda butunlay e'tiborsiz qoldiriladi (dalil: `api_namuna\dump_schottky_contact.txt`).
`Phi` hech qachon berilmagani uchun standart qiymat ishlatilgan, `Phi_B0` va ion holati `x`
hech qachon haqiqiy barerga ta'sir qilmagan edi.

**Tuzatish:**
```java
mc1.set("Phi", "chi_mos + Phi_B0 - x");   // source
mc2.set("Phi", "chi_mos + Phi_B0 + x");   // drain
```

### Bug 2: tor-band diskret trap modeli almashtirildi

`TrapAssistedSurfaceRecombination`/`ContinuousEnergyLevelsBoundary` (faqat donor, midgap
atrofida 0.3eV tor to'rtburchak) OLIB TASHLANDI. O'rniga: `SurfaceChargeDensity`, Fermi
sathiga chiziqli bog'liq zaryad:
```java
sfit.set("rhoqs", "-e_const*Dit*Dit_scale*(semi.Efn - (semi.Ec+semi.Ev)/2 - dE0)");
```
`semi.Efn`/`semi.Ec`/`semi.Ev` nomlari `test\EfProbe.java` bilan MUSTAQIL tasdiqlandi
(oddiy MoS2 to'rtburchak + Equilibrium + nuqtaviy baholash): natija Ec=Eg/2, Ev=−Eg/2,
V birligida (`e_const`ga bo'lish shart emas). Boshqa nomlar (`semi.V`, `semi.EFn`,
`semi.Emid`, `semi.phin`) COMSOL 6.0'da MAVJUD EMAS. Yangi kalibrlanadigan parametr `dE0`
(neytrallik sathi siljishi, sukut 0V) qo'shildi. **Oqibat:** gisterezis endi FAQAT
ionlardan (Global ODE, `x`) keladi, tuzoqlardan emas — bu to'g'ri fizika (PROMPT.md
shunday deydi).

## 3. Tekshiruvlar natijalari

### 3.1. Tekshiruv 1: barerga sezgirlik — Bug 1 TASDIQLANDI va TUZATILDI

Tuzoqlarsiz/F4TCNQsiz, VG=80V, VD=0.1V, uch xil `Phi_B0`:

| Phi_B0 | I_D | nisbat oldingisiga | kutilgan (~50×/0.1V, agar to'liq kontakt-cheklangan) |
|---|---|---|---|
| 0.15 V | 3.7321×10⁻⁶ A | — | — |
| 0.25 V | 1.8144×10⁻⁶ A | 2.06× pasaydi | ~50× |
| 0.35 V | 2.0377×10⁻⁷ A | 8.91× pasaydi | ~50× |

Barer endi HAQIQATDA ta'sir qiladi (2-kunda butunlay ta'sirsiz edi). Sezgirlik to'liq
termoemissiya chegarasiga hali yetmaydi, lekin Phi_B0 ortishi bilan kuchayib bormoqda —
PROMPT.md 3-tekshiruvda aytilgan "past barerda kanal cheklaydi" stsenariysiga mos.

### 3.2. Tekshiruv 2: SS = 0.06(1+q·D_it/C_ox) — BAJARILMADI (D_it yaqinlashmadi)

Tuzoqlarsiz baza (VD=0.1, VG=80→40, 11 nuqta, `model\transfer_ss_notraps_partial.csv`):
tok faqat 1.814×10⁻⁶ dan 1.284×10⁻⁶ gacha (29% pasaydi) — kontakt-cheklangan rejim, haqiqiy
subporog emas. **D_it=3×10¹³ bilan solishtirish sinalgan (pastga, 3.3-bo'lim), lekin hech
qanday nolmas D_it qiymati yaqinlashmagani uchun formal SS o'lchash iloji bo'lmadi.**

### 3.3. D_it yaqinlashmasligining chuqurroq tekshiruvi (3-kun, qo'shimcha)

Bug 2'dan keyin D_it=3×10¹³ sweep qayta sinalganda, **6 ta qo'shimcha, mustaqil solver-darajasidagi
tuzatish sinaldi**:

1. **Kashfiyot (`test\SolverProbe.java`/`SolverProbe2.java` bilan):** Newton yechuvchisi
   (`FullyCoupled`, `sol1.s1.fc1`) ayrim urinishlarda atigi 6 iteratsiyada to'xtagan —
   standart `maxiter`=50 hali yetib bormagan. Sabab: `reserrfact`=1000 — qoldiq oldingi eng
   yaxshisidan 1000× yomonroq bo'lsa, solver "divergensiya" deb to'xtaydi.
2. `reserrfact` 1000→1e8 ga oshirildi. Ba'zi bosqichlarni o'tkazdi, lekin ba'zilarida
   SolEst bitta iteratsiyada 1e17–1e26 gacha portlab ketdi (qadamning o'zi haddan katta).
3. `initstep` (boshlang'ich damping) 0.1→0.01 ga kamaytirildi — ehtiyotkorroq qadamlar.
4. Bumplar RETRY o'rniga Dit_scale=0 dan KEYIN, birinchi nolmas bosqichdan OLDIN proaktiv
   qo'llanildi; ramp yanada silliqlashtirildi: {0, 0.02, 0.05, 0.1, 0.15, 0.22, 0.32, 0.46,
   0.65, 0.85, 1.0}.
5. **YAKUNIY NATIJA:** hatto Dit_scale=0.02 (D_it=6×10¹¹, maqsad 3×10¹³ dan 50×, PROMPT.md'ning
   1×10¹³ pastki chegarasidan 16× kichik) HAM ikkala urinishda (birinchi + barcha bumplar
   bilan retry) muvaffaqiyatsiz bo'ldi.

**Xulosa:** bu endi ramp qadam kattaligi yoki solver murosasozligi masalasi emas — muammo
`rhoqs` ifodasining o'zida, ehtimol uning TO'YINMAYDIGAN (chiziqli, chegaralanmagan)
tabiatida: real tuzoqlar chekli sonli holatlarga ega bo'lib to'yinadi, lekin bu formula
Efn qancha siljisa ham mutanosib zaryad beradi — bu yupqa MoS2 qatlamida potentsial va
zaryad orasida kuchli, o'z-o'ziga bog'liq musbat fikr-aloqa hosil qilib, standart damped-
Newton yechuvchisi uchun barqarorsiz bo'lishi mumkin. Bu PROMPT.md 6b bo'limida oldindan
aytilmagan, yangi topilma.

### 3.4. Tekshiruv 3: V_on barerga bog'liqmi? — ADAPTATSIYA QILINGAN (D_it'siz)

So'zma-so'z talab (D_it bilan, VD=0.5, x=0 vs x=0.05) D_it yaqinlashmagani uchun bajarilmadi.
1-tekshiruvning natijalari (Phi_B0 orqali to'g'ridan-to'g'ri barer o'zgarishi, D_it'siz)
savolga qisman javob beradi: HA, tok barerga bog'liq (2.06×–8.91×), lekin to'liq
termoemissiya darajasida emas — D_it yo'qligida kanal (Nd_mos=1e17, "normally-on") keng
VG oralig'ida kuchli o'tkazuvchan qoladi, kontakt bareri tokni QISMAN cheklaydi.
`beta_COMSOL` hisoblanmadi (D_it bilan ΔV_on/Δx kerak edi, bu mavjud emas).

## 4. B3: transfer sweep — qisman, haqiqiy natija

Yagona ishonchli konfiguratsiya (D_it'siz, F4TCNQ yoqilgan) bilan **VD=0.2V, VG=80→0
(21 nuqta, -4V qadam)** muvaffaqiyatli hisoblandi (`model\transfer_b3_vd02_partial.csv`).
VG=-4V va pastda (chuqur subporog) yaqinlashish avval sekinlashdi, keyin bitta nuqta
(VG=-8V) 246 iteratsiya / ~40 daqiqa ishlab ham yaqinlashmadi (ResEst 1.4×10¹⁰ da "muzlab
qoldi") — vaqt tejash uchun shu yerda to'xtatildi.

**Olingan qiymatlar:** I_D 4.130×10⁻⁶ A (VG=80) dan 1.819×10⁻⁷ A (VG=0) gacha monoton
kamaydi — 22.7× o'zgarish 80V oralig'ida. Bu 1 nA (maqoladagi V_on mezoni)ga YETMAYDI,
shuning uchun rasmiy V_on interpolyatsiya qilinmadi.

Hisoblangan qiyaliklar (COMSOL'dan haqiqiy, halol):
- Uchidan-uchigacha (VG=80→0): **59.0 V/dek**
- Mahalliy, eng tik qism (VG=12→0): **14.5 V/dek**

Qiziq (lekin ehtiyotkor talqin talab qiladigan) kuzatuv: mahalliy qiyalik (14.5 V/dek)
maqolaning 2-rasm qiymatiga (~12 V/dek, T8) yaqin — D_it'SIZ olingan bo'lsada. Bu D_it
orqali kelgan "toza" subporog qiyaligi EMAS, balki kontakt bareri + kanal birgalikda
cheklagan egri chiziqning mahalliy qiyaligi (3.1-tekshiruv shuni ko'rsatganidek, D_it'siz
holatda kontakt tokni sezilarli cheklaydi). D_it qo'shilsa bu qiymat o'zgarishi kutiladi,
lekin buni sonli tekshirib bo'lmadi.

**Boshqa VD qiymatlari (0.4, 0.6, 0.8, 1.0) va to'liq VG=-80 gacha sweep BAJARILMADI** —
vaqt byudjeti va chuqur subporogdagi qo'shimcha yaqinlashish qiyinligi sababli.

## 5. T1-T8 jadvali

`natijalar\jadval.csv` ga qarang — barcha T1-T8 "yetilmadi" deb belgilangan, sabab bilan
(D_it yaqinlashmadi → V_D orqali siljish, SS, to'g'rilash, gisterezis — barchasi D_it'ga
bog'liq). B3'dagi qisman natija (22.7× tok o'zgarishi, mahalliy 14.5 V/dek) rasmiy T1/T2/T8
o'rnini bosmaydi, lekin modelning to'g'ri yo'nalishda ishlashini ko'rsatadi.

## 6. Model cheklovlari (ochiq, halol)

1. **D_it yaqinlashishi — ENG MUHIM, hal qilinmagan cheklov.** Fizika formulasi to'g'ri
   (PROMPT.md'ning o'zi bergan), lekin COMSOL'ning standart Newton yechuvchisi uchun
   ushbu konfiguratsiyada raqamli beqaror. Bu holda T1-T8'dan birortasi ham olinmaydi.
2. **Chuqur subporog (past tok, D_it'dan mustaqil ham) qiyin.** VG=-8V va pastda hatto
   D_it'siz ham yaqinlashish 40+ daqiqa cho'zilib, muvaffaqiyatsiz bo'ldi.
3. Geometriya rasm eksporti — custom zoom/axis COMSOL batch orqali ishlamadi.
4. Mesh sifati o'rtacha (minimal element sifati ~0.05-0.23, sinovdan-sinovga farq qiladi).
5. B4 (kalibrlash), B5 (Time Dependent/gisterezis), B6 (2-rasm), B6b (ion transporti) —
   boshlanmadi.
6. **`.mph` fayllarni GUI'da ochish:** `ish\model\MoS2Fet_Model.mph` COMSOL Desktop 6.0'da
   ochilishi mumkin — to'liq geometriya va mesh ko'rinadi, D_it yechimi saqlanmagan.

## 7. Keyingi qadamlar (tavsiya, ustuvorlik bo'yicha)

1. **D_it uchun to'yinuvchi (saturating) formula sinash** — masalan `tanh`-asosida
   chegaralangan `rhoqs`, real trap zichligi kabi to'yinadigan. Bu joriy chiziqli
   (chegaralanmagan) formuladan ko'ra barqarorroq bo'lishi mumkin.
2. **Segregated/damped-update yondashuvi:** `rhoqs`'ni implicit (to'g'ridan-to'g'ri `semi.Efn`
   ga bog'liq) emas, balki har iteratsiyada OLDINGI yechimdan hisoblab, alohida "lag"li
   qadam sifatida qo'yish (COMSOL'da qo'lda sozlanadigan murakkab variant).
3. Diskret trap sathlari (`SpecifyDiscreteLevelsOnly`) — qollanma "battar mos kelmaydi"
   deb yozgan, lekin endi ChiziqliEfn formulasi ham ishlamagani uchun qayta ko'rib
   chiqarli.
4. Chuqur subporog uchun alohida, maxsus mesh/solver sozlamalari (masalan carrier
   statistics formulasini "Fermi-Dirac" ga o'zgartirish yoki qo'shimcha stabilizatsiya).
5. Agar D_it hal qilinsa: geometriya, fizika arxitekturasi va pipeline (CSV eksport,
   `extract_metrics.py`) allaqachon tayyor va tasdiqlangan — B3-B8 nisbatan tez
   bajarilishi mumkin.

## 8. Fayllar

- `ish\model\MoS2Fet.java` — to'liq model kodi (2 bug tuzatilgan, solver bumplari bilan).
- `ish\model\params.txt` — oxirgi ishlatilgan parametrlar (B3 konfiguratsiyasi).
- `ish\model\run_d3_*.log` — 3-kun barcha sinovlarining to'liq loglari.
- `ish\model\transfer_b3_vd02_partial.csv` — B3 qisman natija (21 nuqta, VD=0.2V).
- `ish\model\transfer_ss_notraps_partial.csv` — 2-tekshiruv bazaviy egri chizig'i.
- `ish\test\EfProbe.java`, `SolverProbe.java`, `SolverProbe2.java` — 3-kun API/solver
  tekshiruv dasturlari.
- `ish\natijalar\jadval.csv` — T1-T8 jadvali (barchasi "yetilmadi", sabab bilan).
- `ish\natijalar\geom_full.png`, `geom_source_edge.png` — geometriya tasdiqlash rasmlari.
- `ish\STATUS.md` — to'liq texnik jurnal (barcha qarorlar, 2-kun va 3-kun).
