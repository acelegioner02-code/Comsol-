/*
 * GeTeSb2Te3_RON.java
 *
 * 1-model: tashqi bosim -> deformatsiya -> GeSbTe4 filament qalinligi -> R_ON
 * (Troyan & Doronin, ICCS 2020, LNNS 186, 427-433 maqolasi asosida)
 *
 * Geometriya: 2D o'qqa simmetrik stek (pastdan yuqoriga)
 *   TiN (BE) / Sb2Te3 / [filament | interfeys] / GeTe / TiN (TE)
 * Fizika:     Solid Mechanics (solid) + Electric Currents (ec)
 * Natija:     R_ON(p_ext) jadvali, k_state = 1 (xotira) va k_state = 0 (kalitlash)
 *             -> R_ON_vs_pressure.csv va GeTe_Sb2Te3_RON.mph
 *
 * Faqat bazaviy COMSOL Multiphysics litsenziyasi kerak.
 * Material parametrlari TAXMINIY (o'quv uchun). Ularni o'z ma'lumotlaringiz bilan almashtiring.
 *
 * Kompilyatsiya va ishga tushirish: README_JAVA.md ga qarang.
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class GeTeSb2Te3_RON {

  // ---------- Sozlanadigan qiymatlar ----------
  // Geometriya [nm]. Bu qiymatlar COMSOL parametrlariga ham yoziladi
  // va Box selectionlarning koordinatalari uchun ham ishlatiladi.
  static final double R_CELL = 100, T_BE = 20, T_ST = 20, T_F0 = 3, T_GT = 20, T_TE = 20, R_F = 10;

  // Bosim sweep [MPa]: P_START dan P_STOP gacha, qadam P_STEP
  static final double P_START = 0, P_STOP = 2000, P_STEP = 100;

  // Holatlar: 1 = xotira (bosimga sezgir), 0 = kalitlash (sezgir emas)
  static final int[] K_STATES = {1, 0};

  // Maksimal to'r elementi o'lchami [nm]. Filament qatlami 3 nm, shuning uchun <= 0.5 bo'lsin.
  static final String H_MAX = "0.5";

  // Natija fayllari joriy papkaga yoziladi.
  static final String OUT_DIR = System.getProperty("user.dir");
  // --------------------------------------------

  public static Model run() {
    Model model = ModelUtil.create("Model");
    model.label("GeTe_Sb2Te3_RON.mph");
    model.comments("R_ON under external pressure for a GeTe/Sb2Te3 interfacial memory cell "
        + "(phenomenological, Troyan & Doronin 2021).");

    // ===== 1. Parametrlar =====
    model.param().set("p_ext", "0[MPa]", "Tashqi bosim");
    model.param().set("V_read", "0.05[V]", "O'qish kuchlanishi");
    model.param().set("R_cell", R_CELL + "[nm]", "Yacheyka radiusi");
    model.param().set("t_BE", T_BE + "[nm]", "Pastki elektrod (TiN)");
    model.param().set("t_ST", T_ST + "[nm]", "Sb2Te3 qatlami");
    model.param().set("t_f0", T_F0 + "[nm]", "GeSbTe4 filament qatlami (vdW)");
    model.param().set("t_GT", T_GT + "[nm]", "GeTe qatlami");
    model.param().set("t_TE", T_TE + "[nm]", "Yuqori elektrod (TiN)");
    model.param().set("r_f", R_F + "[nm]", "Filament radiusi");
    model.param().set("sigma_on", "1e4[S/m]", "Filament o'tkazuvchanligi (ON)");
    model.param().set("sigma_off", "1[S/m]", "Filament atrofi");
    model.param().set("lambda_t", "0.05[nm]", "Qalinlikka sezgirlik uzunligi");
    model.param().set("k_state", "1", "1 = xotira, 0 = kalitlash");
    model.param().set("E_f", "20[GPa]", "Filament qatlami Young moduli");
    // Materiallar (taxminiy)
    model.param().set("E_TiN", "250[GPa]");  model.param().set("sig_TiN", "5e6[S/m]");
    // Kristall GeTe va Sb2Te3 degenerat p-tip, sigma ~1e5-1e6 S/m. Ular filamentdan ancha yaxshi o'tkazishi kerak,
    // aks holda ketma-ket qarshilik R_ON ni "yopib qo'yadi" (README, 4-bo'lim).
    model.param().set("E_ST", "55[GPa]");    model.param().set("sig_ST", "1e6[S/m]");
    model.param().set("E_GT", "50[GPa]");    model.param().set("sig_GT", "1e6[S/m]");
    model.param().set("nu0", "0.25", "Puasson koeffitsiyenti (hammasi uchun)");

    // ===== 2. Komponent va geometriya =====
    model.component().create("comp1", true);
    GeomSequence g = model.component("comp1").geom().create("geom1", 2);
    g.axisymmetric(true);
    g.lengthUnit("nm");

    addRect(g, "r1", "R_cell", "t_BE", "0", "0");                                   // TiN BE
    addRect(g, "r2", "R_cell", "t_ST", "0", "t_BE");                                // Sb2Te3
    addRect(g, "r3", "r_f", "t_f0", "0", "t_BE+t_ST");                              // filament
    addRect(g, "r4", "R_cell-r_f", "t_f0", "r_f", "t_BE+t_ST");                     // interfeys qolgani
    addRect(g, "r5", "R_cell", "t_GT", "0", "t_BE+t_ST+t_f0");                      // GeTe
    addRect(g, "r6", "R_cell", "t_TE", "0", "t_BE+t_ST+t_f0+t_GT");                 // TiN TE
    g.run();

    // ===== 3. Selectionlar =====
    double zTop = T_BE + T_ST + T_F0 + T_GT + T_TE;
    double eps = 1e-3;
    boxSel(model, "sel_bot", -eps, R_CELL + eps, -eps, eps);            // z = 0
    boxSel(model, "sel_top", -eps, R_CELL + eps, zTop - eps, zTop + eps); // z = zTop
    boxSel(model, "sel_out", R_CELL - eps, R_CELL + eps, -eps, zTop + eps); // r = R_cell

    model.component("comp1").selection().create("sel_TiN", "Union");
    model.component("comp1").selection("sel_TiN").set("entitydim", 2);
    model.component("comp1").selection("sel_TiN").set("input", new String[]{"geom1_r1_dom", "geom1_r6_dom"});
    model.component("comp1").selection("sel_TiN").label("TiN elektrodlar");

    model.component("comp1").selection().create("sel_intf", "Union");
    model.component("comp1").selection("sel_intf").set("entitydim", 2);
    model.component("comp1").selection("sel_intf").set("input", new String[]{"geom1_r3_dom", "geom1_r4_dom"});
    model.component("comp1").selection("sel_intf").label("Interfeys qatlami (filament + atrofi)");

    // ===== 4. O'zgaruvchilar =====
    model.component("comp1").variable().create("var1");
    model.component("comp1").variable("var1").set("R_ON", "V_read/ec.I0_1", "ON-holat qarshiligi");
    model.component("comp1").variable("var1").set("sigma_fil",
        "sigma_on*exp(-k_state*t_f0*solid.eZZ/lambda_t)", "Filament o'tkazuvchanligi (deformatsiyaga bog'liq)");
    // Filamentning o'z qarshiligi (bir jinsli silindr deb baholangan), ketma-ket qarshiliklarsiz
    model.component("comp1").cpl().create("aveop1", "Average");
    model.component("comp1").cpl("aveop1").selection().named("geom1_r3_dom");
    model.component("comp1").variable("var1").set("R_fil",
        "t_f0*(1+aveop1(solid.eZZ))/(aveop1(sigma_fil)*pi*r_f^2)", "Filamentning o'z qarshiligi");

    // ===== 5. Solid Mechanics =====
    model.component("comp1").physics().create("solid", "SolidMechanics", "geom1");
    // lemm1 barcha domenlarda TiN qiymatlari bilan boshlanadi. Keyingi featurelar o'z domenlarida uni almashtiradi.
    elastic(model, "lemm1", null, "E_TiN");
    elastic(model, "lemm2", "geom1_r2_dom", "E_ST");
    elastic(model, "lemm3", "sel_intf", "E_f");
    elastic(model, "lemm4", "geom1_r5_dom", "E_GT");

    model.component("comp1").physics("solid").create("fix1", "Fixed", 1);
    model.component("comp1").physics("solid").feature("fix1").selection().named("sel_bot");

    model.component("comp1").physics("solid").create("roll1", "Roller", 1);
    model.component("comp1").physics("solid").feature("roll1").selection().named("sel_out");

    // Tekis yuqori sirtga normal bosim: F/A = (0, 0, -p_ext) (r, phi, z)
    model.component("comp1").physics("solid").create("bndl1", "BoundaryLoad", 1);
    model.component("comp1").physics("solid").feature("bndl1").selection().named("sel_top");
    model.component("comp1").physics("solid").feature("bndl1").set("FperArea", new String[]{"0", "0", "-p_ext"});

    // ===== 6. Electric Currents =====
    model.component("comp1").physics().create("ec", "ConductiveMedia", "geom1");
    conduct(model, "cucn1", null, "sig_TiN");            // hamma joyda TiN, keyin almashtiriladi
    conduct(model, "cucn2", "geom1_r2_dom", "sig_ST");
    conduct(model, "cucn3", "geom1_r4_dom", "sigma_off");
    conduct(model, "cucn4", "geom1_r5_dom", "sig_GT");
    conduct(model, "cucn5", "geom1_r3_dom", "sigma_fil"); // filament: deformatsiyaga bog'liq

    model.component("comp1").physics("ec").create("gnd1", "Ground", 1);
    model.component("comp1").physics("ec").feature("gnd1").selection().named("sel_bot");

    model.component("comp1").physics("ec").create("term1", "Terminal", 1);
    model.component("comp1").physics("ec").feature("term1").selection().named("sel_top");
    model.component("comp1").physics("ec").feature("term1").set("TerminalType", "Voltage");
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_read");

    // ===== 7. To'r (Mesh): hamma domenlar to'rtburchak, shuning uchun Mapped =====
    model.component("comp1").mesh().create("mesh1");
    model.component("comp1").mesh("mesh1").feature("size").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size").set("hmax", H_MAX);
    model.component("comp1").mesh("mesh1").feature("size").set("hmin", "0.05");
    model.component("comp1").mesh("mesh1").create("map1", "Map");
    model.component("comp1").mesh("mesh1").run();

    // ===== 8. Tadqiqot =====
    model.study().create("std1");
    model.study("std1").create("stat", "Stationary");
    model.study("std1").label("Stationary: bosim -> R_ON");

    // ===== 9. Birinchi yechim va global baholash =====
    // dset1 dataset faqat birinchi yechimdan keyin paydo bo'ladi, shuning uchun avval bir marta yechamiz.
    model.study("std1").run();
    model.result().numerical().create("gev1", "EvalGlobal");
    model.result().numerical("gev1").set("data", "dset1");
    model.result().numerical("gev1").set("expr", new String[]{"R_ON", "ec.I0_1", "R_fil"});

    // ===== 10. Sweep (Java sikli: har bir p va k_state uchun alohida yechiladi) =====
    String csv = OUT_DIR + java.io.File.separator + "R_ON_vs_pressure.csv";
    try (PrintWriter out = new PrintWriter(new FileWriter(csv))) {
      out.println("k_state,p_ext_MPa,R_ON_Ohm,I_A,R_fil_Ohm");
      for (int k : K_STATES) {
        model.param().set("k_state", Integer.toString(k));
        for (double p = P_START; p <= P_STOP + 1e-9; p += P_STEP) {
          model.param().set("p_ext", fmt(p) + "[MPa]");
          model.study("std1").run();
          double[][] v = model.result().numerical("gev1").getReal();
          double rOn = v[0][0], cur = v[1][0], rFil = v[2][0];
          out.println(k + "," + fmt(p) + "," + String.format(Locale.US, "%.6e", rOn)
              + "," + String.format(Locale.US, "%.6e", cur)
              + "," + String.format(Locale.US, "%.6e", rFil));
          System.out.println(String.format(Locale.US,
              "k_state=%d  p=%7.1f MPa  R_ON=%.4e Ohm  R_fil=%.4e Ohm", k, p, rOn, rFil));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("CSV yozib bo'lmadi: " + csv, e);
    }

    // Oxirgi yechim bo'yicha 2D rasmlar (GUI'da ochganda ko'rinadi)
    model.result().create("pg1", "PlotGroup2D");
    model.result("pg1").label("Deformatsiya eZZ");
    model.result("pg1").set("data", "dset1");
    model.result("pg1").create("surf1", "Surface");
    model.result("pg1").feature("surf1").set("expr", "solid.eZZ");

    model.result().create("pg2", "PlotGroup2D");
    model.result("pg2").label("Tok zichligi |J|");
    model.result("pg2").set("data", "dset1");
    model.result("pg2").create("surf1", "Surface");
    model.result("pg2").feature("surf1").set("expr", "log10(ec.normJ)");

    // Boshlang'ich holatga qaytarish (GUI'da ochganda parametrlar toza bo'lsin)
    model.param().set("k_state", "1");
    model.param().set("p_ext", fmt(P_STOP) + "[MPa]");

    try {
      model.save(OUT_DIR + java.io.File.separator + "GeTe_Sb2Te3_RON.mph");
    } catch (IOException e) {
      throw new RuntimeException("MPH faylni saqlab bo'lmadi", e);
    }
    System.out.println("Tayyor: " + csv);
    return model;
  }

  // ---------- Yordamchi metodlar ----------

  static void addRect(GeomSequence g, String tag, String w, String h, String r0, String z0) {
    g.create(tag, "Rectangle");
    g.feature(tag).set("size", new String[]{w, h});
    g.feature(tag).set("pos", new String[]{r0, z0});
    g.feature(tag).set("selresult", true);   // "geom1_<tag>_dom" selection hosil qiladi
  }

  static void boxSel(Model model, String tag, double rMin, double rMax, double zMin, double zMax) {
    model.component("comp1").selection().create(tag, "Box");
    model.component("comp1").selection(tag).set("entitydim", 1);
    model.component("comp1").selection(tag).set("xmin", rMin);
    model.component("comp1").selection(tag).set("xmax", rMax);
    model.component("comp1").selection(tag).set("ymin", zMin);
    model.component("comp1").selection(tag).set("ymax", zMax);
    model.component("comp1").selection(tag).set("condition", "inside");
  }

  /** Linear Elastic Material. sel == null bo'lsa, default feature (lemm1) ishlatiladi. */
  static void elastic(Model model, String tag, String sel, String E) {
    PhysicsFeature f;
    if (sel == null) {
      f = model.component("comp1").physics("solid").feature(tag);
    } else {
      f = model.component("comp1").physics("solid").create(tag, "LinearElasticModel", 2);
      f.selection().named(sel);
    }
    f.set("E_mat", "userdef");   f.set("E", E);
    f.set("nu_mat", "userdef");  f.set("nu", "nu0");
    f.set("rho_mat", "userdef"); f.set("rho", "6000[kg/m^3]");
  }

  /** Current Conservation. sel == null bo'lsa, default feature (cucn1) ishlatiladi. */
  static void conduct(Model model, String tag, String sel, String sigma) {
    PhysicsFeature f;
    if (sel == null) {
      f = model.component("comp1").physics("ec").feature(tag);
    } else {
      f = model.component("comp1").physics("ec").create(tag, "CurrentConservation", 2);
      f.selection().named(sel);
    }
    f.set("sigma_mat", "userdef");
    f.set("sigma", new String[]{sigma, "0", "0", "0", sigma, "0", "0", "0", sigma});
    f.set("epsilonr_mat", "userdef");
    f.set("epsilonr", new String[]{"30", "0", "0", "0", "30", "0", "0", "0", "30"});
  }

  static String fmt(double x) {
    return String.format(Locale.US, "%.6g", x);
  }

  public static void main(String[] args) {
    run();
  }
}
