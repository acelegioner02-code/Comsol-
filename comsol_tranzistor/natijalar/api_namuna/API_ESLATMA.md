# COMSOL 6.0 Java API eslatmalari (B1 natijasi)

Bu fayl COMSOL 6.0.0.318 (comsolbatch/comsolcompile) da haqiqiy tekshirilgan (introspeksiya +
sinov kompilyatsiya/ishga tushirish) Java API nomlari. Manba: `applications\Semiconductor_Module\`
ichidagi haqiqiy .mph namunalar (`ModelUtil.load` bilan ochib, `physics().feature().properties()`
orqali tag/type/prop nomlari o'qildi — natijalar shu papkadagi `dump_*.txt` fayllarda) va
to'g'ridan-to'g'ri comsolcompile/comsolbatch bilan sinov dasturlar (`TestGlobalEq.java`,
`TestThickness.java`, `TestStudy.java`, `LicenseTest.java`).

**Litsenziya:** Semiconductor Module ishlaydi (`LicenseTest.java` -> `SEMI_OK`).

## 1. Semiconductor physics interfeysini yaratish
```java
model.component().create("comp1", true);
model.component("comp1").geom().create("geom1", 2);
model.component("comp1").physics().create("semi", "Semiconductor", "geom1");
Physics semi = model.component("comp1").physics("semi");
```
Import: `import com.comsol.model.*; import com.comsol.model.util.*; import com.comsol.model.physics.*;`
(`Physics` va `PhysicsFeature` interfeysi `com.comsol.model.physics` paketida — buni alohida import
qilish kerak, `com.comsol.model.*` uni avtomatik qamrab olmaydi agar to'g'ridan-to'g'ri tur sifatida
ishlatmoqchi bo'lsangiz. `GeTeSb2Te3_RON.java` da faqat `com.comsol.model.*` bilan ishlagani sababi —
comsolcompile java faylini avval maxsus preprocessor orqali o'tkazadi, u standart holatda
`com.comsol.model.physics.*` ni ham qo'shib qo'yishi mumkin; xavfsizlik uchun har doim ikkalasini
ham import qiling.)

Standart formulasiya allaqachon talab qilinganidek: `ModelProperties.Formulation = FVM`,
`ModelProperties.CarrierStatistics = Boltzmann` — bularni o'zgartirish shart emas (PROMPT.md talabiga mos).

## 2. Out-of-plane thickness (W)
Bu **physics-level prop**, feature emas:
```java
semi.prop("d").set("d", "12.8[um]");
```
(`semi.prop().tags()` orqali tekshirilgan tag'lar: PhysicsSymbols, ShapeProperty, EquationForm,
LatticeProperties, `d`, ModelProperties, Stabilization, Continuation, NormalVector,
PortSweepSettings, StudyStep.)

## 3. Thin Insulator Gate — `GateContact` feature
(Barcha 5 ta namunada: moscap_1d, moscap_1d_interface_traps, mosfet, nanowire_traps, isfet)
```java
PhysicsFeature gc1 = semi.create("gc1", "GateContact", 1);  // 1 = chegara (1D geometriyaning nuqtasi/2D ning qirrasi)
gc1.selection().set(...);   // yoki .named("sel_tag")
gc1.set("TerminalName", "4");
gc1.set("TerminalType", "Voltage");
gc1.set("V0", "VG");                 // gate kuchlanishi (ifoda/parametr nomi bo'lishi mumkin)
gc1.set("epsilon_ins", "3.9");       // oksidning nisbiy dielektrik doimiysi
gc1.set("d_ins", "285[nm]");         // oksid qalinligi
gc1.set("Phi", "phiM");              // metall ish funksiyasi [V] (yoki son)
```
Boshqa mavjud xossalar: `Q0`, `I_cir_src`, `I_cir`, `ContinuationType`, `cp_input`,
`DGexteriorBC`, `Phi_nOx`, `Phi_pOx`, `meOx*`, `mhOx*` (Density-Gradient uchun, bizga kerak emas).

**Muhim:** `nti1`ning o'zi D_it bermaydi — D_it interfeys tuzoqlari alohida feature (quyida 5-band).

## 4. Metal Contact (Schottky) — `MetalContact` feature
(schottky_contact.mph dan)
```java
PhysicsFeature mc1 = semi.create("mc1", "MetalContact", 1);
mc1.set("ContactType", "Schottky");
mc1.set("SpecifyUsing", "richardsons");     // yoki boshqa variant
mc1.set("SpecifyBarrierHeight", "ideal");   // Phi_B ni to'g'ridan-to'g'ri ifoda sifatida berish
mc1.set("Phi_B", "Phi_B0 - x");             // source: pasaygan barer (ifoda global "x" o'zgaruvchisini ishlatishi mumkin)
mc1.set("TerminalName", "1");
mc1.set("TerminalType", "Voltage");
mc1.set("V0", "0");                          // yoki "VD" drain uchun
mc1.set("Astar_n", "110[A/(K*cm)^2]");       // Richardson konstantasi (kerak bo'lsa moslashtiriladi)
```
Drain uchun `Phi_B = "Phi_B0 + x"`, `V0 = "VD"`.
Ohmic kontakt kerak bo'lsa: `ContactType = "ohmic"`.

## 5. Interfeys tuzoqlari (D_it) — `TrapAssistedSurfaceRecombination` + sub-feature `ContinuousEnergyLevelsBoundary`
(moscap_1d_interface_traps.mph dan, yagona haqiqiy "continuous DOS" namunasi)
```java
PhysicsFeature tasr1 = semi.create("tasr1", "TrapAssistedSurfaceRecombination", 1);
tasr1.set("IncludeTraps", "ExplicitTraps");
tasr1.set("SpecifyDiscreteContinuous", "SpecifyContinuousAndOrDiscreteLevels");

PhysicsFeature ctb1 = tasr1.feature().create("ctb1", "ContinuousEnergyLevelsBoundary", -1);
ctb1.set("TrapType", "Donor");                       // yoki "Acceptor" — ikkalasini ham qo'shish mumkin (2 ta ctb)
ctb1.set("TrapDensityDistribution", "Rectangle");     // energiya bo'yicha bir xil D_it uchun
ctb1.set("Ewidth", "Ew0");                            // energiya kengligi [V]=[eV]
ctb1.set("Nt_b", "Dit*Ew0*e_const");                  // sirt zichligi; namunada aynan shu ko'rinish ishlatilgan
ctb1.set("sigman", "sigma_n");                        // elektron tutish ko'ndalang kesimi
ctb1.set("sigmap", "sigma_p");
```
Namunadagi aniq qiymat: `Nt_b (strArr) = [Nss*Ew0*e_const;]` — ya'ni sirt tuzoq zichligi
(1/cm^2) = Nss [1/(cm^2*eV)] * Ew0 [eV] * e_const. Bizning `D_it` (cm^-2 eV^-1) shu Nss o'rniga
qo'yiladi. SS talabi: SS ≈ 0.06 V/dek * (1 + q*D_it/C_ox) — D_it ni 1e13-5e13 cm^-2 eV^-1 oralig'ida
skanerlab SS ni T8 ga moslashtirish kerak (B4).

Muqobil (agar Continuous DOS yaqinlashmasa): `SpecifyDiscreteLevelsOnly` + bir nechta diskret
sath (`DiscreteTrapLevel`-siman sub-feature, mosfet.mph/nanowire_traps.mph namunasida ishlatilgan,
lekin bizga battar mos kelmaydi chunki diskret sathlar D_it*kT taxminiga ko'proq ish talab qiladi).

## 6. Analitik legirlash — `AnalyticDopingModel`
```java
PhysicsFeature adm1 = semi.create("adm1", "AnalyticDopingModel", 2);
adm1.set("impurityType", "donor");     // yoki "acceptor"
adm1.set("NDc", "Nd0[1/cm^3]");
adm1.set("impurityDistribution", "uniform_full");  // eng oddiy: butun domenda bir xil (namunalarda
                                                     // "user_defined" bilan gradient ham bor, lekin
                                                     // MoS2 kanali uchun bir xil (uniform) yetarli)
```
(Aniq `impurityDistribution` qiymatlari ro'yxatini B2 kompilyatsiyasida xato orqali tekshiring —
`user_defined` tasdiqlangan, boshqa variantlar `dropoff_profile`/`JunctionOrLength`ga bog'liq.)

## 7. Global Equations (ion holati x uchun) — `GlobalEquations` feature, TO'G'RIDAN-TO'G'RI semi physics ichida
(`TestGlobalEq.java` bilan compile+run orqali tasdiqlangan, hujjatda: "Global Equations" nodini istalgan
physics interfeysiga to'g'ridan-to'g'ri qo'shish mumkin, alohida "Global ODEs and DAEs" interfeysisiz)
```java
PhysicsFeature ge1 = semi.create("ge1", "GlobalEquations", -1);   // -1 = global (geometriyasiz)
ge1.setIndex("name", "x", 0);
ge1.setIndex("equation", "xt - (beta*VDt - x)/tau_ion", 0);
ge1.setIndex("initialValueU", "0", 0);
ge1.setIndex("initialValueUt", "0", 0);
```
Tasdiqlangan xossalar ro'yxati: `name, equation, initialValueU, initialValueUt, description,
valueType, DependentVariableQuantity, CustomDependentVariableUnit, SourceTermQuantity,
CustomSourceTermUnit, StudyStep, pairContrib`. Bir nechta tenglama uchun `setIndex(..., 1)`,
`setIndex(..., 2)` va hokazo (jadval qatorlari).

**Diqqat:** `initu`/`initut` XATO nomlar (sinovda `Unknown_parameter` xatosi chiqdi) — faqat
`initialValueU`/`initialValueUt` ishlaydi.

## 8. Study step'lar (haqiqiy namunalardan)
- Stationary: `model.study("std1").create("stat", "Stationary");`
- Semiconductor Equilibrium (tuzoqlar/legirlash uchun boshlang'ich muvozanat):
  `model.study("std1").create("semie", "SemiconductorEquilibrium");`
  (moscap_1d_interface_traps.mph da "Study 1 - Vg sweep at 50 Hz" shu bilan boshlanadi)
- Transient (chiqish xarakteristikasi, B5 uchun; generic COMSOL, semiconductor-ga xos emas, lekin
  `TestStudy.java` bilan tasdiqlangan): `model.study("std1").create("time", "Transient");`
  Xossa: `tlist` (masalan `"range(0,0.01,1)"`), `initialtime`.
- Parametrik continuation (VG sweep): `Stationary` step'ning `plistarr`/`pname`/`pcontinuationmode`
  xossalari (schottky_contact.mph namunasida "Va" parametri 0.01:0.01:0.25 oralig'ida "plistarrexcel..."
  emas, oddiy `plistarr_vector_start/stop/step` bilan berilgan — B3'da xuddi shunday `pname="VG"`,
  `plistarr_vector_start/stop/step` bilan ishlatiladi, `pcontinuationmode` orqali oldingi yechimdan
  boshlash yoqiladi).

## 9. Boundary tag mos kelishi (segment index)
`semi.create(tag, "FeatureType", entityDim)` ning oxirgi argumenti geometrik o'lcham
(2D domenda qirralar uchun `1`, global/geometriyasiz feature uchun `-1`). Selection keyin
`.selection().set(int...)` (qirra raqamlari bilan) yoki `.selection().named("tagname")` bilan beriladi.

## 10. Manba fayllar
- `dump_schottky_contact.txt` — MetalContact (Schottky) to'liq xossalari.
- `dump_moscap_1d_interface_traps.txt` — GateContact + TrapAssistedSurfaceRecombination +
  ContinuousEnergyLevelsBoundary (D_it) to'liq xossalari, SemiconductorEquilibrium study.
- `dump_mosfet.txt` — to'liq nMOSFET: 3 ta MetalContact (source/drain/body) + 1 GateContact +
  3 ta AnalyticDopingModel.
- `dump_nanowire_traps.txt`, `dump_isfet.txt`, `dump_insb_pfet_density_gradient.txt`,
  `dump_moscap_1d.txt`, `dump_pn_diode_circuit.txt` — qo'shimcha tekshiruv/qiyoslash uchun.
- `schottky_contact.java` va boshqa `.java` fayllar — bular ASLIDA `.mph` (zip/binary) formatida
  saqlanib qoldi, chunki `model.save(path)` COMSOL 6.0 batch muhitida kengaytmadan qat'i nazar
  har doim to'liq .mph formatini yozadi (haqiqiy Java source eksporti faqat Desktop GUI'ning
  "File > Save As > Java" menyusida ishlaydi, runtime API orqali emas). Shuning uchun API
  ma'lumotlari introspeksiya (`IntrospectModel.java`, yuqoridagi dump_*.txt) orqali olindi —
  bu usul hujjatdan ko'ra ishonchliroq, chunki COMSOL'ning o'zidan, ishlayotgan namunalardan
  to'g'ridan-to'g'ri o'qildi.
