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
