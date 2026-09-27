/*
 * Model1_Vertical.java  —  1-MODEL, 1-BOSQICH ("tutun testi")
 *
 * Troyan & Doronin, ICCS 2020 (LNNS 186, 427-433, 2021, doi:10.1007/978-3-030-66093-2_41)
 * maqolasidagi GeTe/Sb2Te3 interfeysli xotira elementining FENOMENOLOGIK modeli.
 *
 * Struktura (2D o'q-simmetrik, pastdan yuqoriga, r = 0 simmetriya o'qi):
 *   pastki elektrod (BE) / Sb2Te3 / interfeys qatlami [filament r<r_f | halqa r>r_f] / GeTe / yuqori elektrod (TE)
 *
 * Bu bosqichda:  GEOMETRY + MATERIALS + Electric Currents (ec) + S1 Stationary (x = 0 va x = 1).
 * Keyingi bosqichlar (hali YO'Q): Heat Transfer + Electromagnetic Heating, x uchun Global ODE,
 * Time Dependent, to'liq natijalar.
 *
 * Holat o'zgaruvchisi x bu bosqichda oddiy PARAMETR (xs). 2-bosqichda u Global ODE ga almashtiriladi,
 * sig_fil o'zgaruvchisining ko'rinishi esa o'zgarmaydi: sig_fil = sig_off^(1-x) * sig_on^x.
 *
 * Kerakli litsenziya: faqat COMSOL Multiphysics (ConductiveMedia bazaviy paketda bor).
 *
 * "// TEKSHIRILSIN" belgisi: COMSOL 6.0 API da nomi yoki xatti-harakati 100% aniq bo'lmagan chaqiruv.
 *
 * Ishga tushirish (Windows, fayl joylashgan papkada):
 *   "C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64\comsolcompile.exe" Model1_Vertical.java
 *   "C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64\comsolbatch.exe" -inputfile Model1_Vertical.class -outputfile Model1_Vertical.mph
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class Model1_Vertical {

  // Natija fayllari (CSV, MPH) comsolbatch ishga tushirilgan papkaga yoziladi.
  static final String OUT_DIR = System.getProperty("user.dir");

  // S1 da sig_off va sig_on ni R_OFF / R_ON maqsadlariga avtomatik moslash (oddiy qat'iy nuqta iteratsiyasi).
  // false qilinsa, parametrlar jadvalidagi boshlang'ich qiymatlar o'zgarmaydi.
  static final boolean CALIBRATE = true;
  static final int CAL_MAX_IT = 8;
  static final double CAL_TOL = 0.01;   // 1 %

  public static Model run() {
    Model model = ModelUtil.create("Model");
    model.label("Model1_Vertical.mph");
    model.comments("Troyan & Doronin (2021) GeTe/Sb2Te3 interfeys xotira elementining fenomenologik modeli. "
        + "1-bosqich: geometriya, materiallar, Electric Currents, S1 Stationary (x = 0 va x = 1).");

    // =====================================================================================
    // PARAMETERS
    // =====================================================================================
    // --- Geometriya ---
    p(model, "R_dev", "50[nm]", "Element radiusi");
    p(model, "t_be", "10[nm]", "Pastki elektrod qalinligi (faraz)");
    p(model, "t_ST", "20[nm]", "Sb2Te3 qalinligi");
    p(model, "t_int", "1.5[nm]", "Interfeys (vdW) qatlami qalinligi");
    p(model, "t_GT", "20[nm]", "GeTe qalinligi");
    p(model, "t_te", "10[nm]", "Yuqori elektrod qalinligi (faraz)");
    p(model, "r_f", "5[nm]", "Filament radiusi");
    p(model, "z_ST", "t_be", "Sb2Te3 pastki chegarasi");
    p(model, "z_int", "t_be+t_ST", "Interfeys pastki chegarasi");
    p(model, "z_GT", "z_int+t_int", "GeTe pastki chegarasi");
    p(model, "z_te", "z_GT+t_GT", "Yuqori elektrod pastki chegarasi");
    p(model, "z_top", "z_te+t_te", "Strukturaning yuqori chegarasi");

    // --- Elektr ---
    p(model, "sig_m", "5e6[S/m]", "Elektrod (TiN, faraz) o'tkazuvchanligi");
    p(model, "sig_ST", "1e5[S/m]", "Sb2Te3 o'tkazuvchanligi (degenerat p-tip, taxmin)");
    p(model, "sig_GT", "1e5[S/m]", "GeTe o'tkazuvchanligi (degenerat p-tip, taxmin)");
    p(model, "sig_off", "1.9[S/m]", "Interfeys OFF o'tkazuvchanligi (fitting: R_OFF)");
    p(model, "sig_on", "3e3[S/m]", "Filament ON o'tkazuvchanligi (fitting: R_ON)");
    p(model, "epsr_m", "1", "Elektrod nisbiy dielektrik singdiruvchanligi (formal)");
    p(model, "epsr_ST", "50", "Sb2Te3 epsilon_r (taxmin, stationar EC ga ta'sir qilmaydi)");
    p(model, "epsr_GT", "30", "GeTe epsilon_r (taxmin, stationar EC ga ta'sir qilmaydi)");
    p(model, "epsr_int", "10", "Interfeys epsilon_r (taxmin)");

    // --- Issiqlik (2-bosqichda ishlatiladi; hozir materiallarga yoziladi) ---
    p(model, "k_m", "20[W/(m*K)]", "Elektrod issiqlik o'tkazuvchanligi (yupqa TiN, taxmin)");
    p(model, "k_ST", "1.0[W/(m*K)]", "Sb2Te3 issiqlik o'tkazuvchanligi (c-o'q bo'yicha, taxmin)");
    p(model, "k_GT", "2.0[W/(m*K)]", "GeTe issiqlik o'tkazuvchanligi (taxmin)");
    p(model, "k_int", "0.5[W/(m*K)]", "Interfeys (vdW bo'shliq) issiqlik o'tkazuvchanligi (taxmin)");
    p(model, "rho_m", "5220[kg/m^3]", "TiN zichligi");
    p(model, "rho_ST", "6500[kg/m^3]", "Sb2Te3 zichligi");
    p(model, "rho_GT", "6140[kg/m^3]", "GeTe zichligi");
    p(model, "rho_int", "6300[kg/m^3]", "Interfeys zichligi (ST va GT o'rtachasi, faraz)");
    p(model, "Cp_m", "600[J/(kg*K)]", "TiN issiqlik sig'imi");
    p(model, "Cp_ST", "200[J/(kg*K)]", "Sb2Te3 Cp (Dyulong-Pti: 15R/M)");
    p(model, "Cp_GT", "250[J/(kg*K)]", "GeTe Cp (Dyulong-Pti: 6R/M)");
    p(model, "Cp_int", "220[J/(kg*K)]", "Interfeys Cp (faraz)");
    p(model, "Tm_GT", "998[K]", "GeTe erish harorati (~725 C)");
    p(model, "Tm_ST", "891[K]", "Sb2Te3 erish harorati (~618 C)");
    p(model, "T_amb", "300[K]", "Atrof harorati");

    // --- Filament kinetikasi (3-bosqichda ishlatiladi) ---
    p(model, "xs", "0", "Filament holati (1-bosqichda parametr, keyin Global ODE)");
    p(model, "k0", "1e13[1/s]", "Urinish chastotasi (fonon chastotasi tartibi)");
    p(model, "Ea", "0.9[eV]", "Migratsiya aktivatsiya energiyasi (fitting)");
    p(model, "a_hop", "0.3[nm]", "Sakrash masofasi (fitting)");
    p(model, "tau_rel", "1e3[s]", "Relaksatsiya vaqti (katta -> nonvolatil)");
    p(model, "p_win", "2", "Joglekar/Biolek oynasi darajasi");

    // --- Signal ---
    p(model, "V_read", "0.1[V]", "S1 dagi o'qish kuchlanishi");
    p(model, "V_app", "V_read", "Terminal kuchlanishi (2-bosqichda V(t) ga almashadi)");
    p(model, "Vamp", "4.5[V]", "Uchburchak signal amplitudasi");
    p(model, "f0", "100[Hz]", "Signal chastotasi");

    // --- Fitting maqsadlari (Fig. 1a, grafikdan taxminan o'qilgan) ---
    p(model, "R_ON_t", "7[kohm]", "Maqsad R_ON (0.43 mA, 3 V)");
    p(model, "R_OFF_t", "100[kohm]", "Maqsad R_OFF");
    p(model, "V_SET_t", "3.5[V]", "Maqsad V_SET");
    p(model, "V_RESET_t", "-3.5[V]", "Maqsad V_RESET");

    // --- To'r ---
    p(model, "h_int", "0.5[nm]", "Interfeys va filamentda maksimal element");
    p(model, "h_glob", "2[nm]", "Qolgan joyda maksimal element");

    // =====================================================================================
    // GEOMETRY
    // =====================================================================================
    model.component().create("comp1", true);
    GeomSequence g = model.component("comp1").geom().create("geom1", 2);
    g.axisymmetric(true);
    g.lengthUnit("nm");

    // Interfeys ikkita alohida to'rtburchakdan quriladi (filament + halqa): ustma-ust tushish yo'q,
    // shuning uchun har birining "selresult" selectioni toza qoladi.
    rect(g, "r_be", "R_dev", "t_be", "0", "0");
    rect(g, "r_st", "R_dev", "t_ST", "0", "z_ST");
    rect(g, "r_fil", "r_f", "t_int", "0", "z_int");
    rect(g, "r_ring", "R_dev-r_f", "t_int", "r_f", "z_int");
    rect(g, "r_gt", "R_dev", "t_GT", "0", "z_GT");
    rect(g, "r_te", "R_dev", "t_te", "0", "z_te");

    // Chegara selectionlari geometriya ichida (parametrlar o'zgarsa qayta hisoblanadi).
    // Koordinatalar geometriya birligida (nm).
    boxSel(g, "box_top", "-1", "R_dev+1[nm]", "z_top-0.01[nm]", "z_top+0.01[nm]");   // Terminal
    boxSel(g, "box_bot", "-1", "R_dev+1[nm]", "-0.01", "0.01");                        // Ground
    boxSel(g, "box_out", "R_dev-0.01[nm]", "R_dev+0.01[nm]", "-1", "z_top+1[nm]");      // tashqi yon devor (keyinchalik)
    g.run();

    // Domen birlashmalari
    union(model, "sel_int", 2, new String[]{"geom1_r_fil_dom", "geom1_r_ring_dom"}, "Interfeys qatlami (filament + halqa)");
    union(model, "sel_el", 2, new String[]{"geom1_r_be_dom", "geom1_r_te_dom"}, "Elektrodlar");

    // =====================================================================================
    // DEFINITIONS (o'zgaruvchilar va operatorlar)
    // =====================================================================================
    model.component("comp1").variable().create("var1");
    model.component("comp1").variable("var1").label("Filament o'tkazuvchanligi");
    // sig_off^(1-x)*sig_on^x ni birliklar bo'yicha to'g'ri ko'rinishda yozamiz:
    model.component("comp1").variable("var1").set("sig_fil", "sig_off*(sig_on/sig_off)^xs",
        "Filament o'tkazuvchanligi: sig_off^(1-x)*sig_on^x");

    // Filament bo'yicha hajmiy o'rtacha (keyingi bosqichda Global ODE uchun <Ez> va <T>)
    model.component("comp1").cpl().create("aveop_fil", "Average");
    model.component("comp1").cpl("aveop_fil").selection().named("geom1_r_fil_dom");
    model.component("comp1").cpl("aveop_fil").set("axisym", true);   // TEKSHIRILSIN: 2*pi*r og'irlik bilan o'rtacha
    model.component("comp1").cpl().create("intop_top", "Integration");
    model.component("comp1").cpl("intop_top").selection().geom("geom1", 1);
    model.component("comp1").cpl("intop_top").selection().named("geom1_box_top");
    model.component("comp1").cpl("intop_top").set("axisym", true);   // TEKSHIRILSIN

    // =====================================================================================
    // MATERIALS (qiymatlar faqat parametrlardan; Material Library ishlatilmaydi)
    // =====================================================================================
    material(model, "mat_el", "Elektrod (TiN, faraz)", "sel_el", "sig_m", "epsr_m", "k_m", "rho_m", "Cp_m");
    material(model, "mat_ST", "Sb2Te3", "geom1_r_st_dom", "sig_ST", "epsr_ST", "k_ST", "rho_ST", "Cp_ST");
    material(model, "mat_GT", "GeTe", "geom1_r_gt_dom", "sig_GT", "epsr_GT", "k_GT", "rho_GT", "Cp_GT");
    material(model, "mat_ring", "Interfeys halqasi (doim OFF)", "geom1_r_ring_dom", "sig_off", "epsr_int", "k_int", "rho_int", "Cp_int");
    material(model, "mat_fil", "Filament (GeSbTe4, sig_fil(x))", "geom1_r_fil_dom", "sig_fil", "epsr_int", "k_int", "rho_int", "Cp_int");

    // =====================================================================================
    // PHYSICS: Electric Currents
    // =====================================================================================
    model.component("comp1").physics().create("ec", "ConductiveMedia", "geom1");
    // cucn1 (Current Conservation) sigma va epsilon_r ni materiallardan oladi (default "from material").
    // r = 0 o'qi avtomatik simmetriya o'qi; qolgan tashqi chegaralar Electric Insulation (default).

    model.component("comp1").physics("ec").create("term1", "Terminal", 1);
    model.component("comp1").physics("ec").feature("term1").label("Yuqori elektrod (GeTe tomoni)");
    model.component("comp1").physics("ec").feature("term1").selection().named("geom1_box_top");
    model.component("comp1").physics("ec").feature("term1").set("TerminalType", "Voltage");
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_app");

    model.component("comp1").physics("ec").create("gnd1", "Ground", 1);
    model.component("comp1").physics("ec").feature("gnd1").label("Pastki elektrod (Sb2Te3 tomoni)");
    model.component("comp1").physics("ec").feature("gnd1").selection().named("geom1_box_bot");

    // Qutb kelishuvi: V_app > 0  <=>  GeTe tomoni musbat  <=>  interfeysda Ez < 0 (maydon pastga).
    // Bu maqoladagi SET qutbiga mos keladi (keyingi bosqichda E_drive = -<Ez> ishlatiladi).

    // =====================================================================================
    // MULTIPHYSICS
    // =====================================================================================
    // 1-bosqichda yo'q. 2-bosqichda: Heat Transfer in Solids + Electromagnetic Heating.

    // =====================================================================================
    // MESH
    // =====================================================================================
    model.component("comp1").mesh().create("mesh1");
    model.component("comp1").mesh("mesh1").feature("size").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size").set("hmax", "h_glob");
    model.component("comp1").mesh("mesh1").feature("size").set("hmin", "0.02[nm]");
    model.component("comp1").mesh("mesh1").feature("size").set("hgrad", 1.2);

    model.component("comp1").mesh("mesh1").create("size_int", "Size");
    model.component("comp1").mesh("mesh1").feature("size_int").selection().geom("geom1", 2);
    model.component("comp1").mesh("mesh1").feature("size_int").selection().named("sel_int");
    model.component("comp1").mesh("mesh1").feature("size_int").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size_int").set("hmaxactive", true);
    model.component("comp1").mesh("mesh1").feature("size_int").set("hmax", "h_int");

    model.component("comp1").mesh("mesh1").create("ftri1", "FreeTri");
    model.component("comp1").mesh("mesh1").run();

    // =====================================================================================
    // STUDY
    // =====================================================================================
    // std1: bitta Stationary (xs parametrining joriy qiymati bilan) -> kalibrovka sikli uchun.
    model.study().create("std1");
    model.study("std1").label("S1a: Stationary (kalibrovka, bitta holat)");
    model.study("std1").create("stat", "Stationary");

    // std2: Stationary + auxiliary sweep xs = 0, 1 -> R_OFF va R_ON bitta datasetda (GUI va eksport uchun).
    model.study().create("std2");
    model.study("std2").label("S1: Stationary, x = 0 (OFF) va x = 1 (ON)");
    model.study("std2").create("stat", "Stationary");
    model.study("std2").feature("stat").set("useparam", true);
    model.study("std2").feature("stat").set("pname", new String[]{"xs"});
    model.study("std2").feature("stat").set("plistarr", new String[]{"0 1"});
    model.study("std2").feature("stat").set("punit", new String[]{""});

    // =====================================================================================
    // SOLVER
    // =====================================================================================
    // Masala chiziqli (sigma maydonga bog'liq emas), shuning uchun default solver yetarli.
    // study.run() solver ketma-ketligini (sol1 -> dset1) avtomatik yaratadi.   // TEKSHIRILSIN: dataset nomlari
    model.study("std1").run();

    // Bitta ifodali global baholash (getReal()[0][0] indekslash bir ma'noli bo'lsin)
    model.result().numerical().create("gev_R", "EvalGlobal");
    model.result().numerical("gev_R").set("data", "dset1");
    model.result().numerical("gev_R").set("expr", new String[]{"V_app/ec.I0_1"});   // [ohm], SI

    double rOffT = 100e3, rOnT = 7e3;   // R_OFF_t, R_ON_t parametrlari bilan bir xil
    double sigOff = 1.9, sigOn = 3e3;   // sig_off, sig_on boshlang'ich qiymatlari [S/m]
    String calCsv = new File(OUT_DIR, "S1_calibration.csv").getAbsolutePath();
    try (PrintWriter out = new PrintWriter(new FileWriter(calCsv))) {
      out.println("step,state,sig_off_S_per_m,sig_on_S_per_m,R_ohm,target_ohm");
      if (CALIBRATE) {
        // (1) R_OFF faqat sig_off ga bog'liq (x = 0 da filament ham sig_off).
        for (int it = 0; it < CAL_MAX_IT; it++) {
          double r = solveR(model, "0");
          out.println(it + ",OFF," + e(sigOff) + "," + e(sigOn) + "," + e(r) + "," + e(rOffT));
          System.out.println(String.format(Locale.US, "CAL OFF it=%d  sig_off=%.4g S/m  R_OFF=%.4g ohm", it, sigOff, r));
          if (Math.abs(r / rOffT - 1) < CAL_TOL) break;
          sigOff *= clamp(r / rOffT);
          model.param().set("sig_off", String.format(Locale.US, "%.4g[S/m]", sigOff));
        }
        // (2) R_ON = R_ketma-ket + R_filament || R_halqa. Multiplikativ yangilash R_ketma-ket < R_ON_t bo'lsa yaqinlashadi.
        for (int it = 0; it < CAL_MAX_IT; it++) {
          double r = solveR(model, "1");
          out.println(it + ",ON," + e(sigOff) + "," + e(sigOn) + "," + e(r) + "," + e(rOnT));
          System.out.println(String.format(Locale.US, "CAL ON  it=%d  sig_on=%.4g S/m  R_ON=%.4g ohm", it, sigOn, r));
          if (Math.abs(r / rOnT - 1) < CAL_TOL) break;
          sigOn *= clamp(r / rOnT);
          model.param().set("sig_on", String.format(Locale.US, "%.4g[S/m]", sigOn));
        }
      }
      double rOff = solveR(model, "0");
      double rOn = solveR(model, "1");
      out.println("final,OFF," + e(sigOff) + "," + e(sigOn) + "," + e(rOff) + "," + e(rOffT));
      out.println("final,ON," + e(sigOff) + "," + e(sigOn) + "," + e(rOn) + "," + e(rOnT));
      System.out.println(String.format(Locale.US,
          "S1 NATIJA: sig_off=%.4g S/m, sig_on=%.4g S/m -> R_OFF=%.4g ohm, R_ON=%.4g ohm, R_OFF/R_ON=%.3g, I_ON(3V)=%.3g mA",
          sigOff, sigOn, rOff, rOn, rOff / rOn, 3.0 / rOn * 1e3));
    } catch (IOException ex) {
      System.out.println("XATO (CSV): " + calCsv + " : " + ex.getMessage());
    }
    model.param().set("xs", "0");

    // Asosiy S1 (x = 0 va x = 1) -> sol2 / dset2
    model.study("std2").run();

    // =====================================================================================
    // RESULTS
    // =====================================================================================
    // N6 (qisman): R_OFF, R_ON jadvali
    model.result().table().create("tbl_S1", "Table");
    model.result().table("tbl_S1").label("S1: R_OFF va R_ON");
    model.result().numerical().create("gev_S1", "EvalGlobal");
    model.result().numerical("gev_S1").label("S1: qarshilik va tok");
    model.result().numerical("gev_S1").set("data", "dset2");
    model.result().numerical("gev_S1").set("expr", new String[]{
        "V_app/ec.I0_1",
        "ec.I0_1",
        "3[V]*ec.I0_1/V_app",
        "-aveop_fil(ec.Ez)*t_int/V_app",
        "intop_top(ec.nJ)"});
    model.result().numerical("gev_S1").set("unit", new String[]{"kohm", "uA", "mA", "1", "uA"});
    model.result().numerical("gev_S1").set("descr", new String[]{
        "R = V_app/I",
        "Terminal toki",
        "Chiziqli ekstrapolyatsiya: I(3 V)",
        "Filamentdagi kuchlanish ulushi",
        "Tekshiruv: yuqori chegara orqali tok"});
    model.result().numerical("gev_S1").set("table", "tbl_S1");
    model.result().numerical("gev_S1").setResult();

    // Potensial xaritasi
    model.result().create("pg_V", "PlotGroup2D");
    model.result("pg_V").label("S1: Elektr potensiali V");
    model.result("pg_V").set("data", "dset2");
    model.result("pg_V").create("surf1", "Surface");
    model.result("pg_V").feature("surf1").set("expr", "V");
    model.result("pg_V").create("con1", "Contour");
    model.result("pg_V").feature("con1").set("expr", "V");

    // Tok zichligi (log) xaritasi (N5 ning boshlang'ich varianti)
    model.result().create("pg_J", "PlotGroup2D");
    model.result("pg_J").label("S1: log10|J|");
    model.result("pg_J").set("data", "dset2");
    model.result("pg_J").create("surf1", "Surface");
    model.result("pg_J").feature("surf1").set("expr", "log10(ec.normJ/(1[A/m^2]))");
    model.result("pg_J").create("str1", "Streamline");
    model.result("pg_J").feature("str1").set("expr", new String[]{"ec.Jr", "ec.Jz"});   // TEKSHIRILSIN: o'q-simmetrik komponent nomlari

    // Simmetriya o'qi bo'ylab V(z) (x = 0 va x = 1)
    model.result().dataset().create("cln_axis", "CutLine2D");
    model.result().dataset("cln_axis").set("data", "dset2");
    model.result().dataset("cln_axis").set("genpoints", new String[][]{{"0", "0"}, {"0", "z_top"}});   // TEKSHIRILSIN: birlik (nm)
    model.result().create("pg_Vz", "PlotGroup1D");
    model.result("pg_Vz").label("S1: V(z), r = 0");
    model.result("pg_Vz").set("data", "cln_axis");
    model.result("pg_Vz").create("lngr1", "LineGraph");
    model.result("pg_Vz").feature("lngr1").set("expr", "V");
    model.result("pg_Vz").feature("lngr1").set("xdata", "expr");
    model.result("pg_Vz").feature("lngr1").set("xdataexpr", "z");
    model.result("pg_Vz").feature("lngr1").set("legend", true);

    // =====================================================================================
    // EXPORT
    // =====================================================================================
    // Har bir eksport alohida try/catch ichida: biri xato bersa ham qolgani va .mph saqlanadi.
    try {
      model.result().export().create("exp_S1", "Table");
      model.result().export("exp_S1").set("table", "tbl_S1");
      model.result().export("exp_S1").set("filename", new File(OUT_DIR, "S1_R_on_off.csv").getAbsolutePath());
      model.result().export("exp_S1").run();
    } catch (Exception ex) {
      System.out.println("XATO (eksport S1_R_on_off.csv): " + ex.getMessage());
    }
    try {
      model.result().export().create("exp_Vz", "Plot");
      model.result().export("exp_Vz").set("plotgroup", "pg_Vz");
      model.result().export("exp_Vz").set("plot", "lngr1");
      model.result().export("exp_Vz").set("filename", new File(OUT_DIR, "S1_V_axis.csv").getAbsolutePath());
      model.result().export("exp_Vz").run();
    } catch (Exception ex) {
      System.out.println("XATO (eksport S1_V_axis.csv): " + ex.getMessage());
    }

    try {
      model.save(new File(OUT_DIR, "Model1_Vertical.mph").getAbsolutePath());
    } catch (IOException ex) {
      throw new RuntimeException("MPH faylni saqlab bo'lmadi", ex);
    }
    System.out.println("TAYYOR: " + OUT_DIR);
    return model;
  }

  // =====================================================================================
  // Yordamchi metodlar
  // =====================================================================================

  static void p(Model model, String name, String expr, String descr) {
    model.param().set(name, expr, descr);
  }

  static void rect(GeomSequence g, String tag, String w, String h, String r0, String z0) {
    g.create(tag, "Rectangle");
    g.feature(tag).set("size", new String[]{w, h});
    g.feature(tag).set("pos", new String[]{r0, z0});
    g.feature(tag).set("selresult", true);   // "geom1_<tag>_dom" selectionini yaratadi
  }

  // Geometriya darajasidagi Box Selection -> "geom1_<tag>" nomli chegara selectioni.
  // TEKSHIRILSIN: o'q-simmetrik geometriyada xossa nomlari "xmin/xmax/ymin/ymax" (r va z uchun) deb faraz qilindi.
  static void boxSel(GeomSequence g, String tag, String rMin, String rMax, String zMin, String zMax) {
    g.create(tag, "BoxSelection");
    g.feature(tag).set("entitydim", 1);
    g.feature(tag).set("xmin", rMin);
    g.feature(tag).set("xmax", rMax);
    g.feature(tag).set("ymin", zMin);
    g.feature(tag).set("ymax", zMax);
    g.feature(tag).set("condition", "inside");
  }

  static void union(Model model, String tag, int dim, String[] inputs, String label) {
    model.component("comp1").selection().create(tag, "Union");
    model.component("comp1").selection(tag).set("entitydim", dim);
    model.component("comp1").selection(tag).set("input", inputs);
    model.component("comp1").selection(tag).label(label);
  }

  static void material(Model model, String tag, String label, String sel,
                       String sigma, String epsr, String k, String rho, String cp) {
    model.component("comp1").material().create(tag, "Common");
    model.component("comp1").material(tag).label(label);
    model.component("comp1").material(tag).selection().named(sel);
    model.component("comp1").material(tag).propertyGroup("def").set("electricconductivity", new String[]{sigma});
    model.component("comp1").material(tag).propertyGroup("def").set("relpermittivity", new String[]{epsr});
    model.component("comp1").material(tag).propertyGroup("def").set("thermalconductivity", new String[]{k});
    model.component("comp1").material(tag).propertyGroup("def").set("density", rho);
    model.component("comp1").material(tag).propertyGroup("def").set("heatcapacity", cp);
  }

  /** xs = x qiymati bilan std1 ni yechib, R = V_app/I [ohm] ni qaytaradi. */
  static double solveR(Model model, String x) {
    model.param().set("xs", x);
    model.study("std1").run();
    return model.result().numerical("gev_R").getReal()[0][0];
  }

  /** Bir iteratsiyadagi o'zgarishni 0.1...10 marta bilan cheklaydi. */
  static double clamp(double f) {
    return Math.max(0.1, Math.min(10.0, f));
  }

  static String e(double v) {
    return String.format(Locale.US, "%.6e", v);
  }

  public static void main(String[] args) {
    run();
  }
}
