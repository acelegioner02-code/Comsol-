# 1-model (bosim → R_ON) uchun COMSOL Java API: ishlatish yo'riqnomasi

| Fayl | Nima qiladi |
|---|---|
| `GeTeSb2Te3_RON.java` | COMSOL modelini noldan quradi: geometriya, fizika, to'r. Bosim sweep qiladi va natijani saqlaydi. |
| `plot_R_ON.py` | Natija CSV faylidan R_ON(p) grafigini chizadi (ixtiyoriy). |

Model `../GeTe_Sb2Te3_COMSOL_qollanma.md` qo'llanmasining **3-qismidagi** qadamlarni kod ko'rinishida bajaradi.

---

## 1. Talablar

- **COMSOL Multiphysics 6.x** va bazaviy litsenziya. Qo'shimcha modul kerak emas.
- Java'ni alohida o'rnatish shart emas. COMSOL o'zining kompilyatorini (`comsolcompile`) ishlatadi.

## 2. Kod qanday tuzilgan

Faylning boshida o'zgartirish mumkin bo'lgan konstantalar bor:

```java
static final double R_CELL = 100, T_BE = 20, T_ST = 20, T_F0 = 3, T_GT = 20, T_TE = 20, R_F = 10; // [nm]
static final double P_START = 0, P_STOP = 2000, P_STEP = 100;   // bosim sweep [MPa]
static final int[] K_STATES = {1, 0};                           // 1 = xotira, 0 = kalitlash
static final String H_MAX = "0.5";                              // to'r elementi [nm]
```

`run()` metodi quyidagi qadamlarni bajaradi (koddagi raqamlangan bo'limlar):

| Bo'lim | Nima qiladi | COMSOL GUI'dagi ekvivalenti |
|---|---|---|
| 1 | Parametrlar (`p_ext`, `sigma_on`, `lambda_t`, `E_f`, …) | Global Definitions → Parameters |
| 2 | 2D o'qqa simmetrik geometriya, 6 ta to'rtburchak, birlik nm | Geometry |
| 3 | Chegaralar uchun Box selectionlar (past, yuqori, tashqi) va domen birlashmalari | Definitions → Selections |
| 4 | `R_ON = V_read/ec.I0_1` va `sigma_fil` o'zgaruvchilari | Definitions → Variables |
| 5 | Solid Mechanics: har bir qatlamga E va ν, taglik mahkamlangan, yon tomonda Roller, yuqorida `p_ext` bosimi | Solid Mechanics |
| 6 | Electric Currents: har bir qatlamga σ, filamentda σ deformatsiyaga bog'liq, pastda Ground, yuqorida Terminal | Electric Currents |
| 7 | Mapped to'r | Mesh |
| 8–9 | Stationary study, birinchi yechim, Global Evaluation | Study, Results |
| 10 | `k_state` va `p_ext` bo'yicha Java sikli, natija CSV faylga yoziladi | Parametric Sweep o'rniga |

Filament o'tkazuvchanligi (asosiy fizika):
```
sigma_fil = sigma_on*exp(-k_state*t_f0*solid.eZZ/lambda_t)
```

Nega Parametric Sweep emas, Java sikli? Sikl natijani to'g'ridan-to'g'ri CSV faylga yozadi va har bir qadamni konsolda
ko'rsatadi. Dataset nomlari bilan chalkashlik ham bo'lmaydi.

---

## 3. Ishga tushirish

Barcha buyruqlar `GeTeSb2Te3_RON.java` joylashgan papkada bajariladi.

### A usul: buyruq qatori (tavsiya etiladi)

**Windows** (yo'lni o'zingizdagi versiyaga moslang, masalan `COMSOL62` yoki `COMSOL63`):

```bat
cd C:\ish\Comsol-\comsol_java
"C:\Program Files\COMSOL\COMSOL62\Multiphysics\bin\win64\comsolcompile.exe" GeTeSb2Te3_RON.java
"C:\Program Files\COMSOL\COMSOL62\Multiphysics\bin\win64\comsolbatch.exe" -inputfile GeTeSb2Te3_RON.class
```

**Linux**:

```bash
cd ~/Comsol-/comsol_java
/usr/local/comsol62/multiphysics/bin/comsol compile GeTeSb2Te3_RON.java
/usr/local/comsol62/multiphysics/bin/comsol batch -inputfile GeTeSb2Te3_RON.class
```

**macOS**: `/Applications/COMSOL62/Multiphysics/bin/comsol compile …` va `… comsol batch …`.

1. `compile` buyrug'i `GeTeSb2Te3_RON.class` faylini yaratadi.
2. `batch` buyrug'i modelni quradi va 42 marta yechadi (21 bosim × 2 holat). Konsolda shunday qatorlar chiqadi:
   ```
   k_state=1  p=    0.0 MPa  R_ON=1.0xxe+03 Ohm  R_fil=9.5xxe+02 Ohm
   k_state=1  p=  100.0 MPa  R_ON=...
   ```
3. Ish tugagach, papkada ikki fayl paydo bo'ladi:
   - `R_ON_vs_pressure.csv` — natija jadvali.
   - `GeTe_Sb2Te3_RON.mph` — to'liq model. Uni COMSOL Desktop'da oddiy fayl kabi ochish mumkin.

### B usul: COMSOL Desktop orqali

1. Avval A usuldagi kabi faqat `compile` qiling.
2. COMSOL Desktop'da **File → Open** ni tanlang. Fayl turini *Compiled Model File for Java (\*.class)* qilib, `GeTeSb2Te3_RON.class` ni oching. COMSOL kodni bajaradi va modelni ochadi.
3. CSV va `.mph` fayllar Desktop'ning joriy ishchi papkasiga yoziladi. Ularni topa olmasangiz, koddagi `OUT_DIR` ni to'liq yo'lga o'zgartiring, masalan `"C:/ish/natija"`.

### C usul: LiveLink for MATLAB (agar bo'lsa)

```matlab
javaaddpath('C:\ish\Comsol-\comsol_java');   % .class joylashgan papka
model = GeTeSb2Te3_RON.run();
mphlaunch(model);                               % modelni Desktop'da ko'rsatadi
```

---

## 4. Natijalarni ko'rish

**Python bilan:**
```bash
pip install matplotlib
python plot_R_ON.py
```
Skript `R_ON_vs_pressure.png` faylini yaratadi va R_ON(0)/R_ON(p_max) nisbatini chiqaradi.

**COMSOL'da:** `GeTe_Sb2Te3_RON.mph` ni oching. Unda ikkita tayyor rasm bor:
- *Deformatsiya eZZ* — siqilish qaysi qatlamlarda to'planadi.
- *Tok zichligi |J|* — tok filament orqali oqishini ko'rsatadi.

**Excel bilan:** CSV'ni oching va R_ON ustuni uchun logarifmik o'q qo'ying.

### Kutiladigan natija (standart parametrlar bilan)

CSV'da ikki xil qarshilik bor:
- `R_ON` — butun yacheykaning qarshiligi, ya'ni o'lchanadigan qiymat.
- `R_fil` — faqat filamentning o'z qarshiligi.

Filamentning effektiv moduli M ≈ 24 GPa (E_f = 20 GPa, ν = 0.25, yon tomoni cheklangan). U holda p = 2 GPa da ε_zz ≈ −8 %
bo'ladi. Qo'lda hisoblangan taxminiy qiymatlar:

| Holat | R_fil: 0 → 2000 MPa | R_ON: 0 → 2000 MPa |
|---|---|---|
| `k_state = 1` (xotira) | ~950 Ω → ~6 Ω (~150 marta) | ~1 kΩ → ~60 Ω (~15–20 marta) |
| `k_state = 0` (kalitlash) | deyarli o'zgarmaydi | deyarli o'zgarmaydi |

**Muhim fizik xulosa:** `R_ON` ning o'zgarishi `R_fil` nikidan kichik. Sababi GeTe va Sb₂Te₃ qatlamlarining ketma-ket
(spreading) qarshiligi (~50 Ω). Bu qarshilik bosimga deyarli bog'liq emas, shuning uchun u pastki chegara bo'lib qoladi.
Demak, maqolada aytilgan **2–3 tartib o'zgarish kuzatilishi uchun ikki shart bir vaqtda bajarilishi kerak**:
(1) GPa darajasidagi bosim yoki juda sezgir σ(t) bog'lanishi; (2) filament qarshiligi boshqa barcha ketma-ket
qarshiliklardan ~1000 marta katta bo'lishi. Buni tekshirish uchun `sig_GT` va `sig_ST` ni 1e5 ga tushiring:
spreading qarshiligi ~10 marta oshadi va effekt yanada kamayadi.

Natijangiz bu qiymatlardan ancha farq qilsa, to'r va chegara shartlarini tekshiring.

---

## 5. Model bilan tajribalar

| Nima tekshirmoqchisiz | Nimani o'zgartirasiz |
|---|---|
| Filament qatlami yumshoqroq bo'lsa (vdW) | `model.param().set("E_f", "10[GPa]")` |
| O'tkazuvchanlik qalinlikka kamroq yoki ko'proq sezgir bo'lsa | `lambda_t`: 0.03…0.2 nm |
| Filament kengroq yoki torroq bo'lsa | `R_F` |
| Ketma-ket qarshilikning ta'siri | `sig_GT`, `sig_ST`: 1e5…1e7 S/m |
| Zond bilan bosish (lokal bosim) | 5-bo'limdagi `bndl1` da `-p_ext` o'rniga `-p_ext*(r<20[nm])` yozing |
| Yon tomon erkin bo'lsa | `roll1` bloki (3 qator) ni o'chiring |
| Kichikroq bosim oralig'i | `P_STOP = 500; P_STEP = 25;` |

### DFT natijasini ulash

Agar DFT'dan σ(t) jadvali olingan bo'lsa (`sigma_DFT.txt`: 1-ustun t [nm], 2-ustun σ [S/m]), 4-bo'limdan oldin shu kodni qo'shing:

```java
model.func().create("int1", "Interpolation");
model.func("int1").set("source", "file");
model.func("int1").set("filename", OUT_DIR + "/sigma_DFT.txt");
model.func("int1").set("funcname", "sig_DFT");
model.func("int1").set("argunit", new String[]{"nm"});
model.func("int1").set("fununit", new String[]{"S/m"});
```

Keyin `sigma_fil` ifodasini shunday almashtiring:

```java
model.component("comp1").variable("var1").set("sigma_fil", "sig_DFT(t_f0*(1+solid.eZZ))");
```

---

## 6. Muammolarni hal qilish

| Xato | Sababi va yechimi |
|---|---|
| `Unknown property: ...` yoki `Unknown feature type: ...` | COMSOL versiyalarida xususiyat nomlari biroz farq qilishi mumkin. Eng ishonchli yechim: o'sha featureni GUI'da qo'lda yarating, **File → Save As → Model File for Java (\*.java)** bilan saqlang va to'g'ri nomni o'sha fayldan ko'chiring. |
| `Unknown selection: geom1_r1_dom` | Geometriyada `selresult` ishlamagan. GUI'da Rectangle → *Resulting objects selection* belgisi borligini tekshiring. |
| Chegara shartlari qo'llanmagan (bosim yoki Ground yo'q) | Box selection chegarani topmagan. `.mph` ni ochib, `sel_bot`, `sel_top`, `sel_out` ni tekshiring. Kerak bo'lsa `eps` qiymatini oshiring. |
| `UnsupportedClassVersionError` | `.class` fayl boshqa Java yoki COMSOL versiyasida kompilyatsiya qilingan. Ishga tushiradigan COMSOL'ning o'z `comsolcompile` buyrug'i bilan qayta kompilyatsiya qiling. |
| Yechim yaqinlashmaydi | `lambda_t` juda kichik bo'lishi mumkin (exp juda tez o'sadi). Uni oshiring yoki `P_STEP` ni kamaytiring. |
| Juda sekin ishlaydi | `H_MAX = "1"` qiling (tekshiruv uchun yetarli), keyin aniq natija uchun `0.5` ga qaytaring. |
| Litsenziya xatosi | Solid Mechanics va Electric Currents bazaviy paketda bor. `comsol batch` ni litsenziya serveri ko'rinadigan kompyuterda ishga tushiring. |

> Eslatma: kod COMSOL'siz muhitda yozilgan va faqat Java sintaksisi tekshirilgan. COMSOL'ning o'zida ishga tushirilmagan.
> Birinchi ishga tushirishda 6-bo'limdagi xatolardan biri chiqsa, jadvaldagi yechimdan foydalaning.
