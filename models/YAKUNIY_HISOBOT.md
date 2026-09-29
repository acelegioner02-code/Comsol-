# YAKUNIY tungi avtonom sessiya - doimiy jurnal

Boshlanish: 2026-09-29. Topshiriq: `YAKUNIY_VAZIFA.md`. Qoidalar o'sha faylda.
Bu jurnal har bosqichdan keyin yangilanadi.

## Holat jadvali

| Bosqich | Holat |
|---|---|
| 0. Sozlash (vazifa fayli, jurnal) | BAJARILDI |
| 1.1. Panel (d) sifat jihatidan tuzatish (tau_rel keskin sigmoid) | BAJARILDI |
| 1.2. Panel (a)/(b) RESET keskinligi (z_eff) | BAJARILDI |
| 1.3. Panel (b) kichik volatil tarmoq | BAJARILMADI (quyida sabab) |
| 1.4-1.5. Ballash, Fig1_analog.png, Fig1_side_by_side.png | BAJARILDI |
| 2. Bosim natijasi (Fig_pressure.png) | BAJARILDI |
| 3. MPH fayllar, YAKUNIY/ papka | BAJARILDI (Troyan_Doronin_All.mph bundan mustasno, sababi bilan) |
| 4. NATIJALAR.md (dissertatsiya hisoboti) | BAJARILDI |

## 1-BOSQICH: Fig.1 yaqinlashtirish - bajarilgan ishlar

### Boshlang'ich holat (oldingi sessiyadan meros, "7-iteratsiya")

Ball: baholanmagan (score_fig1.sh oldin yo'q edi). Muammolar (foydalanuvchi tomonidan
aniqlangan): panel (d) SIFAT jihatidan noto'g'ri (volatil halqa, xotira emas);
panel (a)/(b) da RESET yetarli keskin emas (kenglik ~0.7V, maqsad <0.2-0.3V).

### Ishlatilgan vosita: `score_fig1.sh`

Yangi skript yozildi - `iv_fig1_continuous.csv` ni `fig1_targets.csv` bilan
solishtirib, har bir nishon uchun eng yaqin V dagi modeldan olingan I ni topadi
va |farq|<20% bo'lsa 1 ball beradi. Boshlang'ich (7-iteratsiya) ball: **13/31**.

### A1-A4 iteratsiyalari (jami 4 ta to'liq hisob, +1 qayta tasdiqlash = 5)

**A1 (1.1-band, birinchi urinish)**: `tau_rel_eff` eski formulasi
(`tau_v*(tau_nv/tau_v)^P`, silliq) `tau_rel(P)=tau_v+(tau_nv-tau_v)*S((P-P_c)/w_P)`
(KESKIN sigmoid, P_c=0.17, w_P=0.008) bilan almashtirildi. NATIJA: panel (d) da
ON tarmoq V=0 dan o'tib manfiy V ga davom etdi (SIFAT jihatidan TUZATILDI!), lekin
**panel (c) HAM "buzildi"** - u ham xotira-o'xshash bo'lib qoldi (RESET faqat
-3.6V da, V_h yo'qoldi). Sabab: `tau_nv-tau_v~1000` juda katta bo'lgani uchun
HATTO kichik `S_tau~0.076` (P chegaradan 0.02 past bo'lsa ham) `tau_rel` ni ~76
SONIYAGA oshirib yuborardi - 10ms panel uchun bu CHEKSIZ "muzlash" degani.

**A2 (tuzatish)**: `w_P` 0.008->**0.0015** ga torайтirildi (S_tau endi P chegaradan
0.02 past bo'lganda ~1e-7 - amaliy jihatdan sezilmaydigan qo'shimcha beradi).
NATIJA: panel (c) ASL xatti-harakatiga qaytdi (V_h=0.54V, RESET yo'q chuqur V da),
panel (d) esa XOTIRA xususiyatini SAQLAB QOLDI (RESET endi faqat -3.52V da,
ON tarmoq V=0 dan manfiy V ga davom etadi) - **1.1-band MUVAFFAQIYATLI BAJARILDI**.

**A3 (1.2-band)**: `z_eff` (samarali zaryad ko'paytiruvchisi) kiritildi:
`a_eff = dir_smooth*a_SET + (1-dir_smooth)*(z_eff*a_RESET)`, `z_eff=2.42`,
`a_RESET=0.3nm` (a_SET bilan bir xil), `Ea_RESET=1.4eV` (z_eff uchun analitik
qayta hisoblangan: d(ln rate)/dV=q*z_eff*a_RESET/(2*kB*T*L_gap)~12.5 1/V maqsadga
mos). NATIJA: panel (a) RESET kengligi (x:0.9->0.1) ~0.7V dan **~0.30V ga**
qisqardi (maqsad <0.2-0.3V ga DEYARLI ERISHILDI), V_RESET=-3.65V (maqsad ichida),
panel (b) ham xuddi shunday sharpened. Panel (a) ON tarmoq -3.5V da -0.317mA
(avval -0.135, maqsad -0.5 - yaqinlashdi). Solver barqaror qoldi (Tfail=4,
NLfail=4 - me'yorida).

**A4 (V0_off kalibrlash, RAD ETILDI)**: `V0_off` 0.8->0.95V, `Ea_RESET`
1.4->1.43eV sinaldi. Panel (a) I_OFF(+3.5V) yaxshilandi (56% farq, avval 147%),
LEKIN boshqa OFF nuqtalar (ayniqsa -3.8V, boshqa panellarning OFF qiymatlari)
YOMONLASHDI - sabab: `sig_off_eff` SIMMETRIK `cosh` funksiya, lekin
`fig1_targets.csv` dagi maqsad qiymatlar ASIMMETRIK (+3.5V:0.03mA vs -3.8V:0.04mA,
nisbat kuchlanish nisbatidan farq qiladi) - bitta V0_off BARCHASINI qondira
olmaydi. Umumiy ball 12->10 ga TUSHDI. **QOIDAGA MUVOFIQ: eng yaxshi (A3) ga
QAYTARILDI**, V0_off=0.8V saqlanib qoldi.

### Yakuniy tanlov: A3

- `iv_fig1_continuous.csv`, `Model2_FET_Fig1.java`, `Model2_FET_Fig1.mph` - barchasi
  A3 parametrlari bilan QAYTA HISOBLANIB saqlandi (bir xil natija, floating-point
  darajasidagi arzimas farq bilan tasdiqlandi).
- `Model2_FET_Fig1_best.java`, `iv_fig1_best.csv`, `Fig1_best.png` - A3 zaxira nusxasi.
- Yakuniy ball: **12/31** raqamli mezon bo'yicha (score_fig1.sh chekli - ba'zi
  nuqtalarda bir xil V da IKKI nuqta (o'tish oldidan/keyin) bo'lganda birinchisini
  tanlaydi, bu ba'zan to'liq ON qiymatni emas, o'tish jarayonidagi qiymatni
  ko'rsatadi - masalan panel (d) R_ON haqiqatda ~0.046mA/3.68V, maqsad 0.057mA ga
  ~20% farq, lekin skript "syn" nuqtani tanlab 71% farq ko'rsatgan). QO'LDA
  tekshirilgan asosiy ko'rsatkichlar sifat jihatidan A'LO:
  - Panel (a): V_SET~3.5-3.65V, V_RESET~-3.65V (KESKIN, ~0.30V kenglik), R_ON~7.1kOhm
    (maqsad 7kOhm, ~2%).
  - Panel (b): R_ON~9.7kOhm (maqsad 9kOhm, ~8%), xotira saqlanadi, RESET ham keskinlashdi.
  - Panel (c): SAQLANDI (buzilmadi) - V_h~0.72V (maqsad 0.7V, ~3%), faqat musbat
    qutb asimmetriyasi, ulanish +3.7...+3.96V.
  - Panel (d): **SIFAT jihatidan TUZATILDI** - endi XOTIRA (ON tarmoq V=0 dan
    manfiy V ga davom etadi, RESET faqat -3.52...-3.68V da chuqur manfiyda),
    R_ON (to'liq ON nuqtada, V=3.68 pasayish tomonida)~0.046mA/3.68V=80kOhm
    (maqsad 70kOhm, ~14%).

### 1.3-band: bajarilmadi

Panel (b) dagi kichik volatil sub-tarmoq (2...3.7V, 0.09-0.22mA) - bizning
uzluksiz bitta-holatli (x) kinetikamizda bunday IKKILAMCHI metastabil tarmoqni
tabiiy hosil qilish uchun YANA BIR holat o'zgaruvchisi (masalan ikkinchi, tezroq
relaksatsiyali "yordamchi filament" yoki ikki bosqichli window funksiyasi) kerak
bo'lardi. Vaqt tanqisligi va "ixtiyoriy" deb belgilanganligi sababli bajarilmadi.

### Fayllar

- `score_fig1.sh` - ballash skripti (qayta ishlatiladi).
- `Fig1_analog.png` - yakuniy 2x2 panel (A3 natijasi).
- `Fig1_side_by_side.png` - 4 panel, kattaroq, panjara chiziqlari va katta shrift
  bilan (`plot_fig1_sidebyside.ps1` yangi skript).
- `Model2_FET_Fig1_best.java`, `iv_fig1_best.csv`, `Fig1_best.png` - eng yaxshi
  natija zaxirasi (A3 = joriy asosiy fayllar bilan bir xil).

Davomi pastda.

## 2-BOSQICH: Bosim natijasi

`Ron_p.csv` (Model3_Pressure.java, oldingi sessiyada tayyor) asosida
`Fig_pressure.png` chizildi (`plot_pressure.ps1`, yangi skript) - R_ON(p)/R_OFF(p)
log shkalada, xotira (beta_mem=30) va volatil (beta=0) holatlar solishtirilgan.

**Sonli natija** (0->2 GPa):
- R_OFF: 99993 -> 99993 Ohm (O'ZGARMAYDI, ikkala holatda ham bir xil - kutilgan,
  chunki beta faqat ON filament tunnel o'tkazuvchanligiga ta'sir qiladi).
- R_ON (XOTIRA, beta_mem=30): 6989 -> 53.5 Ohm - **~130x pasayish (~2.1 tartib)**.
  Maqola da'vosi "2-3 tartibga kamayadi" bilan MOS (pastki chegarada).
- R_ON (VOLATIL, beta=0): 6989 -> 6989 Ohm - **AYNAN O'ZGARMAYDI** (beta=0 bo'lgani
  uchun `sig_on_p=sig_on0*exp(0*(...))=sig_on0`, bosimga bog'liqlik formuladan
  matematik jihatdan yo'qoladi). Maqola da'vosi "volatil deyarli o'zgarmaydi"
  bilan A'LO MOS (bizda "deyarli" o'rniga "aynan" - modelning soddaligi tufayli,
  lekin sifat jihatidan bir xil xulosa).

### Parametr mosligi tekshiruvi (Model2 o'zgarishlari Model3 ga ta'sir qilmaydi)

Model3_Pressure.java Model1 (VERTIKAL) geometriyasiga asoslangan, MUSTAQIL
kalibrlangan (`sig_on0=1.594 S/m`, o'z geometriyasida, Model2_FET dan FARQLI
birliklar/kontekst), maqsadlari `R_ON_t=7kOhm`, `R_OFF_t=100kOhm` (Model1/Model3
o'z ichida). Bu YAKUNIY sessiyada Model2_FET_Fig1.java ga kiritilgan o'zgarishlar
(tau_rel keskin sigmoid, z_eff, r_off) FAQAT Model2 fayliga tegishli o'zgaruvchilar
va formulalar - Model3_Pressure.java bularning HECH BIRIGA murojaat qilmaydi
(alohida .java fayl, alohida parametr fazosi). Shuning uchun **Model3 ni qayta
hisoblash SHART EMAS** - vazifada aytilganidek. Yagona umumiy "aloqa nuqtasi":
ikkala model ham TAXMINAN bir xil R_ON~7kOhm/R_OFF~100kOhm maqsad diapazoniga
mo'ljallangan (fenomenologik jihatdan mos kelishi kerak bo'lgan, lekin matematik
jihatdan bog'lanmagan ikkita mustaqil kalibrovka) - bu mosliksiz emas.

Fayl: `Fig_pressure.png`, skript: `plot_pressure.ps1`.

## 3-BOSQICH: MPH fayllar va YAKUNIY/ papka

- **Model2_FET_Fig1.mph** (16.6MB) - `pg_VI` (V(t),I(t)*1000 - panel d) va `pg_xP`
  (xode(t),P(t) - panel d) plot grouplar qo'shildi, `.run()` qilindi, qayta
  hisoblanib saqlandi (natijalar aynan bir xil, A3 bilan mos - tekshirildi).
- **Model3_Pressure.mph** (17.4MB) - `pg_stress` (Von Mises kuchlanish xaritasi,
  oxirgi holat) va `pg_dgap` (d_gap qiymati) plot grouplar qo'shildi, `.run()`
  qilindi, 24 ta bosim/holat kombinatsiyasi qayta hisoblanib saqlandi (173s,
  natijalar `Ron_p.csv` bilan AYNAN bir xil - reproduktivlik tasdiqlandi).
- **Model1_Vertical.mph** (743MB) - allaqachon `pg_V`, `pg_J`, `pg_Vz`, `pg_N3_*`,
  `pg_N4_*`, `pg_N5_*` kabi ko'p sonli plot grouplarga ega (oldingi sessiyalarda
  yaratilgan), lekin ularga aniq `.run()` chaqiruvi QO'SHILMAGAN (faqat
  `export().run()` PNG hosil qilish uchun chaqirilgan - bu alohida narsa).
  **QAROR: Model1 TO'LIQ QAYTA HISOBLANMADI** - sababi: (1) fayl hajmi 743MB,
  **YAKUNIY/ papkasiga 50MB chegarasi tufayli BARIBIR NUSXALANMAYDI**, shuning
  uchun GUI-qulayligi bu FAYL UCHUN past ustuvorlik; (2) to'liq qayta hisoblash
  (N1-N8 barcha bosqichlar) taxminan bir necha soat talab qilardi - bu YAKUNIY
  9 soatlik sessiyaning qolgan barcha bosqichlariga (4-BOSQICH hisoboti va h.k.)
  ajratilgan vaqtni yeb qo'yardi. Model1_Vertical.mph diskda saqlanib qoladi
  (models/ papkasida, YAKUNIY/ da EMAS), barcha natijalar CSV/PNG fayllar orqali
  allaqachon mavjud.
- **Troyan_Doronin_All.mph (birlashtirilgan, 3 komponent)**: **BAJARILMADI**.
  Sabab (oldingi sessiyada batafsil tahlil qilingan, o'zgarmagan): barcha 3
  modelni bitta faylga birlashtirish xotira jihatidan ENG YOMON variant (8GB
  RAM'li mashinada barcha komponentlar uchun mesh/xotira bir vaqtda band
  bo'ladi, hatto faqat bittasi yechilayotganda ham) - OOM xavfi yuqori, vaqt esa
  cheklangan. Qanday qilish mumkinligi `HISOBOT_FIG1.md` da avval yozilgan
  (bosqichma-bosqich yo'riqnoma, hali ham amal qiladi).

### YAKUNIY/ papka tarkibi

`Fig1_analog.png`, `Fig1_side_by_side.png`, `Fig_pressure.png`,
`iv_fig1_continuous.csv` (=iv_fig1_best.csv), `Ron_p.csv`,
`Model2_FET_Fig1.java` (=Model2_FET_Fig1_best.java, ular bir xil - A3 yakuniy),
`Model3_Pressure.java`, `Model1_Vertical.java`, `Model1_Sensitivity.java`,
`Model2_FET_Fig1.mph`, `Model3_Pressure.mph`.
