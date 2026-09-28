# 3-model (bosim ta'siri, N7): parametrlar va farazlar

Manba maqola: Troyan & Doronin, ICCS 2020, LNNS 186, 427-433 (2021). Maqolada bosim ostida xotira
holatining R_ON qiymati 2-3 tartibga kamayishi, volatil holatniki deyarli o'zgarmasligi tilga
olinadi (aniq sonlar/mexanizm berilmagan). Bu FENOMENOLOGIK model shu kuzatuvni qayta ishlab
chiqarish uchun quriladi — mexanizm "isbotlanmaydi", faqat mos keladigan fitting taqdim etiladi.

## Geometriya va elektr

Model1_Vertical.java ning 2-bosqichdagi (R_dev=1um, r_f=200nm) geometriyasi va S1 dan kalibrlangan
sig_off=4.775e-3 S/m, sig_on0=1.594 S/m qiymatlari AYNAN qayta ishlatiladi (bosimsiz, p=0 holatga
mos).

## Solid Mechanics chegaraviy shartlari

- Yuqori chegara (box_top): Boundary Load, bosim p_load (0 dan 2 GPa gacha, siqib turuvchi,
  pastga yo'nalgan).
- Pastki chegara (box_bot): Fixed Constraint (mahkamlangan — substrat qattiq deb faraz qilinadi).
- Yon devor (box_out, r=R_dev): Roller (vertikal siljishga ruxsat, lateral siljish nolga teng —
  o'q-simmetrik geometriyada bu "cheksiz lateral qattiq muhit" farazini fenomenologik ifodalaydi).

## Tunnel-o'tkazuvchanlik farazi

`d(p) = t_int*(1+aveop_int(solid.eZZ))` — interfeys (vdW bo'shlig'i) ning bosim ostidagi samarali
qalinligi. `eZZ` manfiy (siqilish) bo'lgani uchun d(p) < t_int.

`sig_on(p) = sig_on0*exp(beta_mem*(t_int-d(p))/t_int)` — WKB/tunnel taxminiga o'xshash EKSPONENSIAL
bog'liqlik: qalinlik bir oz kamaysa, tunnel o'tkazuvchanlik eksponensial ortadi. Bu STANDART tunnel
effekti ifodasi T ~ exp(-2*kappa*d) ning FENOMENOLOGIK versiyasi (beta_mem ~ 2*kappa*t_int
ma'nosida).

**Volatil holat uchun beta=0 FARAZI**: topshiriqqa ko'ra, volatil (qisqa muddatli, tez relaksatsiya
qiluvchi) holatda filament geometriyasi/tabiati bosimga bunchalik sezgir emas deb faraz qilinadi —
buning aniq mikroskopik asosi yo'q, bu SODDALASHTIRISH, natijalarni solishtirish uchun boshlang'ich
nuqta sifatida xizmat qiladi.

## Elastik parametrlar (TEKSHIRILSIN — barchasi taxmin, adabiyotdan aniq qiymat izlanmagan)

| Material | E | nu | Asos |
|---|---|---|---|
| TiN (elektrodlar) | 250 GPa | 0.25 | Umumiy TiN qattiq plyonka qiymati tartibi — TEKSHIRILSIN |
| Sb2Te3 | 55 GPa | 0.25 | Xalkogenid yarim o'tkazgichlar uchun odatiy tartib — TEKSHIRILSIN |
| GeTe | 50 GPa | 0.25 | Xuddi shu tartib — TEKSHIRILSIN |
| Interfeys (vdW) | 5-15 GPa (boshlang'ich: 10) | 0.25 | c-o'q bo'ylab vdW bog'lanish ANCHA YUMSHOQ (grafit c-o'qiga o'xshash, E_c~5-40GPa tartibida turli qatlamli materiallar uchun) — ENG NOANIQ parametr |

## beta_mem kalibrlash va fizik reallik bahosi

Maqsad: R_ON(2GPa)/R_ON(0) = 1e-2...1e-3 oralig'ida. Bu `beta_mem*(t_int-d(2GPa))/t_int` ni
ln(1e-2)..ln(1e-3) = -4.6..-6.9 oralig'iga (R kamayishi uchun sig_on ORTISHI kerak, ya'ni eksponent
argumenti MUSBAT va katta bo'lishi kerak: R kamayadi <=> sig_on ortadi <=> exp(+katta son)) —
demak `beta_mem*(t_int-d)/t_int` ≈ +4.6..+6.9 bo'lishi kerak (2 GPa da).

**Fizik reallik tekshiruvi**: tunnel effektida T ~ exp(-2*kappa*Delta_d), demak
beta_mem*(t_int-d)/t_int ekvivalenti 2*kappa*Delta_d ga teng bo'lishi kerak, bu yerda
Delta_d = t_int-d(2GPa) (siqilish miqdori, metrda), kappa ~ 5-10 1/nm (odatiy tunnel parchalanish
doimiysi). Demak beta_mem ≈ 2*kappa*Delta_d*t_int/(t_int-d) = 2*kappa*t_int (agar Delta_d/t_int
kichik bo'lsa, beta_mem ≈ 2*kappa*t_int). t_int=1.5nm, kappa=5-10 1/nm bilan: beta_mem ≈
2*(5..10)*1.5 = 15..30. Bu qiymat E_int tanloviga BEVOSITA BOG'LIQ EMAS to'g'ridan-to'g'ri (beta_mem
mustaqil fitting parametri), LEKIN Delta_d (demak eZZ, demak qanchalik SIQILISH sodir bo'lishi)
E_int orqali aniqlanadi: yumshoqroq E_int (masalan 5 GPa) -> katta Delta_d 2 GPa da -> KICHIKROQ
beta_mem kifoya qiladi bir xil R_ON pasayishini olish uchun (chunki beta_mem*Delta_d/t_int ko'paytmasi
sobit maqsadga erishishi kerak). Qattiqroq E_int (15 GPa) -> kichik Delta_d -> KATTAROQ beta_mem
kerak bo'ladi.

Haqiqiy ishga tushirish natijasidan keyin: qaysi E_int qiymatida topilgan beta_mem 15-30 oralig'iga
(fizik jihatdan "real" tunnel parchalanish doimiysiga mos) yaqinroq tushishi shu yerga yoziladi.

## Natijalar (2026-09-28, yakuniy)

- **beta_mem (kalibrlangan) = 30** (boshlang'ich taxmin 10 dan; 10 bilan R_ON atigi ~5x pasaydi,
  maqsad 100-1000x edi).
- **R_ON(p), R_OFF(p) jadvali** (Ron_p.csv, E_int=10 GPa bilan):

  | p (GPa) | d_gap (nm) | R_OFF (ohm) | R_ON, xotira (ohm) | R_ON, volatil (ohm) |
  |---|---|---|---|---|
  | 0.00 | 1.500 | 99993 | 6989 | 6989 |
  | 0.25 | 1.469 | 99993 | 3863 | 6989 |
  | 0.50 | 1.438 | 99993 | 2105 | 6989 |
  | 1.00 | 1.375 | 99993 | 614.1 | 6989 |
  | 1.50 | 1.313 | 99993 | 178.8 | 6989 |
  | 2.00 | 1.250 | 99993 | 53.47 | 6989 |

  **R_ON(2GPa)/R_ON(0) = 53.47/6989 = 0.00765** — MAQSAD ORALIG'IDA (1e-3...1e-2)!
  R_OFF bosimga BUTUNLAY BOG'LIQ EMAS (kutilganidek — R_OFF formulasi sig_on(p) ni o'z ichiga
  olmaydi, faqat x=1 (ON) holatida sig_on(p) ishlatiladi). Volatil holatda (beta=0) R_ON ham
  bosimga bog'liq emas — dizayn bo'yicha to'g'ri.

- **Fizik reallik xulosasi**: beta_mem=30, t_int=1.5nm bilan kappa = beta_mem/(2*t_int) = 10 1/nm —
  bu "odatiy" tunnel parchalanish doimiysi oralig'ining (5-10 1/nm) YUQORI CHEGARASIDA joylashgan,
  ya'ni FIZIK JIHATDAN REAL qiymat (juda katta yoki juda kichik emas). Bu E_int=10 GPa tanlovi
  bilan mos keladi (o'rtacha qattiqlik, natijada o'rtacha siqilish ~16.7% 2 GPa da, kappa esa
  o'rtacha-yuqori tunnel parchalanish tezligini talab qiladi bir xil R_ON pasayishini olish uchun).
  YUMSHOQROQ E_int (masalan 5 GPa) tanlansa, siqilish kattaroq bo'lardi, demak KICHIKROQ beta_mem
  (demak kichikroq, "realroq" kappa) kifoya qilardi — bu keyingi tekshirish uchun ochiq savol.

## TEKSHIRILSIN -> TASDIQLANDI (COMSOL 6.0 API, shu ishlash jarayonida)

- Fizika interfeysi: "SolidMechanics" (default tag "solid") — TASDIQLANDI, birinchi urinishda ishladi.
- Chegaralar: "Fixed" (Fixed Constraint) va "Roller" — TASDIQLANDI, birinchi urinishda ishladi.
- "BoundaryLoad" feature turi — TASDIQLANDI, birinchi urinishda ishladi.
- "LoadType" qiymati "Pressure" ISHLAMAYDI — to'g'ri qiymat "FollowerPressure".
- Bosim miqdori xossasi ("Pressure","P","p","p0" barchasi ISHLAMADI) — to'g'ri nom xuddi ENUM
  qiymatining o'zi: "FollowerPressure". `.properties()` diagnostika metodi orqali topildi.
- "solid.eZZ" (Green-Lagrange/muhandislik cho'zilish tenzori Z-komponenti) — TASDIQLANDI, birinchi
  urinishda ishladi (Structural Mechanics Module qo'llanmasi, "solid.eXY" naqshiga mos).
- Ketma-ket ikki STEP li Stationary STUDY (1-qadam Solid Mechanics, 2-qadam Electric Currents,
  "activate" bilan boshqarilgan) — ODATIY holatda, HECH QANDAY qo'shimcha "usesol"/"notstudy"
  sozlashsiz, oldingi qadam natijasini avtomatik meros qiladi — TASDIQLANDI.
- EvalGlobal natijasi ("d_gap", t_int="1.5[nm]" dan olingan o'zgaruvchi) o'zining TABIIY birligida
  (nm) qaytariladi, SI (metr)da EMAS — birlik konversiyasida (*1e9) xato qilingan va tuzatilgan.
