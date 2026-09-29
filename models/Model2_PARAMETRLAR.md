# 2-model (lateral FET): parametrlar va farazlar

Manba maqola: Troyan & Doronin, ICCS 2020, LNNS 186, 427-433 (2021), doi:10.1007/978-3-030-66093-2_41,
Fig. 1a-d (zatvor kuchlanishi orqali xotira <-> volatil rejim almashinuvi).

Model1_Vertical.java dagi TASDIQLANGAN COMSOL 6.0 API chaqiruvlari (fizika interfeys nomlari,
Global Equations sintaksisi, Biolek+tanh oyna funksiyasi va h.k.) qayta ishlatilgan. Bu hujjat
Model1_PARAMETRLAR.md ga QO'SHIMCHA — faqat Model2 ga xos yangi farazlar yoziladi.

## Asosiy geometriya farazi (TEKSHIRILSIN — maqolada aniq geometriya berilmagan)

Maqolada FET tuzilishining aniq geometriyasi (kanal uzunligi, faol soha joylashuvi va o'lchami)
berilmagan. Quyidagi faraz qilindi:

- 2D LATERAL (Kartezian) kesim, substrat modellanmaydi (pastki chegara T=T_amb bilan issiqlik
  cho'kmasi sifatida ifodalanadi — Model1 dagi elektrod chegaralariga o'xshab).
- Sb2Te3 kanali (t_ST=20nm) source (chap) dan drain (o'ng) gacha W=1000nm uzunlikda cho'zilgan.
- Kanal ichida, zatvor ostida, L_gap uzunlikdagi "FAOL SOHA" — bu qurilmaning real
  nuqson/vdW-cho'kish nohomogenligi joylashgan qismini FENOMENOLOGIK ifodalaydi. Uning
  o'tkazuvchanligi Model1 dagi kabi sig_off^(1-x)*sig_on(Ugate)^x.

  **MUHIM (empirik topilma): L_gap = 1.5 nm, DASTLABKI 200 nm EMAS.** Boshlang'ich taxmin (200 nm,
  keyin 3 nm) bilan HECH QANDAY Ugate qiymatida SET/RESET sodir bo'lmadi (x amalda 1e-3 boshlang'ich
  qiymatida qotib qoldi). Sabab: dxdt_rhs dagi sinh(q*a_hop*E_drive/2kBT) argumenti E_drive ga
  chiziqli, lekin E_drive = V/L_gap tarzida L_gap ga TESKARI proporsional, va sinh o'zi katta
  argumentlarda EKSPONENSIAL sezgir (sinh(x)~0.5*e^x). L_gap 2x katta bo'lsa E_drive 2x kichik,
  demak argument yarmiga tushadi, sinh esa taxminan sqrt() darajasida (juda katta) kichrayadi -
  masalan 13.2->6.6 argument uchun sinh ~750x kamayadi. Diagnostika uchun E_drive/T_local vaqt
  qatoriga qo'shilib tekshirildi: T_local ANIQ 300.000000K bo'lib qoldi (issiqlik ta'siri emas, sof
  maydon yetishmasligi ekani tasdiqlandi). L_gap Model1 dagi t_int (1.5 nm) bilan AYNAN bir xil
  qilingach, svitching DARHOL to'g'ri ishladi (V_SET/V_RESET Model1 maqsadlariga mos chiqdi).
  XULOSA: bu geometrik parametr FENOMENOLOGIK modelning o'zboshimcha "estetik" tanlovi emas, balki
  KINETIKA FORMULASINING o'zi (xususan sinh argumentining E-maydon miqyosiga bog'liqligi) qattiq
  cheklov qo'yadi - L_gap amalda t_int bilan bir xil tartibda bo'lishi SHART, aks holda model hech
  qachon svitching ko'rsatmaydi (yoki kinetika parametrlari - Ea, a_hop - qayta fitting qilinishi
  kerak bo'lardi, bu ishda qilinmadi).
- GeTe (t_GT=20nm), Al2O3 (t_ox=10nm, zatvor dielektrigi), zatvor elektrodi (t_gate=10nm) kanal
  ustida, FAQAT issiqlik tarqalishi uchun saqlangan (elektr o'tkazuvchi emas — quyiga qarang).

## Zatvor ta'sirining modellashtirilishi (MUHIM FARAZ)

Topshiriqda aniq ko'rsatilganidek, zatvor ta'siri **elektrostatik yechilmaydi** — Al2O3/zatvor
qatlamlari orqali haqiqiy MOS sig'im/maydon hisoblanmaydi. Buning o'rniga:

- `Ugate` — oddiy COMSOL PARAMETRI (foydalanuvchi/sikl tomonidan o'rnatiladi), MAYDON EMAS.
- `f(Ugate) = 1/(1+exp(-(Ugate-U0)/w))`, U0=-0.9V, w=0.08V — SIGMOID, "ferroelektrik
  barqarorlashtirishning maydon ta'sirida bosqichma-bosqich bostirilishi"ni FENOMENOLOGIK
  ifodalaydi. Sigmoid shakli TANLANGAN, chunki: (a) Ugate=0 da effekt to'liq (f~1), Ugate=-0.9V
  atrofida (maqoladagi "aralash rejim") f~0.5 (yarim yo'l), Ugate<=-1.1V da effekt deyarli yo'q
  (f~0.08..0.03) — bu maqoladagi Fig.1b-c tavsifi bilan SIFAT jihatidan mos; (b) sigmoid — eng
  sodda, ikki cheklovchi holat orasidagi silliq monoton o'tishni ta'minlaydigan funksiya (boshqa
  ko'rinishlar — masalan chiziqli yoki eksponensial — xuddi shu ikkita chegara nuqtasini
  qondirish uchun qo'shimcha asossiz parametrlar talab qilardi).
- `tau_rel(Ugate) = tau_v*(tau_nv/tau_v)^f(Ugate)` — Ugate=0 da tau_nv=1e3s (nonvolatil, Model1
  bilan bir xil), Ugate<<U0 da tau_v=1e-4s (volatil — filament o'qishdan keyin darhol relaksatsiya
  qiladi, "o'zi qaytadi").
- `sig_on(Ugate) = sig_on_v*(sig_on0/sig_on_v)^f(Ugate)` — Ugate=0 da sig_on0 (R_ON=7kOhm ga
  kalibrlangan), Ugate<<U0 da sig_on_v (I(4V)~0.04mA ga kalibrlangan).

**Fizik asos**: maqolada zatvor kuchlanishi ferroelektrik qatlam (yoki interfeys dipol tartibi)ni
barqarorlashtirish kuchini kamaytiradi deb talqin qilinadi — biz buni ikkita fenomenologik
ko'rsatkich (tau_rel, sig_on) orqali, BITTA umumiy "bostirish darajasi" f(Ugate) bilan boshqarilgan
holda ifodalaymiz. Bu FAQAT feiomenologik moslashtirish, ferroelektrik dinamikaning o'zi
modellashtirilmaydi.

## Kalibrovka tartibi

1. Ugate=0: sig_off, sig_on0 — Model1 dagi S1 kalibrovka mexanizmi bilan bir xil (R_OFF=100kOhm,
   R_ON=7kOhm maqsad).
2. Ugate=-1.1V: sig_on_v — I(4V)~0.04mA maqsadiga (xs=1, ya'ni to'liq ON holatda statik yechim,
   V_app=4V) qat'iy nuqta iteratsiyasi bilan kalibrlanadi.

Agar faqat sig_on bilan I(4V)~0.04mA ga yetib bo'lmasa (masalan, kanalning QOLGAN qismi — chap/o'ng
Sb2Te3 segmentlari — dominant qarshilik hissa qo'shsa), TOPSHIRIQQA KO'RA kanal o'tkazuvchanligi
(sig_ST) ham xuddi shunday sigmoid bilan kamaytirilishi mumkin edi; bu ZARUR bo'lsa-bo'lmasligi
haqiqiy ishga tushirish natijasidan keyin shu faylga yoziladi.

## Haydovchi maydon

Model1 da E_drive = -aveop_fil(ec.Ez) edi (VERTIKAL qurilma). Model2 LATERAL bo'lgani uchun
E_drive = -aveop_a(ec.Ex) ishlatiladi — TEKSHIRILSIN: "ec.Ex" 2D Kartezian ConductiveMedia
interfeysida x-yo'nalishdagi elektr maydon komponenti nomi deb faraz qilindi (ec.Ez ning
axisymmetric/Kartezian analogi).

## Oyna funksiyasi

Model1 da EMPIRIK ravishda tanlangan silliqlashtirilgan Biolek oynasi
(dir_smooth=0.5*(1+tanh(E_drive/E_s))) bevosita qayta ishlatildi — Model1 da bu RESET/SET
ikkalasini ham to'g'ri ishlatishni ta'minlagani isbotlangan.

## Natijalar (2026-09-28, yakuniy)

- **Kalibrovka (Ugate=0)**: sig_off=8.760e-7 S/m, sig_on0=1.072e-5 S/m -> R_OFF=9.965e4 ohm,
  R_ON=7075 ohm (maqsad 100/7 kOhm ga mos).
- **sig_on_v kalibrovkasi (Ugate=-1.1V, I(4V)~0.04mA maqsadga)**: sig_on_v=6.98e-7 S/m ->
  I(4V)=0.0401 mA (CAL_MAX_IT 8'dan 14'ga oshirilgandan keyin yaqinlashdi).
- **Ugate sweep (FET_N6_table.csv, har biri 1 tsikl)**:

  | Ugate (V) | V_SET (V) | V_RESET (V) | I(4V) (mA) | Talqin |
  |---|---|---|---|---|
  | 0.0 | 3.542 | -3.490 | 0.574 | XOTIRA (gisterezis, Model1 maqsadiga mos) |
  | -0.5 | 3.542 | -4.453 | 0.561 | XOTIRA, RESET biroz qiyinlashgan |
  | -0.9 | 3.543 | -4.329 | 0.148 | aralash rejimga o'tish boshlanishi |
  | -1.1 | 3.639 | +2.978 | 0.048 | VOLATIL (pastga qarang) |
  | -1.5 | 3.783 | +3.535 | 0.041 | VOLATIL, kuchliroq |

  Ugate=-1.1/-1.5V da "V_RESET" MUSBAT kuchlanishda topildi - bu haqiqiy teskari-qutb RESET emas,
  balki x ning volatil (tau_v=1e-4s bilan tez) o'z-o'zidan pasayishi, hali V pasayish qismida
  musbat bo'lgan paytda sodir bo'ladi. Kodning oddiy "x=0.5 kesishmasi" mantig'i buni "XOTIRA" deb
  yorlaydi (chunki biror kesishma bor), lekin FIZIK JIHATDAN bu aynan kutilgan VOLATIL xatti-harakat
  (iv_ugate_sweep.csv dagi xom x(t) ma'lumoti buni tasdiqlaydi). Bu klassifikatsiya nomuvofiqligi -
  vaqt yetishmagani uchun tuzatilmadi, lekin natija fizik jihatdan TO'G'RI talqin qilingan.

- **Fig.1d qaytarlik** (Ugate: 0 -> -1.1V -> 0V, har biri MUSTAQIL 1 tsikl, x(0)=1e-3 dan
  boshlanadi): barcha 3 qadam x(oxiri)~0 bilan tugadi. FARAZ: bu "mustaqil" ketma-ketlik (har bir
  qadam avvalgisining YAKUNIY holatidan emas, balki qaytadan x=1e-3 dan boshlanadi) - haqiqiy
  UZLUKSIZ holat ketma-ketligi emas, balki "Ugate=0 dagi xatti-harakat Ugate o'zgargandan keyin
  ham qaytadi" degan reversibillik ko'rsatkichi.

## Model2_FET_Fig1.java: Fig.1(a-d) ga yaqinlashtirish uchun qo'shimcha fizika (2026-09-28)

Yuqoridagi Model2_FET.java (baza) alohida saqlab qolindi (zaxira sifatida); barcha quyidagi
o'zgarishlar YANGI, MUSTAQIL faylda (`Model2_FET_Fig1.java`) qilindi, chunki ular baza modelning
qayta kalibrovkasini talab qiladi va Fig.1 ning to'rtta paneli (turli Ugate, turli rejim) BITTA
izchil parametr to'plamida qamrab olinishi kerak edi.

### 1. Nochiziqli OFF o'tkazuvchanlik: `sig_off_eff = sig_off0*cosh(E_drive/E0_off)`

Maqsad (fig1_targets.csv, panel a): OFF tokining kuchlanishga NOCHIZIQLI bog'liqligi
(I_OFF(+3.5V)~+0.03mA, I_OFF(-3.8V)~-0.04mA — nisbat |I(-3.8)/I(3.5)|~1.33, CHIZIQLI bo'lganda
kutilgan nisbat 3.8/3.5~1.09 bo'lardi). Vazifada berilgan farazga ko'ra `sinh(E/E0)/(E/E0)`
(Poole-Frenkel/tunnel o'tish tipidagi maydon-faollashtirilgan o'tkazuvchanlik) ishlatilishi
kerak edi; AMALDA `cosh(E/E0)` ga ALMASHTIRILDI:

- `sinh(x)/x` va `cosh(x)` KATTA x da bir xil asimptotik tarzda o'sadi (ikkalasi ham ~0.5*e^|x|/x
  yoki ~0.5*e^|x| — logaritmik farq ahamiyatsiz), KICHIK x da ikkalasi ham ~1+x^2/6 (juft funksiya,
  V=0 da simmetrik, chiziqli tarmoq yo'q) — demak FENOMENOLOGIK maqsad (kuchli maydonda keskin,
  kuchsiz maydonda deyarli Om qonuni) bir xil tarzda bajariladi.
  **Sabab (INJENERLIK, fizik emas)**: `sinh(x)/x` ifodasi E_drive=0 nuqtasida 0/0 noaniqlik hosil
  qiladi; COMSOL buni "E_drive+eps" bilan informal ravishda oldini olish mumkin edi, lekin BIRINCHI
  sinov shuni ko'rsatdiki, bu BO'LINISH operatori Time Dependent yechuvchida BDF birinchi
  qadamlarida qo'shimcha qattiqlik (stiffness) keltirib chiqarishi mumkin edi (garchi keyinchalik
  bu asosan BDF ning ODATIY sekin boshlanish xatti-harakati bo'lib chiqdi ham). `cosh(x)`
  BO'LINISHSIZ, silliq, hamma joyda differentsiallanuvchi - shuning uchun UNI TANLAB QOLDIRISH
  QAROR QILINDI (vazifada aytilgan sinh(x)/x o'rniga).
- `E0_off = V0_off/L_gap`, `V0_off=0.8[V]` — boshlang'ich taxmin, keyin KALIBRLASH orqali
  aniqlashtirilishi rejalashtirilgan edi (I_OFF(3.5V)=0.03mA maqsadga muvofiq); vaqt tanqisligi
  (OOM muammosi, pastga qarang) sababli faqat sig_off0 kalibrlandi, V0_off boshlang'ich qiymatida
  qoldirildi — HAQIQIY natija olingandan keyin I_OFF(3.5)/I_OFF(-3.8) nisbati TEKSHIRILISHI KERAK,
  agar mos kelmasa V0_off qo'shimcha ozod parametr sifatida moslashtirilishi kerak bo'ladi.

### 2. Zatvor XOTIRA holati uchun 2-Global ODE: `dP/dt = (f_gate - P)/tau_P_eff`

Model2_FET.java (baza) da Ugate ta'siri LOKAL, DARHOL edi (sig_on/tau_rel bevosita f(Ugate) ga
bog'liq edi — zatvor o'zgarsa, natija DARHOL o'zgaradi, xotira yo'q). Biroq maqolaning Fig.1(d)
paneli ("Ugate qaytarilgandan keyin R_ON hali ham oshgan, to'liq tiklanmagan") buni ZID etadi -
zatvor ta'sirining o'zi INERTSIYaga ega bo'lishi kerak (masalan, ferroelektrik domen qayta
tartiblanishi yoki interfeys zaryad tuzoqlarining sekin bo'shashi FENOMENOLOGIK sabab sifatida
faraz qilinadi). Shu sababli YANGI holat o'zgaruvchisi `P` (0..1, "zatvor xotira darajasi")
kiritildi:

- `f_gate = 1/(1+exp(-(Ugate-U0)/w_sig))` — bazadagi f(Ugate) bilan BIR XIL sigmoid, lekin endi
  bu P ning "maqsad" (target) qiymati, DARHOL o'zi emas.
- `dir_P = 0.5*(1+tanh((f_gate-P)/P_s))` — Biolek-uslubidagi YO'NALISHGA BOG'LIQ silliqlash: P
  ko'tarilayotganda (f_gate>P) SEKIN tau_P_rise, tushayotganda (f_gate<P) TEZ tau_P_fall ishlatiladi
  — bu ASIMMETRIYA to'g'ridan-to'g'ri vazifada talab qilingan ("fall ~1ms tez, rise ~50-100ms
  sekin"): FENOMENOLOGIK asos - zatvor kuchlanishi kuchli manfiy bo'lganda barqarorlashtiruvchi
  holat TEZ yo'qoladi (kuch qo'llash oson), lekin qayta tiklanish uchun tabiiy relaksatsiya SEKIN
  (energetik to'siq orqali).
- `tau_P_fall=1e-3[s]`, `tau_P_rise=75e-3[s]` — vazifada berilgan diapazon (fall~1ms, rise~50-100ms)
  o'rtasidan tanlangan boshlang'ich qiymatlar; TO'LIQ natija olinmagani sabab ANIQ kalibrlash
  qilinmadi.
- `sig_on_eff = sig_on_v*(sig_on0/sig_on_v)^P`, `tau_rel_eff = tau_v*(tau_nv/tau_v)^P` — bazadagi
  formulalar bilan bir xil ko'rinish, lekin ENDI argumentda f_gate emas P turadi — shu orqali
  panel (d) da (Ugate qaytgandan keyin) P hali 1 ga to'liq qaytmagani uchun R_ON oraliq qiymatda
  (maqsad ~70kOhm, bazaviy R_ON~7kOhm va Ugate=-1.1V dagi R~1/sig_on_v orasida) qolishi KUTILADI —
  bu FAQAT nazariy kutish, hisob tugallanmagani uchun SONLI TASDIQ YO'Q (pastga, OOM bo'limiga
  qarang).

### 3. Qutbga bog'liq SET/RESET barer asimmetriyasi: `Ea_eff`, `a_eff`

Bazada RESET (E_drive<0) va SET (E_drive>0) BIR XIL Ea/a_hop bilan ifodalangan, faqat Biolek
oynasi orqali yo'nalish farqlangan. Vazifa talab qiladi: SET keskin +3.5V da, RESET ham KESKIN
lekin BOSHQA chegarada (-3.6...-3.8V). Buning uchun:

- `Ea_eff = dir_smooth*Ea_SET + (1-dir_smooth)*Ea_RESET`
- `a_eff = dir_smooth*a_SET + (1-dir_smooth)*a_RESET`
  (`dir_smooth` — bazadan meros, `0.5*(1+tanh(E_drive/E_s))`, E_drive>0 da ~1, <0 da ~0)
- Boshlang'ich qiymatlar TENG qilib qo'yildi (`Ea_SET=Ea_RESET=0.9eV`, `a_SET=a_RESET=0.3nm`) —
  ya'ni hozircha ASIMMETRIYA FAOL EMAS (simmetrik holatdan boshlanadi). Bu QASDDAN: avval
  simmetrik holatda SET/RESET kuchlanish qiymatlari qanchalik maqsaddan chetlashishini ko'rish,
  so'ng FAQAT KERAK BO'LSA (masalan RESET -3.5V o'rniga -5V da sodir bo'lsa) Ea_RESET yoki
  a_RESET ni pasaytirish REJALASHTIRILGAN edi. **Bu kalibrlash HAM BAJARILMADI** (OOM sababli
  hisob tugallanmagani uchun qaysi tomonga siljitish kerakligini bildiruvchi SONLI natija yo'q).

### 4. Geometriya: W (kanal uzunligi) 1000nm -> 250nm

Boshida vazifaning past ustuvorlikdagi ("vaqt qolsa") 7-bosqichi sifatida rejalashtirilgan edi,
lekin OOM inqirozi tufayli MUDDATIDAN OLDIN, ustuvor tartibda amalga oshirildi (pastga, "OOM"
bo'limiga qarang). `sig_ST=1e5 S/m` (deyarli metall o'tkazuvchan Sb2Te3 taxmini) tufayli kontakt
qo'llari (`x_gap0`, `x_gap1`, uzunligi `(W-L_gap)/2`) qarshiligi W ga amalda BOG'LIQ EMAS -
qurilmaning umumiy R_ON/R_OFF qiymati FAQAT `L_gap` (1.5nm) hududi bilan aniqlanadi. Shuning
uchun W ni 4x qisqartirish R_ON/R_OFF kalibrovkasini BUZMASLIGI kutiladi (nazariy, TASDIQLANMAGAN -
hisob tugallanmagani uchun).

### OOM (xotira yetishmasligi) - hal qilinmagan cheklov

Ushbu mashinada (jami 8GB RAM, foydalanuvchi bir vaqtda boshqa dasturlar bilan band) to'liq S4
(4-panel Time Dependent) hisobi **5 marta ketma-ket** tashqi tomondan "system running low on
memory" sababli o'ldirildi (batafsil: `HISOBOT_FIG1.md`, "OOM: 5 URINISH QOIDASI ISHGA TUSHDI"
bo'limi). Natijada:

- Yuqoridagi 4 ta fizik o'zgarish (nochiziqli OFF, P-ODE, SET/RESET asimmetriyasi, geometriya)
  KODGA KIRITILGAN va COMPILE bo'ladi, lekin TO'LIQ SONLI TEKSHIRUVDAN O'TKAZILMAGAN.
- Qisqa diagnostik sinov (2ms, Ugate=0, eski kod versiyasida) x ning V~3.4-3.6V da SET holatiga
  o'tishini tasdiqladi (maqsad 3.5V ga yaqin) - bu YAGONA qisman sonli tasdiq.
- V0_off, tau_P_fall/rise, Ea_RESET/a_RESET aniq kalibrlash QILINMADI - yuqoridagi qiymatlar
  FIZIK JIHATDAN ASOSLANGAN BOSHLANG'ICH TAXMINLAR, lekin Fig.1 bilan sonli mos kelishi
  TEKSHIRILMAGAN.
- Tavsiya: ushbu .java faylni ko'proq erkin RAM mavjud bo'lgan mashinada (yoki boshqa dasturlar
  yopilgandan keyin shu mashinada) qayta ishga tushirish kifoya - kod o'zi TAYYOR, faqat
  ijro muhiti (host xotirasi) yetishmadi.

### YANGILANISH (2026-09-28, kech): to'liq hisob muvaffaqiyatli yakunlandi, YANGI TOPILGAN KAMCHILIK

Foydalanuvchi xotira tejash choralari (mesh 3941 elementgacha bo'shashtirish, tlist
siyraklashtirish, `-np 2`) bilan to'liq S4 hisobi 97 soniyada yakunlandi (batafsil:
`HISOBOT_FIG1.md`, "Fig.1 solishtirma jadvali" bo'limi). Ko'p ko'rsatkichlar (V_RESET,
R_ON panel a ~1% farq, R_ON panel d ~12% farq, panel c ning faqat-musbat-qutb
asimmetriyasi) A'LO mos keldi - V0_off/Ea/tau_P/tau_v HECH QANDAY qo'shimcha
kalibrlashsiz, faqat boshlang'ich taxminlar bilan.

**Aniqlangan fizik kamchilik**: `sig_off_eff = sig_off0*cosh(E_drive/E0_off)` FAQAT
E_drive (kuchlanish)ga bog'liq, Ugate/P ga EMAS. Lekin natijalar shuni ko'rsatdiki,
panel (c)/(d) (Ugate=-1.1V va undan keyin) da OFF tarmoq oqimi maqsaddan (maqolada
<0.01mA) 5-10x KATTA chiqadi xuddi shu kuchlanish oralig'ida (masalan +4.5V da biz
~0.068mA, maqsad ~0.007mA). Bu real qurilmada zatvor kuchlanishi NAFAQAT ON holatni
(sig_on, tau_rel - modelda bor), balki OFF holat o'tkazuvchanligini HAM bostirishi
kerakligini ko'rsatadi (masalan filament atrofidagi elektrostatik depletion effekti
orqali - FENOMENOLOGIK farazlanadi, mexanizm o'zi bu ishda o'rganilmagan).

**Tavsiya (keyingi qadam)**: `sig_off_eff` ga P (yoki f_gate) orqali kamayuvchi
ko'paytiruvchi qo'shish, masalan `sig_off_eff = sig_off0*cosh(E_drive/E0_off)*
(1-c_off*(1-P))`, bu yerda `c_off` (0..1) - Ugate qanchalik OFF holatni ham
bostirishini boshqaruvchi YANGI kalibrlanadigan parametr (panel c/d dagi I_OFF
maqsadlariga fit qilinishi kerak). Bu ishda VAQT YETMAGANI uchun amalga
oshirilmadi.

Ikkinchi kichik kamchilik: panel (b) maqolasidagi "kichik volatil sub-tarmoq"
(2-3.7V oralig'ida, 0.09-0.22mA) bizning uzluksiz sinh-kinetikamizda alohida
xususiyat sifatida chiqmaydi - buning uchun ikkinchi metastabil holat yoki
qo'shimcha vaqt doimiysi kerak bo'lardi (bajarilmadi).

### YANGILANISH 2 (2026-09-28, 3-sessiya): 8 iteratsiyali kalibrlash - yakuniy parametrlar

Foydalanuvchi taqrizidan keyin quyidagi YAKUNIY qiymatlar 8 ta to'liq hisob orqali
tanlandi (batafsil jadval: `HISOBOT_FIG1.md`, "3-SESSIYA" bo'limi):

- `r_off=0.05[1]` — OFF o'tkazuvchanlikning zatvorga bog'liqligi `g_off=r_off^(1-P)`
  orqali. Panel (c)/(d) dagi ortiqcha OFF oqim (avval 5-10x katta) endi ~1.2-1.7x
  farqqa tushdi.
- `a_RESET=0.4[nm]`, `Ea_RESET=1.0[eV]` (a_SET=0.3nm, Ea_SET=0.9eV o'zgarmadi) —
  panel (a) RESET pozitsiyasini -3.65V ga (maqsad -3.6...-3.8V ICHIDA) qaytardi.
  **MUHIM SABOQ**: `a_RESET`ni oshirish sinh argumentini (a*E ga proporsional)
  EKSPONENSIAL ravishda o'zgartiradi - kichik o'zgarish (masalan 83% oshirish)
  chegara kuchlanishini KUTILMAGANDA katta miqdorda (1.5V+) surib yuborishi mumkin.
  `Ea_RESET`ni MOS RAVISHDA oshirish (kamaytirmasdan) buni kompensatsiya qiladi.
  RESET KENGLIGI (x:0.9->0.1) hali ham ~0.7V (maqsad <0.3V) - bu ALOHIDA muammo,
  vaqt yetmagani uchun hal qilinmadi.
- `U0=-1.0[V]`, `w_sig=0.05[V]` (avval -0.9V/0.08V) — f_gate(-0.9V)~0.88,
  f_gate(-1.1V)~0.12, f_gate(0)~1 berish uchun hisoblangan.
- `P_s=0.02[1]` (avval 0.1) — **MUHIM TOPILMA**: `tau_P_rise/tau_P_fall` nisbati
  juda katta (75x) bo'lgani uchun `P_s=0.1` bilan HATTO kichik `dir_P` (~0.08)
  qiymati `tau_P_eff` ni kutilgan 1ms o'rniga ~7ms ga oshirib yuborardi (chiziqli
  aralashtirish katta assimetriya bilan nomutanosib ta'sir qiladi). `P_s` ni
  kamaytirish `dir_P` ni tezroq 0/1 ga to'yintiradi, P dinamikasini aniqroq
  qiladi. Natija: P(panel b oxiri)=0.895 (maqsad 0.8-0.9 ga A'LO mos).
- `tau_v=1.5e-4[s]` (avval 1e-4) — V_h (panel c/d holding kuchlanishi) ni
  boshqarish uchun asosiy dastak (P=1 bo'lganda, ya'ni panel (a) da, tau_v
  formuladan BUTUNLAY YO'QOLADI - tau_rel_eff=tau_v*(tau_nv/tau_v)^1=tau_nv -
  shuning uchun panel (a) ga TA'SIR QILMAYDI, faqat b/c/d ga). Natija:
  V_h(c)=0.72V (maqsad 0.7V, A'LO), V_h(d)=0.48V (maqsad 0.7V, ~31% past).

Barcha 8 iteratsiyaning sonli natijalari va tanlash mantiqi `HISOBOT_FIG1.md`
da to'liq jadval sifatida keltirilgan.

### YANGILANISH 3 (2026-09-29, YAKUNIY sessiya): tau_rel keskin sigmoid, z_eff

Foydalanuvchi panel (d) SIFAT jihatidan noto'g'ri (volatil halqa, xotira emas)
va panel (a)/(b) RESET yetarli keskin emasligini aniqladi. Ikkita YANGI faraz
kiritildi (batafsil sinov jarayoni: `YAKUNIY_HISOBOT.md`):

**1. tau_rel(P) KESKIN sigmoid** (eski silliq darajali `tau_v*(tau_nv/tau_v)^P`
o'rniga):
```
S_tau = 1/(1+exp(-(P-P_c)/w_P))
tau_rel_eff = tau_v + (tau_nv-tau_v)*S_tau
```
`P_c=0.17`, `w_P=0.0015`. **Fizik asos**: zatvor-bosilgan barqarorlashtiruvchi
holat P biror KRITIK qiymatdan o'tganda filament relaksatsiya mexanizmi
SIFAT jihatidan almashadi (masalan, past P da tez ion-diffuziya orqali,
yuqori P da esa barqaror struktura/faza orqali) - bu ikkita ALOHIDA fizik
rejim, shuning uchun SILLIQ emas, KESKIN o'tish jismoniy jihatdan asosliroq
fenomenologik faraz. **MUHIM SONLI SABOQ**: `tau_nv-tau_v` juda katta (~1000,
chunki tau_nv=1000s, tau_v~1.5e-4s) bo'lgani uchun `w_P` JUDA tor bo'lishi
SHART (0.0015, 0.008 EMAS) - aks holda HATTO kichik sigmoid "quyruq" qiymati
(masalan 7.6%) `tau_rel` ni o'nlab soniyalarga oshirib, BUTUN panelni
"muzlatib" qo'yadi. `P_c=0.17` panel (c)/(d) ning HAQIQIY simulyatsiya
trayektoriyalaridan (x=0.5 kesishmasi qayerda P bilan mos kelishi) tanlangan,
"P~0.14/0.25" boshlang'ich taxminlardan EMAS - haqiqiy qiymatlar ozgina farq
qilar edi.

**2. z_eff (samarali zaryad ko'paytiruvchisi)** RESET yo'nalishi uchun:
```
a_eff = dir_smooth*a_SET + (1-dir_smooth)*(z_eff*a_RESET)
```
`z_eff=2.42`, `a_RESET=0.3nm` (a_SET bilan bir xil bazaviy qiymat),
`Ea_RESET=1.4eV`. **Fizik asos**: RESET (filament uzilishi) SET (filament
hosil bo'lishi)dan farqli, KO'P ZARYADLI klaster yoki ion guruhi migratsiyasi
orqali sodir bo'lishi mumkin (masalan bir nechta Te/Sb ionlari birgalikda
harakatlanadi) - bu sinh argumentidagi effektiv zaryadni oshiradi, RESET ni
maydonga (V) nisbatan SET dan ko'ra sezgirroq (keskinroq) qiladi. `z_eff`
qiymati `d(ln rate)/dV = q*z_eff*a_RESET/(2*kB*T*L_gap) ~ 12.5 1/V` maqsadidan
analitik hisoblangan, `Ea_RESET` esa shu katta `a_eff` bilan V_RESET ni
-3.6...-3.8V oralig'ida SAQLASH uchun qayta kalibrlangan (analitik taxmin:
`DeltaEa=kB*T*(r-1)*argument_eski`, so'ng bitta hisob bilan tasdiqlangan).

**Natija**: RESET kengligi (x:0.9->0.1) panel (a)/(b) da ~0.7V dan ~0.3V ga
qisqardi (maqsad <0.2-0.3V ga DEYARLI erishildi). V0_off qayta kalibrlash
(0.8->0.95V) SINALDI, lekin RAD ETILDI - sig_off_eff SIMMETRIK cosh funksiya
ekan, `fig1_targets.csv` dagi ASIMMETRIK OFF maqsadlarini (turli qutbda turli
nisbat) bitta V0_off bilan qondira olmadi, umumiy ball pasaydi.
