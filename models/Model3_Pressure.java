/*
 * Model3_Pressure.java  —  3-MODEL: Model1 geometriyasi + Solid Mechanics (bosim ta'siri)
 *
 * Troyan & Doronin, ICCS 2020 maqolasidagi kuzatuv: bosim ostida xotira holatining R_ON qiymati
 * 2-3 tartibga kamayadi, volatil holatniki deyarli o'zgarmaydi. Bu FENOMENOLOGIK model orqali
 * qayta ishlab chiqiladi (Model1_Vertical.java dagi TASDIQLANGAN API chaqiruvlari qayta ishlatilgan).
 *
 * FIZIK MODEL (FARAZ, PARAMETRLAR3.md da batafsil):
 *   Solid Mechanics: yuqori chegarada bosim p (0..2 GPa), pastki chegara Fixed, yon devor Roller.
 *   d(p) = t_int*(1+aveop_int(solid.eZZ))  - interfeys (vdW bo'shliq) samarali qalinligi bosim ostida
 *   (eZZ manfiy - siqilish, demak d(p) < t_int).
 *   sig_on(p) = sig_on0*exp(beta*(t_int-d)/t_int)  - tunnel o'tkazuvchanlik EKSPONENSIAL ravishda
 *   masofaga bog'liq (WKB taxminiga o'xshash), beta - fitting parametri.
 *   Volatil holat uchun beta=0 (bosimga sezgir emas - FARAZ, chunki volatil rejimda filament
 *   holati boshqa mexanizm bilan aniqlanadi deb faraz qilinadi).
 *
 * Ketma-ket yechim: (1) Stationary Solid Mechanics -> eZZ, (2) shu eZZ asosida sig_on(p) bilan
 * Stationary Electric Currents -> R_ON(p), R_OFF(p).
 *
 * "// TEKSHIRILSIN" - Model3 ga xos yangi API chaqiruvlari (Solid Mechanics interfeysi, Fixed/Roller/
 * BoundaryLoad feature nomlari, solid.eZZ o'zgaruvchisi).
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class Model3_Pressure {

  public static Model run() {
    Model model = ModelUtil.create("Model3");
    model.label("Model3_Pressure.mph");
    model.comments("Model1 geometriyasi + Solid Mechanics: bosim ostida R_ON(p), R_OFF(p) "
        + "(fenomenologik tunnel-o'tkazuvchanlik farazi).");

    // =====================================================================================
    // PARAMETERS (Model1 bilan bir xil geometriya/elektr, + elastik parametrlar)
    // =====================================================================================
    p(model, "R_dev", "1[um]", "Element radiusi (Model1 2-bosqich bilan bir xil)");
    p(model, "t_be", "10[nm]", "Pastki elektrod qalinligi");
    p(model, "t_ST", "20[nm]", "Sb2Te3 qalinligi");
    p(model, "t_int", "1.5[nm]", "Interfeys qalinligi (bosimsiz)");
    p(model, "t_GT", "20[nm]", "GeTe qalinligi");
    p(model, "t_te", "10[nm]", "Yuqori elektrod qalinligi");
    p(model, "r_f", "200[nm]", "Filament radiusi");
    p(model, "z_ST", "t_be", "");
    p(model, "z_int", "t_be+t_ST", "");
    p(model, "z_GT", "z_int+t_int", "");
    p(model, "z_te", "z_GT+t_GT", "");
    p(model, "z_top", "z_te+t_te", "");

    p(model, "sig_m", "5e6[S/m]", "Elektrod o'tkazuvchanligi");
    p(model, "sig_ST", "1e5[S/m]", "Sb2Te3 o'tkazuvchanligi");
    p(model, "sig_GT", "1e5[S/m]", "GeTe o'tkazuvchanligi");
    p(model, "sig_off", "4.775e-3[S/m]", "Interfeys OFF o'tkazuvchanligi (Model1 S1 dan kalibrlangan)");
    p(model, "sig_on0", "1.594[S/m]", "Filament ON o'tkazuvchanligi, p=0 (Model1 S1 dan)");
    p(model, "epsr_m", "1", "");
    p(model, "epsr_ST", "50", "");
    p(model, "epsr_GT", "30", "");
    p(model, "epsr_int", "10", "");

    p(model, "V_read", "0.1[V]", "O'qish kuchlanishi");
    p(model, "xs", "0", "Filament holati (0=OFF, 1=ON)");
    p(model, "R_ON_t", "7[kohm]", "");
    p(model, "R_OFF_t", "100[kohm]", "");

    // --- Elastik parametrlar (TEKSHIRILSIN belgili taxminlar) ---
    p(model, "E_m", "250[GPa]", "TiN elastik moduli - TEKSHIRILSIN");
    p(model, "E_ST", "55[GPa]", "Sb2Te3 elastik moduli - TEKSHIRILSIN");
    p(model, "E_GT", "50[GPa]", "GeTe elastik moduli - TEKSHIRILSIN");
    p(model, "E_int", "10[GPa]", "Interfeys (vdW, c-o'q) elastik moduli - TEKSHIRILSIN, juda noaniq");
    p(model, "nu_m", "0.25", "TiN Puasson nisbati - TEKSHIRILSIN");
    p(model, "nu_ST", "0.25", "Sb2Te3 Puasson nisbati - TEKSHIRILSIN");
    p(model, "nu_GT", "0.25", "GeTe Puasson nisbati - TEKSHIRILSIN");
    p(model, "nu_int", "0.25", "Interfeys Puasson nisbati - TEKSHIRILSIN");
    p(model, "rho_m", "5220[kg/m^3]", "");
    p(model, "rho_ST", "6500[kg/m^3]", "");
    p(model, "rho_GT", "6140[kg/m^3]", "");
    p(model, "rho_int", "6300[kg/m^3]", "");

    // --- Bosim va tunnel-fitting ---
    p(model, "p_load", "0[GPa]", "Yuqoridan qo'yiladigan bosim");
    p(model, "beta_mem", "10", "Tunnel-fitting parametri (xotira holati, beta>0) - kalibrlanadi");

    p(model, "h_int", "0.5[nm]", "Interfeys/filamentda maksimal element");
    p(model, "h_glob", "2[nm]", "Qolgan joyda maksimal element");

    // =====================================================================================
    // GEOMETRY
    // =====================================================================================
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
    boxSel(g, "box_out", "R_dev-0.01[nm]", "R_dev+0.01[nm]", "-1", "z_top+1[nm]");
    g.run();

    union(model, "sel_int", 2, new String[]{"geom1_r_fil_dom", "geom1_r_ring_dom"}, "Interfeys qatlami");
    union(model, "sel_el", 2, new String[]{"geom1_r_be_dom", "geom1_r_te_dom"}, "Elektrodlar");

    // =====================================================================================
    // DEFINITIONS
    // =====================================================================================
    model.component("comp1").variable().create("var1");
    model.component("comp1").cpl().create("aveop_int", "Average");
    model.component("comp1").cpl("aveop_int").selection().named("sel_int");
    model.component("comp1").cpl("aveop_int").set("axisym", true);

    // d(p) = t_int*(1+eZZ) [eZZ manfiy -> siqilish -> d<t_int]; sig_on(p) = sig_on0*exp(beta*(t_int-d)/t_int)
    // TEKSHIRILSIN: "solid.eZZ" - Solid Mechanics interfeysining default strain komponenti nomi.
    model.component("comp1").variable("var1").set("d_gap", "t_int*(1+aveop_int(solid.eZZ))",
        "Interfeys samarali qalinligi bosim ostida");
    model.component("comp1").variable("var1").set("sig_on_p", "sig_on0*exp(beta_mem*(t_int-d_gap)/t_int)",
        "Bosimga bog'liq ON o'tkazuvchanlik (tunnel farazi)");
    model.component("comp1").variable("var1").set("sig_fil", "sig_off*(sig_on_p/sig_off)^xs",
        "Filament o'tkazuvchanligi");

    // =====================================================================================
    // MATERIALS
    // =====================================================================================
    material(model, "mat_el", "Elektrodlar", "sel_el", "sig_m", "epsr_m", "E_m", "nu_m", "rho_m");
    material(model, "mat_ST", "Sb2Te3", "geom1_r_st_dom", "sig_ST", "epsr_ST", "E_ST", "nu_ST", "rho_ST");
    material(model, "mat_GT", "GeTe", "geom1_r_gt_dom", "sig_GT", "epsr_GT", "E_GT", "nu_GT", "rho_GT");
    material(model, "mat_ring", "Interfeys halqasi", "geom1_r_ring_dom", "sig_off", "epsr_int", "E_int", "nu_int", "rho_int");
    material(model, "mat_fil", "Filament", "geom1_r_fil_dom", "sig_fil", "epsr_int", "E_int", "nu_int", "rho_int");

    // =====================================================================================
    // PHYSICS: Solid Mechanics
    // =====================================================================================
    model.component("comp1").physics().create("solid", "SolidMechanics", "geom1");   // TEKSHIRILSIN
    model.component("comp1").physics("solid").create("fix1", "Fixed", 1);   // TEKSHIRILSIN
    model.component("comp1").physics("solid").feature("fix1").label("Pastki chegara (mahkamlangan)");
    model.component("comp1").physics("solid").feature("fix1").selection().named("geom1_box_bot");

    model.component("comp1").physics("solid").create("roll1", "Roller", 1);   // TEKSHIRILSIN
    model.component("comp1").physics("solid").feature("roll1").label("Yon devor (roller)");
    model.component("comp1").physics("solid").feature("roll1").selection().named("geom1_box_out");

    model.component("comp1").physics("solid").create("load1", "BoundaryLoad", 1);   // TEKSHIRILSIN
    model.component("comp1").physics("solid").feature("load1").label("Yuqori bosim");
    model.component("comp1").physics("solid").feature("load1").selection().named("geom1_box_top");
    model.component("comp1").physics("solid").feature("load1").set("LoadType", "Pressure");   // TEKSHIRILSIN
    model.component("comp1").physics("solid").feature("load1").set("Pressure", "p_load");   // TEKSHIRILSIN

    // =====================================================================================
    // PHYSICS: Electric Currents
    // =====================================================================================
    model.component("comp1").physics().create("ec", "ConductiveMedia", "geom1");
    model.component("comp1").physics("ec").create("term1", "Terminal", 1);
    model.component("comp1").physics("ec").feature("term1").selection().named("geom1_box_top");
    model.component("comp1").physics("ec").feature("term1").set("TerminalType", "Voltage");
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_read");
    model.component("comp1").physics("ec").create("gnd1", "Ground", 1);
    model.component("comp1").physics("ec").feature("gnd1").selection().named("geom1_box_bot");

    // =====================================================================================
    // MESH
    // =====================================================================================
    model.component("comp1").mesh().create("mesh1");
    model.component("comp1").mesh("mesh1").feature("size").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size").set("hmax", "h_glob");
    model.component("comp1").mesh("mesh1").feature("size").set("hmin", "0.02[nm]");
    model.component("comp1").mesh("mesh1").feature("size").set("hgrad", 1.2);
    model.component("comp1").mesh("mesh1").create("map1", "Map");
    model.component("comp1").mesh("mesh1").feature("map1").selection().geom("geom1", 2);
    model.component("comp1").mesh("mesh1").feature("map1").selection().named("sel_int");
    model.component("comp1").mesh("mesh1").create("ftri1", "FreeTri");
    model.component("comp1").mesh("mesh1").run();

    // =====================================================================================
    // STUDY: ketma-ket - avval Solid Mechanics, keyin Electric Currents (ikkalasi bir xil
    // Stationary tadqiqot ichida, ec solid dan keyin hisoblanadi - "activate" tartib emas, balki
    // ikkita alohida STUDY STEP orqali ketma-ketlik ta'minlanadi).
    // =====================================================================================
    model.study().create("std1");
    model.study("std1").label("N7: Solid Mechanics -> Electric Currents (ketma-ket)");
    model.study("std1").create("stat1", "Stationary");
    model.study("std1").feature("stat1").label("1-qadam: Solid Mechanics");
    model.study("std1").feature("stat1").set("activate", new String[]{"solid", "on", "ec", "off"});
    model.study("std1").create("stat2", "Stationary");
    model.study("std1").feature("stat2").label("2-qadam: Electric Currents");
    model.study("std1").feature("stat2").set("activate", new String[]{"solid", "off", "ec", "on"});
    model.study("std1").feature("stat2").set("usesol", true);   // TEKSHIRILSIN: oldingi qadam yechimidan foydalanish
    model.study("std1").feature("stat2").set("notsolmethod", "sol");
    model.study("std1").feature("stat2").set("notstudy", "std1");

    model.result().numerical().create("gev_R", "EvalGlobal");
    model.result().numerical("gev_R").set("expr", new String[]{"V_read/ec.I0_1"});

    double[] pVals = {0, 0.25, 0.5, 1, 1.5, 2};   // GPa
    String ronCsv = "Ron_p.csv";
    PrintWriter out = null;
    try {
      out = new PrintWriter(new FileWriter(ronCsv));
      out.println("p_GPa,xs,beta,d_gap_nm,R_ohm");
      for (int rep = 0; rep < 2; rep++) {   // rep=0: xotira (beta=beta_mem), rep=1: volatil (beta=0)
        double betaUse = (rep == 0) ? Double.NaN : 0.0;   // beta_mem qiymati parametrdan olinadi (kalibrlangandan keyin)
        for (int j = 0; j < pVals.length; j++) {
          model.param().set("p_load", String.format(Locale.US, "%.4g[GPa]", pVals[j]));
          if (rep == 1) model.param().set("beta_mem", "0");
          for (int s = 0; s < 2; s++) {
            model.param().set("xs", String.format(Locale.US, "%d", s));
            model.study("std1").run();
            double r = model.result().numerical("gev_R").getReal()[0][0];
            out.println(String.format(Locale.US, "%.4g,%d,%s,%.6e,%.6e",
                pVals[j], s, rep == 1 ? "0(volatil)" : "beta_mem(xotira)", 0.0, r));
            System.out.println(String.format(Locale.US, "N7: p=%.2fGPa xs=%d rep=%d -> R=%.6e ohm",
                pVals[j], s, rep, r));
          }
        }
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV Ron_p): " + ex.getMessage());
    } finally {
      if (out != null) out.close();
      model.param().set("p_load", "0[GPa]");
      model.param().set("xs", "0");
    }

    try {
      model.save("Model3_Pressure.mph");
    } catch (IOException ex) {
      throw new RuntimeException("MPH faylni saqlab bo'lmadi", ex);
    }
    System.out.println("N7 TAYYOR.");
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
    model.component("comp1").selection(tag).label(label);
  }

  static void material(Model model, String tag, String label, String sel,
                       String sigma, String epsr, String E, String nu, String rho) {
    model.component("comp1").material().create(tag, "Common");
    model.component("comp1").material(tag).label(label);
    model.component("comp1").material(tag).selection().named(sel);
    model.component("comp1").material(tag).propertyGroup("def").set("electricconductivity", new String[]{sigma});
    model.component("comp1").material(tag).propertyGroup("def").set("relpermittivity", new String[]{epsr});
    model.component("comp1").material(tag).propertyGroup("def").set("youngsmodulus", new String[]{E});
    model.component("comp1").material(tag).propertyGroup("def").set("poissonsratio", new String[]{nu});
    model.component("comp1").material(tag).propertyGroup("def").set("density", rho);
  }

  public static void main(String[] args) {
    run();
  }
}
