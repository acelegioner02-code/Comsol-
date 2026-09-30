HOLAT: YAKUNLANDI
Oxirgi yangilanish: 2026-10-01 03:45
Bosqichlar: B0 [x] B1 [x] B2 [x] B3 [~] B4 [ ] B5 [ ] B6 [ ] B6b [ ] B7 [x] B8 [x]

## B3 (3-kun): to'liq transfer sweep - qisman natija (halol)

Yagona ishonchli konfiguratsiya (tuzoqlarsiz, F4TCNQ yoqilgan - D_it yaqinlashmagani uchun,
yuqoriga qarang) bilan VD=0.2V uchun VG=80 dan VG=0 gacha (21 nuqta, -4V qadam) muvaffaqiyatli
hisoblandi - `model\transfer_b3_vd02_partial.csv`. VG=-4 va undan pastda (chuqur subporog,
juda past tok zichligi) yaqinlashish avval sekinlashdi (bitta nuqta VG=-8'da 246 iteratsiya,
~40 daqiqa ishlab, haqiqiy yaqinlashishga erishmadi - ResEst 1.4e10 da "muzlab qoldi", SolEst
kichik bo'lsada). Vaqt tejash uchun shu yerda to'xtatildi.

**Olingan natija (VD=0.2V, VG=80..0):** Tok 4.13e-06 A dan 1.82e-07 A gacha monoton kamaydi
(22.7x, 80V oralig'ida). Bu 1 nA (maqoladagi V_on mezoni) ga YETMAYDI - shuning uchun rasmiy
V_on ni interpolyatsiya qilib bo'lmaydi (T1, T2 ham shu sababli olinmadi).

Hisoblangan qiyaliklar (halol, chegaralar bilan):
- Uchidan-uchigacha (VG=80->0): 59.0 V/dek
- Mahalliy, eng tik qism (VG=12->0): **14.5 V/dek**

Qiziq kuzatuv: mahalliy qiyalik (14.5 V/dek) maqolaning 2-rasm (F4TCNQ, past namlik proxy)
qiymatiga (~12 V/dek, T8) SURPRIZINGLY yaqin - garchi bu D_it'SIZ olingan bo'lsa ham! Buni
ehtiyotkorlik bilan talqin qilish kerak: bu D_it orqali kelgan "toza" subporog qiyaligi emas,
balki **kontakt bareri + kanal birgalikda cheklagan** tok egri chizig'ining mahalliy qiyaligi
(1-tekshiruvda ko'rsatilganidek, D_it'siz holatda kontakt tokni sezilarli cheklaydi). D_it
qo'shilganda bu qiymat yanada o'zgarishi kutiladi (qay tomonga - kattalashishi (yomonlashishi)
ham, kichiklashishi ham mumkin, chunki ikkita mexanizm - kontakt cheklovi va D_it - bir xil
yo'nalishda ishlamasligi mumkin), lekin buni sonli tekshirib bo'lmadi (D_it yaqinlashmagani
uchun).

**Boshqa VD qiymatlari (0.4, 0.6, 0.8, 1.0) va to'liq VG=-80 gacha sweep BAJARILMADI** - vaqt
byudjeti tugagani va chuqur subporogdagi yaqinlashish qiyinligi sababli. B4 (kalibrlash),
B5 (Time Dependent/gisterezis), B6 (2-rasm) ham shu sababdan boshlanmadi.

## Yakuniy holat va keyingi qadam (3-kun)

Ushbu sessiyada 2 ta tasdiqlangan, muhim xato tuzatildi (Bug 1: Schottky bareri ulanishi,
Bug 2: D_it fizika formulasi) va YANA bitta yangi, PROMPT.md'da oldindan aytilmagan muammo
aniqlandi va qisman hal qilindi (solver'ning erta to'xtash sozlamalari - reserrfact,
initstep). Ammo **D_it hali ham to'liq ishlamaydi** (yangi, chuqurroq sabab - formula
tabiatan raqamli beqaror bo'lishi mumkin) va **chuqur subporog** (past tok) o'zi ham alohida
qiyinchilik ekanligi aniqlandi. T1-T8 dan birortasi ham to'liq olinmadi, lekin B2 (geometriya)
va asosiy fizika (Bug 1/2 tuzatilgandan keyin) ishonchli ishlaydi, va B3 dan HAQIQIY (qisman)
sonli natija bor. Bu HISOBOT.md'da to'liq, halol yozilgan.

## 3-kun: bulutdagi tekshiruvda topilgan ikki xato tuzatildi

Bulutda ishlagan Claude PROMPT.md ga "6b. 3-kun" bo'limini qo'shdi (`git pull` bilan olindi,
commit bc6c747). U 2-kundagi D_it yaqinlashmaslik va juda kichik tokning ikkita ANIQ sababini
topdi - ikkalasi ham `MoS2Fet.java` da, D_it ning o'zi "tabiatan qattiq" emas edi.

### Xato 1 tuzatildi: Schottky bareri modelga ulanmagan edi

`MetalContact`da `SpecifyBarrierHeight="ideal"` rejimida COMSOL barerni **`Phi_B` emas, balki
metall chiqish ishi `Phi`dan** hisoblar ekan (Phi_B = Phi - chi_mos), `Phi_B` xossasi shu
rejimda butunlay e'tiborsiz qoldiriladi. `Phi` hech qachon berilmagani uchun standart qiymat
(~4.5V) ishlatilgan - `Phi_B0` va ion holati `x` amalda hech qachon barer orqali modelga
ta'sir qilmagan edi. Tuzatildi: endi `mc1.set("Phi","chi_mos+Phi_B0-x")`,
`mc2.set("Phi","chi_mos+Phi_B0+x")`.

### Xato 2 tuzatildi: tor-band diskret trap modeli almashtirildi

`TrapAssistedSurfaceRecombination`/`ContinuousEnergyLevelsBoundary` (faqat donor, midgap
atrofida 0.3eV tor to'rtburchak, bizning Nss namunadagidan 150x katta) OLIB TASHLANDI. O'rniga:
oddiy `SurfaceChargeDensity`, Fermi sathiga chiziqli bog'liq zaryad:
`rhoqs = -e_const*Dit*Dit_scale*(semi.Efn - (semi.Ec+semi.Ev)/2 - dE0)`.
`semi.Efn`/`semi.Ec`/`semi.Ev` nomlari `test\EfProbe.java` bilan alohida tasdiqlandi (oddiy
MoS2 to'rtburchak + Equilibrium + EvalPoint): Ec=Eg/2, Ev=-Eg/2 (V birligida, `e_const`ga
bo'lish shart emas - boshqa nomlar, masalan `semi.V`, `semi.EFn`, `semi.Emid`, ishlamadi).
Yangi kalibrlanadigan parametr `dE0` (neytrallik sathi siljishi, sukut 0V) qo'shildi.
Dit_scale ramp PROMPT.md tavsiyasiga ko'ra soddalashtirildi: {0, 0.1, 0.3, 1.0} (avvalgi
18-bosqichli/native-continuation murakkabligi endi shart emas).

**Muhim oqibat:** gisterezis endi FAQAT ionlardan (Global ODE, x) keladi, tuzoqlardan emas -
bu to'g'ri fizika (PROMPT.md shunday deydi), hisobotda alohida ta'kidlanadi.

### Tekshiruv 1: barerga sezgirlik (NATIJA: Bug 1 tasdiqlandi, tuzatildi)

Tuzoqlarsiz/F4TCNQsiz, VG=80V, VD=0.1V, uch xil Phi_B0:

| Phi_B0 | I_D | nisbat oldingisiga | kutilgan (~50x/0.1V, agar kontakt-cheklangan) |
|---|---|---|---|
| 0.15 V | 3.7321e-06 A | - | - |
| 0.25 V | 1.8144e-06 A | 2.06x pasaydi | ~50x |
| 0.35 V | 2.0377e-07 A | 8.91x pasaydi | ~50x |

**Xulosa:** Bareр endi HAQIQATDA ta'sir qiladi (2-kunda butunlay ta'sirsiz edi - bu Bug 1
tasdiqlanishi). Sezgirlik to'liq termoemissiya chegarasiga (~50x) hali yetmaydi, lekin
Phi_B0 ortishi bilan kuchayib bormoqda (2.06x -> 8.91x) - bu PROMPT.md 3-tekshiruvda aytilgan
"past barerda kanal cheklaydi, baland barerda kontakt cheklay boshlaydi" stsenariysiga mos.
To'liq 3-tekshiruv (D_it bilan, x orqali) pastda davom etadi.

### Tekshiruv 2: SS = 0.06(1+q*Dit/Cox) (NATIJA: qisman - tuzoqlarsiz baza, D_it=3e13 davom etmoqda)

Tuzoqlarsiz/F4TCNQsiz, VD=0.1, VG=80'dan pastga (-4V qadam) sweep qilindi. `Boshlang'ich nuqta`
qiymati (1.814425e-06 A, VG=80) 1-tekshiruvdagi Phi_B0=0.25 natijasiga (1.8144e-06 A) mos -
ikkala test ham izchil (avval ikkalasi orasida 9x farq ko'ringan edi, lekin bu FAQAT
`transfer.csv` ni process CSV yozish bosqichiga yetmasdan turib o'qib qo'yganim sababli edi -
fayl oldingi urinishning eski qoldig'ini ko'rsatgan, haqiqiy xato emas).

VG=80 dan VG=40 gacha (11 nuqta) tok FAQAT 1.814e-6 dan 1.284e-6 gacha (29% pasaydi) - bu
hali TO'LIQ subporog rejimi emas (kutilgan SS=0.06 V/dek, tuzoqlarsiz holatda, juda tik bo'lishi
kerak edi, lekin biz ko'rgan sekin pasayish shuni ko'rsatadiki, **bu VG oralig'ida tok
kontakt bareri bilan CHEKLANGAN, kanal bilan emas** - PROMPT.md 3-tekshiruvning aynan shu
stsenariysini oldindan aytgan edi ("V_on ni kontakt emas, kanal cheklasa..." aksi - bu yerda
aksincha, kontakt cheklayotganga o'xshaydi). Vaqt tejash uchun to'liq -80V gacha sweep
to'xtatildi (qisman natija: `model\transfer_ss_notraps_partial.csv`, 11 nuqta). D_it=3e13
bilan xuddi shu sweep navbatda - agar u SEZILARLI KESKINROQ pasaysa (SS taxminan 24 V/dek),
bu D_it ning o'zi to'g'ri ishlayotganini tasdiqlaydi, garchi tuzoqlarsiz holat "toza" subporog
ko'rsatkichini bermagan bo'lsa ham (kontakt cheklovi tufayli).

**D_it=3e13 sweep NATIJASI: yaqinlashmadi.** Quyida batafsil - bu Xato 2 tuzatilgandan keyin
ham hal bo'lmagan, YANGI (PROMPT.md 6b da yo'q) ildiz sababga ega muammo.

### Qo'shimcha topilma: D_it yaqinlashmasligining YANGI ildiz sababi (solver darajasida)

Bug 2 tuzatilgandan keyin D_it=3e13 sweep (2-tekshiruv uchun) sinalganda, yaqinlashish HALI HAM
muvaffaqiyatsiz bo'ldi - lekin Bug 2'dan OLDINGI (eski, tor-band trap) muammosidan FARQLI, yangi
sabab aniqlandi. `test\SolverProbe.java`/`SolverProbe2.java` bilan solver daraxti tekshirildi:

- **Kashfiyot:** Newton yechuvchisi (`FullyCoupled` tuguni, `sol1.s1.fc1`) ba'zi urinishlarda
  atigi **6 iteratsiyada** to'xtagan - `maxiter` standart qiymati (50) ham hali yetib
  bormagan! Sabab: `reserrfact` (standart 1000) - agar navbatdagi qoldiq oldingi eng
  yaxshisidan 1000 martadan ko'proq yomonroq bo'lsa, solver "divergensiya" deb hisoblab,
  ERTA TO'XTAYDI. Bizning yangi rhoqs ifodamiz (Fermi sathiga TO'YINMAYDIGAN chiziqli bog'liq
  zaryad) katta oraliq qadamlarida ResEst'ni bir necha ming marta oshirib yuborishi mumkin.
- **1-tuzatish:** `reserrfact` 1000 dan 1e8 gacha oshirildi (`bumpMaxIterFeature`da). Bu
  ba'zi bosqichlarni (0.1, 0.3, 0.5 keyin 0.1/0.3 alohida testda) o'tkazishga yordam berdi,
  lekin baribir ba'zi bosqichlarda SolEst BITTA iteratsiyada 1e17-1e26 gacha portlab ketdi -
  bu reserrfact (ResEst nisbati) emas, balki Newton QADAMINING O'ZI haddan tashqari katta
  bo'lgani sabab edi.
- **2-tuzatish:** `initstep` (boshlang'ich damping) 0.1 dan 0.01 gacha kamaytirildi -
  ehtiyotkorroq birinchi qadamlar. Bu ayrim bosqichlarni (Dit_scale=0, ba'zan 0.1-0.3) juda
  toza yaqinlashtirdi (SolEst 1e-13 darajasigacha!).
- **3-tuzatish:** bumplar RETRY'da emas, Dit_scale=0 (trivial, doim yaqinlashadigan) nuqtadan
  KEYIN, birinchi nolmas bosqichdan OLDIN proaktiv qo'llanildi (vaqt tejash: har bosqichda
  keraksiz "birinchi urinish muvaffaqiyatsiz" siklini aylanib o'tish). Ramp yanada
  silliqlashtirildi: {0, 0.02, 0.05, 0.1, 0.15, 0.22, 0.32, 0.46, 0.65, 0.85, 1.0}.
- **NATIJA (barcha uch tuzatish bilan birga):** Dit_scale=0.02 (ENG KICHIK nolmas qadam,
  D_it=6e11 cm^-2eV^-1 ga teng - PROMPT.md'ning 1e13 pastki chegarasidan HAM 16 marta kichik)
  HALI HAM ikkala urinishda ham (birinchi + retry, barcha bumplar bilan) muvaffaqiyatsiz
  bo'ldi. Bu **ramp qadam kattaligi yoki solver murosasozligi muammosi EMASLIGINI** ko'rsatadi
  - muammo `rhoqs = -e_const*Dit*Dit_scale*(semi.Efn - (semi.Ec+semi.Ev)/2 - dE0)`
  ifodasining o'zida, ehtimol uning TO'YINMAYDIGAN (chiziqli, chegaralanmagan) tabiatida:
  real tuzoqlar sonli holatlarga ega bo'lib to'yinadi, lekin bu chiziqli formula Efn har
  qancha siljisa ham mutanosib ravishda o'sadigan zaryad beradi - bu MoS2 yupqa qatlamida
  (t_mos=20nm, kam DOF) potentsial va zaryad orasida kuchli, o'z-o'ziga bog'liq musbat
  fikr-aloqa (positive feedback) hosil qilib, Newton uchun barqarorsiz bo'lishi mumkin.

**Xulosa:** Bug 2'ning o'zi (fizika formulasi, PROMPT.md'ning aynan ko'rsatgan `Q_it = -q*D_it*
(E_F-E_0)` ifodasi) TO'G'RI joriy qilindi va `semi.Efn`/`semi.Ec`/`semi.Ev` nomlari mustaqil
tasdiqlandi (`test\EfProbe.java`). Lekin bu formula ushbu geometriya/mesh/kontakt
konfiguratsiyasida COMSOL'ning standart damped-Newton yechuvchisi uchun RAQAMLI JIHATDAN
BARQAROR emas - hatto eng kichik (D_it 1e13 dan 16x kichik) qiymatda ham. Bu PROMPT.md 6b
bo'limida oldindan aytilmagan, YANGI, chuqurroq muammo. Vaqt tejash uchun bu yerda
to'xtatiladi; keyingi sessiya uchun tavsiya: (a) formulaga yumshoq TO'YINISH qo'shish
(masalan `tanh`), yoki (b) segregated/damped-update yondashuvi (rhoqs'ni har iteratsiyada
oldingi Efn'dan HISOBLAB, alohida qadam sifatida, to'g'ridan-to'g'ri implicit emas).

### Tekshiruv 2 - YAKUNIY HOLAT: to'liq D_it bilan bajarilmadi (halol)

SS = 0.06*(1+q*Dit/Cox) formulasi D_it=3e13 bilan **tekshirib bo'lmadi** - yuqoridagi sabab
bilan hech qanday nolmas D_it qiymati yaqinlashmadi. Tuzoqlarsiz baza (SS o'rniga, VG=80..40
oralig'ida kontakt-cheklangan xatti-harakat) qisman natija sifatida saqlanadi. **Bu T8 va
D_it'ga bog'liq barcha boshqa T1-T8 ko'rsatkichlari (T1,T2,T3,T6) uchun ham amal qiladi.**

### Tekshiruv 3: V_on barerga bog'liqmi? (ADAPTATSIYA QILINGAN - D_it'siz)

PROMPT.md'ning so'zma-so'z talabi ("D_it bilan, VD=0.5 da x=0 vs x=0.05") D_it yaqinlashmagani
uchun bajarilmadi. O'rniga, 1-tekshiruvning natijalari (Phi_B0 orqali barerni to'g'ridan-to'g'ri
o'zgartirish, D_it'siz) aynan shu savolga (V_on/tok barerga bog'liqmi?) allaqachon javob
beradi: **HA, lekin qisman** - Phi_B0 0.15->0.25->0.35 o'zgarganda tok 2.06x va 8.91x pasaydi
(to'liq termoemissiya ~50x dan kam). PROMPT.md'ning o'z bashorati ("past barerda kanal
cheklaydi") bilan mos: D_it yo'qligida kanal (Nd_mos=1e17, "normally-on") barcha VG=80..40
oralig'ida kuchli o'tkazuvchan bo'lib qoladi, shuning uchun kontakt bareri current'ni
TO'LIQ emas, QISMAN cheklaydi. D_it (m-faktor) yoqilgan holatda (agar yaqinlashganida)
kontakt-cheklov ta'siri KUCHAYISHI kutilardi (kanal o'zi susayganda kontakt nisbiy rolini
oshiradi) - lekin buni sonli tasdiqlash D_it yaqinlashmagani uchun iloji bo'lmadi.
**beta_COMSOL hisoblanmadi** (buning uchun D_it bilan ΔV_on/Δx kerak edi).

## Qisqa xulosa (to'liq tafsilot HISOBOT.md da)

**B2 (geometriya + asosiy fizika) to'liq muvaffaqiyatli.** To'liq 10-domenli geometriya
(GEOMETRIYA.md bo'yicha: Si, SiO2 285nm, MoS2, Cr 1nm, pog'onali Au, F4TCNQ, suv, havo)
qayta qurildi, tasdiqlandi (rasmlar, domen soni, W-proportsionallik aniq 2.0000x).

**B3-B8 (D_it kalibrlash orqali T1-T8) muvaffaqiyatsiz — D_it Newton yaqinlashishi.**
**8 ta mustaqil usul** sinaldi (turli continuation strategiyalari, mesh zichligi, D_it
qiymatlari, native COMSOL continuation solver) — barchasi BIR XIL, reproduktiv nuqtada
"muzlab qoldi" (batafsil HISOBOT.md, 2-bo'lim). Bu D_it ~1e13 cm^-2eV^-1 darajasidagi Fermi
pinning'ning ushbu geometriya/mesh konfiguratsiyasida COMSOL Semiconductor Module standart
Newton yechuvchisi uchun tabiatan qattiq (stiff) ekanligini ko'rsatadi.

**T1-T8: BIRORTASI OLINMADI.** `natijalar\jadval.csv` da hammasi "yetilmadi" deb belgilangan,
sabab bilan. Bu halol - hech qanday raqam soxtalashtirilmagan yoki maqoladan ko'chirilmagan.

## 1-kun diagnozi (qisqa, to'liq versiya natijalar\eski_model_diagnoz.txt da)

Kechagi geometriya faqat MoS2 (1 domen, Polygon) edi - qollanma 5-qismidagi soddalashtirilgan
"A model", GEOMETRIYA.md talabiga mos emas edi. 2-kun boshida aniqlandi va to'liq qayta qurildi.

## 2-kun: bajarilgan ishlar xronologiyasi

1. 1-kun .mph fayllari introspeksiya qilindi (`test\DiagOld.java`) - diagnoz tasdiqlandi.
2. Geometriya GEOMETRIYA.md bo'yicha TO'LIQ qayta qurildi (`model\MoS2Fet.java`) - 10 domen,
   yangi COMSOL geometriya API'lari (Rectangle/Polygon/Ellipse/Mirror/Union/Intersection/
   Difference/CumulativeSelection) o'rganildi va tasdiqlandi (muhim xato: `keepadd`/
   `keepsubtract` noto'g'ri ishlatilib 10 o'rniga 5 domen chiqqan edi - tuzatildi).
3. Mesh: boshlang'ich urinish 632,425 element berib "low on memory" bilan o'chirildi (7.8GB
   mashina) - domen-bo'yicha aniq hmax/hmin bilan ~111K-153K elementga tushirildi.
4. Fizika arxitekturasi: SiO2/F4TCNQ/suv/havo uchun "semi" ICHIDA sun'iy ultra-keng-bandgap
   SemiconductorMaterialModel (izolyator taqlidi) ishlatilganda Newton beqarorligi chiqdi -
   arxitektura soddalashtirildi: "semi" faqat MoS2 ga cheklandi, GateContact MoS2'ning o'z
   chegarasida (haqiqiy t_ox=285nm) - kechagi ishlagan "A model" texnikasi, endi to'liq
   geometriya atrofida. Bu ARXITEKTURA ISHLADI: tuzoqlarsiz/F4TCNQsiz smoke-test yaqinlashdi
   (ID=1.1825e-09 A, VG=80,VD=0.1), W-proportsionallik tasdiqlandi (2.0000x).
5. D_it (tuzoqlar) yoqilganda 8 ta mustaqil yondashuv sinaldi - barchasi muvaffaqiyatsiz
   (batafsil HISOBOT.md 2.1-bo'lim): turli Dit_scale ramp granularity (8->18 bosqich), mesh
   zichligi (D_it chegarasida 13nm->5nm, MoS2 qalinligida 1.5->6 element), maxiter (200->400),
   maqsad D_it (3e13->1.5e13->5e12), VD=0 bootstrap ajratish, COMSOL native parametrik
   continuation solver (`useparam`/`pname`/`plist`/`pcontinuationmode`). HAMMASI bir xil
   reproduktiv nuqtada (ResEst muzlab qoladi, SolEst tebranadi/qotadi) to'xtadi - bu D_it
   fizikasining o'zi ushbu modelda tabiiy qattiq (stiff) ekanligini ko'rsatadi, mesh yoki
   solver sozlamalari emas.
6. Tuzoqlarsiz bazaviy transfer sweep (pipeline tekshiruvi uchun, 5 nuqta) - `natijalar\
   transfer_baseline_notraps_partial.csv`. Maqola bilan solishtirilmaydi (D_it yo'q).
7. `HISOBOT.md` yozildi (to'liq, halol, barcha 8 urinish tafsiloti bilan).
8. B8: natijalar repo'ga nusxalandi, git commit va push muvaffaqiyatli bajarildi
   (commit a5a872d, birinchi push urinishi HTTP 408 bilan vaqt tugadi, ikkinchi urinish o'tdi).

## COMSOL Java API topilmalari (kelajak sessiyalar uchun, API_ESLATMA.md ga qo'shimcha)

- Geometriya: Rectangle/Polygon/Ellipse/Mirror/Union/Intersection/Difference barchasi ishladi.
  **Difference**: `keepadd` = input (asl/kesiluvchi obyekt)ni saqlaydi, `keepsubtract` = input2
  (ayiriluvchi asboblar)ni saqlaydi - bularni almashtirib yuborish domenlarni yo'qotadi.
- `CumulativeSelection` + `contributeto` - domen guruhlarini kuzatish uchun ishonchli usul.
- Component-level `Union`/`Difference` selection: `(SelectionFeature)` cast SHART, keyin
  `entitydim` majburiy, Union="input", Difference="add"/"subtract" (String[]).
- Semiconductor standart domen feature'lari (`smm1`, `ins1`) `"Selection_is_not_editable"`
  bilan qulflangan - ularga tegmasdan, YANGI (semi.create bilan) feature yaratib "override"
  qilish kerak.
- `"Insulation"` feature CHEGARA (1D) darajasida, domen (2D) EMAS (tunnel-mos parametrlar -
  DGexteriorBC, Phi_nOx/Phi_pOx, meOx/mhOx - buni tasdiqlaydi).
- Image export: `sourcetype="geometry"`, `sourceobject="geom1"`, `view="view1"` (standart
  view qayta ishlatiladi - yangi View'da `.axis()` NPE beradi), `imagetype="png"`,
  `pngfilename`. `axis().set("autocontext",...)` HECH QANDAY qiymatni qabul qilmaydi (sinab
  ko'rilgan barcha variantlar xato) - shuning uchun custom zoom ishlamadi, standart avtomatik
  masshtab bilan qoldirildi.
- `comsolbatch` `main(String[] args)`ga argument BERMAYDI - `args` NULL (bo'sh massiv emas).
- Study continuation: `Stationary` step'ning `useparam`(bool)/`pname`(String[])/`plist`
  (String[], bitta element - bo'shliq bilan ajratilgan qiymatlar ro'yxati)/
  `pcontinuationmode`("last") - COMSOL'ning o'z native parametrik continuation solveri.
  Ishlaydi (property darajasida), lekin D_it muammosini hal qilmadi.
- Embedded Python (pytools\py311): cwd/script-directory sys.path'da YO'Q (embeddable
  distributsiya standart xatti-harakati) - `compact_model.py`ni import qiladigan skriptlar
  (masalan `extract_metrics.py`) uchun `sys.path.insert(0, ...)` workaround kerak.

## Muhim qarorlar va sabablari

- Kechagi fayllarni hech birini o'chirmadim.
- D_it uchun 1+ soatlik intensiv sinovdan keyin (8 usul) muvaffaqiyatsizlikni qabul qilib,
  vaqtni B7/B8 (halol hisobot, repo'ga saqlash) ga yo'naltirdim - PROMPT.md "Vaqt qoidasi"
  bo'limi buni to'g'ridan-to'g'ri tavsiya qiladi ("taxminan 6 soatdan keyin yangi kalibrlashni
  boshlama: B7-B8 ga o't").
- `params.txt` oxirgi holatida D_it=3e13 (asl maqsad) qoldirildi - kelajak sessiya uchun
  boshlang'ich nuqta sifatida foydali (5-chi urinishda past qiymat ham yordam bermaganini
  bilamiz, shuning uchun asl maqsadga qaytarildi).

## Hal qilinmagan muammolar / eng muhim keyingi qadam

**D_it Newton yaqinlashishi** - HISOBOT.md 5-bo'limidagi tavsiyalarga qarang (diskret trap
sathlari, segregated solver, yoki COMSOL qo'llab-quvvatlashga murojaat). Bu hal qilinsa,
qolgan hamma narsa (geometriya, fizika arxitekturasi, pipeline) tayyor va tasdiqlangan -
B3-B8 nisbatan tez bajarilishi kutiladi.
