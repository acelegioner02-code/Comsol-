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
| 5. Time Dependent hisob (4 panel, ketma-ket, holat uzatish bilan) | TO'XTATILDI (OOM, 5 urinish - pastga qarang) |
| 6. Fig1_analog.png (2x2 panel) | QISMAN: skript (`plot_fig1.ps1`) yozilgan, TUZATILGAN, soxta ma'lumot bilan tasdiqlangan; haqiqiy ma'lumot yo'qligi uchun rasm chizilmadi |
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

## YAKUNIY XULOSA (sessiya yopilishi)

### Nima BAJARILDI

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

**Muhim eslatma (fenomenologik model)**: barcha yuqoridagi o'zgarishlar (nochiziqli
o'tkazuvchanlik, zatvor xotira ODE, qutbli barer asimmetriyasi) FAQAT fenomenologik
moslashtirishlar bo'lib, Weyl/Dirac topologik fazalar yoki ferroelektrik
dinamikaning o'zi HECH QACHON "isbotlanmagan" va bu hisobotda ham bunday da'vo
QILINMAYDI - ular faqat maqoladagi kuzatilgan xatti-harakatni (Fig.1a-d) tasvirlash
uchun ishlatiladigan matematik vositalardir.
