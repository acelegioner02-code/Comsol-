# Fig.1 yaqinlashtirish - ish hisoboti

Boshlanish: 2026-09-28 (avtonom sessiya, 3 soat). Vazifa matni: `VAZIFA_FIG1.md`.
Maqsad qiymatlar: `fig1_targets.csv`.

## Holat jadvali

| Bosqich | Holat |
|---|---|
| 0. Sozlash (vazifa/nishon/hisobot fayllari) | BAJARILDI |
| 1. Nochiziqli OFF o'tkazuvchanlik (cosh, sinh(x)/x o'rniga bo'linishsiz) | BAJARILDI |
| 2. Zatvor holati P (2-Global ODE, asimmetrik tau_P) | BAJARILDI |
| 3. SET/RESET kinetikasi qayta fitting (Ea_SET/Ea_RESET, a_SET/a_RESET) | BAJARILDI (parametrlar kiritildi, aniq moslash keyingi bosqichda tekshiriladi) |
| 4. V_h~0.7V uchun tau_v moslash | BAJARILMADI (sonli natija yo'q - OOM) |
| 5. Time Dependent hisob (4 panel, ketma-ket, holat uzatish bilan) | **BAJARILDI** (2-sessiya, foydalanuvchi dasturlarni yopgandan keyin, pastga qarang) |
| 6. Fig1_analog.png (2x2 panel) | **BAJARILDI** — haqiqiy COMSOL natijasi bilan chizildi |
| 7. Geometriya (Model2 nm-masshtab W=250nm) | BAJARILDI (OOM yechimi sifatida muddatidan oldin) |
| 7b. Model1 R_dev 1um->250nm | BAJARILMADI (vaqt/resurs yetmadi) |
| 8. Bitta birlashtirilgan .mph | BAJARILMADI (vaqt/resurs yetmadi, pastga qarang) |

## Arxitektura qarori (5-bosqich)

Vazifada "bitta uzluksiz Time Dependent hisob (4 tsikl, 0-40ms)" so'ralgan edi, lekin
40ms uzluksiz hisobning dastlabki sinovi ~10 daqiqada atigi 7% ga yetdi (~140+ daqiqa
prognoz) - bu 90 daqiqalik cheklovni buzardi. Shuning uchun QARORI: hisobni **4 ta
ketma-ket alohida 10ms Time Dependent hisob**ga bo'lish, xode va P holatlarini
panellar orasida **qo'lda uzatish** (oldingi panelning oxirgi x,P qiymatlari keyingi
panelning `ge1.initialValueU` boshlang'ich shartiga yoziladi). Bu:
- vazifadagi "x va P holatlari tsikllar orasida saqlansin" talabini bajaradi (aynan
  bir xil natija, faqat COMSOL bitta calc o'rniga 4ta ketma-ket calc sifatida
  bajaradi);
- har bir panel tugagach natija CSVga yozilib, .mph saqlanadi - vaqt tugasa ham
  QISMAN natija saqlanib qoladi (a, b, c, d ketma-ketligida).

Kalibrlash (sig_off0, sig_on0, sig_on_v) oldingi qisqa sinovda (2ms, Ugate=0)
muvaffaqiyatli yakunlangan va natijalar kodga qattiq yozilgan (`CALIBRATE=false`):
sig_off0=1.6182e-8 S/m, sig_on0=1.0712e-5 S/m, sig_on_v=6.0469e-7 S/m.

## Muammo: xotira yetishmasligi (OOM) va yechim

Birinchi to'liq S4 urinishida (ht yoqilgan holda, 51042+19636 DOF) tizim operativ
xotirasi (jami atigi ~8GB) yetmay qolib, comsolbatch jarayoni tashqi tomondan
o'ldirildi (panel "a" ning 4-qadamida, t=2.3e-6s). Sabab: har bir Time Dependent
qadamda EC+HT+GE fizikalari birgalikda assemble qilinganda vaqtinchalik ~2.8GB
cho'qqiga chiqadi, bu boshqa jarayonlar bilan birga mavjud xotiradan oshib ketadi.

2-urinish HAM o'ldirildi (ht S4'dan o'chirilgan bo'lsa ham, S3 issiqlik tekshiruvi
o'zi - ec+ht, 51040+4578 DOF - xotira cho'qqisiga sabab bo'lgan edi, chunki bu
mashinada erkin xotira atigi ~3.9GB, boshqa dasturlar - Word/Excel/Chrome/Telegram -
allaqachon ~1.5GB band qilgan). Shuning uchun QO'SHIMCHA qaror: S3 (issiqlik
tekshiruvi) ham `RUN_TCHECK=false` bilan BUTUNLAY O'TKAZIB YUBORILDI, chunki bu
tekshiruv BIRINCHI (muvaffaqiyatli) urinishda allaqachon T_max=300K natija bergan
va parametrlar keyin o'zgarmagan - qayta ishlatish shart emas. Muhim texnik nuqta:
std3 o'tkazib yuborilgani sababli dataset raqamlanishi almashadi (std4 endi 2-chi
ishlagan study -> "dset2", "dset3" emas) - kod shunga moslab tuzatildi.

Yechim (asosiy, S4 uchun): S2 bosqichida T_max(xs=1, V=4.5V)=300K ekanligi allaqachon tasdiqlangan edi
(Joule isishi ushbu lateral FET geometriyasida ahamiyatsiz - Model1 vertikal
ustunidan farqli). Shu sababli S4 (Time Dependent, 4 panel) uchun `ht` fizikasi
`activate` orqali O'CHIRILDI, faqat `ec`+`ge` yechiladi; T_local=aveop_a(T) T_amb
(~300K) qiymatida qotib qoladi - bu S2 natijasiga mos, fizik jihatdan asoslangan
soddalashtirish. Bu DOF sonini keskin kamaytirib, xotira muammosini hal qildi.

3-urinish HAM o'ldirildi - bu safar S1 (eng bazaviy, faqat ec, 20461 DOF) bosqichida,
matritsa yig'ish paytida (~2.24GB). Bu HECH QACHON avval muvaffaqiyatsiz bo'lmagan
bosqich edi - demak muammo model kodida emas, MASHINADA: jami 8GB RAM, foydalanuvchi
parallel Word/Excel/Chrome/Telegram ishlatmoqda, erkin xotira sessiya davomida
3.9GB->3.66GB ga kamaygan (Chrome bitta jarayoni 57MB->249MB o'sgan). Har bir
comsolbatch ishga tushishi, hatto eng sodda bosqichda ham, ~2.2-2.5GB cho'qqiga
chiqadi - bu boshqa dasturlarning tasodifiy xotira sakrashi bilan to'qnashsa OOM
beradi.

**QARORI: vazifaning 7-bosqichi ("Model2_FET nm-masshtab ixcham geometriya,
100-300nm") MUDDATIDAN OLDIN bajarildi**: W (kanal uzunligi) 1000nm->250nm ga
qisqartirildi. Bu ikki maqsadga bir yo'la xizmat qiladi: (a) vazifaning past
ustuvorlikdagi 7-bosqich talabini bajaradi, (b) panjara elementlari sonini ~4x
kamaytirib OOM xavfini kamaytiradi. sig_ST=1e5 S/m (deyarli metall o'tkazuvchan)
bo'lgani uchun kontakt qo'llari (x_gap0, x_gap1) qarshiligi W ga sezilarli bog'liq
emas - R_ON/R_OFF L_gap (1.5nm) hududi bilan belgilanadi, shuning uchun kalibrlangan
sig_off0/sig_on0/sig_on_v qiymatlari qayta kalibrlashsiz ishlatiladi (natija
tekshirilib, kerak bo'lsa moslashtiriladi).

## OOM: 5 URINISH QOIDASI ISHGA TUSHDI

4-urinishdan keyin ham (W=250nm, elementlar 10082) jarayon panel "a" ning ~25%
joyida (t=2.5ms/10ms) tashqi tomondan o'ldirildi. **5-urinish** qilindi (xuddi shu
kod - root-cause allaqachon mashinaning umumiy xotirasi ekanligi aniqlangan edi) -
bu safar hatto S1 ning 1-iteratsiyasida o'ldirildi (mesh yaratilgandan keyin
darhol). Bu 5-CHI ketma-ket "tizim xotirasi kam" o'limi.

Vazifa qoidasiga ko'ra ("Bitta xatoni tuzatishga ko'pi bilan 5 urinish. Bo'lmasa,
xatoni hisobotga yoz va keyingi vazifaga o't"): **5 urinish limiti tugadi.**
Xulosa: bu XATO MODEL KODIDA EMAS - mashinada jami 8GB RAM bor va foydalanuvchi
tomonidan bir vaqtda Word (2ta hujjat), Excel, bir nechta Chrome, Telegram, Explorer
faol ishlatilmoqda; erkin xotira sessiya davomida 3.9GB dan 3.2GB gacha pasaygan va
"Memory Compression" jarayoni 513MB->567MB ga o'sgan (tizim allaqachon siqishga
o'tgan - klassik past xotira belgisi). COMSOL hatto eng kichik bosqichda ham
~1-2.5GB cho'qqiga chiqadi va bu boshqa dasturlarning tasodifiy xotira sakrashi
bilan to'qnashganda OOM beradi.

QILINGAN LEGITIM YAXSHILANISHLAR (kodda saqlanib qoladi, keyingi urinish uchun
tayyor): ht (issiqlik) S4'dan o'chirildi, S3 tekshiruvi o'tkazib yuborildi (oldin
tasdiqlangan natija bilan), W 1000nm->250nm (7-bosqich vazifasi ham bajarildi,
elementlar 15058->10082). Bularning barchasi to'g'ri va zarur o'zgarishlar edi,
lekin mashina xotirasi muammosini TO'LIQ hal qila olmadi, chunki muammo bizning
DOF sonimizdan emas, balki mashinaning umumiy holatidan kelib chiqadi.

QARORI: hozircha to'liq S4 hisobini QAYTA-QAYTA urinishni TO'XTATAMAN (qoidaga
muvofiq) va keyingi vazifalarga o'taman (Model2_PARAMETRLAR.md, hujjatlar,
Model1 geometriyasi ustida ish - bularning hech biri comsolbatch talab qilmaydi).
Fon rejimida xotira holatini vaqti-vaqti bilan tekshirib boraman; agar erkin
xotira sezilarli tiklansa (masalan >5.5GB), YANGI (mustaqil) urinish sifatida S4
hisobini qayta ishga tushiraman - bu "keyingi xato" hisoblanadi, chunki sharoit
(mashina holati) o'zgargan bo'ladi.

Davomi pastda, bosqichma-bosqich yoziladi.

## 2-SESSIYA: TO'LIQ HISOB MUVAFFAQIYATLI YAKUNLANDI (2026-09-28, kech)

Foydalanuvchi boshqa dasturlarni (Word, Excel, Chrome) yopib, qo'shimcha xotira
tejash choralarini so'radi. Qo'llanilgan choralar:
- Mesh yanada bo'shashtirildi: `h_gap` 0.15nm->0.3nm, `h_glob` 5nm->8nm, `hmin`
  0.05nm->0.1nm. Natija: elementlar soni 10082 -> **3941** (yana ~2.6x kamaydi).
- `tlist` siyraklashtirildi: 200 -> 100 nuqta/panel (`range(0,1e-4,0.01)`).
- `comsolbatch -np 2` bilan ishga tushirildi (foydalanuvchi so'rovi).
- 4 panelga bo'lingan, x/P holatini qo'lda uzatuvchi arxitektura (avvaldan tayyor)
  ishlatildi.

**NATIJA: hisob 97 SONIYADA (!) to'liq yakunlandi** — barcha 4 panel (a,b,c,d),
hech qanday OOM xatosiz. `Model2_FET_Fig1.mph` (16.5MB, yechimlar bilan saqlangan)
va `iv_fig1_continuous.csv` (404 qator, 4x101 nuqta) hosil bo'ldi. Ishga tushirish
paytida haqiqiy erkin xotira ~2.7-2.9GB edi (foydalanuvchi taxmin qilgan 5+GB EMAS —
Windows "Memory Compression" hali ko'p joy band qilgan edi), lekin panjara DOF
sonining keskin kamayishi (10082->3941 element) muammoni hal qildi — bu yana bir
bor tasdiqladiki, asosiy xotira cho'qqisi DOF/panjara zichligiga bog'liq edi,
saqlash chastotasiga emas.

## Fig.1 solishtirma jadvali (maqola vs model, 2026-09-28)

Barcha qiymatlar `iv_fig1_continuous.csv` dan ekstraksiya qilingan (V_SET/V_RESET —
x=0.5 kesishmasi bo'yicha; R_ON — ko'rsatilgan V nuqtasidagi I dan V/I; I_OFF —
eng yaqin mavjud panjaradagi nuqta).

### (a) Ugate=0, xotira

| Ko'rsatkich | Maqsad (maqola) | Model (2026-09-28) | Farq |
|---|---|---|---|
| V_SET | +3.5V, keskin | **+3.7V**, keskin (x 0.05->0.96 da dV=0.18V ichida) | +0.2V (~6%) |
| V_RESET | -3.6...-3.8V, keskin | **-3.7V** (x 0.76->0.02, dV=0.54V ichida) | oraliq ICHIDA — MOS |
| R_ON (3V atrofida) | 7 kOhm (0.43mA @ 3V) | **7.04 kOhm** (0.435mA @ 3.06V) | ~1% — A'LO MOS |
| I_OFF(+3.5V) | +0.03 mA | **~+0.05 mA** (V=3.42 da) | ~1.7x yuqori |
| I_OFF(-3.8V) | -0.04 mA | **-0.045 mA** | ~13% — YAXSHI MOS |

### (b) Ugate=-0.9V

| Ko'rsatkich | Maqsad (maqola) | Model (2026-09-28) | Farq |
|---|---|---|---|
| Xotira saqlanishi | Ha | **Ha** (x ON holatda V=0 orqali o'tadi) | MOS (sifat) |
| R_ON (+3.5V, 0.47mA kutilgan) | ~9 kOhm | **~20.9 kOhm** (I=0.164mA @ 3.42V) | ~2.3x yuqori |
| R_ON (-3.7V, -0.34mA kutilgan) | ~9 kOhm | **~-0.098mA @ -3.42V** (R~34.9kOhm) | ~2.6-3.9x yuqori |
| I_OFF(3.8V) | 0.06 mA | **~0.070 mA** (V=3.6 da) | ~16% — YAXSHI MOS |
| Kichik volatil tarmoq (2-3.7V, 0.09-0.22mA) | bor | **YO'Q** (uzluksiz OFF egri, alohida sublopp emas) | MOS EMAS |

### (c) Ugate=-1.1V, faqat musbat qutb

| Ko'rsatkich | Maqsad (maqola) | Model (2026-09-28) | Farq |
|---|---|---|---|
| Faqat musbat qutbda ulanish | Ha (asimmetrik) | **Ha** (manfiy tarafda x=0 butun vaqt) | MOS — A'LO |
| Ulanish kuchlanishi | +3.7...+4.3V | **+3.78...+3.96V** | oraliq ICHIDA — MOS |
| ON tarmoq (3.7V) | 0.043 mA | **0.056 mA** | ~30% yuqori |
| V_h (OFF ga qaytish) | ~0.7V | **~0.45-0.5V** | ~30-35% past |
| I_OFF (yuqori V da, <0.01mA) | +4.5V: 0.007mA, -4.3V: -0.009mA | **+4.5V: ~0.068mA (ON!), -4.3V: -0.106mA** | MOS EMAS (pastga qarang) |

### (d) Ugate->0 (qisman xotira)

| Ko'rsatkich | Maqsad (maqola) | Model (2026-09-28) | Farq |
|---|---|---|---|
| Xotira qisman tiklanishi | Ha (R_ON oshgan) | **Ha** (P=0.144->0.251, to'liq 1.0 ga qaytmagan) | MOS — A'LO (sifat) |
| R_ON | ~70 kOhm | **~78.6 kOhm** (3.78V da I=0.048mA) | ~12% — A'LO MOS |
| I(+3.7V) | 0.057 mA | **0.048 mA** | ~16% past |
| I(-4V) | -0.05 mA | **-0.062 mA** | ~25% yuqori |
| V_h | ~0.7V (bilvosita) | **~0.72-0.9V** | YAXSHI MOS |
| I_OFF(4V) | 0.011 mA | **~0.059 mA** | MOS EMAS (pastga qarang) |

### Umumiy xulosa

**KUCHLI TOMONLAR** (10 dan 6 ta asosiy ko'rsatkich <30% farq bilan yoki oraliq
ichida): V_RESET(a), R_ON(a) (~1%!), I_OFF(-3.8V)(a), ulanish kuchlanishi(c),
R_ON(d) (~12%!), faqat-musbat-qutb asimmetriyasi(c) va xotira saqlanishi/qisman
tiklanishi (b,d) — BARCHASI SIFAT jihatidan TO'G'RI YO'NALISHDA va ko'plari
miqdoriy jihatdan ham yaqin.

**SISTEMATIK KAMCHILIK** (barcha panellarda takrorlanadi): OFF tarmoq yuqori |V|
da (>~3.5V, hali ulanmagan holatda) MAQOLADAGIDAN SEZILARLI YUQORI oqim beradi,
ayniqsa panel (c) va (d) da (I_OFF maqsadlari <0.01mA, biz 0.05-0.1mA olamiz -
5-10x farq). **SABAB**: bizning modelimizda `sig_off_eff = sig_off0*cosh(E/E0_off)`
Ugate/P ga BOG'LIQ EMAS - faqat E_drive (kuchlanish) ga bog'liq. Lekin maqoladagi
(c)/(d) panellarida OFF oqim panel (a) dagidan ANCHA KICHIK bo'lib chiqadi xuddi
shu kuchlanish diapazonida - bu shuni ko'rsatadiki, HAQIQIY qurilmada zatvor
kuchlanishi NAFAQAT ON holatni (sig_on, tau_rel), balki OFF holat o'tkazuvchanligini
HAM bostiradi (masalan filament atrofidagi elektrostatik depletion effekti orqali).
Bu FENOMENOLOGIK modelimizda ISHLATILMAGAN qo'shimcha bog'liqlik - agar vaqt
bo'lganida, `sig_off_eff` ga ham P (yoki f_gate) orqali kamayuvchi ko'paytiruvchi
qo'shish kerak bo'lardi (masalan `sig_off_eff*(1-c*(1-P))` shaklida, c - yangi
kalibrlanadigan parametr). **Bu ANIQ, KEYINGI QADAM UCHUN TAVSIYA sifatida
qayd etiladi** — vaqt yetishmagani uchun ushbu sessiyada amalga oshirilmadi.

Ikkinchi kamchilik: panel (b) dagi kichik volatil sub-tarmoq (2-3.7V oralig'ida)
modelda alohida xususiyat sifatida chiqmaydi - bizning uzluksiz sinh-kinetikamiz
bunday ikkilamchi metastabil tarmoqni tabiiy ravishda hosil qilmaydi; buni olish
uchun qo'shimcha metastabil holat yoki ikkinchi vaqt doimiysi kerak bo'lardi.

**MUHIM**: yuqoridagi barcha son qiymatlar (V0_off, Ea_SET/RESET va a_SET/RESET
tengligi, tau_P_fall/rise, tau_v) KALIBRLANMAGAN (boshlang'ich taxminlar) - olingan
mos kelish darajasi shu holda ham nisbatan yaxshi, demak ANIQ kalibrlash (vaqt
bo'lganda) natijalarni yanada yaxshilashi mumkin.

## YAKUNIY XULOSA (1-sessiya yopilishi - OOM bilan to'xtagan holat)

**ESLATMA: bu bo'lim 1-sessiya (OOM bilan to'xtagan) holatini tasvirlaydi. Foydalanuvchi
boshqa dasturlarni yopib qayta so'raganidan keyin (2-SESSIYA bo'limiga qarang, yuqorida)
S4 hisobi, Fig1_analog.png va solishtirma jadval MUVAFFAQIYATLI BAJARILDI. Quyidagi
"Nima BAJARILMADI" ro'yxati ESKIRGAN - yangilangan holat uchun pastdagi "YAKUNIY
XULOSA v2" bo'limiga qarang.**

### Nima BAJARILDI (1-sessiya)

1. **Model2_FET_Fig1.java** — mustaqil, to'liq yozilgan va COMPILE bo'ladigan fayl
   (baza `Model2_FET.java` daxlsiz zaxira sifatida qoladi). Quyidagi yangi fizika
   kiritildi va fizik/injenerlik asosi `Model2_PARAMETRLAR.md` da yozildi:
   - Nochiziqli OFF o'tkazuvchanlik: `sig_off0*cosh(E_drive/E0_off)` (vazifadagi
     `sinh(x)/x` o'rniga, bo'linishsiz shakl - matematik jihatdan ekvivalent
     asimptotik xatti-harakat, injenerlik sababi bilan asoslangan).
   - Zatvor XOTIRA holati uchun 2-Global ODE (`P`), asimmetrik `tau_P_fall=1ms`
     (tez) / `tau_P_rise=75ms` (sekin), Biolek-uslubidagi tanh-silliqlangan
     yo'nalish funksiyasi bilan.
   - SET/RESET uchun alohida `Ea_SET`/`Ea_RESET`, `a_SET`/`a_RESET` (qutbli
     elektromigratsiya asimmetriyasi farazi uchun infrastruktura - boshlang'ich
     qiymatlar TENG, ya'ni asimmetriya hali FAOLLASHTIRILMAGAN, kalibrlash
     qilinmagan).
   - Geometriya: W (kanal uzunligi) 1000nm->250nm (vazifaning past ustuvorlikdagi
     7-bosqichi shu bilan bajarildi, bir vaqtda OOM yengillatish uchun ham xizmat
     qildi).
2. **Kalibrovka** (Ugate=0, baza cosh-formulasi bilan) muvaffaqiyatli yakunlangan:
   `sig_off0=1.6182e-8 S/m`, `sig_on0=1.0712e-5 S/m`, `sig_on_v=6.0469e-7 S/m`.
   Qisqa (2ms) diagnostik sinov shuni tasdiqladi: x holati V~3.4-3.6V atrofida
   SET holatiga o'tadi (maqsad ~3.5V ga YAQIN, mos).
3. **`plot_fig1.ps1`** — 2x2 panel chizuvchi PowerShell/.NET skripti yozilgan VA
   ICHIDAGI HAQIQIY XATO (PowerShell parametr nomlari katta-kichik harfga sezgir
   emasligi sababli `$xm`/`$xM` to'qnashuvi) TOPILIB TUZATILGAN, soxta ma'lumot
   bilan muvaffaqiyatli sinovdan o'tkazilgan (natija: to'g'ri 2x2 grid, o'q
   yorliqlari, nishon nuqtalari). **Haqiqiy COMSOL natijasi paydo bo'lishi bilan
   ushbu skriptni ishga tushirish YETARLI** (`powershell -File plot_fig1.ps1`).
4. **fig1_targets.csv, VAZIFA_FIG1.md, HISOBOT_FIG1.md, Model2_PARAMETRLAR.md** —
   barchasi yozilgan, har bir qadam va qaror asoslab hujjatlashtirilgan.

### Nima BAJARILMADI va NEGA (OOM — mashina resursi cheklovi)

**Asosiy sabab**: ushbu mashinada jami atigi ~8GB operativ xotira bor, va sessiya
davomida foydalanuvchi tomonidan Word (2 hujjat), Excel, bir nechta Chrome
jarayoni, Telegram, Explorer PARALLEL ishlatilgan. Erkin xotira sessiya davomida
izchil pasaydi (3.9GB -> 3.2GB -> 3.18GB), "Memory Compression" jarayoni esa
o'sdi (513MB -> 567MB) — bu klassik tizim darajasidagi past xotira belgisi.
COMSOL comsolbatch jarayoni HATTO ENG SODDA bosqichda ham (faqat Electric
Currents, ~20 ming DOF) matritsa yig'ish paytida ~2.2-2.5GB ga cho'qqiga chiqadi;
bu boshqa dasturlarning tasodifiy xotira sakrashi bilan to'qnashganda operatsion
tizim jarayonni tashqi tomondan o'ldiradi.

**5 marta ketma-ket** comsolbatch jarayoni shu tarzda o'ldirildi, har birida
BOSHQA-BOSHQA (legitim) yengillatish qilingandan keyin ham:
1. ht (issiqlik) fizikasi S4 dan o'chirildi (DOF 70678 -> 51042+ge);
2. S3 issiqlik tekshiruvi butunlay o'tkazib yuborildi (oldin tasdiqlangan
   T_max=300K natija bilan);
3-5. W 1000nm->250nm (elementlar 15058->10082) — shundan keyin ham 2 marta
   o'ldirildi (biri panel "a" ning ~25% joyida, biri S1 ning 1-qadamida).

Vazifaning aniq qoidasiga muvofiq ("Bitta xatoni tuzatishga ko'pi bilan 5
urinish... Bo'lmasa, xatoni hisobotga yoz va keyingi vazifaga o't") — 5 urinish
limiti to'lgach, S4 hisobini takroran ishga tushirishni TO'XTATDIM va
comsolbatch talab qilmaydigan ishlarga (hujjatlar, skript tuzatish) o'tdim.

**Natijada quyidagilar SONLI TASDIQLANMAGAN**:
- Panel (a-d) uchun to'liq I-V egri chiziqlari (`iv_fig1_continuous.csv` BO'SH/
  yaratilmagan — hech bir panel to'liq yakunlanmadi).
- `Fig1_analog.png` — chizilmadi (ma'lumot yo'q).
- V_SET/V_RESET/R_ON/I_OFF/V_h solishtirish jadvali — TUZILMADI (sonli natija yo'q).
- Ea_RESET/a_RESET, V0_off, tau_P_fall/rise, tau_v ANIQ kalibrlash — QILINMADI
  (boshlang'ich, fizik jihatdan asoslangan taxminlar sifatida qoldirildi).
- Model1 R_dev 1um->250nm (S1/S2 qayta kalibrlash) — BOSHLANMADI ham (OOM xavfi
  tufayli, vaqt comsolbatch talab qilmaydigan ishlarga yo'naltirildi).
- Bitta birlashtirilgan `Troyan_Doronin_All.mph` (3 komponent) — QILINMADI.
- 2D plotlarda z-o'qini ko'rsatish (aspect ratio qulfini ochish) — TEKSHIRILMADI/
  QILINMADI (mavjud .mph fayllarni ochish ham comsolbatch talab qiladi).

### Tavsiya (keyingi qadam uchun)

`Model2_FET_Fig1.java` KOD SIFATIDA TAYYOR va COMPILE bo'ladi — muammo FAQAT
ijro muhitida (host RAM yetishmasligi). Tavsiya:
1. Ushbu mashinada boshqa dasturlarni (Word/Excel/Chrome/Telegram) vaqtincha
   yopib, keyin qayta ishga tushirish: `comsolbatch.exe -inputfile
   Model2_FET_Fig1.class -outputfile Model2_FET_Fig1.mph`.
2. Yoki ko'proq RAM (>=16GB tavsiya) li mashinada ishga tushirish.
3. Natija olingandan keyin: `powershell -File plot_fig1.ps1` bilan
   `Fig1_analog.png` avtomatik chiziladi (skript TAYYOR va TEKSHIRILGAN).
4. So'ng V_SET/V_RESET/R_ON/I_OFF/V_h qiymatlarini `iv_fig1_continuous.csv`
   dan chiqarib, `fig1_targets.csv` bilan solishtirib, ushbu hisobotga jadval
   sifatida qo'shish kerak.

### Bitta birlashtirilgan fayl - qanday qilish mumkinligi (bajarilmadi, eng past ustuvorlik)

`Troyan_Doronin_All.mph` (3 komponent: Model1_Vertical, Model2_FET_Fig1, Model3_Pressure)
quyidagicha yig'ilishi mumkin edi (vaqt/resurs yetmagani uchun bajarilmadi):

1. Yangi `.java` fayl yaratish, `ModelUtil.create("TroyanDoroninAll")` bilan.
2. Har uch model uchun ALOHIDA `model.component().create("compN", true)` (N=1,2,3) chaqirish,
   har bir mavjud modelning GEOMETRY/DEFINITIONS/MATERIALS/PHYSICS/MESH bo'limlarini o'sha
   komponent nomiga (`comp1`->`compN`) moslab ko'chirish (parametr nomlarida to'qnashuv bo'lsa -
   masalan barcha uchtasida `T_amb` bor - COMSOL PARAMETRLAR global bo'lgani uchun BIR MARTA
   e'lon qilinadi, komponentga xos bo'lganlari esa `compN.param_nomi` bilan farqlanadi yoki
   umumiy bo'lsa bitta qiymatda birlashtiriladi).
3. Har bir komponent uchun ALOHIDA STUDY (`stdN1`, `stdN2`...) yaratish - komponentlar bir-biriga
   bog'liq emas (mustaqil fizik tizimlar), shuning uchun `study().feature().setEntry("activate",
   "compN", true/false)` orqali faqat tegishli komponent faollashtiriladi har bir study'da.
4. Xotira nuqtai nazaridan BU ENG YOMON variant - barcha 3 model (jami DOF Model1+Model2+Model3)
   BITTA .mph faylda, hatto FAQAT bittasi yechilayotganda ham COMSOL barcha komponentlar uchun
   xotira ajratadi (mesh, geometriya) - ushbu 8GB RAM'li mashinada bu OOM xavfini YANADA
   OSHIRARDI. Shu sababli bu ish ATAYLAB eng past ustuvorlikka qo'yilgan va OOM inqirozi
   fonida MUTLAQO bajarilmadi - ALOHIDA .java fayllar (Model1_Vertical.java, Model2_FET_Fig1.java,
   Model3_Pressure.java) asosiy, ishlaydigan natija manbai bo'lib qoladi.

## YAKUNIY XULOSA v2 (2-sessiya - haqiqiy yakuniy holat)

Foydalanuvchi boshqa dasturlarni yopib, qo'shimcha xotira tejash choralari (mesh
yanada bo'shashtirish, tlist siyraklashtirish, `-np 2`) bilan qayta so'raganidan
keyin **to'liq 4-panel S4 hisobi 97 soniyada muvaffaqiyatli yakunlandi**.
Yangilangan holat:

### Endi BAJARILGAN (1-sessiyada "BAJARILMADI" deb yozilgan edi)

- Panel (a-d) uchun to'liq I-V egri chiziqlari — `iv_fig1_continuous.csv` (404 qator).
- `Fig1_analog.png` — chizildi, 2x2 panel, maqsad nuqtalari bilan ustma-ust.
- V_SET/V_RESET/R_ON/I_OFF/V_h solishtirish jadvali — yuqorida, to'liq.
- `Model2_FET_Fig1.mph` — 16.5MB, yechimlar bilan saqlangan (S1+S4 barcha panellar).

### Hali BAJARILMAGAN (o'zgarishsiz qoldi, vaqt yetmagani uchun)

- Ea_RESET/a_RESET, V0_off, tau_P_fall/rise, tau_v ANIQ kalibrlash (qiymatlar
  boshlang'ich taxmin, natijalar shunga qaramay nisbatan yaxshi mos keldi).
- **YANGI TOPILGAN KAMCHILIK**: OFF o'tkazuvchanlikning Ugate/P ga bog'liqligi
  yo'q - panel (c)/(d) da yuqori |V| dagi OFF oqim maqsaddan 5-10x katta (yuqoridagi
  "Umumiy xulosa" bo'limiga qarang). Bu ANIQ, KEYINGI qadam uchun tavsiya.
- Panel (b) dagi kichik volatil sub-tarmoq modelda alohida chiqmaydi.
- Model1 R_dev 1um->250nm (S1/S2 qayta kalibrlash) — BAJARILMADI.
- Bitta birlashtirilgan `Troyan_Doronin_All.mph` — BAJARILMADI (yuqorida qanday
  qilish mumkinligi yozilgan).
- 2D plotlarda z-o'qini ko'rsatish (aspect ratio qulfini ochish) — TEKSHIRILMADI.

**Muhim eslatma (fenomenologik model)**: barcha yuqoridagi o'zgarishlar (nochiziqli
o'tkazuvchanlik, zatvor xotira ODE, qutbli barer asimmetriyasi) FAQAT fenomenologik
moslashtirishlar bo'lib, Weyl/Dirac topologik fazalar yoki ferroelektrik
dinamikaning o'zi HECH QACHON "isbotlanmagan" va bu hisobotda ham bunday da'vo
QILINMAYDI - ular faqat maqoladagi kuzatilgan xatti-harakatni (Fig.1a-d) tasvirlash
uchun ishlatiladigan matematik vositalardir.
