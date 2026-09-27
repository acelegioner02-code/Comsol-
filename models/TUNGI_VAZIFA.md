# Tungi avtonom vazifa (2026-09-27 kechasi -> 2026-09-28 ertalab)

Foydalanuvchi tomonidan berilgan, o'zgarishsiz saqlangan topshiriq matni. Bu fayl faqat ma'lumotnoma
uchun; joriy holat va natijalar uchun `TUNGI_HISOBOT.md` ga qarang.

## Rejim

TUNGI AVTONOM REJIM. Foydalanuvchi ertalabgacha (~8 soat) yo'q. Savol berilmaydi, AskUserQuestion
ishlatilmaydi - har bir qaror quyidagi qoidalar asosida mustaqil qabul qilinadi va hisobotga yoziladi.

## Umumiy qoidalar

- Faqat `C:\comsol_ish` ichida ishlash. COMSOL prefs va tizim fayllariga tegilmaydi. Ruxsat so'rashi
  mumkin bo'lgan amallardan qochiladi.
- Bir vaqtda faqat BITTA comsolbatch jarayoni. Uzoq hisoblar fonda (log faylga yozib), log har 3-5
  daqiqada tekshiriladi. Kutish vaqtida keyingi modelning kodi yoziladi.
- Tolerantlik bo'shatilmaydi. Hisob 90 daqiqadan oshib ketsa, avval vaqt oralig'i qisqartiriladi
  (masalan, 1 tsikl), tolerantlik emas.
- Bitta xatoni tuzatishga ko'pi bilan 5 urinish. Bo'lmasa, xato hisobotga yoziladi va keyingi
  vazifaga o'tiladi. To'xtab qolinmaydi.
- Har bir ishlaydigan natijadan keyin: commit + `git push origin claude/trusting-noether-ksc9q6`.
  Push o'tmasa, lokal commit bilan davom etiladi.
- Barcha izoh va hisobotlar o'zbek tilida. Model fenomenologik: topologik fazalar (Weyl/Dirac)
  "isbotlandi" deyilmaydi. Har bir yangi faraz tegishli PARAMETRLAR.md ga yoziladi.
- Doimiy hisobot fayli: `models/TUNGI_HISOBOT.md`. Har bir bosqichdan keyin yangilanadi: nima
  qilindi, natija (raqamlar bilan), maqola bilan solishtirish, qabul qilingan qarorlar, muammolar.

## Maqoladagi maqsadlar (Troyan & Doronin 2021, Fig. 1, f = 100 Hz)

a) Ugate = 0: faqat xotira effekti. V_SET ~ +3.5 V, V_RESET ~ -3.5 V, R_ON ~ 7 kOhm (0.43 mA, 3 V),
   R_OFF ~ 100 kOhm.
b) Ugate = -0.9 V: aralash rejim (qisman xotira + qisman volatil).
c) Ugate = -1.1 V: faqat volatil bo'sag'aviy qayta ulanish, tok ~10 marta kichik (~0.04 mA, 4 V).
d) Ugate -> 0: xotira effekti qaytadi (jarayon qaytar).

Bosim: xotira holatining R_ON qiymati 2-3 tartibga kamayadi, volatil holatniki deyarli o'zgarmaydi.

## 1-vazifa: Model1 T_amb sweep + N1-N6 (ustuvorlik: eng yuqori)

- S2 (300 K, 3 tsikl, 0-30 ms) natijasini saqlash: bu takrorlanuvchanlikning isboti.
- T_amb = 300/350/400/450 K, har biri 1 tsikl (0-10 ms). Buni asoslab PARAMETRLAR.md ga yozish.
- N1: I-V (chiziqli va log|I|), N3: x(t), V(t), I(t), N4: SET paytidagi T xaritasi + T_max(t)
  (Tm_GT = 998 K, Tm_ST = 891 K chiziqlari bilan), N5: SET dan oldin/keyin |E| va |J| xaritalari,
  N6: T_amb bo'yicha R_ON, R_OFF, V_SET, V_RESET, R_OFF/R_ON jadvali.
- Eksport: iv_ugate0.csv, iv_Tamb_sweep.csv, xt_cycle.csv, Tmax_t.csv, N6_table.csv + PNG (N1, N3,
  N4, N5).
- Kutilgan tendensiya: T_amb oshgan sari V_SET va |V_RESET| kamayadi. Boshqacha chiqsa, sababi
  tahlil qilinadi.

## 2-vazifa: Model2_FET.java (maqoladagi Fig. 1 a-d analogi, asosiy natija)

- Alohida mustaqil fayl, Model1 dagi ishlaydigan API chaqiruvlarini qayta ishlatib. 2D planar kesim:
  substrat (faraz: SiO2/Si, izolyator) / Sb2Te3 plyonka (20 nm) / GeTe (20 nm) / Al2O3 (10 nm,
  faraz) / zatvor elektrodi. Sb2Te3 ga ulangan ikki kontakt: source (Ground) va drain (Terminal,
  V(t)).
- Geometriya fazasi: lateral tok vdW interfeysidan ketma-ket o'tishi uchun, zatvor ostidagi Sb2Te3
  kanalida uzunligi L_gap (masalan 200 nm, parametr) bo'lgan "faol soha" bor. U interfeys materiali
  bilan to'ldirilgan, sigma = sig_off^(1-x)*sig_on^x. Bu fenomenologik faraz, PARAMETRLAR ga aniq
  yoziladi. Joul isishi Model1 kabi (T_max < Tm tekshiriladi).
- Zatvor ta'siri faqat fenomenologik: f(Ug) = 1/(1+exp(-(Ug-U0)/w)), U0 = -0.9[V], w = 0.08[V]
  (sigmoid: -0.9 V da aralash rejim, 0 da f ~ 1, -1.1 V da f ~ 0.08).
  tau_rel(Ug) = tau_v*(tau_nv/tau_v)^f (tau_nv = 1e3 s, tau_v = 1e-4 s),
  sig_on(Ug) = sig_on_v*(sig_on0/sig_on_v)^f.
  Maqsad: -1.1 V da I(4 V) ~ 0.04 mA. Buning uchun sig_on_v kalibrlanadi. Agar faqat sig_on bilan
  yetmasa, kanal o'tkazuvchanligi ham xuddi shunday sigmoid bilan kamaytiriladi va hisobotda
  asoslanadi. Sigmoid shakli hisobotda asoslanadi (maydon ta'sirida ferroelektrik
  barqarorlashtirishning bosqichma-bosqich bostirilishi).
- Avval sig_off va sig_on0 Ugate = 0 da R_OFF = 100 kOhm, R_ON = 7 kOhm ga kalibrlanadi (Model1
  dagi kabi).
- S2: Ugate = 0, -0.5, -0.9, -1.1, -1.5 V, har biri 1 tsikl. Qo'shimcha "Fig. 1d" hisobi: Ugate(t)
  bosqichli (0 -> -1.1 V -> 0 V, har biri 1 tsikl, 3 tsikl) - jarayon qaytarligini ko'rsatadi.
- N2: har bir Ugate uchun I-V (Fig. 1 a-d uslubida: tok mA da, V -4.5..4.5 V), 4 panelli PNG.
  Eksport: iv_ugate_sweep.csv, iv_fig1d_sequence.csv, N6 jadvali Ugate bo'yicha
  (FET_N6_table.csv).
- Ugate = 0 da xotira (gisterezis, OFF ga faqat teskari qutbda qaytadi), -1.1 V da volatil (V
  pasayganda o'zi OFF ga qaytadi) bo'lishi tekshiriladi. Bo'lmasa, tau_v, U0, w o'zgartiriladi
  (ko'pi bilan 5 urinish).

## 3-vazifa: Model3_Pressure.java (N7)

- Model1 geometriyasi + Solid Mechanics. Yuqori chegarada p = 0...2 GPa (masalan 0, 0.25, 0.5, 1,
  1.5, 2 GPa), pastki chegara mahkamlangan, yon devor roller. Ketma-ket yechim: avval Stationary
  Solid Mechanics, keyin Electric Currents.
- d(p) = t_int*(1+aveop_int(solid.eZZ)), sig_on(p) = sig_on0*exp(beta*(t_int-d)/t_int). Volatil
  holat uchun beta = 0.
- Elastik parametrlar parametr sifatida kiritiladi (tekshirilsin deb belgilab): E_GT ~ 50 GPa,
  E_ST ~ 55 GPa, E_TiN ~ 250 GPa, nu ~ 0.25. Interfeys (vdW bo'shliq) c-o'qi bo'yicha ancha yumshoq:
  E_int ~ 5-15 GPa (taxmin, tekshirilsin).
- beta R_ON(2 GPa) / R_ON(0) = 1e-2...1e-3 bo'ladigan qilib topiladi. Fizik reallik bahosi:
  beta*eps_zz ni tunnel ko'rinishida 2*kappa*Delta_d bilan solishtirish, kappa ~ 5-10 1/nm odatiy.
  Qaysi E_int da beta real bo'lishi hisobotda yoziladi.
- N7: R_ON(p) va R_OFF(p) log shkalada, xotira (beta > 0) va volatil (beta = 0) holatlar
  solishtirmasi. Eksport: Ron_p.csv + PNG.

## 4-vazifa (vaqt qolsa): N8 sezgirlik

Model1 da r_f, t_int, sig_on ni +-50% o'zgartirib R_ON, R_OFF ga ta'sirini Stationary bilan
hisoblash (tez). Vaqt qolsa, V_SET/V_RESET uchun ham 1 tsikl. Eksport: N8_sensitivity.csv.

## Yakun

`models/TUNGI_HISOBOT.md` da yakuniy xulosa:
- maqola bilan solishtirish jadvali (maqola qiymati / model qiymati / farq);
- barcha CSV va PNG fayllar ro'yxati;
- barcha farazlar;
- nima bajarilmadi va nima uchun;
- ertaga nima qilish kerak.

Oxirgi commit + push.
