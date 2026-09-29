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
| 2. Bosim natijasi (Fig_pressure.png) | KUTILMOQDA |
| 3. MPH fayllar, YAKUNIY/ papka | KUTILMOQDA |
| 4. NATIJALAR.md (dissertatsiya hisoboti) | KUTILMOQDA |

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
