/*
 * Model1_Sensitivity.java  —  N8: sezgirlik tahlili (Tungi vazifa, 4-topshiriq)
 *
 * Model1_Vertical.java dagi 2-bosqich geometriyasi (r_f=200nm, R_dev=1um) va S1 dan kalibrlangan
 * sig_off/sig_on qiymatlari asosida, r_f, t_int, sig_on ni +-50% o'zgartirib R_OFF/R_ON ga
 * ta'sirini Stationary (tez) hisoblaydi. Mustaqil, soddalashtirilgan fayl (Time Dependent, Global
 * ODE, Heat Transfer YO'Q - faqat Electric Currents Stationary).
 *
 * Mesh: r_f va t_int o'zgarishi geometriyani o'zgartiradi, shuning uchun HAR BIR parametr
 * kombinatsiyasi uchun geometriya+to'r QAYTA QURILADI (mesh.run() qayta chaqiriladi).
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class Model1_Sensitivity {

  // Model1 S1 dan kalibrlangan bazaviy qiymatlar (2-bosqich, R_dev=1um, r_f=200nm bilan).
  static final double SIG_OFF_BASE = 4.775e-3;
  static final double SIG_ON_BASE = 1.594;
  static final double R_F_BASE = 200;      // nm
  static final double T_INT_BASE = 1.5;    // nm

  public static Model run() {
    Model model = ModelUtil.create("ModelSens");
    model.label("Model1_Sensitivity.mph");

    p(model, "R_dev", "1[um]", "");
    p(model, "t_be", "10[nm]", "");
    p(model, "t_ST", "20[nm]", "");
    p(model, "t_int", T_INT_BASE + "[nm]", "");
    p(model, "t_GT", "20[nm]", "");
    p(model, "t_te", "10[nm]", "");
    p(model, "r_f", R_F_BASE + "[nm]", "");
    p(model, "z_ST", "t_be", "");
    p(model, "z_int", "t_be+t_ST", "");
    p(model, "z_GT", "z_int+t_int", "");
    p(model, "z_te", "z_GT+t_GT", "");
    p(model, "z_top", "z_te+t_te", "");

    p(model, "sig_m", "5e6[S/m]", "");
    p(model, "sig_ST", "1e5[S/m]", "");
    p(model, "sig_GT", "1e5[S/m]", "");
    p(model, "sig_off", SIG_OFF_BASE + "[S/m]", "");
    p(model, "sig_on", SIG_ON_BASE + "[S/m]", "");
    p(model, "epsr_m", "1", "");
    p(model, "epsr_ST", "50", "");
    p(model, "epsr_GT", "30", "");
    p(model, "epsr_int", "10", "");
    p(model, "V_read", "0.1[V]", "");
    p(model, "xs", "0", "");
    p(model, "h_glob", "5[nm]", "");

    model.component().create("comp1", true);
    GeomSequence g = model.component("comp1").geom().create("geom1", 2);
    g.axisymmetric(true);
    g.lengthUnit("nm");

    rect(g, "r_be", "R_dev", "t_be", "0", "0");
    rect(g, "r_st", "R_dev", "t_ST", "0", "z_ST");
    rect(g, "r_fil", "r_f", "t_int", "0", "z_int");
    rect(g, "r_ring", "R_dev-r_f", "t_int", "r_f", "z_int");
    rect(g, "r_gt", "R_dev", "t_GT", "0", "z_GT");
    rect(g, "r_te", "R_dev", "t_te", "0", "z_te");

    boxSel(g, "box_top", "-1", "R_dev+1[nm]", "z_top-0.01[nm]", "z_top+0.01[nm]");
    boxSel(g, "box_bot", "-1", "R_dev+1[nm]", "-0.01", "0.01");
    g.run();

    union(model, "sel_el", 2, new String[]{"geom1_r_be_dom", "geom1_r_te_dom"}, "");

    model.component("comp1").variable().create("var1");
    model.component("comp1").variable("var1").set("sig_fil", "sig_off*(sig_on/sig_off)^xs", "");

    material(model, "mat_el", "sel_el", "sig_m", "epsr_m");
    material(model, "mat_ST", "geom1_r_st_dom", "sig_ST", "epsr_ST");
    material(model, "mat_GT", "geom1_r_gt_dom", "sig_GT", "epsr_GT");
    material(model, "mat_ring", "geom1_r_ring_dom", "sig_off", "epsr_int");
    material(model, "mat_fil", "geom1_r_fil_dom", "sig_fil", "epsr_int");

    model.component("comp1").physics().create("ec", "ConductiveMedia", "geom1");
    model.component("comp1").physics("ec").create("term1", "Terminal", 1);
    model.component("comp1").physics("ec").feature("term1").selection().named("geom1_box_top");
    model.component("comp1").physics("ec").feature("term1").set("TerminalType", "Voltage");
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_read");
    model.component("comp1").physics("ec").create("gnd1", "Ground", 1);
    model.component("comp1").physics("ec").feature("gnd1").selection().named("geom1_box_bot");

    model.component("comp1").mesh().create("mesh1");
    model.component("comp1").mesh("mesh1").feature("size").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size").set("hmax", "h_glob");
    model.component("comp1").mesh("mesh1").feature("size").set("hmin", "0.01[nm]");
    model.component("comp1").mesh("mesh1").create("ftri1", "FreeTri");
    model.component("comp1").mesh("mesh1").run();

    model.study().create("std1");
    model.study("std1").create("stat", "Stationary");
    model.study("std1").run();

    model.result().numerical().create("gev_R", "EvalGlobal");
    model.result().numerical("gev_R").set("data", "dset1");
    model.result().numerical("gev_R").set("expr", new String[]{"V_read/ec.I0_1"});

    // ---- Bazaviy R_OFF/R_ON ----
    double rOffBase = solveR(model, "0");
    double rOnBase = solveR(model, "1");
    System.out.println(String.format(Locale.US, "N8 BAZAVIY: R_OFF=%.6e ohm, R_ON=%.6e ohm", rOffBase, rOnBase));

    String csv = "N8_sensitivity.csv";
    PrintWriter out = null;
    try {
      out = new PrintWriter(new FileWriter(csv));
      out.println("parametr,ozgarish,qiymat,R_OFF_ohm,R_ON_ohm,R_OFF_nisbat,R_ON_nisbat");
      out.println(String.format(Locale.US, "bazaviy,0%%,-,%.6e,%.6e,1,1", rOffBase, rOnBase));

      // --- sig_on +-50% (geometriya o'zgarmaydi, tez) ---
      double[] sigOnMults = {0.5, 1.5};
      for (double m : sigOnMults) {
        model.param().set("sig_on", String.format(Locale.US, "%.6e[S/m]", SIG_ON_BASE * m));
        double rOff = solveR(model, "0");
        double rOn = solveR(model, "1");
        out.println(String.format(Locale.US, "sig_on,%+.0f%%,%.6e,%.6e,%.6e,%.4g,%.4g",
            (m - 1) * 100, SIG_ON_BASE * m, rOff, rOn, rOff / rOffBase, rOn / rOnBase));
        System.out.println(String.format(Locale.US, "N8 sig_on x%.1f: R_OFF=%.6e R_ON=%.6e", m, rOff, rOn));
      }
      model.param().set("sig_on", SIG_ON_BASE + "[S/m]");

      // --- t_int +-50% (geometriya o'zgaradi, mesh qayta quriladi) ---
      double[] tIntMults = {0.5, 1.5};
      for (double m : tIntMults) {
        model.param().set("t_int", String.format(Locale.US, "%.6e[nm]", T_INT_BASE * m));
        model.component("comp1").mesh("mesh1").run();
        double rOff = solveR(model, "0");
        double rOn = solveR(model, "1");
        out.println(String.format(Locale.US, "t_int,%+.0f%%,%.6e,%.6e,%.6e,%.4g,%.4g",
            (m - 1) * 100, T_INT_BASE * m, rOff, rOn, rOff / rOffBase, rOn / rOnBase));
        System.out.println(String.format(Locale.US, "N8 t_int x%.1f: R_OFF=%.6e R_ON=%.6e", m, rOff, rOn));
      }
      model.param().set("t_int", T_INT_BASE + "[nm]");
      model.component("comp1").mesh("mesh1").run();

      // --- r_f +-50% (geometriya o'zgaradi, mesh qayta quriladi) ---
      double[] rFMults = {0.5, 1.5};
      for (double m : rFMults) {
        model.param().set("r_f", String.format(Locale.US, "%.6e[nm]", R_F_BASE * m));
        model.component("comp1").mesh("mesh1").run();
        double rOff = solveR(model, "0");
        double rOn = solveR(model, "1");
        out.println(String.format(Locale.US, "r_f,%+.0f%%,%.6e,%.6e,%.6e,%.4g,%.4g",
            (m - 1) * 100, R_F_BASE * m, rOff, rOn, rOff / rOffBase, rOn / rOnBase));
        System.out.println(String.format(Locale.US, "N8 r_f x%.1f: R_OFF=%.6e R_ON=%.6e", m, rOff, rOn));
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV N8): " + ex.getMessage());
    } finally {
      if (out != null) out.close();
    }

    try {
      model.save("Model1_Sensitivity.mph");
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    System.out.println("N8 TAYYOR.");
    return model;
  }

  static void p(Model model, String name, String expr, String descr) {
    model.param().set(name, expr, descr);
  }

  static void rect(GeomSequence g, String tag, String w, String h, String r0, String z0) {
    g.create(tag, "Rectangle");
    g.feature(tag).set("size", new String[]{w, h});
    g.feature(tag).set("pos", new String[]{r0, z0});
    g.feature(tag).set("selresult", true);
  }

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
  }

  static void material(Model model, String tag, String sel, String sigma, String epsr) {
    model.component("comp1").material().create(tag, "Common");
    model.component("comp1").material(tag).selection().named(sel);
    model.component("comp1").material(tag).propertyGroup("def").set("electricconductivity", new String[]{sigma});
    model.component("comp1").material(tag).propertyGroup("def").set("relpermittivity", new String[]{epsr});
  }

  static double solveR(Model model, String x) {
    model.param().set("xs", x);
    model.study("std1").run();
    return model.result().numerical("gev_R").getReal()[0][0];
  }

  public static void main(String[] args) {
    run();
  }
}
