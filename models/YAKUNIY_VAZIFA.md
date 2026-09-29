# YAKUNIY TUNGI AVTONOM REJIM (~9 soat) - topshiriq matni

Foydalanuvchi tomonidan 2026-09-29 boshida berilgan, o'zgarishsiz saqlangan topshiriq.
Foydalanuvchi ertalabgacha kompyuterga tegmaydi. Savol berilmaydi, barcha qarorlar
mustaqil qabul qilinadi va hisobotga yoziladi.

Oldingi kontekst (o'qib chiqilishi shart): `TUNGI_HISOBOT.md`, `HISOBOT_FIG1.md`,
`Model2_PARAMETRLAR.md`, `fig1_targets.csv`.

## UMUMIY QOIDALAR

- Faqat C:\comsol_ish ichida ishlash. COMSOL prefs va tizim fayllariga tegilmaydi.
- Kompyuterda 8 GB RAM bor. Bir vaqtda faqat BITTA comsolbatch, doim `-np 2` bilan.
  Time Dependent da faqat global qiymatlarni zich saqlash, maydonlarni esa faqat
  kerakli bir necha vaqtda. Jarayon xotira yetmasligidan o'lsa: to'rni siyraklashtirish
  yoki hisobni bo'laklarga bo'lish (holatni keyingi bo'lakka o'tkazib). Tolerantlik
  bo'shatilmaydi.
- Uzoq hisoblar fonda ishga tushiriladi, log har 3-5 daqiqada tekshiriladi.
- Bitta muammoga ko'pi bilan 6 urinish. Yechilmasa, hisobotga halol yoziladi va
  keyingi bosqichga o'tiladi. HECH QACHON to'xtab qolinmaydi.
- Har bir ishlaydigan natijadan keyin commit + git push origin claude/trusting-noether-ksc9q6.
  Push o'tmasa, lokal commit bilan davom etiladi.
- Eng yaxshi natija hech qachon yo'qotilmaydi: har bir yangi variantdan oldin eng
  yaxshisining nusxasi saqlanadi (Fig1_best.png, Model2_FET_Fig1_best.java). Yangi
  variant yomonroq chiqsa, qaytariladi.
- Barcha izoh va hisobotlar o'zbek tilida. Model fenomenologik: Weyl/Dirac fazalarini
  "isbotlandi" deyilmaydi. Har bir yangi faraz va parametr tegishli PARAMETRLAR.md
  ga asosi bilan yoziladi.
- Doimiy jurnal: `models/YAKUNIY_HISOBOT.md` (har bosqichdan keyin yangilanadi).

## 1-BOSQICH (eng muhim, ~4 soat): Fig. 1 a-d ni maqolaga yaqinlashtirish

Maqsadlar `fig1_targets.csv` da. Hozirgi eng yaxshi: 7-iteratsiya. (c) yaxshi, uni
buzmaslik kerak.

### 1.1. (d) SIFAT JIHATDAN NOTO'G'RI

Maqolada (d) - XOTIRA. ON tarmoq ikkala qutbda chiziqli: +3.7 V da 0.057 mA, -4 V
da -0.05 mA, RESET faqat ~-4 V da. Modelda esa volatil halqa. Tuzatish: `tau_rel`
va `sig_on` ni P dan ALOHIDA bog'lash.

```
tau_rel(P) = tau_v + (tau_nv - tau_v)*S((P-P_c)/w_P)
```

S - keskin sigmoid, P_c ~ 0.18-0.2: (c) da P ~ 0.14 volatil, (d) da P ~ 0.25 xotira.
`sig_on(P)` log-interpolyatsiya bo'lib qoladi, (d) da R_ON ~ 70 kOhm. Tekshirish:
(d) da V = 0 dan o'tganda x > 0.5 va manfiy ON tarmoq bor.

### 1.2. (a), (b) da RESET keskin emas

ON tarmoq -3 V da qayriladi. Maqolada ON chiziq -3.5 V gacha to'g'ri (-0.45 mA),
keyin keskin sakrash. Tuzatish: samarali zaryad `z_eff` kiritish (sinh argumentida
q -> z_eff*q, fenomenologik: ko'p zaryadli ion yoki klaster migratsiyasi).
`d(ln rate)/dV ~ 12-15 1/V` bo'lsin, keyin k0/Ea qayta moslanib V_SET = +3.5 V,
V_RESET = -3.6...-3.7 V saqlansin. x 0.9 -> 0.1 o'tishi dV < 0.2 V ichida. (b) da
manfiy ON tarmoq -3.7 V gacha (-0.34 mA) chiziqli bo'lsin.

### 1.3. Ixtiyoriy

(b) dagi kichik volatil tarmoq (2...3.7 V, 0.09-0.22 mA). Bo'lmasa, sababi yoziladi.

### 1.4. Ballash

Har iteratsiyadan keyin sonli ball hisoblanadi: `fig1_targets.csv` dagi har bir
ko'rsatkich uchun |farq| < 20% bo'lsa 1 ball, sifat mezonlari (xotira/volatil,
qutb asimmetriyasi, keskin sakrash) uchun ham 1 ball. Eng yuqori ballli variant
"best" sifatida saqlanadi. Ko'pi bilan 10 iteratsiya.

### 1.5. Yakuniy rasmlar

`Fig1_analog.png`: 2x2 panel, maqoladagidek masshtab va ranglar (OFF ko'k, ON to'q
sariq, markerlar, maqola nuqtalari kulrang). Qo'shimcha `Fig1_side_by_side.png`:
har bir panel uchun model va maqola nuqtalari bitta o'qda, katta shrift bilan.

## 2-BOSQICH (~1.5 soat): bosim natijasi (maqoladagi 2-asosiy da'vo)

- Model3_Pressure natijalari (Ron_p.csv) asosida `Fig_pressure.png`: R_ON(p) log
  shkalada, xotira (beta > 0) va volatil (beta = 0) holatlar, R_OFF(p) bilan.
  Maqola da'vosi: xotira R_ON 2-3 tartibga kamayadi, volatil deyarli o'zgarmaydi.
- Agar 1-bosqichda Model2 kinetikasi o'zgargan bo'lsa ham, Model3 statsionar
  bo'lgani uchun uni qayta hisoblash shart emas. Faqat parametr mosligini
  tekshirish va yozish kifoya.

## 3-BOSQICH (~1.5 soat): MPH fayllar va tartib

- Har bir yakuniy model (.mph) yechimlar bilan saqlansin va barcha plot grouplar
  saqlashdan oldin run() qilinsin: GUI da ochilganda grafiklar darhol ko'rinsin.
  .mph hajmi tekshiriladi.
- Vaqt yetsa: bitta Troyan_Doronin_All.mph (3 komponent) yaratiladi. Yetmasa,
  sababi yoziladi.
- Barcha yakuniy natijalar `models/YAKUNIY/` papkasiga yig'iladi: Fig1_analog.png,
  Fig1_side_by_side.png, Fig_pressure.png, asosiy CSV lar, yakuniy .java fayllar
  va (hajmi 50 MB dan kichik bo'lsa) .mph lar. Eski/oraliq fayllar o'chirilmaydi,
  faqat yakuniylari nusxalanadi.

## 4-BOSQICH (qolgan vaqt): dissertatsiya uchun yakuniy hisobot

`models/YAKUNIY/NATIJALAR.md` (o'zbek tilida, ilmiy uslubda):

1. Model tavsifi: geometriya, fizika, tenglamalar (sigma_int, dx/dt, dP/dt, OFF
   nochiziqlilik, bosim bog'liqligi).
2. Parametrlar jadvali: nomi, qiymati, birligi, manbasi yoki "taxmin/fitting".
3. Fig. 1 a-d solishtirish jadvali: maqola / model / farq %, ball.
4. Bosim natijasi.
5. Qo'shimcha natijalar: T_amb bo'yicha, Joul isishi (T_max), sezgirlik N8.
6. Barcha farazlar va cheklovlar (geometriya o'lchamlari nega shunday: Joul
   isishi, L_gap = 1.5 nm va h.k.).
7. Maqola bilan mos kelmagan joylar va ularning ehtimoliy sabablari (halol).
8. Fayllar ro'yxati: qaysi rasm qaysi natija.

Oxirida commit + push va konsolga qisqa yakuniy xulosa.
