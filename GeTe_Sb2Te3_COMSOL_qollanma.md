# GeTe/Sb₂Te₃ yupqa plyonkali xotira elementi: maqola tahlili va COMSOL Multiphysics'da modellashtirish

**Maqola:** E. Troyan, A. Doronin, *"Change of GeTe/Sb₂Te₃ Thin-Film Memory Elements Resistance R_ON Under External Pressure"*,
ICCS 2020, LNNS 186, pp. 427–433, Springer, 2021. DOI: 10.1007/978-3-030-66093-2_41

---

## 1-qism. Maqolaning chuqur tahlili

### 1.1. Maqola nima haqida

Ge₂Sb₂Te₅ (GST) faza o'zgaruvchi material (PCM). U PC-RAM xotirada ishlatiladi. Klassik tushuntirish shunday:
elektr impulsi materialni **eritadi va tez sovitadi** (melt-quench). Natijada material amorf (yuqori qarshilik, OFF)
va kristall (past qarshilik, ON) holatlar orasida o'tadi.

Mualliflar bu **issiqlik modelini rad etadi** va boshqa, **interfeys + topologik** modelni taklif qiladi:

| Qadam | Muallif da'vosi |
|---|---|
| 1 | Amorf GST aslida nano-qatlamlardan iborat: `[GeTe]₂[Sb₂Te₃]₁`, ya'ni "Petrov strukturasi" (1968). |
| 2 | Shuning uchun **bitta GeTe/Sb₂Te₃ interfeysi** (MBE bilan o'stirilgan) yetarli. Undan barqaror xotira elementi hosil bo'ladi. |
| 3 | Kalit **qutbga bog'liq** (asimmetrik VAX). ON holatga faqat Sb₂Te₃ tomonidagi elektrodda manfiy kuchlanish bo'lganda o'tadi. OFF holatga qaytish uchun qutbni almashtirish kerak. |
| 4 | U_th kuchlanishda ionlar qutbli elektromigratsiya qiladi. Van der Vaals (vdW) tirqishida yuqori tartibli **GeSbTe₄ superstrukturasi** hosil bo'ladi. Bu o'tkazuvchan "filament". |
| 5 | Filament ikki xil blokdan iborat: **GeTe₂** (ferroelektrik, inversiya simmetriyasini buzadi) va **SbTe₂** (bandlar inversiyasi). |
| 6 | ON holat **3D Veyl yarimmetalli** (WSM) deb talqin qilinadi. O'tkazuvchanlik 10⁵ martagacha va undan ko'p ortadi. Mualliflar buni qarama-qarshi xirallikka ega Fermi yoylaridagi konstruktiv interferensiya bilan tushuntiradi. |
| 7 | **R_ON Fermi yoylarining uzunligiga bog'liq**. Uzunlik esa GeSbTe₄ qatlami qalinligi `q` ga bog'liq. Demak **bosim qalinlikni o'zgartiradi va R_ON 2–3 tartibga kamayadi**. |
| 8 | 3 elektrodli (FET) strukturada gate GeTe ustida joylashgan. Gate'dagi kuchlanish U ≈ −1.1…−1.5 V bo'lganda **xotira effekti ostona kalitlash** (threshold switching) effektiga o'tadi. Bu o'tish qaytar (1-rasm). |
| 9 | Kalitlash holati: Veyl tugunlari birlashib **Dirak nuqtalari** hosil qiladi (2D topologik izolyator). Interferensiya destruktiv bo'ladi va R_ON 1–2 tartibga oshadi. Holat faqat tashqi maydon bilan ushlab turiladi (volatil). Bu holatda **R_ON bosimga deyarli bog'liq emas**. |

**Asosiy eksperimental faktlar** (maqoladagi yagona raqamli ma'lumot):
- 1-rasm: FET'da VAX, f = 100 Hz. U_gate = 0 V bo'lsa faqat xotira; −0.9 V bo'lsa aralash; −1.1 V bo'lsa faqat ostona kalitlash; 0 V ga qaytganda yana xotira.
- Kichik bosimda xotira elementining R_ON qiymati 2–3 tartibga kamayadi. Kalitlash elementining R_ON qiymati deyarli o'zgarmaydi. Bu faqat matnda aytilgan, grafik yo'q.

### 1.2. Ilmiy kontekst

- GeTe/Sb₂Te₃ superpanjaralari (**iPCM**, *interfacial phase-change memory*) haqiqiy va faol soha. Simpson va boshq., *Nature Nanotech.* 2011 ishida iPCM kam energiya sarflashi ko'rsatilgan. Mualliflar ishlatgan asosiy nazariy ish Kim & Kioussis, *PRB* 96, 235304 (2017) [14]. Unda aynan "Veyl tugunlari yordamida o'tkazuvchanlikni kalitlash" DFT bilan hisoblangan.
- Sb₂Te₃ haqiqatan 3D topologik izolyator. GeTe haqiqatan ferroelektrik (T_C ≈ 630–700 K).
- Demak maqolaning **g'oyasi** mavjud nazariyaga tayanadi. Lekin maqolaning o'zi bu g'oyani **isbotlamaydi**, faqat taklif qiladi.

### 1.3. Tanqidiy baholash

**Kuchli tomonlari**
1. Qutbga bog'liq asimmetrik VAX va gate yordamida xotira ↔ kalitlash o'tishi qiziq eksperimental kuzatuv. Ikki effekt uchun yagona mexanizmni ko'rsatadi.
2. Tekshirsa bo'ladigan bashorat bor: xotira holatida R_ON bosimga sezgir, kalitlash holatida esa sezgir emas.
3. Bu ish PCM'ni topologik fizika bilan bog'laydi (spintronika, topologik o'ta o'tkazuvchanlik).

**Zaif tomonlari va savollar**
1. **Sarlavha va mazmun mos kelmaydi.** Sarlavha bosim haqida, lekin maqolada **bosim bo'yicha birorta raqam yo'q**: bosim qiymati (MPa? GPa?), qanday berilgani (zond, gidrostatik, DAC), R_ON(p) grafigi, namunalar soni. Faqat "2–3 tartib" degan bitta jumla bor.
2. **O'z hisob-kitobi yo'q.** Veyl/Dirak, Fermi yoylari va bandlar inversiyasi haqidagi barcha da'volar boshqa ishlardan ([14], [11], [19]) olingan. Mualliflarning o'zi ham "we speculate (assume)" deb yozadi.
3. **Strukturaviy dalil yo'q.** GeSbTe₄ filamenti hosil bo'lganini ko'rsatadigan TEM, XRD, Raman yoki ARPES natijalari keltirilmagan.
4. **Qurilma parametrlari yo'q.** Plyonka qalinliklari, elektrod maydoni, gate dielektrigi, harorat va takrorlanuvchanlik statistikasi berilmagan.
5. **Issiqlik modeli haqiqatda tekshirilmagan.** 100 Hz VAX o'lchashda Joule qizishi muhim bo'lishi mumkin, lekin harorat o'lchanmagan yoki hisoblanmagan.
6. **Fizik talqin nostandart.** Bir necha nm qalinlikdagi filamentda "3D Veyl" fazasi va "Veyl tugunlaridagi konstruktiv interferensiya" o'tkazuvchanlik mexanizmi sifatida yaxshi asoslanmagan. Metodika uchun patentlarga havola qilinadi, ular esa taqriz qilinmagan manba.
7. **Mayda xatolar.** "GST 2017 yildan beri ishlatiladi" noaniq: GST optik disklarda 1990-yillardan beri ishlatiladi. Familiyalarda xatolar bor: Lontyk → Lotnyk, Simson → Simpson, Kolobow → Kolobov, Grenberg → Greenberg.

**Miqdoriy tekshiruv: "kichik bosim 2–3 tartib" realmi?**

Filament qalinligi t_f ≈ 3 nm deb olaylik. Qatlam vdW tufayli yumshoq, effektiv moduli M ≈ 20 GPa.
Agar o'tkazuvchanlik qalinlikka eksponensial bog'liq bo'lsa, σ ∝ exp(−t/λ), λ ≈ 0.05 nm (tunnel turi, κ ≈ 1 Å⁻¹):

| p | ε = p/M | Δt = ε·t_f | R_ON ning o'zgarishi, exp(Δt/λ) |
|---|---|---|---|
| 100 MPa | 0.5 % | 0.015 nm | ×1.35 |
| 500 MPa | 2.5 % | 0.075 nm | ×4.5 |
| 1.5 GPa | 7.5 % | 0.23 nm | ×100 (2 tartib) |

Xulosa: bir jinsli siqilish bilan 2–3 tartibga erishish uchun **GPa darajasidagi bosim** kerak. Agar bosim "kichik" bo'lsa, boshqa
mexanizm ishlagan bo'lishi mumkin: zond ostida kuchlanish konsentratsiyasi, kontakt maydonining o'zgarishi, filamentning geometrik
qayta tuzilishi. **Aynan shu savolga COMSOL aniq javob bera oladi** (2-qism, 1-model).

---

## 2-qism. Buni COMSOL'da qilish mumkinmi?

**Qisqa javob: qisman mumkin.** Maqolada ikki xil daraja bor:

| Daraja | Nima | COMSOL? | Qaysi vosita kerak |
|---|---|---|---|
| **Kvant/atom** | Bandlar strukturasi, Veyl tugunlari, Fermi yoylari, Berry egriligi, spin-orbita, topologik invariantlar | **Yo'q.** COMSOL kontinuum (FEM) dasturi, kristall elektron strukturasini hisoblamaydi. | DFT: Quantum ESPRESSO yoki VASP (SOC bilan). Wannier90 + WannierTools (Veyl tugunlari, Fermi yoylari). Z2Pack (invariantlar). Kwant (transport). |
| **Qurilma/kontinuum** | Bosim → deformatsiya → qatlam qalinligi → R_ON; elektr maydon taqsimoti; gate ta'siri; qutbga bog'liq VAX; Joule qizishi | **Ha, bemalol.** | COMSOL: Solid Mechanics, Electric Currents, Electrostatics, Heat Transfer, Global ODE |

**To'g'ri yondashuv ko'p masshtabli (multiscale):**

```
DFT (QE/VASP + Wannier)                COMSOL (FEM)
───────────────────────                ─────────────────────────────
GeSbTe4 qatlami qalinligi t ──►  σ(t)  ──►  Interpolation funksiya
                                            │
    tashqi bosim p ──► Solid Mechanics ──► haqiqiy geometriyada t(p)
                                            │
                                   Electric Currents ──► R_ON(p)
```

DFT kvant qismini beradi (σ yoki R qalinlikka qanday bog'liq). COMSOL esa real qurilmada bosim qatlamni qanchalik
siqishini, tok qanday oqishini va gate maydonini hisoblaydi. DFT natijasi bo'lmasa, **fenomenologik** σ(t) ishlatiladi
va u maqoladagi "2–3 tartib" bo'yicha kalibrlanadi.

Quyidagi 3 ta model **bazaviy COMSOL Multiphysics litsenziyasi** bilan qilinadi (Solid Mechanics, Electric Currents,
Electrostatics, Heat Transfer va Global ODEs bazaviy paketda bor). Qo'shimcha modullar kerak bo'lsa, alohida ko'rsatilgan.
Buyruq nomlari COMSOL 6.x versiyasiga mos. Eski versiyalarda menyu joylashuvi biroz farq qilishi mumkin.

> ⚠️ Material parametrlari **taxminiy** va o'quv uchun berilgan. Jiddiy ish uchun ularni adabiyotdan yoki o'z o'lchovlaringizdan tekshiring.

---

## 3-qism. 1-model: Bosim → R_ON (maqolaning asosiy natijasi)

**Maqsad:** tashqi bosimda xotira elementining R_ON qiymati qanday o'zgarishini hisoblash. "Xotira" va
"kalitlash" holatlarini solishtirish.

### 3.1. Model Wizard
1. **File → New → Model Wizard**.
2. Space dimension: **2D Axisymmetric**. Element silindrsimon, filament o'q bo'ylab joylashgan.
3. Physics: quyidagilarni **Add** qiling:
   - *Structural Mechanics → Solid Mechanics (solid)*
   - *AC/DC → Electric Currents (ec)*
4. Study: **Stationary** → **Done**.

### 3.2. Parametrlar
**Global Definitions → Parameters 1** (jadvalga kiriting yoki `.txt` fayldan Load qiling):

```
p_ext      0[MPa]        "Tashqi bosim"
V_read     0.05[V]       "O'qish kuchlanishi"
R_cell     100[nm]       "Yacheyka radiusi"
t_BE       20[nm]        "Pastki elektrod (TiN)"
t_ST       20[nm]        "Sb2Te3 qatlami"
t_f0       3[nm]         "GeSbTe4 filament qatlami (vdW)"
t_GT       20[nm]        "GeTe qatlami"
t_TE       20[nm]        "Yuqori elektrod (TiN)"
r_f        10[nm]        "Filament radiusi"
sigma_on   1e4[S/m]      "Filament o'tkazuvchanligi (ON)"
sigma_off  1[S/m]        "Filament atrofi / OFF"
lambda_t   0.05[nm]      "O'tkazuvchanlikning qalinlikka sezgirlik uzunligi"
k_state    1             "1 = xotira (bosimga sezgir), 0 = kalitlash (sezgir emas)"
E_f        20[GPa]       "Filament qatlamining Young moduli (vdW, yumshoq)"
```

`k_state` maqolaning asosiy da'vosini ifodalaydi: xotira holatida R_ON bosimga sezgir, kalitlash holatida sezgir emas.

### 3.3. Geometriya
1. **Geometry 1** → *Length unit* = **nm**.
2. Pastdan yuqoriga 6 ta **Rectangle** chizing (r-kengligi, z-balandligi, pastki chap burchak (r, z)):

| Nomi | Width | Height | Position (r, z) |
|---|---|---|---|
| BE (TiN) | R_cell | t_BE | (0, 0) |
| Sb2Te3 | R_cell | t_ST | (0, t_BE) |
| Filament (GeSbTe4) | r_f | t_f0 | (0, t_BE+t_ST) |
| Interfeys qolgani | R_cell−r_f | t_f0 | (r_f, t_BE+t_ST) |
| GeTe | R_cell | t_GT | (0, t_BE+t_ST+t_f0) |
| TE (TiN) | R_cell | t_TE | (0, t_BE+t_ST+t_f0+t_GT) |

3. **Build All**. Keyinroq qulay bo'lishi uchun **Definitions → Selections → Explicit** orqali har bir domen uchun selection yarating: `sel_BE`, `sel_ST`, `sel_fil`, `sel_int`, `sel_GT`, `sel_TE`.

### 3.4. Materiallar
**Materials → Blank Material**, har bir qatlam uchun (taxminiy qiymatlar):

| Material | E [GPa] | ν | ρ [kg/m³] | σ [S/m] | ε_r |
|---|---|---|---|---|---|
| TiN (BE, TE) | 250 | 0.25 | 5220 | 5e6 | 1 |
| Sb₂Te₃ | 55 | 0.25 | 6500 | 1e6 | 50 |
| GeSbTe₄ filament | `E_f` | 0.25 | 6300 | *(fizikada beriladi)* | 30 |
| Interfeys qolgani | `E_f` | 0.25 | 6300 | `sigma_off` | 30 |
| GeTe (kristall) | 50 | 0.25 | 6140 | 1e6 | 36 |

> GeTe va Sb₂Te₃ filamentdan ancha yaxshi o'tkazishi kerak (σ ≫ sigma_on). Aks holda ularning ketma-ket (spreading)
> qarshiligi R_ON ni "yopib qo'yadi" va bosim effekti ko'rinmaydi. Batafsil: `comsol_java/README_JAVA.md`, 4-bo'lim.

Java API versiyasi (bu modelni avtomatik quradi): `comsol_java/GeTeSb2Te3_RON.java`.

### 3.5. Solid Mechanics (bosimni berish)
1. **Solid Mechanics → Linear Elastic Material 1**: hamma domenlar, material qiymatlaridan.
2. **Fixed Constraint**: eng pastki chegara (z = 0), ya'ni taglik.
3. **Roller**: tashqi chegaralar (r = R_cell). Bu yupqa plyonkani yon tomondan qo'shni material ushlab turishini ifodalaydi (bir o'qli deformatsiya). Alternativa: erkin chegara. Ikkala holatni solishtiring.
4. **Boundary Load**: eng yuqori chegara. *Load type* = **Pressure**, `p = p_ext`.
   - Zond bilan bosishni modellashtirish uchun bosimni faqat r < r_probe qismiga bering. Buning uchun yuqori chegarani qismlarga bo'ling yoki `p_ext*(r<r_probe)` ifodasini ishlating. Shunday qilib kuchlanish konsentratsiyasini tekshirasiz (1.3-bo'limdagi savol).

### 3.6. Electric Currents (R_ON ni o'lchash)
1. **Current Conservation 1**: barcha domenlar, σ materialdan olinadi.
2. Filament domeni (`sel_fil`) uchun ikkinchi **Current Conservation** qo'shing. *Electrical conductivity* → **User defined**:
   ```
   sigma_on*exp(-k_state*t_f0*solid.eZZ/lambda_t)
   ```
   Siqilishda `solid.eZZ < 0`, shuning uchun qalinlik kamayadi va σ ortadi. Bu maqoladagi "qalinlik kichrayadi, R_ON kamayadi" mexanizmining oddiy ifodasi.
   *DFT natijasi bo'lsa:* **Definitions → Functions → Interpolation** orqali `sig_DFT(t)` jadvalini yuklang va `sig_DFT(t_f0*(1+solid.eZZ))` deb yozing.
3. **Ground**: pastki elektrodning pastki chegarasi (Sb₂Te₃ tomoni).
4. **Terminal**: yuqori elektrodning yuqori chegarasi. *Terminal type* = **Voltage**, `V0 = V_read`.
   (Maqoladagi qutb: Sb₂Te₃ tomonidagi elektrod manfiy, ya'ni yuqori elektrod musbat.)

### 3.7. Global o'zgaruvchi
**Definitions → Variables** (Component 1):
```
R_ON   V_read/ec.I0_1    "ON-holat qarshiligi"
```

### 3.8. To'r (Mesh)
Qatlamlar juda yupqa (3 nm), elektrodlar esa kengroq. Shuning uchun:
1. **Mesh → User-controlled** → **Mapped** (hamma domenlar).
2. **Distribution**: filament va interfeys qatlamlarining vertikal qirralariga kamida 6 element. Boshqa qatlamlarga 8–10 element. Gorizontal yo'nalishda r_f atrofida zichroq qiling (*Distribution → Predefined → Geometric sequence*).
3. Tekshiruv: to'rni 2 marta maydalang. R_ON 1 % dan kam o'zgarsa, to'r yetarli.

### 3.9. Tadqiqot
1. **Study 1 → Stationary**. Ikkala fizika birga yechiladi. Bog'lanish bir tomonlama (mexanika → elektr), shuning uchun yaqinlashish oson.
2. **Study → Parametric Sweep** qo'shing:
   - `p_ext` = `range(0, 100, 2000)` [MPa]
   - `k_state` = `0 1`
   - *Sweep type* = **All combinations**.
3. **Compute**.

### 3.10. Natijalar
1. **1D Plot Group → Global**: y = `R_ON`, x = `p_ext`. **Y-axis log scale** yoqing. Ikkala `k_state` uchun ikki chiziq chiqadi:
   - `k_state = 1` (xotira): R_ON bosim oshishi bilan eksponensial kamayadi.
   - `k_state = 0` (kalitlash): R_ON deyarli o'zgarmaydi. Bu maqolaning bashorati.
2. **2D Plot**: `solid.eZZ` va `ec.normJ` (tok zichligi). Tok filamentda to'planadi, siqilish esa qatlamlar bo'yicha taqsimlanadi.
3. **Kalibrlash:** qaysi `p_ext` qiymatida R_ON 100–1000 marta kamayishini aniqlang. Keyin `lambda_t` va `E_f` ni fizik jihatdan mantiqli oraliqda o'zgartiring (λ ≥ 0.03 nm, E_f = 10–50 GPa). Natija: maqoladagi effekt uchun **qanday bosim kerakligini** baholaysiz. Bu maqolani mustaqil tekshirishning eng qimmatli qismi.

---

## 4-qism. 2-model: Qutbga bog'liq VAX va gate ta'siri (1-rasmni takrorlash)

**Maqsad:** 1-rasmdagi xatti-harakatni fenomenologik ravishda qayta hosil qilish. U_gate = 0 V da xotira (histerezisli, nonvolatil),
U_gate ≤ −1.1 V da ostona kalitlash (volatil). Mikroskopik mexanizmni COMSOL modellashtirmaydi. Uning o'rniga
**holat o'zgaruvchisi** `x` ishlatiladi (filament ulushi, 0…1), memristor modellaridagi kabi.

### 4.1. Tayyorlash
1-modelni nusxalang (**File → Save As**). Keyin:
1. **Add Physics → Mathematics → ODE and DAE Interfaces → Global ODEs and DAEs (ge)**.
2. **Add Study → Time Dependent**.

### 4.2. Qo'shimcha parametrlar
```
V_amp      2[V]          "Qo'llaniladigan sinus amplitudasi"
f0         100[Hz]       "Chastota (maqoladagidek)"
U_gate     0[V]          "Gate kuchlanishi"
U_crit    -1.0[V]        "Xotira -> kalitlash o'tish nuqtasi"
dU         0.1[V]        "O'tish silliqligi"
E_set      3e8[V/m]      "SET maydoni (~0.9 V / 3 nm)"
E_reset    2e8[V/m]      "RESET maydoni (teskari qutb)"
E_hold     1e8[V/m]      "Kalitlash holatini ushlab turish maydoni"
dE         2e7[V/m]      "Silliqlash kengligi"
tau_set    1e-5[s]
tau_reset  1e-5[s]
tau_relax  1e-4[s]       "Volatil holat so'nish vaqti"
```

### 4.3. O'zgaruvchilar (Definitions → Variables)
Avval **Definitions → Nonlocal Couplings → Average** (`aveop1`) ni filament domeniga qo'shing. Keyin:
```
V_app   V_amp*sin(2*pi*f0*t)
E_fil   aveop1(ec.Ez)                          "Filamentdagi o'rtacha maydon"
m_mem   flc2hs(U_gate-U_crit, dU)              "1 = xotira rejimi, 0 = kalitlash rejimi"
rate    (1-x)/tau_set*flc2hs(-E_fil-E_set, dE) ...
      - x/tau_reset*flc2hs(E_fil-E_reset, dE) ...
      - (1-m_mem)*x/tau_relax*flc2hs(E_hold-abs(E_fil), dE)
I_cell  ec.I0_1
```
(`...` belgisi faqat qatorni bo'lish uchun yozilgan. COMSOL'ga ifodani bitta qatorda kiriting.)

Fizik ma'nosi:
- **1-had (SET).** Yuqori elektrod musbat bo'lsa, `E_z < 0` bo'ladi, filament o'sadi. Bu maqoladagi "Sb₂Te₃ tomonida manfiy kuchlanish" sharti.
- **2-had (RESET).** Qutb teskari bo'lsa, filament yo'qoladi.
- **3-had (volatillik).** Faqat kalitlash rejimida ishlaydi (U_gate < U_crit). Maydon kamayganda filament o'z-o'zidan so'nadi. Bu maqoladagi "barqarorlik faqat tashqi maydon bilan saqlanadi" jumlasi.

### 4.4. Global ODE
**Global Equations 1**: nomi `x`, *f(u,ut,utt,t)* = `xt - rate`, boshlang'ich qiymat `x = 0` (OFF).

### 4.5. Electric Currents o'zgarishlari
1. Filament o'tkazuvchanligi (holat + bosim):
   ```
   sigma_off^(1-x)*sigma_on^x*exp(-k_state*t_f0*solid.eZZ/lambda_t)
   ```
   Birliklar haqida ogohlantirish chiqsa: `sigma_off*(sigma_on/sigma_off)^x*exp(...)` ko'rinishida yozing.
2. **Terminal**: `V0 = V_app`.

### 4.6. Tadqiqot
1. **Time Dependent**: *Times* = `range(0, 1e-5, 0.02)` (2 davr). Faqat `ec` va `ge` fizikalarini belgilang. Mexanikani o'chiring yoki 1-modeldagi statsionar yechimni boshlang'ich qiymat sifatida bering.
2. **Solver Configurations → Time-Dependent Solver → Time Stepping**: *Maximum step* = `1e-5`. `flc2hs` o'tishlari keskin, shuning uchun qadam cheklanishi kerak.
3. **Parametric Sweep**: `U_gate` = `0 -0.9 -1.1`.
4. **Compute**.

### 4.7. Natijalar
**1D Plot Group → Global**: y = `I_cell`, *x-Axis Data* = **Expression** → `V_app`. `U_gate` ning har bir qiymati uchun bitta egri chiziq chiqadi:
- **0 V:** histerezis sirtmog'i. Musbat yarim davrda SET bo'ladi. Kuchlanish nolga qaytganda ham ON saqlanadi (xotira). Manfiy yarim davrda RESET bo'ladi. Bu 1a-rasm.
- **−0.9 V:** m_mem ≈ 0.5, aralash xatti-harakat (1b).
- **−1.1 V:** V kamayganda ON so'nadi, ya'ni faqat ostona kalitlash (1c).

`U_crit`, `E_set`, `tau_*` parametrlarini o'zgartirib, egri chiziqlarni tajriba natijasiga yaqinlashtiring.

---

## 5-qism. 3-model (ixtiyoriy): Gate maydoni va issiqlik modeli bilan solishtirish

### 5.1. Real FET geometriyasida gate maydoni (Electrostatics)
2-modelda `U_gate` to'g'ridan-to'g'ri parametr sifatida ishlatildi. Aniqroq yondashuv:
1. **2D** (tekis kesim) model: taglik (SiO₂), Sb₂Te₃ kanal (source/drain elektrodlari uchlarida), ustida GeTe, uning ustida gate elektrodi.
2. **AC/DC → Electrostatics (es)**. GeTe domeni uchun *Charge Conservation* → material modeli **Remanent electric displacement**, `Dr = (0, P_r)`. Qutblanish uchun taxminiy qiymat P_r ≈ 0.1–0.6 C/m² (adabiyotdan aniqlang).
3. Gate chegarasiga **Electric Potential** = `U_gate` bering. Sb₂Te₃ bilan interfeys chegarasini **Ground** qiling.
4. **Parametric Sweep** `U_gate = range(-2, 0.1, 1)` qiling. Interfeysdagi o'rtacha `es.Ez` ni hisoblang.
5. Natijani GeTe'ning koersitiv maydoni E_c bilan solishtiring. |E| > E_c bo'lgan nuqta tajribadagi U ≈ −1.1 V ga mos kelishi kerak. Shundan **effektiv E_c** ni baholash mumkin. Keyin 2-modeldagi `m_mem` ni `U_gate` o'rniga shu maydonga bog'lang.
   *(Haqiqiy ferroelektrik histerezis kerak bo'lsa, COMSOL 6.x'dagi Ferroelectroelasticity xususiyatidan foydalaning. U AC/DC yoki MEMS modulini talab qiladi.)*

### 5.2. Issiqlik modeli (melt-quench) bilan solishtirish
Mualliflar issiqlik modelini rad etadi. Buni COMSOL'da tekshirish mumkin:
1. 2-modelga **Heat Transfer in Solids** qo'shing va **Multiphysics → Electromagnetic Heating** ni yoqing (Joule qizishi).
2. Material issiqlik parametrlari: k ≈ 0.5–3 W/(m·K), C_p ≈ 200 J/(kg·K). Tashqi chegaralarda 293 K.
3. 100 Hz va 2 V da filamentdagi maksimal haroratni toping. GST uchun kristallanish harorati ~430 K, erish harorati ~900 K.
   - Agar T ≪ 430 K bo'lsa, bu **maqola foydasiga** dalil: kalitlash issiqliksiz bo'lyapti.
   - Agar T erish haroratiga yaqin bo'lsa, issiqlik modelini inkor etib bo'lmaydi.

Bu solishtirish maqolada yo'q, lekin uning asosiy da'vosini tekshirish uchun juda muhim.

---

## 6-qism. Kvant qismi uchun yo'l xaritasi (COMSOL'dan tashqarida)

COMSOL'dagi fenomenologik `σ(t)` ni haqiqiy fizika bilan almashtirish uchun:

1. **Struktura.** GeTe/GeSbTe₄/Sb₂Te₃ supercell quring (VESTA, ASE). vdW tuzatmasini qo'llang (DFT-D3).
2. **DFT + SOC.** Quantum ESPRESSO yoki VASP bilan ishlang. Qatlamni c o'qi bo'yicha 0…−8 % siqing va har bir deformatsiyada bandlar strukturasini hisoblang.
3. **Wannier90 → WannierTools.** Veyl tugunlarining k-fazodagi joylashuvi, xiralligi, Fermi yoylari (sirt Grin funksiyasi) va ularning uzunligi deformatsiyaga qanday bog'liqligini toping.
4. **Transport.** Kwant yoki Boltzmann (BoltzTraP2) bilan G(t) yoki σ(t) ni hisoblang.
5. **COMSOL'ga qaytish.** σ(t) jadvalini **Interpolation** funksiya qilib yuklang (3.6-bo'lim). Endi R_ON(p) prognozi fenomenologik emas, balki birinchi prinsiplarga asoslangan bo'ladi.

---

## 7-qism. Xulosa

| Savol | Javob |
|---|---|
| Maqolani to'liq COMSOL'da takrorlash mumkinmi? | **Yo'q.** Topologik (Veyl/Dirak) qism kvant-mexanik hisobni talab qiladi. |
| Qurilma darajasidagi natijalar (R_ON(p), qutbga bog'liq VAX, gate ta'siri)? | **Ha.** 1- va 2-modellar bazaviy COMSOL bilan qilinadi. |
| COMSOL maqolaga nima qo'shadi? | (1) Maqolada yo'q bo'lgan **bosim miqdori** bo'yicha tekshiruv. (2) Issiqlik modelini haqiqatda tekshirish. (3) Real geometriyadagi gate maydoni. |
| Eng yaxshi natija qanday olinadi? | **DFT (σ(t)) + COMSOL (t(p), R_ON(p))** ko'p masshtabli yondashuvi bilan. |
