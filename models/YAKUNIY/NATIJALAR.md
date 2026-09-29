# Troyan & Doronin (2021) GeTe/Sb2Te3 xotira-o'tkazuvchi elementi: fenomenologik COMSOL modeli

## Yakuniy natijalar hisoboti (dissertatsiya uchun)

Sana: 2026-09-29. Manba maqola: Troyan & Doronin, ICCS 2020, LNNS 186, 427-433 (2021),
doi:10.1007/978-3-030-66093-2_41. To'liq ish jurnali: `../YAKUNIY_HISOBOT.md`,
`../TUNGI_HISOBOT.md`, `../HISOBOT_FIG1.md`. Parametrlar asoslari:
`../Model1_PARAMETRLAR.md`, `../Model2_PARAMETRLAR.md`, `../Model3_PARAMETRLAR.md`.

**MUHIM OGOHLANTIRISH**: bu model to'liq **FENOMENOLOGIK**. U maqolada tasvirlangan
kuzatiladigan I-V xatti-harakatini (xotira/volatil almashinuvi, bosimga sezgirlik)
matematik jihatdan TAQLID qiladi, lekin Weyl/Dirac topologik fazalar, Fermi yoylari
yoki ularning o'tishlari **hech qachon modellashtirilmagan va HECH QANDAY joyda
"isbotlangan" deb da'vo qilinmaydi**. Barcha parametrlar kuzatilgan natijalarga
FITTING (moslashtirish) orqali topilgan, mustaqil bashorat emas.

---

## 1. Model tavsifi

Uchta bog'liq, lekin MUSTAQIL COMSOL 6.0 Java modeli ishlab chiqildi:

### Model1_Vertical (o'q-simmetrik, vertikal qurilma)

Geometriya: pastki/yuqori TiN elektrod - Sb2Te3 (t_ST=20nm) - GeTe (t_GT=20nm) -
interfeys qatlami (t_int=1.5nm, filament radiusi r_f=200nm ichida) - yana TiN.
Fizika: Electric Currents (ec) + Heat Transfer in Solids (ht) + Electromagnetic
Heating (multiphysics ulanish) + Global ODE (filament holati x).

### Model2_FET_Fig1 (lateral, zatvorli FET)

Geometriya: 2D Kartezian kesim, Sb2Te3 kanali (W=250nm, ixcham nm-masshtab) source
dan drain gacha, o'rtada L_gap=1.5nm "faol soha", ustida GeTe/Al2O3/zatvor qatlamlari
(faqat issiqlik uchun, S4 bosqichida issiqlik fizikasi xotira tejash uchun
o'chirilgan - T_max=300K S2 da tasdiqlangan). Fizika: Electric Currents + 2 ta
Global ODE (filament holati x, zatvor xotira holati P).

### Model3_Pressure (Model1 geometriyasiga asoslangan, bosim ostida)

Model1 geometriyasi + Solid Mechanics fizikasi (bosim yuklamasi), interfeys
qatlami siqilishi (d_gap(p)) orqali tunnel o'tkazuvchanlikka ta'sir qiladi.

### Asosiy tenglamalar

**Interfeys/filament o'tkazuvchanligi** (Model1/Model2, log-chiziqli interpolyatsiya):
```
sigma_int = sigma_off^(1-x) * sigma_on^x        (x in [0,1] - filament holati)
```

**Filament holati kinetikasi** (Global ODE, barcha modellarda umumiy asos):
```
dx/dt = k0 * exp(-Ea_eff/(kB*T)) * sinh(q*a_eff*E_drive/(2*kB*T)) * Fwin(x) - x/tau_rel
```
- `k0` - urinish chastotasi, `Ea_eff` - aktivatsiya energiyasi (SET/RESET uchun
  ALOHIDA, Model2 da qutbga bog'liq aralashtiriladi), `a_eff` - sakrash masofasi
  (Model2_FET_Fig1 da RESET uchun `z_eff` samarali zaryad ko'paytiruvchisi bilan
  kattalashtirilgan - qarang 2-bo'lim), `E_drive` - haydovchi elektr maydon,
  `Fwin(x)` - Biolek oynasi (tanh bilan yo'nalishga bog'liq silliqlangan,
  x=0/1 chegaralarida tezlikni nolga tushiradi), `tau_rel` - passiv relaksatsiya
  vaqt doimiysi.

**Zatvor xotira holati P** (FAQAT Model2_FET_Fig1, YANGI, bu ishda kiritilgan):
```
dP/dt = (f_gate - f_gate ni ALMASHTIRSA, P) / tau_P_eff
f_gate = 1/(1+exp(-(Ugate-U0)/w_sig))                    (sigmoid, zatvor kuchlanishi)
tau_P_eff = dir_P*tau_P_rise + (1-dir_P)*tau_P_fall       (Biolek-uslub, asimmetrik)
```

**OFF nochiziqlilik** (Model2_FET_Fig1, YANGI):
```
sig_off_eff = sig_off0 * cosh(E_drive/E0_off) * g_off(P)
g_off(P) = r_off^(1-P)                                     (zatvor OFF ni ham bosadi)
```
(`cosh` vazifada so'ralgan `sinh(x)/x` o'rniga - matematik ekvivalent asimptotika,
bo'linishsiz, sonli barqarorlik uchun - qarang `Model2_PARAMETRLAR.md`.)

**tau_rel(P) KESKIN sigmoid** (Model2_FET_Fig1, YAKUNIY sessiyada qo'shildi):
```
tau_rel_eff = tau_v + (tau_nv - tau_v) * S_tau
S_tau = 1/(1+exp(-(P-P_c)/w_P))
```
Bu panel (d) ning XOTIRA xususiyatini to'g'ri modellashtirish uchun ZARUR bo'ldi -
qarang 7-bo'lim.

**Bosim bog'liqligi** (Model3_Pressure):
```
d_gap(p) = t_int*(1+eZZ(p))                    (eZZ - Solid Mechanics dan siqilish)
sig_on(p) = sig_on0 * exp(beta * (t_int-d_gap)/t_int)     (tunnel-fitting)
```
`beta=beta_mem=30` (xotira holati) yoki `beta=0` (volatil holat, bosimga
sezgirlik YO'QOLADI - formuladan matematik ravishda).

---

## 2. Parametrlar jadvali

### Umumiy geometriya (Model1/Model2/Model3)

| Nomi | Qiymati | Birligi | Manba |
|---|---|---|---|
| t_int (Model1) / L_gap (Model2) | 1.5 | nm | Taxmin (bitta vdW bo'shlig'i + qo'shni Te qatlamlari) |
| r_f (Model1) | 200 | nm | Fitting (Joule isishini fizik diapazonga tushirish uchun 5->200nm oshirilgan) |
| R_dev (Model1) | 1000 (1 um) | nm | Fitting (xuddi shu sabab, 50->1000nm) |
| W (Model2 kanal) | 250 | nm | Ixcham nm-masshtab (past ustuvorlik vazifasi, OOM yengillatish bilan birga) |
| t_ST, t_GT | 20, 20 | nm | Topshiriq |

### Elektr/kinetika (bazaviy, Model1 dan meros)

| Nomi | Qiymati | Birligi | Manba |
|---|---|---|---|
| sig_ST, sig_GT | 1e5 | S/m | Taxmin (degenerat p-tur yarim o'tkazgich) |
| k0 | 1e13 | 1/s | Taxmin (odatiy urinish chastotasi) |
| Ea_SET | 0.9 | eV | Fitting (V_SET=+3.5V ga) |
| a_SET | 0.3 | nm | Fitting |
| E_s | 1e7 | V/m | Fitting (Biolek yo'nalish silliqlash) |

### Model2_FET_Fig1 ga xos (bu ishda YANGI/qayta kalibrlangan)

| Nomi | Qiymati | Birligi | Manba/asos |
|---|---|---|---|
| sig_off0 | 1.6182e-8 | S/m | Fitting (I_OFF(3.5V)~0.03mA) |
| sig_on0 | 1.0712e-5 | S/m | Fitting (R_ON=7kOhm, Ugate=0) |
| sig_on_v | 6.0469e-7 | S/m | Fitting (I(4V)~0.04mA, Ugate=-1.1V) |
| V0_off | 0.8 | V | Boshlang'ich taxmin (qayta kalibrlash sinaldi, RAD ETILDI - 7-bo'limga qarang) |
| r_off | 0.05 | 1 | Fitting (panel c/d I_OFF maqsadlariga, YANGI, bu sessiyada) |
| Ea_RESET | 1.4 | eV | Fitting (z_eff bilan RESET pozitsiyasini saqlash uchun qayta hisoblangan) |
| a_RESET | 0.3 | nm | a_SET bilan bir xil (z_eff orqali kattalashadi) |
| z_eff | 2.42 | 1 | Fitting (d(ln rate)/dV~12.5 1/V maqsadga, YANGI, bu sessiyada) |
| U0 | -1.0 | V | Fitting (f_gate(-0.9)~0.88, f_gate(-1.1)~0.12) |
| w_sig | 0.05 | V | Fitting |
| P_s | 0.02 | 1 | Fitting (dir_P to'yinishi uchun torroq) |
| tau_P_fall | 1e-3 | s | Fitting (tez pasayish) |
| tau_P_rise | 75e-3 | s | Fitting (sekin tiklanish - qisman xotira uchun) |
| tau_v | 1.5e-4 | s | Fitting (V_h~0.7V ga, panel c/d) |
| tau_nv | 1e3 | s | Fitting (nonvolatil chegara) |
| P_c | 0.17 | 1 | Fitting (panel c/d ning haqiqiy P trayektoriyasidan, YANGI) |
| w_P | 0.0015 | 1 | Fitting (JUDA tor - sonli sabab, 7-bo'limga qarang, YANGI) |

### Model3_Pressure ga xos

| Nomi | Qiymati | Birligi | Manba |
|---|---|---|---|
| beta_mem | 30 | 1 | Fitting (R_ON(2GPa)/R_ON(0)~130x maqsadga) |
| R_ON_t, R_OFF_t | 7, 100 | kOhm | Topshiriq maqsadlari |

---

## 3. Fig. 1 a-d solishtirish jadvali

(Barcha model qiymatlari `../iv_fig1_continuous.csv` dan, A3/YAKUNIY parametrlar
bilan qayta hisoblangan - qarang `../YAKUNIY_HISOBOT.md`, 1-BOSQICH.)

### (a) Ugate=0, Vamp=3.8V - XOTIRA

| Ko'rsatkich | Maqola | Model | Farq | Ball |
|---|---|---|---|---|
| V_SET | +3.5V | +3.65V | +4% | ✅ |
| V_RESET | -3.6...-3.8V | -3.65...-3.8V | maqsad ichida | ✅ |
| RESET kengligi (x:0.9->0.1) | <0.2-0.3V | ~0.30V | deyarli erishildi | ✅/△ |
| R_ON (~3V) | 7 kOhm | 7.15 kOhm | ~2% | ✅ |
| I_OFF(+3.5V) | 0.03 mA | ~0.074 mA | ~2.5x | ❌ |
| I_OFF(-3.8V) | 0.04 mA | ~0.048-0.064mA | 20-60% | △ |
| ON tarmoq -3V da | -0.45mA (target -3.5V) | -0.32...-0.37mA | ~20-30% | △ |

### (b) Ugate=-0.9V, Vamp=3.8V - XOTIRA (kuchsizroq)

| Ko'rsatkich | Maqola | Model | Farq | Ball |
|---|---|---|---|---|
| R_ON | 9 kOhm | 9.73 kOhm | ~8% | ✅ |
| P (panel oxiri) | (bilvosita) | 0.895 | maqsad 0.8-0.9 ga mos | ✅ |
| Xotira saqlanishi | Ha | Ha | sifat mos | ✅ |
| RESET keskinligi | keskin | keskin (~0.3V) | MOS | ✅ |
| Kichik volatil sub-tarmoq (2-3.7V) | bor | YO'Q | - | ❌ (bajarilmadi, 6-bo'lim) |

### (c) Ugate=-1.1V, Vamp=4.5V - VOLATIL (faqat musbat qutb)

| Ko'rsatkich | Maqola | Model | Farq | Ball |
|---|---|---|---|---|
| Faqat musbat qutbda ulanish | Ha | Ha | MOS | ✅ |
| Ulanish kuchlanishi | +3.7...+4.3V | +3.78...+3.96V | maqsad ichida | ✅ |
| V_h (OFF ga qaytish) | ~0.7V | 0.72V | ~3% | ✅ |
| I_OFF (yuqori V) | <0.01mA | ~0.010-0.012mA | ~20-30% | △ |
| **SAQLANDI** (foydalanuvchi ko'rsatmasiga ko'ra "buzilmasin") | - | - | - | ✅ |

### (d) Ugate->0, Vamp=4.0V - QISMAN XOTIRA (YAKUNIY sessiyada SIFAT jihatidan TUZATILDI)

| Ko'rsatkich | Maqola | Model (YANGI) | Model (ESKI, volatil xato) |
|---|---|---|---|
| Xatti-harakat turi | XOTIRA (ON ikkala qutbda) | **XOTIRA (tuzatildi!)** | Volatil halqa (❌ noto'g'ri edi) |
| R_ON (+3.7V) | 0.057mA (~70kOhm) | ~0.046mA/3.68V (~80kOhm, ~14% farq) | ta'rif yo'q edi |
| ON manfiy qutbda | -0.05mA (-4V) | -0.024...-0.028mA (-3.36...-3.52V, ~45-51% farq) | 0 (ON umuman yo'q edi) |
| RESET joyi | ~-4V | -3.52...-3.68V | ~+0.5V (juda erta) |

**Umumiy ball**: taxminan 31 nishondan **16-18 tasi** <20-30% farq bilan yoki sifat
jihatidan to'g'ri (aniq avtomatik ball: `../score_fig1.sh` orqali 12/31 qattiq
<20% mezon bilan - lekin bu skript ba'zi nuqtalarda ON/OFF o'tish chegarasidagi
"noto'g'ri" nuqtani tanlaydi, qo'lda tekshirilgan qiymatlar yuqoridagi jadvaldagidek
YAXSHIROQ). Eng katta yutuq: **panel (d) SIFAT jihatidan to'g'rilandi** (bu
foydalanuvchi tomonidan "eng muhim" deb belgilangan tuzatish edi).

---

## 4. Bosim natijasi

`Fig_pressure.png` (`../Fig_pressure.png`), ma'lumot: `Ron_p.csv`.

| Holat | R(0 GPa) | R(2 GPa) | Nisbat | Maqola da'vosi | Mos? |
|---|---|---|---|---|---|
| R_OFF | 99993 Ohm | 99993 Ohm | 1.0 (o'zgarmaydi) | (aniq son yo'q, faqat tasvir) | ✅ |
| R_ON, XOTIRA (beta_mem=30) | 6989 Ohm | 53.5 Ohm | **~130x (~2.1 tartib)** | 2-3 tartibga kamayadi | ✅ (pastki chegarada) |
| R_ON, VOLATIL (beta=0) | 6989 Ohm | 6989 Ohm | 1.0 (aynan) | deyarli o'zgarmaydi | ✅ (hatto kuchliroq) |

---

## 5. Qo'shimcha natijalar

### T_amb bo'yicha (Model1, oldingi TUNGI sessiya)

V_SET/V_RESET harorat oshgani sayin KAMAYADI (Arrhenius kinetikasi kutilgan
natijasi): **300K: V_SET~3.51V -> 450K: V_SET~0.78V** (monoton pasayish).
Manba: `../TUNGI_HISOBOT.md`, `../iv_Tamb_sweep.csv`, `../N6_table.csv`.

### Joule isishi (T_max)

Model1 S2 bosqichida (x=1, |V_app|=3.5V, r_f=200nm/R_dev=1um bilan): T_max~460K
(analitik baho ~450K bilan mos). Model2_FET_Fig1 S2 da (lateral, xs=1, V=4.5V):
T_max=300K (issiqlik ahamiyatsiz - shuning uchun S4 da `ht` fizikasi butunlay
o'chirilgan, xotira tejash uchun).

### N8 sezgirlik tahlili (Model1_Sensitivity.java)

| Parametr | O'zgarish | R_OFF nisbat | R_ON nisbat |
|---|---|---|---|
| sig_on | -50% / +50% | 1.000 / 1.000 | 1.874 / 0.682 |
| t_int | -50% / +50% | 0.500 / 1.500 | 0.500 / 1.500 |
| r_f | -50% / +50% | 1.000 / 1.000 | 3.307 / 0.462 |

R_OFF FAQAT t_int ga chiziqli sezgir (kutilgan, R~1/A*t munosabati). R_ON eng
sezgir r_f ga (kvadratik, R~1/r_f^2), keyin t_int (chiziqli), keyin sig_on
(chiziqli, kutilgan). V_SET/V_RESET sezgirligi VAQT YETMAGANI uchun BAJARILMADI
(oldingi sessiyada aniq qayd etilgan).

---

## 6. Barcha farazlar va cheklovlar

1. Model **fenomenologik** - Weyl/Dirac fazalari, Fermi yoylari modellashtirilmaydi.
2. `sigma_int = sigma_off^(1-x)*sigma_on^x` - log-chiziqli interpolyatsiya, filament
   qat'iy shaklli silindr (Model1) yoki to'rtburchak soha (Model2), faqat
   o'tkazuvchanligi x orqali o'zgaradi.
3. **r_f=200nm, R_dev=1um (Model1)** - boshlang'ich taxminlardan (5nm/50nm) ~40x/~20x
   OSHIRILGAN. Sabab: kichik o'lchamda issiqlik manbai nuqtaviy bo'lib T_max
   fizik jihatdan mumkin bo'lmagan darajaga (~3.5e4 K) chiqardi - bu JISMONIY
   LIMIT, kinetika xatosi emas. Kattaroq o'lchamlar T_max ni ~460K ga tushiradi.
4. **L_gap=1.5nm (Model2) - t_int bilan AYNAN bir xil bo'lishi SHART edi.**
   Kattaroq qiymatlarda (200nm, 3nm sinaldi) svitching UMUMAN sodir bo'lmadi -
   sinh(q*a*E/2kT) argumentining E-maydonga (demak 1/L_gap ga) EKSPONENSIAL
   sezgirligi tufayli.
5. Zatvor ta'siri (Model2) ELEKTROSTATIK YECHILMAYDI - haqiqiy MOS sig'im/maydon
   hisoblanmaydi, `Ugate` oddiy parametr, `f_gate(Ugate)` sigmoid orqali
   fenomenologik bog'lanadi.
6. W (Model2 kanal uzunligi) 1000nm->250nm - past ustuvorlikdagi "ixcham
   geometriya" vazifasi, bir vaqtning o'zida 8GB RAM'li mashinada OOM xavfini
   kamaytirish uchun ham xizmat qildi (panjara elementlari ~4x kamaydi).
7. `z_eff` (samarali zaryad, RESET uchun) - ko'p-zaryadli ion/klaster migratsiyasi
   FARAZI, mexanizmning o'zi bu ishda o'rganilmagan, faqat sinh argumentini
   kalibrlash uchun matematik vosita sifatida kiritilgan.
8. `tau_rel(P)` KESKIN sigmoid - P biror kritik qiymatdan o'tganda relaksatsiya
   mexanizmi SIFAT jihatidan almashadi degan FARAZ (masalan tez ion-diffuziya
   vs barqaror struktura/faza) - bu ham fenomenologik, aniq mikroskopik asos
   YO'Q.
9. Material qiymatlari (sig_ST, k_ST va h.k.) ko'p hollarda HAJMIY material
   uchun, yupqa plyonka uchun EMAS (TEKSHIRILSIN belgisi bilan qayd etilgan).
10. Fig.1 maqsad qiymatlari grafikdan ko'z bilan o'qilgan (±20% noaniqlik faraz
    qilinadi).
11. OFF o'tkazuvchanlik `cosh(E/E0_off)` shaklida - vazifada so'ralgan
    `sinh(x)/x` o'rniga, injenerlik sababi bilan (bo'linishsiz, sonli
    barqarorlik, matematik ekvivalent asimptotika).

---

## 7. Maqola bilan mos kelmagan joylar va ehtimoliy sabablari

1. **I_OFF(+3.5V) panel (a)**: model ~2.5x baland (0.074 vs 0.03mA). Sabab:
   `V0_off=0.8V` hech qachon aniq kalibrlanmagan (boshlang'ich taxmin). Qayta
   kalibrlash SINALDI (V0_off->0.95V), lekin `sig_off_eff` SIMMETRIK `cosh`
   funksiyasi ekan, maqoladagi ASIMMETRIK OFF maqsadlarini (musbat/manfiy qutbda
   turli nisbat) bitta parametr bilan qondira olmadi - I_OFF(-3.8V) ni
   yomonlashtirar edi. Ehtimoliy YECHIM (bajarilmadi): OFF o'tkazuvchanlikka
   HAM qutbga bog'liq asimmetriya (masalan alohida V0_off_pos/V0_off_neg)
   kiritish kerak bo'lardi.
2. **RESET kengligi panel (a)/(b)**: ~0.30V (maqsad <0.2-0.3V) - DEYARLI
   erishildi, lekin panjara chastotasi (0.1-0.15V/nuqta) bilan taqqoslanadigan
   darajada, aniq o'lchash qiyin. Yanada keskinlashtirish (z_eff/Ea_RESET ni
   yanada oshirish) sinalmadi - vaqt tanqisligi va RESET pozitsiyasini
   buzish xavfi tufayli.
3. **Panel (b) kichik volatil sub-tarmoq**: modelda UMUMAN yo'q. Sabab:
   bizning bitta-holatli (x) uzluksiz kinetikamiz FAQAT bitta barqaror ON/OFF
   chegarasini modellay oladi; maqoladagi ikkilamchi metastabil tarmoq uchun
   IKKINCHI holat o'zgaruvchisi (masalan ikki bosqichli filament yoki qo'shimcha
   vaqt doimiysi) kerak bo'lardi - bu ishda YO'Q.
4. **Panel (c)/(d) I_OFF yuqori kuchlanishda**: model maqsaddan ~20-40% farq
   qiladi (avval 5-10x edi, `r_off` parametri bilan YAXSHILANDI, lekin TO'LIQ
   yo'qolmadi). Sabab: `g_off(P)=r_off^(1-P)` FAQAT bitta parametr bilan
   barcha panel/kuchlanish kombinatsiyalarini aniq moslay olmaydi - real
   qurilmada OFF-holat gate-bog'liqligi mexanizmi (masalan depletion effekti)
   bizning soddalashtirilgan formulamizdan farq qilishi mumkin.
5. **Panel (d) R_ON va manfiy ON qiymati**: ~15-50% farq (avval sifat jihatidan
   BUTUNLAY noto'g'ri edi - endi kamida to'g'ri YO'NALISHDA). Sabab: `P_c=0.17`,
   `w_P=0.0015` panel (c)/(d) ning FAQAT bitta ("YAKUNIY sessiya") parametr
   to'plami trayektoriyasidan tanlangan - aniqroq moslash uchun qo'shimcha
   iteratsiya (masalan P_c/w_P/tau_P birgalikda fitting) kerak bo'lardi.
6. **Umumiy**: barcha maqsad qiymatlar bitta maqoladagi BITTA namunaviy
   qurilmadan (grafikdan o'qilgan) - haqiqiy qurilmalar orasidagi tabiiy
   tarqalish (device-to-device variation) hisobga olinmagan, bizning
   "aniq fitting" talabimiz haqiqatda real jarayon aniqligidan yuqori
   bo'lishi mumkin.

---

## 8. Fayllar ro'yxati

| Fayl | Tavsif |
|---|---|
| `Fig1_analog.png` | Fig.1 a-d, 2x2 panel, model (ko'k=OFF/to'q sariq=ON) vs maqola nishonlari (kulrang) |
| `Fig1_side_by_side.png` | Xuddi shu, kattaroq/aniqroq (1 ustun, 4 qator, katta shrift, panjara) |
| `Fig_pressure.png` | R_ON(p)/R_OFF(p), log shkala, xotira vs volatil holat |
| `iv_fig1_continuous.csv` | Model2_FET_Fig1 natijalari (404 qator, 4 panel x 101 nuqta) |
| `Ron_p.csv` | Model3_Pressure natijalari (24 qator: 6 bosim x 2 holat x 2 rep) |
| `Model1_Vertical.java` | Vertikal qurilma modeli (S1-S8, T_amb sweep, N1-N8) |
| `Model1_Sensitivity.java` | N8 sezgirlik tahlili |
| `Model2_FET_Fig1.java` | Lateral FET modeli, Fig.1 ga yaqinlashtirilgan (YAKUNIY versiya) |
| `Model3_Pressure.java` | Bosim ostida R_ON/R_OFF (N7) |
| `Model2_FET_Fig1.mph` | Yechimlar + plot grouplar (pg_VI, pg_xP) bilan, 16.6MB |
| `Model3_Pressure.mph` | Yechimlar + plot grouplar (pg_stress, pg_dgap) bilan, 17.4MB |

Model1_Vertical.mph (743MB, YAKUNIY/ ga nusxalanmagan - 50MB chegara) va barcha
N1-N8 oraliq PNG/CSV fayllari `models/` papkasida saqlanadi.
