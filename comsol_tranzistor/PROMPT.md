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

## 6b. 3-kun: bulutdagi tekshiruvda topilgan ikki xato (AVVAL SHULARNI TUZAT)

Bulutdagi Claude 2-kun natijalarini (`repo\comsol_tranzistor\natijalar\`) tekshirdi. D_it yaqinlashmasligi
va juda kichik tok ikkita aniq sababdan kelgan. Ikkalasi ham `MoS2Fet.java` da.

**Xato 1: Schottky bareri umuman berilmagan, shuning uchun ion mexanizmi (x) modelga ulanmagan.**
`mc1/mc2` da `SpecifyBarrierHeight = "ideal"` qo'yilgan. Bu rejimda COMSOL barerni **metall chiqish ishi
`Phi`** dan hisoblaydi (Φ_B = Phi − χ), `Phi_B` xossasini esa **e'tiborsiz qoldiradi**. Dalil:
`api_namuna\dump_schottky_contact.txt` da `SpecifyBarrierHeight=[ideal]` bilan birga `Phi=[phim]` turibdi,
`Phi_B=[0.67]` esa shunchaki sukut qiymati. `Phi` berilmagani uchun sukut qiymati (~4.5 V) ishlagan:
barer ≈ 4.5 − 4.0 = 0.5 eV, `Phi_B0` va `x` esa hech narsaga ta'sir qilmagan. VG = 80 V dagi 1.2 nA tok ham
shunga mos keladi. Tuzatish:
```java
mc1.set("SpecifyBarrierHeight", "ideal");
mc1.set("Phi", "chi_mos + Phi_B0 - x");   // source
mc2.set("Phi", "chi_mos + Phi_B0 + x");   // drain
```
*Tekshiruv:* tuzoqlarsiz, VG = 80 V, VD = 0.1 V da `Phi_B0` = 0.15 / 0.25 / 0.35 V bilan uch marta hisobla.
I_D har 0.1 V da taxminan exp(0.1/0.0259) ≈ 50 marta o'zgarishi kerak (kanal cheklamaguncha). O'zgarmasa,
`Phi` ifodasi ishlamayapti: `SpecifyBarrierHeight` ning boshqa qiymatlarini hujjatdan top va sina.

**Xato 2: tuzoqlar modeli ham fizik jihatdan noto'g'ri, ham sonli jihatdan eng og'ir variant.**
`ContinuousEnergyLevelsBoundary` moscap_1d_interface_traps namunasidan ko'chirilgan: faqat **donor**,
midgap atrofida **0.3 eV kenglikdagi tor** to'rtburchak. Namunada Nss = 2e11, bizda 3e13, ya'ni 150 marta
ko'p. Natijada 9e12 cm^-2 zaryad Fermi sathi 0.3 eV oynadan o'tganda to'satdan almashadi. Newton aynan shu
yerda "muzlaydi". Buning ustiga bu xususiyat qo'shimcha energiya o'lchamini yaratadi (xdim, Edisc = 25),
bu masalani yanada og'irlashtiradi. Tor oyna tashqarisida esa tuzoq yo'q, ya'ni SS ≈ 25 V/dek
butun V_G oralig'ida chiqmaydi.
Maqolaga kerak bo'lgan fizika: **tezkor interfeys holatlari, butun zona bo'yicha bir tekis D_it**. Ularning
statik zaryadi Fermi sathiga **chiziqli** bog'liq:
    Q_it = −q · D_it · (E_F − E_0)   (sirtda; E_0 — neytrallik sathi, midgap + dE0)
Bu m = 1 + q·D_it/C_ox ni aynan beradi va yaqinlashishi oson. Tuzatish: `tasr1/ctb1` ni o'chir va
MoS2/SiO2 chegarasiga (`sel_mos_sio2`) **SurfaceChargeDensity** qo'y:
```java
sfit.set("rhoqs", "-e_const*Dit*Dit_scale*(EFS - EMID - dE0)");
```
- `EFS`, `EMID` — sirtdagi elektron kvazi-Fermi sathi va midgap (Ec+Ev)/2. semi'dagi aniq nomlarini va
  **birliklarini** (V yoki J) o'zing aniqla. Buning uchun ifodani bitta nuqtada baholab ko'r (`semi.Efn`,
  `semi.Ec`, `semi.Ev` yoki 6.0 dagi nomlari). Birlik J bo'lsa, `e_const` ga bo'l. rhoqs birligi C/m^2 chiqishi kerak.
- `dE0` — yangi kalibrlanadigan parametr (sukut 0 V). U V_on darajasini siljitadi, `Phi_Si` va `sigma_F4` bilan birga.
- Ishorani tekshir: E_F yuqoriga (akkumulyatsiyaga) chiqsa, Q_it manfiy bo'lishi kerak (akseptorga o'xshash).
- `Dit_scale` ni 0 → 0.1 → 0.3 → 1 qilib continuation bilan kirit (VG = 80 V da, keyin VG sweep).
- Bu tuzoqlar dinamikasini tashlab yuboradi. Bu to'g'ri: gisterezisni modelda ionlar (x, Global ODE) beradi,
  tuzoqlar emas. Buni hisobotda yoz.
*Tekshiruv:* tuzoqlarsiz va D_it = 3e13 bilan SS ni o'lcha. SS ≈ 0.06·(1 + q·D_it/C_ox) V/dek bo'lishi kerak
(3e13 da ≈ 24 V/dek).

**3-tekshiruv: V_on barerga bog'liqmi? (eng muhim fizik test).**
D_it bilan, VD = 0.5 V da x = 0 va x = 0.05 V uchun V_on ni hisobla. Kutilgan natija: ΔV_on ≈ −m·0.05 V
(m ≈ 400 da ≈ −20 V). ΔV_on ≈ 0 chiqsa, 1 nA darajasida tokni kontakt emas, kanal cheklayapti:
termoemissiyali kontaktda subporog tokni kanal bareri belgilaydi. U holda tartib bilan sina:
(a) Metal Contact'da tunnellash: `extraElectronCurrent` xossasining 6.0 dagi variantlarini hujjatdan top
    (WKB tunnellash bo'lsa, yoq);
(b) `Nd_mos` ni oshir yoki `dE0`/`Phi_Si` ni o'zgartir, shunda kanal "normally on" bo'ladi (maqolada shunday)
    va subporogda kontakt bareri cheklaydi.
Natijani va ΔV_on/Δx nisbatini `STATUS.md` ga yoz. Bu nisbat β ni qayta hisoblash uchun kerak:
beta_COMSOL = 92.5 / |ΔV_on/Δx|.

Uchala tekshiruvdan keyin B3 → B8 ni PROMPT bo'yicha davom ettir. 2-kundagi sinov loglarini o'chirma.
Yangi loglar `run_d3_*.log` deb nomlansin.

## 6c. 4-kun: bulutdagi tekshiruv xulosalari (AVVAL SHULARNI QIL)

3-kun natijasi: barer tuzatildi va ishlayapti (I_D endi µA darajasida, `Phi_B0` ga bog'liq). Ammo ikki
muammo qoldi: (a) D_it bilan VG sweep yaqinlashmaydi; (b) D_it'siz ham VG < 0 da yaqinlashmaydi.
Ikkalasining sababi va tuzatishi quyida. 3-kundagi vaqtinchalik yechimlarni olib tashla, ular muammoni
yashiradi, hal qilmaydi.

**C1. Solver "tuzatishlarini" qaytar.** `reserrfact = 1e8`, `initstep = 0.01`, RETRY va
`clearSolutionData` orqali o'tkazish olib tashlansin. Ular yaqinlashmagan holatni "qabul qilingan"
yechimga aylantiradi. Shu sabab parametr o'zgarmasa ham qayta yechish yiqiladi: boshlang'ich nuqta
aslida yechim emas. Solver sozlamalari sukut qiymatlarida qolsin.

**C2. D_it ni elektr potensialiga chiziqli bog'la (Efn va tanh emas).**
`semi.Efn`, `semi.Ec`, `semi.Ev` hosilaviy kattaliklar. Kambag'allashgan sohada Efn deyarli aniqlanmaydi.
Ularga bog'liq manba Newton Jakobianini noaniq qiladi. tanh esa yana tor oynani qaytaradi
(±Ew0 = ±0.3 V dan keyin D_it yo'qoladi va SS butun oraliqda chiqmaydi). To'g'ri va standart yaqinlash:
tezkor interfeys holatlari kichik V_D da kanal potensialiga chiziqli zaryad beradi.
```java
sfit.set("rhoqs", "-e_const^2*Dit*(V - V_it0)");   // V — semi'ning bog'liq o'zgaruvchisi (potensial)
```
- `e_const^2*Dit` = q²·D_it, ya'ni C_it (3e13 cm^-2 eV^-1 da ≈ 4.8 µF/cm²). COMSOL birlik ogohlantirishi
  bermasligi kerak: rhoqs = C/m^2. Birlikni bir nuqtada baholab tekshir.
- `V_it0` — neytrallik potensiali (kalibrlanadigan, `dE0` o'rnini bosadi). Boshlang'ich qiymat: Equilibrium'da
  VG = 0 bo'lgandagi MoS2/SiO2 sirtidagi V (o'lchab ol).
- Bu manba bog'liq o'zgaruvchiga **chiziqli**: Jakobian aniq, ramp kerak emas, D_it ni birdaniga to'liq qo'yish
  mumkin. Ishonch uchun 0 → 1 ni 3 qadamda native continuation bilan qilsa ham bo'ladi.
- Cheklov (hisobotda yoz): drenaj yaqinida Efn V_D ga pasayadi, model buni hisobga olmaydi. V_D ≤ 1 V va
  subporog uchun xato kichik.

**C3. VG sweepni COMSOL'ning o'z continuation'i bilan bitta studiyada qil.**
Java siklida har VG uchun `run()` chaqirma. Bitta Stationary study step'da **Auxiliary sweep**
`VG = range(80,-2,-80)` qo'y, "Use continuation" yoqilsin. Kerak bo'lsa VD ham shu sweepda (VD tashqi tsikl).
Yechimdan CSV'ni Global Evaluation (barcha parametr qiymatlari) bilan bir marta ol.

**C4. Chuqur subporog (VG < 0) uchun formulyatsiya.**
Kam tashuvchili sohada log formulyatsiya mustahkamroq. Tartib:
(1) semi → Discretization: **Finite element (log formulation)**, tuzoqlarsiz VG = 80 → −80 sinov;
(2) yiqilsa, Finite volume + quasi-Fermi-level formulyatsiyasi (6.0 da bo'lsa);
(3) Stationary solver'da "Relative tolerance" = 1e-4 (sukut), tokni Terminal orqali ol.
Qaysi biri −80 V gacha o'tganini STATUS.md ga yoz va o'shani qoldir.

**C5. Kanal "normally-on" bo'lsin (maqolaga mos).** Hozir Nd = 1e17 da 20 nm MoS2 VG ≈ 0 da kambag'allashadi.
Maqolada esa bare qurilma V_on ≈ −70 V. Kerakli zaryad ≈ C_ox·70 V / q ≈ 5e12 cm^-2, ya'ni 20 nm uchun
Nd ≈ 2–3e18 cm^-3. `Nd_mos` boshlang'ich qiymati 2.5e18 bo'lsin, B4 da kalibrlanadi.

**C6. Tartib va tekshiruvlar.**
1. C1 + C4: D_it'siz, Nd = 2.5e18, VD = 0.2 V, VG 80 → −80 to'liq sweep. *Shart:* −80 V gacha o'tdi, tok
   kamida 4 dekada pasaydi.
2. C2 + C3: D_it = 3e13 bilan xuddi shu sweep. *Shart:* −80 V gacha o'tdi, SS ≈ 0.06·(1 + C_it/C_ox) ± 30%.
   O'tmasa, D_it ni 1e12 → 3e12 → 1e13 → 3e13 sweep qilib, qaysi qiymatda yiqilishini yoz.
3. 6b dagi 3-tekshiruv (ΔV_on/Δx, VD = 0.5 V, x = 0 va 0.05 V). Undan beta_COMSOL = 92.5/|ΔV_on/Δx|.
4. Keyin B3 → B8.
Yangi loglar `run_d4_*.log`. Har qadam natijasini STATUS.md ga yoz.

## 6d. 5-kun: bulutdagi tekshiruv xulosalari (AVVAL SHULARNI QIL)

4-kun natijasi: D_it = 3e13 bilan to'liq VG sweep yaqinlashdi, bu katta yutuq. Lekin `transfer_b3_full.csv` da
I_D butun −80…+80 V oralig'ida µA darajasida qoladi (VD = 0.2 V da 3.1 → 6.9 µA). Ya'ni kanal hech qachon
yopilmaydi, subporog soha umuman yo'q. "SS = 470 V/dek" subporog qiyaligi emas: bu to'liq ochiq kanal tokining
sekin o'zgarishi. Sabablari va tuzatish:

**Sabab 1: kanalni yopib bo'lmaydi.** Nd = 2.5e18 da 20 nm MoS2 ni to'liq kambag'allashtirish uchun sirt
potensiali taxminan 1.4 V o'zgarishi kerak (W_dep = sqrt(2·ε·ψ/(q·Nd)) ≈ 12 nm ψ = 0.5 V da). C_it/C_ox ≈ 400
bo'lganda ±80 V gate potensialni faqat ~±0.2 V ga siljitadi. Demak yopilish fizik jihatdan imkonsiz.
**Sabab 2: `V_it0` muvozanatdagi (VG = 0) sirt potensialiga teng qilib o'lchangan.** Shuning uchun Fermi sathi
aynan kanal to'liq ochiq bo'lgan nuqtada "qadalib" qolgan.

**D1. Nd ni kamaytir, lekin 400 marta emas, fizik mezon bilan.** 20 nm qatlam ~0.2–0.3 V da to'liq
kambag'allashishi kerak: Nd ≤ 2·ε·ψ/(q·t_mos²) ≈ 3–5e17 (ψ = 0.15–0.25 V). Zaxira bilan `Nd_mos = 5e16` dan boshla.
**D2. `V_it0` ni o'lchama, kalibrlanadigan parametr qil.** V_on ≈ const + m·(ψ_on − V_it0), m ≈ 400, ya'ni
V_it0 ning 0.01 V ga o'zgarishi V_on ni ~4 V ga siljitadi. V_it0 ni 0.02 V qadam bilan sweep qilib (VD = 0.2 V),
V_on = V_G(1 nA) ni −80…+80 ichiga olib kir. Maqsad 4b: V_on(0.2 V) ≈ −4 V (yuqori RH).
`sigma_F4` va `Phi_Si` ham V_on ni siljitadi. Avval faqat V_it0 bilan ishla.
*Shart:* tok 1 nA dan pastga tushadi va SS (1e-10…1e-8 A oralig'ida) ≈ 0.06·(1 + C_it/C_ox) ≈ 24 V/dek ±30%.

**D3. Eng muhim fizik test: V_on ionlarga (x) bog'liqmi?** (4-kunda 2% chiqqan, chunki subporog yo'q edi.)
D2 dan keyin VD = 0.5 V da x = 0 va x = 0.05 V bilan V_on ni hisobla. Kutilgan natija: ΔV_on ≈ −m·0.05 ≈ −20 V.
**Diqqat, oldindan bilib qo'y:** faqat termoemissiyali Schottky shartida bu test yana ~0 chiqishi mumkin.
Elektronlar yo'ldagi eng baland barerdan o'tishi kerak. Subporogda bu kanal bareri (gate boshqaradi),
kontakt bareri esa past qoladi. Kontakt bareri baland bo'lsa, tok gate'ga bog'liq bo'lmay qoladi. Maqoladagi
Schottky-barerli FET mexanizmida gate barerni **yupqalashtiradi** va elektronlar **tunnel** orqali o'tadi
(maqola, 3-rasm, "barrier narrowing"). Shuning uchun:
**D4. Kontaktga tunnellash qo'sh.** `mc1/mc2` dagi `extraElectronCurrent` xossasining COMSOL 6.0 dagi ruxsat
etilgan qiymatlarini aniqla. Masalan, ataylab noto'g'ri qiymat berib xato xabaridagi ro'yxatni o'qi yoki
Semiconductor Module User's Guide'dagi "Metal Contact"/"Schottky"/"tunneling" bo'limidan top. WKB yoki
tunnellash varianti bo'lsa, yoq va D3 ni takrorla. Tunnellash bilan `Phi_B0` ni 0.3–0.5 V atrofida qayta tanla.
**D5. Tunnellash opsiyasi bo'lmasa: fenomenologik TFE.** Barerni kontakt qirrasidagi normal maydonga bog'la:
`Phi = chi_mos + Phi_B0 - x - alpha_t*En_c`. Bunda `En_c` source qirrasi yonidagi (5–10 nm) MoS2 ichidagi
o'rtacha |E| (integration operator), `alpha_t` [m] kalibrlanadi. Bu kompakt modeldagi "gate barerni
yupqalashtiradi" farazining COMSOL'dagi ekvivalenti. Hisobotda uni fenomenologik deb aniq yoz.
Yaqinlashish qiyin bo'lsa, `alpha_t` ni continuation bilan kirit.

**Tartib:** D1 → D2 (SS shartigacha) → D3 → (kerak bo'lsa D4, keyin D5) → D3 qayta → B4 (beta = 92.5/|ΔV_on/Δx|,
past RH, T1–T3, T8) → B5 → B7 → B8. Loglar `run_d5_*.log`. Har qadamni STATUS.md ga yoz va har bosqich oxirida push qil.

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
