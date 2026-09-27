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

## Natijalar (ishga tushirilgandan keyin to'ldiriladi)

- beta_mem (kalibrlangan): TEKSHIRILMOQDA / KUTILMOQDA
- R_ON(p), R_OFF(p) jadvali: TEKSHIRILMOQDA / KUTILMOQDA
- Fizik reallik xulosasi: TEKSHIRILMOQDA / KUTILMOQDA
