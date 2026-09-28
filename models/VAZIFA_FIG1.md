# Vazifa: Model2_FET natijalarini Fig.1 ga yaqinlashtirish (3 soatlik avtonom sessiya)

Foydalanuvchi tomonidan berilgan, o'zgarishsiz saqlangan topshiriq matni. Joriy holat va
natijalar uchun `HISOBOT_FIG1.md` ga qarang.

## Rejim

AVTONOM REJIM, 3 SOAT. Savol berilmaydi (AskUserQuestion ishlatilmaydi) - qarorlar mustaqil
qabul qilinadi va hisobotga yoziladi. Qoidalar TUNGI_VAZIFA.md dagidek:
- Faqat C:\comsol_ish ichida ishlash.
- Bir vaqtda faqat BITTA comsolbatch jarayoni. Uzoq hisoblar fonda, log tekshiriladi.
- Tolerantlik bo'shatilmaydi.
- Bitta xatoni tuzatishga ko'pi bilan 5 urinish, bo'lmasa keyingi ishga o'tiladi.
- Har bir ishlaydigan natijadan keyin commit + push.

## ASOSIY MAQSAD

Model2_FET natijalarini maqoladagi Fig.1 ga iloji boricha yaqinlashtirish (f=100 Hz).
Maqsadli qiymatlar `models/fig1_targets.csv` ga yoziladi:

**(a) Ug=0, xotira**: OFF toki |V|<3V da ~0 (<3 uA), I_OFF(+3.5V)~+0.03mA, I_OFF(-3.8V)~-0.04mA
(nochiziqli). SET +3.5V da keskin, ON tarmoq chiziqli, R_ON~7kOhm (0.43mA, 3V), -3.5V gacha
chiziqli davom etadi (-0.45mA), RESET -3.6...-3.8V da KESKIN (x 0.9->0.1, dV<0.3V).

**(b) Ug=-0.9V**: xotira saqlanadi, R_ON~9kOhm (ON: -3.7V da -0.34mA, +3.5V da +0.47mA).
Qo'shimcha kichik volatil tarmoq 2...3.7V da 0.09...0.22mA. I_OFF(3.8V)~0.06mA.

**(c) Ug=-1.1V**: faqat volatil bo'sag'aviy qayta ulanish, FAQAT musbat qutbda (asimmetrik).
OFF: |I|<0.01mA (+4.5V da ~0.007mA, -4.3V da ~-0.009mA). Ulanish +3.7...+4.3V da, ON tarmoq
3.7V da 0.043mA dan chiziqli pasayadi, V_h~0.7V da OFF ga qaytadi (differensial R~70kOhm).

**(d) Ug->0 ((c) dan keyin)**: xotira qaytadi, lekin R_ON~70kOhm (+3.7V da 0.057mA, -4V da
-0.05mA), I_OFF(4V)~0.011mA. Zatvor ta'siri qisman eslab qolinadi.

## Fizik o'zgarishlar (fenomenologik, har birini Model2_PARAMETRLAR.md da asoslash)

1. Nochiziqli OFF o'tkazuvchanlik: I_OFF ~ I0*sinh(V/V0), sig_off(E)=sig_off0*sinh(E/E0)/(E/E0).
   Boshlang'ich V0~0.8V, E0=0.8V/L_gap. (a) dagi OFF qiymatlariga kalibrlash.
2. Zatvor holati P (ferroelektrik barqarorlashtirish) uchun 2-Global ODE:
   dP/dt=(f(Ug)-P)/tau_P, tau_P asimmetrik (pasayish ~1ms tez, tiklanish ~50-100ms sekin).
   sig_on=sig_on_v*(sig_on0/sig_on_v)^P, tau_rel=tau_v*(tau_nv/tau_v)^P.
   (d) da P~0.5 qolib, R_ON~70kOhm va xotira saqlanishi kerak.
3. Keskin va simmetrik RESET: ON holatdagi isish RESET ni -2.5V ga surib silliqlashtiradi.
   Kinetika qayta fitting: Ea kamaytirib a_hop oshirish, yoki SET/RESET uchun alohida
   a_SET/a_RESET yoki Ea_SET/Ea_RESET. Maqsad: V_SET~+3.5V, V_RESET~-3.7V, ikkalasi keskin.
4. V_h~0.7V (c band): tau_v ni moslash - drive(x->1) va relaksatsiya balansi V_h ni beradi.
   Manfiy qutbda ulanish bo'lmasligi kerak.

## Hisob

Bitta UZLUKSIZ Time Dependent hisob, 4 tsikl (0-40ms), Ug(t) bosqichli: 0(a), -0.9V(b),
-1.1V(c), 0(d), o'tishlar silliq (0.2ms). x va P holatlari tsikllar orasida SAQLANSIN.
Avval har bir parametr to'plamini qisqa testda tekshirish, keyin to'liq hisobni ishga tushirish.

## Natija rasmi

`models/Fig1_analog.png` - maqoladagidek 2x2 panel (a,b,c,d). OFF tarmoq ko'k, ON tarmoq to'q
sariq, marker bilan. Tok mA da, o'qlar "Voltage [V]"/"Current [mA]". Masshtablar: a,b ±0.5mA;
c ±0.05mA; d ±0.06mA; V -5...5V. fig1_targets.csv nuqtalari kulrang marker bilan ustma-ust.
Python+matplotlib bo'lsa shu bilan, bo'lmasa COMSOL plot eksportidan. Har bir panel uchun
solishtirma jadval (V_SET,V_RESET,R_ON,I_OFF,V_h,% farq) HISOBOT_FIG1.md ga.

## Geometriya (vaqt qolsa, Fig.1 dan keyin)

- Model2_FET: barcha o'lchamlar nm tartibida, vizual ixcham (kanal/elektrodlar 100-300nm).
  T_max<800K tekshirilsin.
- Model1: R_dev 1um->250nm (r_f=200nm qoladi, sababi Joul isishi PARAMETRLAR da yoziladi),
  sig_off qayta kalibrlanadi, FAQAT S1 va S2 (300K) qayta hisoblanadi. r_f kamaytirilmaydi,
  sababi tushuntiriladi.
- Barcha 2D grafiklarda z o'qi ko'rinadigan (aspect ratio o'chirilgan).

## MPH fayllar

Har bir .mph yechimlar bilan saqlanadi, barcha plot grouplar saqlashdan oldin run() qilinadi
(GUI da ochilganda grafiklar darhol ko'rinishi uchun). .mph fayl hajmi bir necha MB dan katta
bo'lishi tekshiriladi. Qaysi plot grupda qaysi natija borligi HISOBOT_FIG1.md da ro'yxat.

## Bitta fayl (eng past ustuvorlik)

Barcha modellarni bitta Troyan_Doronin_All.mph ga (3 komponent) birlashtirish. Alohida .java
fayllar zaxira qoladi. Vaqt yetmasa, hisobotda qanday qilish mumkinligi yoziladi.

## Yakun

HISOBOT_FIG1.md da: maqola/model solishtirma jadval, qilingan o'zgarishlar va fizik asosi,
nima bajarilmadi. Model fenomenologik - Weyl/Dirac fazalari "isbotlandi" deyilmaydi.
Oxirgi commit + push.
