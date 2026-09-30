# Avtonom vazifa: Kii & Nouchi (2025) MoS2/F4TCNQ FET natijasini COMSOL'da (comsolcompile) olish

Sen Windows kompyuterda PowerShell orqali ishlayotgan Claude Code'san. Foydalanuvchi uxlayapti. Hech kim
savollaringga javob bermaydi. **Savol berma, to'xtab qolma.** Qaror kerak bo'lsa, eng oqilona variantni tanla,
sababini `STATUS.md` ga yoz va davom et. Ertalab foydalanuvchi faqat `HISOBOT.md` ni o'qiydi.

## 0. Qat'iy qoidalar (buzish mumkin emas)

1. **Faqat `C:\comsol_tranzistor\` ichida ishla.** Bu papkadan tashqaridagi hech narsani o'chirma yoki o'zgartirma.
   Tizim sozlamalari, registr va dasturlar o'rnatish taqiqlanadi. Yagona istisno: `pip install --user` bilan
   numpy/scipy/matplotlib o'rnatish mumkin.
2. **Soxtalashtirish qat'iyan taqiqlanadi.** Hisobotdagi har bir "model" raqami haqiqiy COMSOL hisobidan
   olingan CSV'dan kelishi kerak. Maqola raqamlarini natija sifatida ko'chirish, egri chiziqni qo'lda chizish,
   natijani "tuzatish" mumkin emas. Maqsadga yetilmasa, "yetilmadi" deb, qancha farq qilganini yoz.
   Halol "92.5 o'rniga 71 V/V chiqdi" soxta "92.5 chiqdi"dan cheksiz qimmatli.
3. `ish\` papkasidagi `compact_model.py` natijalari COMSOL natijasi **emas**. Ular faqat boshlang'ich parametr
   uchun. Hisobotda ularni aralashtirma.
4. Har bir bosqich tugagach `STATUS.md` ni yangila (format quyida). Seans uzilib qolsa, keyingi seans
   shu fayldan davom etadi.
5. Bitta buyruq 10 daqiqadan uzoq ishlashi mumkin bo'lsa, uni **fonda** ishga tushir (log faylga yozadigan
   qilib) va logni vaqti-vaqti bilan tekshir. Yoki avval to'rni kichikroq qilib tez sinab ko'r.
6. Hech narsani o'chirishdan oldin nima ekanini tekshir. Faqat o'zing yaratgan vaqtinchalik fayllarni o'chir.

## 1. Kirish ma'lumotlari

- `C:\comsol_tranzistor\ish\` — ishchi papka. Unda:
  - `MoS2_F4TCNQ_COMSOL_qollanma.md` — maqola tahlili va COMSOL model tavsifi (4–7-qismlar). **Birinchi o'qi.**
  - `GEOMETRIYA.md`, `geometry_preview.png`, `geometry_preview.py` — 1a-rasm bo'yicha aniq geometriya
    (koordinatalar, domenlar, chegara shartlari, to'r). **Qo'llanmadagi soddalashtirilgan geometriya o'rniga shu ishlatiladi.**
  - `compact_model.py` — kalibrlash modeli. `DATA` lug'atida maqola rasmlaridan olingan raqamlar bor.
  - `extract_metrics.py` — COMSOL CSV'dan V_on, qiyalik, to'g'rilash va gisterezisni hisoblaydi.
- `C:\comsol_tranzistor\comsol_paths.txt` — `comsolcompile.exe` va `comsolbatch.exe` yo'llari
  (start skripti topib yozgan).
- `C:\comsol_tranzistor\repo\` — GitHub klon. Yakunda natijalarni shu yerga nusxalaysan.

Maqola: H. Kii, R. Nouchi, ACS Appl. Electron. Mater. 2025, 7, 5282. Qurilma: ko'p qatlamli MoS2,
285 nm SiO2 / p++ Si gate, Cr/Au kontaktlar, L = 5 um, W = 11.9–12.8 um, ustida F4TCNQ, havoda.
V_on = V_G(|I_D| = 1 nA).

**Muhit:** COMSOL Multiphysics **6.0**, barcha modullar litsenziyalangan (Semiconductor, Chemical Reaction
Engineering va boshqalar). Odatiy yo'l: `C:\Program Files\COMSOL\COMSOL60\Multiphysics\`
(`bin\win64\comsolcompile.exe`, `bin\win64\comsolbatch.exe`, `applications\`, `doc\`).
API nomlarini **6.0 versiyasining o'zidan** ol (B1). Boshqa versiyalar bo'yicha xotirangga tayanma.

## 2. Maqsad: qabul mezonlari (target)

Asosiy nishon: 4-rasm, F4TCNQ, yuqori va past namlik (bitta qurilma, W = 12.8 um). Ikkinchi nishon: 2-rasm.

| # | Kattalik | Maqola | Qabul chegarasi |
|---|---|---|---|
| T1 | V_on(V_D), yuqori RH, V_D = 0.2…1.0 V | −4, −22, −36, −61, −78 V | har nuqta ±6 V |
| T2 | dV_on/dV_D, yuqori RH | 92.5 V/V | ±10% |
| T3 | dV_on/dV_D, past RH | 5.3 V/V (V_on ≈ −51…−55 V) | ±2 V/V |
| T4 | \|I(+1)\|/\|I(−1)\|, V_G = 80 V, yuqori RH | 0.03 | 0.015–0.06 |
| T5 | gisterezis \|I(−1)\|: oldinga vs qaytish, yuqori RH | 87% | 75–95% |
| T6 | \|I(−1 V)\|, V_G = 80 V, yuqori RH | 1.6 uA | ±30% |
| T7 | 2-rasm: dV_on/dV_D = 46.3 V/V, to'g'rilash 0.37, gisterezis 32% | — | ±15% |
| T8 | Subporog qiyalik | ~25 V/dek (yuqori RH), ~12 V/dek (2-rasm) | ±30% |

Yuqori RH va past RH holatlari bir xil geometriya va bir xil kanal parametrlari bilan olinishi kerak. Faqat
ionlar bilan bog'liq parametrlar (beta, sigma_F4, Phi_B0) farq qilishi mumkin. Buni hisobotda ko'rsat.

## 3. Fizik model

**Geometriya: `GEOMETRIYA.md` bo'yicha, maqoladagi 1a-rasm bilan bir xil to'liq kesim.** Unda p++ Si gate,
SiO2 285 nm, MoS2, Cr 1 nm, pog'onali Au kontaktlar, F4TCNQ gumbazlari, suv plyonkasi va havo bor.
Koordinatalar, domen-fizika jadvali va to'r talablari o'sha faylda. Undan chetga chiqma. Birorta primitiv
ishlamasa, xuddi shu shaklni boshqa primitiv bilan qur va buni `STATUS.md` ga yoz.
**Taqiqlanadi:** oksidni Thin Insulator Gate bilan almashtirish, F4TCNQ yoki Cr/Au ni tashlab ketish.
Yaqinlashish yoki tezlik uchun soddalashtirilgan "yordamchi" model qilish mumkin, lekin yakuniy natija
to'liq geometriyada olinadi.

- Semiconductor interfeysi (`semi`), Finite Volume, Maxwell–Boltzmann statistikasi. MoS2 yarimo'tkazgich,
  SiO2/F4TCNQ/suv/havo izolyatorlar (Charge Conservation). Si, Cr va Au semi'ga kirmaydi.
- Gate: SiO2/Si chegarasida Gate Contact, V = VG. MoS2/SiO2 chegarasida **interfeys tuzoqlari D_it**.
  **D_it majburiy.** Busiz SS kichik bo'ladi va V_on siljishi bir necha voltdan oshmaydi.
  Maqsad SS ≈ 0.06·(1 + q·D_it/C_ox): D_it ~ 1e13–5e13 cm^-2 eV^-1.
- MoS2/Cr chegaralari (MoS2 ning ustki sirti va uchi): **Metal Contact, Schottky**, termoemissiya.
  Barer: source `Phi_B0 - x`, drain `Phi_B0 + x`. Izolyator/metall chegaralari: shu metallning potensiali.
  MoS2 ning suv/F4TCNQ bilan chegarasida F4TCNQ uchun `sigma_F4` sirt zaryadi.
- Out-of-plane thickness = W (tok amperda chiqsin, 1 nA mezoni to'g'ri ishlasin).
- Ionlar holati `x` [V]:
  - Stationary (transfer) da `x = beta*VD` (ionlar muvozanatda).
  - Time Dependent (chiqish) da Global ODE: `xt = (beta*VDt(t) - x)/tau_ion`.
    V_D(t) signali: −1 V da `t_hold` ushlab turish, keyin −1 → +1 → −1 (umumiy vaqt `t_sw`).
    **To'g'rilash va gisterezis faqat shu Time Dependent hisobdan olinadi**, Stationary ularni bermaydi.
- Boshlang'ich parametrlar: qo'llanmaning 4-qismidagi jadval va `python compact_model.py` natijasi
  (beta ≈ 0.22 V/V yuqori RH, ≈ 0.044 past RH; tau/t_sw ≈ 0.35; t_hold = 0.5·t_sw).

## 4. Bosqichlar ketma-ketligi

Har bir bosqichdan keyin **tekshiruv sharti** bajarilishi kerak. Bajarilmasa, tuzat. 3 urinishdan keyin ham
o'tmasa, sababini `STATUS.md` ga yoz va eng yaqin ishlaydigan variant bilan keyingi bosqichga o't.

**B0. Muhit (≤15 daqiqa).**
`comsol_paths.txt` ni o'qi. `comsolcompile` ishlashini tekshir: 5 qatorli test `.java` yozib, kompilyatsiya
qil va batch'da ishga tushir. Python va numpy/scipy/matplotlib ni tekshir. `python compact_model.py` ni ishga tushir.
*Shart:* test model ishga tushdi, `compact_model.py` jadval chiqardi.

**B1. Litsenziya va API'ni o'rganish (≤45 daqiqa). Bu eng muhim bosqich, taxmin qilma.**
1. Semiconductor Module ishlashini tekshir: test Java'da `physics().create("semi","Semiconductor","geom1")`.
   Litsenziya bor deb aytilgan. Baribir xato chiqsa, B1-fallbackka o't (quyida).
2. To'g'ri Java API nomlarini COMSOL'ning o'zidan ol. COMSOL papkasidagi `applications\Semiconductor_Module\`
   ichidan fayl nomida mosfet, schottky, trap yoki insulator bo'lgan `.mph` modellarni top. Kichik Java dastur
   bilan ularni `ModelUtil.load(...)` qil va `model.save("...\\api_namuna\\nomi.java")` bilan **.java qilib saqla**.
   Shu fayllardan quyidagilarning aniq tag, xossa va qiymat nomlarini ko'chir: Gate Contact (izolyatorga tegib
   turgan metall), Charge Conservation (izolyator domenlari), Polygon/Ellipse/Mirror/Difference/Intersection, Metal Contact
   (Schottky, barer balandligini berish usuli), interfeys tuzoqlari, Analytic Doping, Semiconductor Equilibrium
   study step, out-of-plane thickness, Global Equations.
3. Qo'shimcha manba: `doc\` papkasidagi Semiconductor Module User's Guide va Programming Reference PDF'lari.
*Shart:* `api_namuna\API_ESLATMA.md` faylida kerakli har bir feature uchun ishlatiladigan aniq Java qatorlari bor.

**B2. Minimal model (≤1.5 soat).**
`ish\model\MoS2Fet.java`. Tuzilishi `repo\comsol_java\GeTeSb2Te3_RON.java` ga o'xshash (`public static Model run()`).
Parametrlarni **`params.txt` dan o'qisin** (`nomi = qiymat`). Shunda kalibrlashda qayta kompilyatsiya kerak bo'lmaydi.
Rejim ham shu faylda beriladi: `mode = transfer | output`. Natija CSV'ga yoziladi
(`transfer.csv`: VD,VG,ID; `output.csv`: t,VD,ID) va `.mph` saqlanadi.
Geometriya `GEOMETRIYA.md` bo'yicha. Model geometriyani PNG qilib eksport qilsin (`natijalar\geom_full.png`,
y o'qi cho'zilgan; `natijalar\geom_source_edge.png`, x ∈ [−1.65, 0.25] um). Ularni `geometry_preview.png` bilan
solishtir. Farq bo'lsa, avval uni tuzat, keyin fizikaga o't.
Avval tuzoqlarsiz, x = 0 bilan: Equilibrium → VG = 80 V, VD = 0.1 V.
*Shart:* geometriya rasmlari `geometry_preview.png` ga mos, yaqinlashdi, I_D > 0, tok W ga proporsional.

**B3. Transfer sweep (≤1 soat).**
VG = 80 → −80 (continuation, 2 V qadam, yaqinlashmasa 1 V), VD = 0.2…1.0. `extract_metrics.py transfer` bilan V_on ni ol.
*Shart:* barcha VD uchun −80…80 V oralig'idagi egri chiziqlar va V_on qiymatlari bor.

**B4. Kalibrlash: SS va V_on darajasi (≤1.5 soat).**
Tartib: D_it → SS (T8); Phi_B0, sigma_F4, Nd → V_on(0.2 V); beta → qiyalik (T1, T2). Past RH uchun beta va Phi_B0 (T3).
Avtomatlashtir: `calibrate.py` params.txt ni yozadi, batch'ni chaqiradi, metrikani o'qiydi. Optimallashtirish
uchun scipy ishlat (Nelder-Mead yoki bitta-bitta bisection). Har bir urinishni `calib_log.csv` ga yoz.
*Shart:* T1, T2, T3, T8.

**B5. Chiqish xarakteristikasi, Time Dependent (≤1.5 soat).**
Global ODE `x`, `VDt(t)` Piecewise/Interpolation, VG = 80 V. Kanal harakatchanligi `mu_n` → T6.
`tau_ion`/`t_sw` va `t_hold` → T4, T5.
*Shart:* T4, T5, T6.

**B6. 2-rasm qurilmasi (≤1 soat, vaqt qolsa).** W = 11.9 um, o'z parametrlari → T7.

**B6b. B modeli (ixtiyoriy, faqat B0–B5 tayyor bo'lsa va ≥1.5 soat qolgan bo'lsa).**
Qo'llanmaning 6-qismi bo'yicha ishla: suv qatlami, H3O+/OH-, Transport of Diluted Species (migratsiya bilan,
Chemical Reaction Engineering litsenziyasi bor), Space Charge, `dPhi = sigma/C_H`. Maqsad: A modelidagi
`beta` va `tau` ni qaysi c0 va D bera olishini topish. Alohida faylda ishla (`MoS2FetIon.java`) va A modeliga tegma.
Natija hisobotning alohida bo'limiga yoziladi.

**B7. Grafiklar va hisobot (≤45 daqiqa, shart emas, lekin albatta bajariladi).**
`natijalar\` papkasiga matplotlib bilan quyidagi grafiklar:
- `fig4b_Von_vs_VD.png` — model chiziq, maqola nuqtalari; yuqori va past RH;
- `fig4a_transfer.png`, `fig4c_output.png` (oldinga va qaytish, maqoladagi kabi);
- `fig2b.png`, `fig2c.png` (B6 bajarilgan bo'lsa);
- `jadval.csv` — T1–T8: maqola, model, farq, o'tdi/o'tmadi.

`HISOBOT.md` (o'zbek tilida) quyidagilarni o'z ichiga oladi: nima olindi, T1–T8 jadvali, yakuniy parametrlar
(qaysi biri fizik, qaysi biri kalibrlangan), model cheklovlari, `.mph` faylni GUI'da qanday ochish, keyingi qadamlar.

**B8. Saqlash.**
`ish\model`, `ish\natijalar`, `HISOBOT.md`, `STATUS.md` ni `repo\comsol_tranzistor\natijalar\` ga nusxala.
Katta `.mph` fayllarni (>50 MB) nusxalama. Keyin `git add`, `git commit`, `git push`. Push ishlamasa,
faqat lokal commit qil va buni hisobotda yoz.

**Vaqt qoidasi.** B0–B5 asosiy ish. Taxminan 6 soatdan keyin yangi kalibrlashni boshlama:
B7–B8 ga o't. Eng yaxshi natija bilan hisobot yarim-tayyor natijasiz hisobotdan yaxshiroq.

**B1-fallback (Semiconductor Module yo'q bo'lsa).** Buni hisobotning birinchi qatorida yoz. Keyin kontakt
cheklagan FET modelini bazaviy COMSOL'da quraman: Electrostatics (gate/oksid/MoS2) + Global ODE (x) + kontakt
toki uchun analitik termoemissiya (Global Equations). Bu yarim-analitik model ekanini ochiq yoz.

## 5. Texnik maslahatlar

- Uzun hisoblarni fonda ishga tushir:
  `Start-Process -FilePath $batch -ArgumentList '-inputfile','MoS2Fet.class' -RedirectStandardOutput run.log -RedirectStandardError run.err -NoNewWindow -PassThru`
  So'ng logni kuzat.
- Yo'llarda bo'sh joy bo'lsa, qo'shtirnoq ishlat. Java ichida `/` yoki `\\` dan foydalan.
- Yaqinlashish muammolari: VG ni on-holatdan (80 V) pastga sweep qil; Equilibrium'dan boshla; tuzoqlarni
  0.1·D_it dan to'liq qiymatgacha parametr continuation bilan kirit; relative tolerance 1e-6;
  kontakt qirralarida to'rni zichlashtir. I ~ 1e-12 A da tok shovqinli bo'lsa, V_on ni 1 nA da interpolatsiya
  qilish yetarli.
- Model `run()` ichida bitta joyda qulasa ham CSV'ga yozilgan qismi saqlansin (try/catch).
- Hech qachon bir xil xatoni 3 martadan ko'p takrorlama. Yondashuvni o'zgartir.

## 6. STATUS.md formati (har bosqichdan keyin to'liq qayta yoz)

```
HOLAT: ISHLAYAPTI | YAKUNLANDI
Oxirgi yangilanish: <sana vaqt>
Bosqichlar: B0 [x] B1 [x] B2 [ ] ...
Hozirgi bosqich va keyingi aniq qadam: ...
Eng yaxshi natija hozircha: T1..T8 qisqacha
Muhim qarorlar va sabablari: ...
Hal qilinmagan muammolar: ...
```

Ish tugagach (yoki vaqt tugab B7–B8 bajarilgach) birinchi qatorni `HOLAT: YAKUNLANDI` qil.
Start skripti shu qatorni ko'rib to'xtaydi.

## 7. Boshlash va davom etish

**Kechagi seans (1-kun) to'liq tugamagan.** U ruxsat so'rab to'xtab qolgan va o'chirilgan. COMSOL faylida
geometriya "faqat to'g'ri chiziq" bo'lib ko'ringan. Buning ikki sababi bo'lishi mumkin: MoS2 20 nm × 7 um
bo'lib, sukut bo'yicha teng masshtabda chiziq ko'rinadi, yoki geometriya oxiriga yetmagan (`GEOMETRIYA.md`, 1-bo'lim).
Davom etishdan oldin:

1. `ish\` ichidagi hamma narsani ko'rib chiq: `STATUS.md` (bo'lsa), `api_namuna\`, `model\`, `*.java`,
   `*.mph`, `*.log`, `C:\comsol_tranzistor\logs\`. Hech narsani o'chirma.
2. Kechagi `.mph` ni tekshir: kichik Java dastur bilan yuklab, geometriyadagi domenlar soni, har birining
   bounding box'i, fizika featurelari ro'yxati va yechim bor-yo'qligini `natijalar\eski_model_diagnoz.txt` ga yoz.
   Qisqa xulosani `STATUS.md` ga "1-kun diagnozi" bo'limi qilib yoz.
3. Kechagi ishdan yaroqlisini qoldir (masalan, ishlagan API namunalari, `API_ESLATMA.md`).
   Geometriyani `GEOMETRIYA.md` bo'yicha **qaytadan** qur: kechagi soddalashtirilgan (faqat MoS2) geometriya
   endi yaroqsiz.
4. Keyin bosqichlarni tartib bilan davom ettir. B0 va B1 bajarilgan bo'lsa, ularni qisqa tekshiruv bilan
   o'tkazib yubor.

`STATUS.md` bo'lmasa, B0 dan boshla. Qo'llanmani va `GEOMETRIYA.md` ni o'qi va ishga kirish.
