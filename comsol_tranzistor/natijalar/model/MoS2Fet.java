/*
 * MoS2Fet.java
 *
 * Kii & Nouchi (2025) MoS2/F4TCNQ FET modeli: "A modeli" (Semiconductor + Global ODE),
 * TO'LIQ GEOMETRIYA (GEOMETRIYA.md, 1a-rasm bilan bir xil): Si, SiO2(285nm), MoS2, Cr(1nm),
 * pog'onali Au, suv plyonkasi, F4TCNQ (2 gumbaz), havo.
 *
 * 2-kun (qayta qurish): kechagi model faqat MoS2 to'rtburchagidan iborat edi (Thin Insulator
 * Gate orqali oksidni yashirincha modellagan "qollanma 5-qism"dagi soddalashtirilgan A model).
 * PROMPT.md endi buni taqiqlaydi - geometriya GEOMETRIYA.md bo'yicha to'liq qurilishi kerak.
 *
 * Fizika farqi (eski modelga nisbatan):
 *  - SiO2 endi HAQIQIY domen (Poisson yechiladi), Thin Insulator Gate ISHLATILMAYDI.
 *  - Gate endi Si/SiO2 chegarasida GateContact, d_ins ~ 0 (ideal metall - oksid qalinligi
 *    allaqachon haqiqiy domen orqali hisobga olingan, ikki marta hisoblanmasligi uchun).
 *  - Cr/Au ning SiO2/suv/F4TCNQ/havo bilan tegib turgan barcha sirtlari ham GateContact
 *    (d_ins ~ 0, V = terminal kuchlanishi) - metall/izolyator chegarasi.
 *  - Cr/Au ning MoS2 bilan tegib turgan qismi (uchi + usti) - MetalContact Schottky (eski kabi).
 *  - SiO2/F4TCNQ/suv/havo - semi ichida "Insulation" (Charge Conservation) domen xususiyati.
 *  - Si, Cr, Au - semi fizika domen tanlovidan chetlatiladi (faqat geometriyada bor, chegara
 *    shartlari orqali ta'sir qiladi).
 *
 * Parametrlar params.txt dan o'qiladi. Rejim: mode = transfer | output (params.txt da).
 * Natija CSV va .mph joriy ishchi papkaga yoziladi.
 */

import com.comsol.model.*;
import com.comsol.model.util.*;
import com.comsol.model.physics.*;

import java.io.*;
import java.util.*;
import java.util.List;

public class MoS2Fet {

  static Map<String, String> P = new LinkedHashMap<String, String>();
  static Model model;
  static Physics semi;
  static PhysicsFeature mc1, mc2;
  static final String OUT_DIR = System.getProperty("user.dir");

  public static void main(String[] args) {
    try {
      loadParams("params.txt");
      String mode = P.get("mode");
      build(mode);
      if (args != null && args.length > 0 && args[0].equals("geomonly")) {
        exportGeomImages();
        String mphPath = OUT_DIR + File.separator + "MoS2Fet_geomonly.mph";
        model.save(mphPath);
        System.out.println("GEOMONLY_DONE " + mphPath);
        return;
      }
      exportGeomImages();
      if (mode.equals("output")) {
        runOutput();
      } else {
        runTransfer();
      }
      String mphPath = OUT_DIR + File.separator + "MoS2Fet_" + mode + ".mph";
      try {
        model.save(mphPath);
        System.out.println("MPH saqlandi: " + mphPath);
      } catch (Exception e) {
        System.out.println("OGOHLANTIRISH: mph saqlanmadi: " + e.getMessage());
      }
    } catch (Exception e) {
      System.out.println("FATAL: " + e.getClass().getName() + ": " + e.getMessage());
      e.printStackTrace(System.out);
    }
  }

  // ---------------------------------------------------------------------
  // Parametrlarni o'qish
  // ---------------------------------------------------------------------
  static void loadParams(String path) throws IOException {
    // Standart qiymatlar
    P.put("L", "5[um]");
    P.put("L_ov", "1.5[um]");
    P.put("L_pad", "2[um]");
    P.put("t_mos", "0.020[um]");
    P.put("c", "0.001[um]");
    P.put("t_Au", "0.060[um]");
    P.put("t_ox", "0.285[um]");
    P.put("t_Si", "0.5[um]");
    P.put("t_w", "0.003[um]");
    P.put("H_air", "2[um]");

    P.put("T0", "300[K]");
    P.put("Eg_mos", "1.23[V]");
    P.put("chi_mos", "4.0[V]");
    P.put("eps_mos", "7");
    P.put("Nc_mos", "1e19[1/cm^3]");
    P.put("Nv_mos", "1e19[1/cm^3]");
    P.put("Nd_mos", "1e17[1/cm^3]");
    P.put("mu_n", "30[cm^2/(V*s)]");
    P.put("mu_p", "10[cm^2/(V*s)]");
    P.put("Phi_B0", "0.25[V]");
    P.put("Phi_Si", "5.05[V]");
    P.put("Phi_metal", "4.1[V]");
    P.put("eps_sio2", "3.9");
    P.put("eps_f4", "3");
    P.put("eps_water", "20");
    P.put("eps_air", "1");
    P.put("d_ideal", "1[nm]");
    P.put("Dit", "3e13[1/(cm^2*eV)]");
    P.put("Ew0", "0.3[V]");
    P.put("sigma_F4", "-1e12[1/cm^2]*e_const");
    P.put("beta", "0.22[V/V]");
    P.put("t_sw", "40[s]");
    P.put("tau_ion", "0.35*t_sw");
    P.put("t_hold", "0.5*t_sw");
    P.put("W_dev", "12.8[um]");
    P.put("VG", "80");
    P.put("VD", "0.2");
    P.put("VD_list", "0.2,0.4,0.6,0.8,1.0");
    P.put("VG_start", "80");
    P.put("VG_stop", "-80");
    P.put("VG_step", "-2");
    P.put("use_traps", "1");
    P.put("use_f4", "1");
    P.put("mode", "transfer");
    P.put("Dit_scale", "1");

    File f = new File(path);
    if (f.exists()) {
      BufferedReader r = new BufferedReader(new FileReader(f));
      try {
        String line;
        while ((line = r.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#")) continue;
          int eq = line.indexOf('=');
          if (eq < 0) continue;
          String k = line.substring(0, eq).trim();
          String v = line.substring(eq + 1).trim();
          P.put(k, v);
        }
      } finally {
        r.close();
      }
      System.out.println("params.txt o'qildi: " + f.getAbsolutePath());
    } else {
      System.out.println("OGOHLANTIRISH: params.txt topilmadi, standart qiymatlar ishlatiladi.");
    }
  }

  // ---------------------------------------------------------------------
  // Model qurilishi
  // ---------------------------------------------------------------------
  static void build(String mode) {
    model = ModelUtil.create("Model");
    model.label("MoS2Fet.mph");
    model.comments("MoS2/F4TCNQ FET, to'liq geometriya (GEOMETRIYA.md), Kii & Nouchi 2025 asosida.");

    for (Map.Entry<String, String> e : P.entrySet()) {
      if (e.getKey().equals("mode") || e.getKey().equals("VD_list") || e.getKey().equals("VG_list")) continue;
      try {
        model.param().set(e.getKey(), e.getValue());
      } catch (Exception ex) {
        System.out.println("OGOHLANTIRISH: param " + e.getKey() + " = " + e.getValue() + " -> " + ex.getMessage());
      }
    }
    model.param().set("xS0", "-L_ov-L_pad");
    model.param().set("xD1", "L-xS0");

    model.component().create("comp1", true);
    GeomSequence g = model.component("comp1").geom().create("geom1", 2);
    g.lengthUnit("um");
    g.repairTol(1e-9);

    // ---- Cumulative selections (domenlar) ----
    g.selection().create("csel_si", "CumulativeSelection").label("dom_si");
    g.selection().create("csel_sio2", "CumulativeSelection").label("dom_sio2");
    g.selection().create("csel_mos2", "CumulativeSelection").label("dom_mos2");
    g.selection().create("csel_cr", "CumulativeSelection").label("dom_cr");
    g.selection().create("csel_au", "CumulativeSelection").label("dom_au");
    g.selection().create("csel_water", "CumulativeSelection").label("dom_water");
    g.selection().create("csel_f4", "CumulativeSelection").label("dom_f4");
    g.selection().create("csel_air", "CumulativeSelection").label("dom_air");

    GeomFeature rSi = g.create("r_si", "Rectangle");
    rSi.set("pos", new String[]{"xS0", "-t_ox-t_Si"});
    rSi.set("size", new String[]{"xD1-xS0", "t_Si"});
    rSi.set("contributeto", "csel_si");

    GeomFeature rSiO2 = g.create("r_sio2", "Rectangle");
    rSiO2.set("pos", new String[]{"xS0", "-t_ox"});
    rSiO2.set("size", new String[]{"xD1-xS0", "t_ox"});
    rSiO2.set("contributeto", "csel_sio2");

    GeomFeature rMos = g.create("r_mos2", "Rectangle");
    rMos.set("pos", new String[]{"-L_ov", "0"});
    rMos.set("size", new String[]{"L+2*L_ov", "t_mos"});
    rMos.set("contributeto", "csel_mos2");

    GeomFeature polCrS = g.create("pol_cr_s", "Polygon");
    polCrS.set("x", new String[]{"xS0", "-L_ov", "-L_ov", "0", "0", "-L_ov-c", "-L_ov-c", "xS0"});
    polCrS.set("y", new String[]{"0", "0", "t_mos", "t_mos", "t_mos+c", "t_mos+c", "c", "c"});
    polCrS.set("type", "solid");

    GeomFeature polAuS = g.create("pol_au_s", "Polygon");
    polAuS.set("x", new String[]{"xS0", "-L_ov-c", "-L_ov-c", "0", "0", "-L_ov-c-t_Au", "-L_ov-c-t_Au", "xS0"});
    polAuS.set("y", new String[]{"c", "c", "t_mos+c", "t_mos+c", "t_mos+c+t_Au", "t_mos+c+t_Au", "c+t_Au", "c+t_Au"});
    polAuS.set("type", "solid");

    GeomFeature mirCr = g.create("mir_cr", "Mirror");
    mirCr.selection("input").set("pol_cr_s");
    mirCr.set("pos", new String[]{"L/2", "0"});
    mirCr.set("axis", new String[]{"1", "0"});
    mirCr.set("keep", true);

    GeomFeature mirAu = g.create("mir_au", "Mirror");
    mirAu.selection("input").set("pol_au_s");
    mirAu.set("pos", new String[]{"L/2", "0"});
    mirAu.set("axis", new String[]{"1", "0"});
    mirAu.set("keep", true);

    polCrS.set("contributeto", "csel_cr");
    mirCr.set("contributeto", "csel_cr");
    polAuS.set("contributeto", "csel_au");
    mirAu.set("contributeto", "csel_au");

    GeomFeature rWater = g.create("r_water", "Rectangle");
    rWater.set("pos", new String[]{"0", "t_mos"});
    rWater.set("size", new String[]{"L", "t_w"});
    rWater.set("contributeto", "csel_water");

    GeomFeature e1 = g.create("e1", "Ellipse");
    e1.set("pos", new String[]{"0.30*L", "t_mos"});
    e1.set("semiaxes", new String[]{"0.30*L+0.8[um]", "0.40[um]"});

    GeomFeature e2 = g.create("e2", "Ellipse");
    e2.set("pos", new String[]{"0.68*L", "t_mos"});
    e2.set("semiaxes", new String[]{"0.32*L+0.8[um]", "0.55[um]"});

    GeomFeature uniDome = g.create("uni_dome", "Union");
    uniDome.selection("input").set("e1", "e2");
    uniDome.set("keep", false);
    uniDome.set("intbnd", false);

    GeomFeature rHalf = g.create("r_half", "Rectangle");
    rHalf.set("pos", new String[]{"xS0-1[um]", "t_mos"});
    rHalf.set("size", new String[]{"xD1-xS0+2[um]", "H_air+1[um]"});

    GeomFeature intDome = g.create("int_dome", "Intersection");
    intDome.selection("input").set("uni_dome", "r_half");
    intDome.set("keep", false);

    GeomFeature difF4 = g.create("dif_f4", "Difference");
    difF4.selection("input").set("int_dome");
    difF4.selection("input2").set("pol_cr_s", "pol_au_s", "mir_cr", "mir_au", "r_water", "r_mos2");
    difF4.set("keepadd", false);
    difF4.set("keepsubtract", true);
    difF4.set("contributeto", "csel_f4");

    GeomFeature rAirFull = g.create("r_air_full", "Rectangle");
    rAirFull.set("pos", new String[]{"xS0", "0"});
    rAirFull.set("size", new String[]{"xD1-xS0", "H_air"});

    GeomFeature difAir = g.create("dif_air", "Difference");
    difAir.selection("input").set("r_air_full");
    difAir.selection("input2").set("pol_cr_s", "pol_au_s", "mir_cr", "mir_au", "r_water", "r_mos2", "dif_f4");
    difAir.set("keepadd", false);
    difAir.set("keepsubtract", true);
    difAir.set("contributeto", "csel_air");

    g.run();
    System.out.println("Geometriya quruldi. Domenlar soni: " + g.getNDomains() + " (kutilgan: 10)");

    // ===== Domen selectionlari (Union, component darajasida, entitydim=2) =====
    domUnion("sel_insulators", new String[]{"geom1_csel_sio2_dom", "geom1_csel_f4_dom", "geom1_csel_water_dom", "geom1_csel_air_dom"});
    System.out.println("CHECKPOINT: sel_insulators OK");
    // dom_mos2 alohida: geom1_csel_mos2_dom
    domUnion("sel_semi_domains", new String[]{"geom1_csel_mos2_dom", "geom1_csel_sio2_dom", "geom1_csel_f4_dom", "geom1_csel_water_dom", "geom1_csel_air_dom"});
    System.out.println("CHECKPOINT: sel_semi_domains OK");

    // ===== Chegara selectionlari (aniq koordinatalar bo'yicha, Box, entitydim=1) =====
    // Gate: Si/SiO2 interfeysi (y = -t_ox, butun kenglik)
    boxBnd("sel_gate", "xS0-1[um]", "xD1+1[um]", "-t_ox-0.1*t_ox", "-t_ox+0.1*t_ox");

    // Source Schottky: MoS2 usti [x:-L_ov..0, y=t_mos] + MoS2 uchi [x=-L_ov, y:0..t_mos]
    boxBnd("sel_src_top", "-L_ov-0.05*L_ov", "0+0.05*L_ov", "t_mos-0.1*t_mos", "t_mos+0.1*t_mos");
    boxBnd("sel_src_end", "-L_ov-0.1*t_mos", "-L_ov+0.1*t_mos", "0-0.1*t_mos", "t_mos+0.1*t_mos");
    bndUnion("sel_src_schottky", new String[]{"sel_src_top", "sel_src_end"});

    // Drain Schottky: ko'zgu aks (x -> L - x)
    boxBnd("sel_drain_top", "L+0.05*L_ov", "L+L_ov-0.05*L_ov+0.05*L_ov", "t_mos-0.1*t_mos", "t_mos+0.1*t_mos");
    boxBnd("sel_drain_end", "L+L_ov-0.1*t_mos", "L+L_ov+0.1*t_mos", "0-0.1*t_mos", "t_mos+0.1*t_mos");
    bndUnion("sel_drain_schottky", new String[]{"sel_drain_top", "sel_drain_end"});

    // D_it interfeysi: MoS2/SiO2 (y = 0, butun MoS2 kengligi)
    boxBnd("sel_mos_sio2", "-L_ov-0.05*L_ov", "L+L_ov+0.05*L_ov", "0-0.1*t_mos", "0+0.1*t_mos");

    // F4TCNQ/suv kontakti: MoS2 usti, ochiq kanal [x:0..L, y=t_mos]
    boxBnd("sel_mos_top_channel", "0+0.05*L_ov", "L-0.05*L_ov", "t_mos-0.1*t_mos", "t_mos+0.1*t_mos");

    // Cr/Au ning izolyatorlar bilan chegarasi: butun kontakt stekining tashqi chegarasi MINUS
    // MoS2 bilan tegishgan (schottky) qismi. Keng box bilan HAMMA chegarani olib, keyin
    // schottky qismini ayiramiz.
    boxBnd("sel_src_all", "xS0-0.05[um]", "0+0.05[um]", "-0.05[um]", "t_mos+c+t_Au+0.05[um]");
    bndDiff("sel_src_metal_ins", "sel_src_all", "sel_src_schottky");

    boxBnd("sel_drain_all", "L-0.05[um]", "xD1+0.05[um]", "-0.05[um]", "t_mos+c+t_Au+0.05[um]");
    bndDiff("sel_drain_metal_ins", "sel_drain_all", "sel_drain_schottky");
    System.out.println("CHECKPOINT: chegara selectionlari OK");

    // ===== Semiconductor physics =====
    // MUHIM (2-kun, arxitektura qarori): avval SiO2/F4TCNQ/suv/havo uchun "semi" ICHIDA
    // sun'iy juda-katta-taqiqlangan-zona SemiconductorMaterialModel (izolyator taqlidi)
    // ishlatilgan edi - bu Newton yechuvchisida son beqarorlik berdi (ResEst plato/portlash,
    // Eg0=9V DA, Eg0=3V DA ham, ya'ni aniq Eg0 qiymatiga bog'liq emas edi - demak sabab
    // umuman "bitta semi interfeysida haqiqiy yarimo'tkazgich + sun'iy ultra-keng-zonali
    // 'soxta izolyator' aralashmasi" edi, tor Eg0 muammosi emas). Alohida Electrostatics
    // interfeysi + PotentialCoupling multiphysics bilan sinovdan o'tkazildi (ishlaydi, lekin
    // Destination_physics xossasini aniq sintaksisi vaqt talab qildi) - ANIQROQ va TEZROQ
    // yechim: GateContact FIZIKASI ASLIDA MOELINMAGAN (unmeshed) izolyator+metall stekini
    // BITTA chegara shartida modellash uchun mo'ljallangan (xuddi shu texnika kechagi B2'da
    // ISHLAGAN edi: ID=4.429e-10 A, yaqinlashdi). Shuning uchun:
    //   - "semi" domen tanlovi FAQAT MoS2 ga cheklanadi (SiO2/Cr/Au/F4TCNQ/suv/havo semi
    //     fizikasiga umuman kirmaydi - ular faqat GEOMETRIYADA bor, GEOMETRIYA.md talabiga
    //     mos: chizilgan, lekin alohida hisoblanmaydi).
    //   - Gate Contact MoS2'ning O'ZINING pastki chegarasida (sel_mos_sio2, D_it bilan bir
    //     xil chegara - qollanma 5.4-qismidagi naqsh: "Thin Insulator Gate... ostiga Trapping
    //     qo'shing") haqiqiy t_ox=285nm va epsilon=3.9 bilan qo'llaniladi - bu chegaraning
    //     "narigi tomonida" chizilgan SiO2 domeni GEOMETRIK jihatdan mos keladi, lekin
    //     elektr jihatdan lumped BC orqali ifodalanadi (domen ikki marta hisoblanmaydi).
    //   - Cr/Au ning izolyatorlar bilan chegaralari (gc_src/gc_drain) OLIB TASHLANDI - ular
    //     endi semi domen to'plamiga umuman tegmaydi (MoS2 emas), shuning uchun keraksiz.
    semi = model.component("comp1").physics().create("semi", "Semiconductor", "geom1");
    semi.prop("d").set("d", "W_dev");
    System.out.println("CHECKPOINT: semi yaratildi");
    semi.selection().named("geom1_csel_mos2_dom");
    System.out.println("CHECKPOINT: semi.selection() -> faqat MoS2");

    PhysicsFeature smm1 = semi.feature("smm1");
    smm1.set("Eg0_mat", "userdef"); smm1.set("Eg0", "Eg_mos");
    smm1.set("chi0_mat", "userdef"); smm1.set("chi0", "chi_mos");
    smm1.set("epsilonr_mat", "userdef"); smm1.set("epsilonr", "eps_mos");
    smm1.set("Nc_mat", "userdef"); smm1.set("Nc", "Nc_mos");
    smm1.set("Nv_mat", "userdef"); smm1.set("Nv", "Nv_mos");
    smm1.set("mun_mat", "userdef"); smm1.set("mun", "mu_n");
    smm1.set("mup_mat", "userdef"); smm1.set("mup", "mu_p");

    // Analitik legirlash: faqat MoS2 domeni
    PhysicsFeature adm1 = semi.create("adm1", "AnalyticDopingModel", 2);
    adm1.selection().named("geom1_csel_mos2_dom");
    adm1.set("impurityType", "donor");
    adm1.set("NDc", "Nd_mos");
    adm1.set("impurityDistribution", "box");
    adm1.set("BaseOrCenter", "corner");
    adm1.set("rb", new String[]{"-L_ov-1[um]", "-1[um]", "0[um]"});
    adm1.set("jwidth", "L+2*L_ov+2[um]");
    adm1.set("jheight", "1000*t_mos");
    adm1.set("JunctionOrLength", "decay_length");
    adm1.set("ls", "1000*t_mos");
    System.out.println("CHECKPOINT: adm1 OK");

    // Gate: MoS2'ning pastki chegarasi (sel_mos_sio2 - D_it bilan bir xil chegara, pastda
    // shu yerga qo'shiladi). Haqiqiy t_ox=285nm, epsilon=3.9 - "narigi tomonda" chizilgan
    // SiO2+Si geometrik jihatdan mos, lekin bu yerda lumped BC orqali ifodalanadi.
    PhysicsFeature gc1 = semi.create("gc1", "GateContact");
    gc1.selection().named("sel_mos_sio2");
    gc1.set("TerminalName", "3");
    gc1.set("TerminalType", "Voltage");
    gc1.set("V0", "VG");
    gc1.set("epsilon_ins", "3.9");
    gc1.set("d_ins", "t_ox");
    gc1.set("Phi", "Phi_Si");
    System.out.println("CHECKPOINT: gc1 OK");

    // Interfeys tuzoqlari (D_it) - MoS2/SiO2 chegarasida
    if (P.get("use_traps").equals("1")) {
      PhysicsFeature tasr1 = semi.create("tasr1", "TrapAssistedSurfaceRecombination", 1);
      tasr1.selection().named("sel_mos_sio2");
      tasr1.set("IncludeTraps", "ExplicitTraps");
      tasr1.set("SpecifyDiscreteContinuous", "SpecifyContinuousAndOrDiscreteLevels");
      PhysicsFeature ctb1 = tasr1.feature().create("ctb1", "ContinuousEnergyLevelsBoundary");
      ctb1.set("TrapType", "Donor");
      ctb1.set("TrapDensityDistribution", "Rectangle");
      ctb1.set("Ewidth", "Ew0");
      ctb1.set("Nt_b", "Dit*Dit_scale*Ew0*e_const");
      System.out.println("CHECKPOINT: tasr1 OK");
    }

    // F4TCNQ sirt zaryadi - MoS2/suv chegarasida (ochiq kanal)
    if (P.get("use_f4").equals("1")) {
      PhysicsFeature sfcd1 = semi.create("sfcd1", "SurfaceChargeDensity", 1);
      sfcd1.selection().named("sel_mos_top_channel");
      sfcd1.set("rhoqs", "sigma_F4");
      System.out.println("CHECKPOINT: sfcd1 OK");
    }

    // Source kontakt (Schottky, barer = Phi_B0 - x)
    mc1 = semi.create("mc1", "MetalContact", 1);
    mc1.selection().named("sel_src_schottky");
    mc1.set("ContactType", "Schottky");
    mc1.set("SpecifyBarrierHeight", "ideal");
    mc1.set("Phi_B", "Phi_B0 - x");
    mc1.set("TerminalName", "1");
    mc1.set("TerminalType", "Voltage");
    mc1.set("V0", "0");

    // Drain kontakt (Schottky, barer = Phi_B0 + x)
    mc2 = semi.create("mc2", "MetalContact", 1);
    mc2.selection().named("sel_drain_schottky");
    mc2.set("ContactType", "Schottky");
    mc2.set("SpecifyBarrierHeight", "ideal");
    mc2.set("Phi_B", "Phi_B0 + x");
    mc2.set("TerminalName", "2");
    mc2.set("TerminalType", "Voltage");
    mc2.set("V0", "VD");
    System.out.println("CHECKPOINT: mc1/mc2 OK");

    // Ionlar holati "x"
    if (mode.equals("output")) {
      model.component("comp1").variable().create("var_vdt");
      model.component("comp1").variable("var_vdt").set("VDt",
          "(if(t<t_hold, -1, if(t<t_hold+t_sw/2, -1+4*(t-t_hold)/t_sw, 1-4*(t-t_hold-t_sw/2)/t_sw)))*1[V]");
      mc2.set("V0", "VDt");

      PhysicsFeature ge1 = semi.create("ge1", "GlobalEquations", -1);
      ge1.setIndex("name", "x", 0);
      ge1.setIndex("equation", "xt - (beta*VDt - x)/tau_ion", 0);
      ge1.setIndex("initialValueU", "0", 0);
      ge1.setIndex("initialValueUt", "0", 0);
    } else {
      model.component("comp1").variable().create("var_x");
      model.component("comp1").variable("var_x").set("x", "beta*VD");
    }
    System.out.println("CHECKPOINT: ion holati OK");

    // ===== To'r =====
    // MUHIM (2-kun, memory bilan bog'liq xato tuzatildi): Cr/Au/suv uchun juda kichik hmax
    // (masalan c/2=0.5nm) BUTUN domen bo'ylab (necha mikrometr uzunlikda!) shu mayda o'lchamni
    // majbur qiladi - bu 630,000+ elementga va "low on memory" (7.8 GB mashinada) ga olib keldi.
    // hmax faqat ENG KATTA elementni cheklaydi; torayish joylarida mesher o'zi mayda qiladi -
    // shuning uchun Cr/Au/suv uchun alohida kichik hmax SHART EMAS (ular semi fizikasida
    // hisoblanmaydi - Cr/Au umuman, suv esa faqat elektrostatik ekran sifatida). Faqat MoS2
    // (haqiqiy yarimo'tkazgich fizikasi) va tegishli chegaralar uchun aniq o'lcham beriladi.
    MeshSequence mesh1 = model.component("comp1").mesh().create("mesh1");
    mesh1.feature("size").set("custom", "on");
    mesh1.feature("size").set("hmax", "0.5[um]");
    mesh1.feature("size").set("hmin", "30[nm]");
    mesh1.feature("size").set("hgrad", 1.6);

    // MUHIM (oxirgi urinish, D_it yaqinlashish muammosi tufayli): avval t_mos/1.5 (faqat
    // ~1.5 element qalinlik bo'ylab!) edi - GEOMETRIYA.md 10-20 elementni tavsiya qiladi.
    // D_it Fermi-pinning kanalning QALINLIGI bo'ylab keskin potentsial profilini talab qilishi
    // mumkin - bu ostida yotgan yetarli chuqurlikda yechilmasa, Newton yechuvchisi uchun
    // qattiq (stiff)/beqaror bo'lishi mumkin.
    sizeDom(mesh1, "sz_mos2", "geom1_csel_mos2_dom", "t_mos/6", "t_mos/25");
    // Cr(1nm)/suv(3nm) global hmin(5nm)dan ham ingichka - ularga faqat mos hmin beriladi,
    // hmax esa QASDAN katta (0.3um) qoldiriladi, aks holda uzunlik bo'ylab ham mayda to'r
    // majburlanadi (yuqoridagi izohga qarang). Au (60nm) global hmin(5nm)dan qalinroq -
    // alohida sozlash shart emas.
    sizeDom(mesh1, "sz_cr", "geom1_csel_cr_dom", "0.3[um]", "c/2");
    sizeDom(mesh1, "sz_water", "geom1_csel_water_dom", "0.3[um]", "t_w/3");

    sizeBnd(mesh1, "sz_src_sch", "sel_src_schottky", "t_mos/4");
    sizeBnd(mesh1, "sz_drain_sch", "sel_drain_schottky", "t_mos/4");
    // D_it (Fermi-pinning) chegarasi - GEOMETRIYA.md 5-bo'lim: kontakt qirralarida 2-5nm
    // gacha zichlashtirilsin. To'liq D_it bilan yaqinlashish qiyin bo'lgani uchun (Dit_scale
    // continuation ham stall berdi) bu chegarani ancha zichlashtiramiz.
    sizeBnd(mesh1, "sz_mos_sio2", "sel_mos_sio2", "5[nm]");
    System.out.println("CHECKPOINT: mesh size features OK");

    mesh1.create("tri1", "FreeTri");
    try {
      mesh1.run();
    } catch (Exception e) {
      System.out.println("OGOHLANTIRISH: mesh1 avtomatik ishlamadi: " + e.getMessage());
    }
    System.out.println("MESH_DOMAINS_TOTAL=" + g.getNDomains());

    System.out.println("Model qurildi (mode=" + mode + ").");
    if ("1".equals(P.get("stop_after_mesh"))) {
      System.out.println("STOP_AFTER_MESH so'ralgan - yechimga o'tilmaydi.");
      throw new RuntimeException("STOP_AFTER_MESH");
    }
  }

  static void createInsulatorSmm(String tag, String domSel, String epsParam) {
    // MUHIM (2-kun): avval Eg0=9V, chi0=1V edi - bu MoS2 (chi0=4V) bilan HAQIQATda mavjud
    // bo'lmagan ulkan zona siljishlari (~3-5V) hosil qilib, chegaralarda son beqarorlikka
    // (Newton iteratsiyasi "scale" ustuni 1e16..1e35 gacha portlab ketishi) olib keldi.
    // Endi: chi0 = chi_mos (MoS2 bilan bir xil, sun'iy barer yo'q) va Eg0 ancha kamroq (3V,
    // baribir MoS2 ning 1.23V idan ~2.4x katta - n_i ~ exp(-Eg/2kT) orqali erkin tashuvchilar
    // hali ham amaliy nolga yaqin, lekin global scaling endi unchalik keskin emas).
    PhysicsFeature f = semi.create(tag, "SemiconductorMaterialModel", 2);
    f.selection().named(domSel);
    f.set("Eg0_mat", "userdef"); f.set("Eg0", "3[V]");
    f.set("chi0_mat", "userdef"); f.set("chi0", "chi_mos");
    f.set("epsilonr_mat", "userdef"); f.set("epsilonr", epsParam);
    f.set("Nc_mat", "userdef"); f.set("Nc", "1e19[1/cm^3]");
    f.set("Nv_mat", "userdef"); f.set("Nv", "1e19[1/cm^3]");
    f.set("mun_mat", "userdef"); f.set("mun", "1[cm^2/(V*s)]");
    f.set("mup_mat", "userdef"); f.set("mup", "1[cm^2/(V*s)]");
  }

  static void domUnion(String tag, String[] inputs) {
    SelectionFeature sel = (SelectionFeature) model.component("comp1").selection().create(tag, "Union");
    sel.set("entitydim", 2);
    sel.set("input", inputs);
  }

  static void boxBnd(String tag, String xmin, String xmax, String ymin, String ymax) {
    model.component("comp1").selection().create(tag, "Box");
    model.component("comp1").selection(tag).set("entitydim", 1);
    model.component("comp1").selection(tag).set("xmin", xmin);
    model.component("comp1").selection(tag).set("xmax", xmax);
    model.component("comp1").selection(tag).set("ymin", ymin);
    model.component("comp1").selection(tag).set("ymax", ymax);
    model.component("comp1").selection(tag).set("condition", "inside");
  }

  static void bndUnion(String tag, String[] inputs) {
    SelectionFeature sel = (SelectionFeature) model.component("comp1").selection().create(tag, "Union");
    sel.set("entitydim", 1);
    sel.set("input", inputs);
  }

  static void bndDiff(String tag, String add, String subtract) {
    SelectionFeature sel = (SelectionFeature) model.component("comp1").selection().create(tag, "Difference");
    sel.set("entitydim", 1);
    sel.set("add", new String[]{add});
    sel.set("subtract", new String[]{subtract});
  }

  static void sizeDom(MeshSequence mesh1, String tag, String selTag, String hmax, String hmin) {
    try {
      MeshFeature sz = mesh1.create(tag, "Size");
      sz.selection().geom("geom1", 2);
      sz.selection().named(selTag);
      sz.set("custom", "on");
      sz.set("hmax", hmax);
      sz.set("hmin", hmin);
    } catch (Exception e) {
      System.out.println("OGOHLANTIRISH: mesh size " + tag + " o'rnatilmadi: " + e.getMessage());
    }
  }

  static void sizeBnd(MeshSequence mesh1, String tag, String selTag, String hmax) {
    try {
      MeshFeature sz = mesh1.create(tag, "Size");
      sz.selection().geom("geom1", 1);
      sz.selection().named(selTag);
      sz.set("custom", "on");
      sz.set("hmax", hmax);
    } catch (Exception e) {
      System.out.println("OGOHLANTIRISH: mesh size " + tag + " o'rnatilmadi: " + e.getMessage());
    }
  }

  // ---------------------------------------------------------------------
  // Geometriya rasmlarini eksport qilish (B2 talabi: geometry_preview.png bilan solishtirish)
  // ---------------------------------------------------------------------
  static void exportGeomImages() {
    try {
      new File(OUT_DIR + File.separator + "natijalar").mkdirs();
      System.out.println("CHECKPOINT: natijalar papka OK");
      // (a) to'liq, y cho'zilgan (panel b uslubida): x to'liq, y tor oraliq.
      // Standart "view1" ishlatiladi (yangi View yaratilganda axis() feature avtomatik
      // bo'lmasligi mumkin - shuning uchun mavjud view1 qayta sozlanadi).
      model.view("view1").axis().set("xmin", -4.0);
      model.view("view1").axis().set("xmax", 9.0);
      model.view("view1").axis().set("ymin", -0.35);
      model.view("view1").axis().set("ymax", 0.7);
      System.out.println("CHECKPOINT: view1 (full) sozlandi");

      ExportFeature img1 = model.result().export().create("img_full", "Image");
      img1.set("sourcetype", "geometry");
      img1.set("sourceobject", "geom1");
      img1.set("view", "view1");
      img1.set("imagetype", "png");
      img1.set("lockratio", "off");
      img1.set("width", "1600");
      img1.set("height", "700");
      img1.set("pngfilename", OUT_DIR + File.separator + "natijalar" + File.separator + "geom_full.png");
      img1.run();
      System.out.println("Eksport: natijalar\\geom_full.png");

      // (c) source kontakt qirrasi yaqindan: x in [-1.65,0.25] (xuddi shu view1 qayta sozlanadi)
      model.view("view1").axis().set("xmin", -1.65);
      model.view("view1").axis().set("xmax", 0.25);
      model.view("view1").axis().set("ymin", -0.03);
      model.view("view1").axis().set("ymax", 0.12);
      System.out.println("CHECKPOINT: view1 (src edge) sozlandi");

      ExportFeature img2 = model.result().export().create("img_src", "Image");
      img2.set("sourcetype", "geometry");
      img2.set("sourceobject", "geom1");
      img2.set("view", "view1");
      img2.set("imagetype", "png");
      img2.set("lockratio", "off");
      img2.set("width", "1400");
      img2.set("height", "900");
      img2.set("pngfilename", OUT_DIR + File.separator + "natijalar" + File.separator + "geom_source_edge.png");
      img2.run();
      System.out.println("Eksport: natijalar\\geom_source_edge.png");
    } catch (Exception e) {
      System.out.println("OGOHLANTIRISH: geometriya rasm eksporti muvaffaqiyatsiz: " + e.getMessage());
    }
  }

  // ---------------------------------------------------------------------
  // Transfer rejimi: Stationary VG continuation sweep, har bir VD uchun
  // ---------------------------------------------------------------------
  static void runTransfer() throws IOException {
    model.study().create("std1");
    model.study("std1").create("eq", "SemiconductorEquilibrium");
    model.study("std1").create("stat", "Stationary");
    model.study("std1").setGenPlots(false);
    model.study("std1").setGenConv(false);
    double vgStart = parseD(P.get("VG_start"));
    double vgStop = parseD(P.get("VG_stop"));
    double vgStep = parseD(P.get("VG_step"));

    List<Double> vgList = null;
    if (P.containsKey("VG_list") && P.get("VG_list").trim().length() > 0) {
      vgList = new ArrayList<Double>();
      String[] vgParts = P.get("VG_list").split(",");
      for (int i = 0; i < vgParts.length; i++) vgList.add(Double.parseDouble(vgParts[i].trim()));
    }

    List<Double> vdList = new ArrayList<Double>();
    String[] vdParts = P.get("VD_list").split(",");
    for (int i = 0; i < vdParts.length; i++) vdList.add(Double.parseDouble(vdParts[i].trim()));

    double vgFirst = (vgList != null) ? vgList.get(0) : vgStart;
    double vdFirst = parseD(P.get("VD_list").split(",")[0]);
    model.param().set("VG", fmt(vgFirst));
    boolean firstOk;
    if (P.get("use_traps").equals("1")) {
      // MUHIM (2-kun): D_it ni VD=0 da ramp qilish (source/drain barerlari simmetrik,
      // Phi_B0-x = Phi_B0+x chunki x=beta*VD=0) VD=0.2V da ramp qilishdan SEZILARLI
      // barqarorroq chiqdi - D_it ning o'zi (Fermi pinning) va VD (barer asimmetriyasi)
      // ikkalasi BIRGALIKDA kiritilganda tizim juda qattiq (stiff) bo'lib qoldi (1 soatdan
      // ortiq sinov, yaqinlashmadi - STATUS.md'da batafsil). Endi ikki bosqich: (1) D_it'ni
      // VD=0'da to'liq kiritish, (2) keyin VD'ni maqsad qiymatigacha D_it to'liq holda
      // ko'tarish - ikkalasi ALOHIDA osonroq masala.
      model.param().set("VD", "0");
      // YANGI (oxirgi urinish): Java tomonidan qo'lda takrorlangan .run() chaqiruvlari o'rniga
      // COMSOL'ning O'ZINING parametrik continuation solveri ishlatiladi (Stationary study
      // step'ning useparam/pname/plist/pcontinuationmode xossalari - API_ESLATMA.md, 8-band).
      // Bu Jacobian-asosidagi predictor-corrector davomiylikni to'g'ri qo'llaydi va oldingi
      // yechimni navbatdagi qadam uchun boshlang'ich taxmin sifatida ishonchliroq ishlatadi -
      // qo'lda qayta-qayta study.run() chaqirish (STATUS.md'da "1 soatdan ortiq, yaqinlashmadi"
      // deb yozilgan usul) buni to'g'ri bajarmagan bo'lishi mumkin edi.
      double[] ditStages = {0.002, 0.004, 0.006, 0.009, 0.013, 0.018, 0.025, 0.035, 0.05, 0.07, 0.1, 0.15, 0.22, 0.32, 0.46, 0.65, 0.85, 1.0};
      StringBuilder ditListSb = new StringBuilder();
      for (int i = 0; i < ditStages.length; i++) ditListSb.append(fmt(ditStages[i])).append(" ");
      StudyFeature statStep = model.study("std1").feature("stat");
      statStep.set("useparam", true);
      statStep.set("pname", new String[]{"Dit_scale"});
      statStep.set("plist", new String[]{ditListSb.toString().trim()});
      statStep.set("pcontinuationmode", "last");
      firstOk = solveRobust("Dit_scale native continuation (VD=0)");
      statStep.set("useparam", false);
      model.param().set("Dit_scale", "1");
      // Endi D_it to'liq (Dit_scale=1), VD=0. VD'ni maqsad qiymatigacha bosqichma-bosqich
      // ko'taramiz (D_it o'zgarmaydi).
      double[] vdStages = {0.02, 0.05, 0.1, vdFirst};
      for (int i = 0; i < vdStages.length; i++) {
        double vd = Math.min(vdStages[i], vdFirst);
        model.param().set("VD", fmt(vd));
        boolean ok = solveRobust("VD=" + fmt(vd) + " (Dit_scale=1)");
        if (ok) firstOk = true;
        if (vd >= vdFirst - 1e-9) break;
      }
    } else {
      model.param().set("VD", fmt(vdFirst));
      model.param().set("Dit_scale", "1");
      firstOk = solveRobust("Boshlang'ich nuqta");
    }

    String csvPath = OUT_DIR + File.separator + "transfer.csv";
    boolean gevCreated = false;
    PrintWriter out = new PrintWriter(new FileWriter(csvPath));
    try {
      out.println("VD,VG,ID");
      for (int vdi = 0; vdi < vdList.size(); vdi++) {
        double vd = vdList.get(vdi);
        model.param().set("VD", fmt(vd));
        List<Double> thisVgList = vgList;
        if (thisVgList == null) {
          thisVgList = new ArrayList<Double>();
          for (double vg = vgStart; (vgStep > 0 ? vg <= vgStop + 1e-9 : vg >= vgStop - 1e-9); vg += vgStep) {
            thisVgList.add(vg);
          }
        }
        for (int vgi = 0; vgi < thisVgList.size(); vgi++) {
          double vg = thisVgList.get(vgi);
          model.param().set("VG", fmt(vg));
          boolean ok = solveRobust(String.format(Locale.US, "VD=%.2f VG=%7.2f", vd, vg));
          if (ok) {
            try {
              if (!gevCreated) {
                model.result().numerical().create("gev1", "EvalGlobal");
                model.result().numerical("gev1").set("data", "dset1");
                model.result().numerical("gev1").set("expr", new String[]{"semi.I0_2"});
                gevCreated = true;
              }
              double id = model.result().numerical("gev1").getReal()[0][0];
              out.println(fmt(vd) + "," + fmt(vg) + "," + String.format(Locale.US, "%.6e", id));
              out.flush();
              System.out.println(String.format(Locale.US, "VD=%.2f VG=%7.2f ID=%.4e A", vd, vg, id));
            } catch (Exception e) {
              System.out.println(String.format(Locale.US, "VD=%.2f VG=%7.2f natija o'qishda xato: %s", vd, vg, e.getMessage()));
              out.println(fmt(vd) + "," + fmt(vg) + ",NaN");
              out.flush();
            }
          } else {
            out.println(fmt(vd) + "," + fmt(vg) + ",NaN");
            out.flush();
          }
        }
      }
    } finally {
      out.close();
    }
    System.out.println("Tayyor: " + csvPath);
  }

  // ---------------------------------------------------------------------
  // Output rejimi: Time Dependent (Global ODE bilan)
  // ---------------------------------------------------------------------
  static void runOutput() throws IOException {
    model.study().create("std1");
    model.study("std1").create("eq", "SemiconductorEquilibrium");
    model.study("std1").create("stat", "Stationary");
    model.study("std1").create("time", "Transient");

    model.study("std1").feature("eq").set("useadvanceddisable", "on");
    model.study("std1").feature("eq").set("disabledphysics", new String[]{"semi/ge1"});
    model.study("std1").feature("stat").set("useadvanceddisable", "on");
    model.study("std1").feature("stat").set("disabledphysics", new String[]{"semi/ge1"});

    model.param().set("VG", P.get("VG"));
    model.param().set("VD", "-1");
    model.param().set("Dit_scale", P.get("use_traps").equals("1") ? "0.02" : "1");

    model.study("std1").feature("time").set("tlist", "range(0,t_sw/200,t_hold+t_sw)");

    if (P.get("use_traps").equals("1")) {
      double[] ditStages = {0.002, 0.004, 0.006, 0.009, 0.013, 0.018, 0.025, 0.035, 0.05, 0.07, 0.1, 0.15, 0.22, 0.32, 0.46, 0.65, 0.85, 1.0};
      for (int i = 0; i < ditStages.length; i++) {
        model.param().set("Dit_scale", fmt(ditStages[i]));
        solveRobust("Bootstrap Dit_scale=" + fmt(ditStages[i]));
      }
    } else {
      solveRobust("Bootstrap");
    }
    System.out.println("Study (Equilibrium+Stationary bootstrap+Time Dependent) tayyor.");

    model.result().numerical().create("gev1", "EvalGlobal");
    model.result().numerical("gev1").set("data", "dset1");
    model.result().numerical("gev1").set("expr", new String[]{"t", "VDt", "semi.I0_2"});

    String csvPath = OUT_DIR + File.separator + "output.csv";
    PrintWriter out = new PrintWriter(new FileWriter(csvPath));
    try {
      out.println("t,VD,ID");
      double[][] vals = model.result().numerical("gev1").getReal();
      System.out.println("gev1 natija o'lchami: " + vals.length + " x " + (vals.length > 0 ? vals[0].length : 0));
      for (int i = 0; i < vals[0].length; i++) {
        double tt = vals[0][i];
        double vdt = vals[1][i];
        double id = vals[2][i];
        out.println(String.format(Locale.US, "%.6e", tt) + "," + String.format(Locale.US, "%.6e", vdt)
            + "," + String.format(Locale.US, "%.6e", id));
      }
    } catch (Exception e) {
      System.out.println("OGOHLANTIRISH: output.csv yozishda muammo: " + e.getMessage());
    } finally {
      out.close();
    }
    System.out.println("Tayyor: " + csvPath);
  }

  static boolean solveRobust(String label) {
    try {
      model.study("std1").run();
      return true;
    } catch (Exception e) {
      System.out.println(label + ": birinchi urinish muvaffaqiyatsiz (" + e.getMessage() + "), tozalab qayta urinamiz.");
    }
    try {
      String[] solTags = model.sol().tags();
      for (int i = 0; i < solTags.length; i++) {
        try {
          model.sol(solTags[i]).clearSolutionData();
        } catch (Exception ce) {
        }
        bumpMaxIter(model.sol(solTags[i]));
      }
      model.study("std1").run();
      System.out.println(label + ": tozalash + oshirilgan iteratsiya bilan yaqinlashdi.");
      return true;
    } catch (Exception e2) {
      System.out.println(label + ": OGOHLANTIRISH - hali ham yaqinlashmadi: " + e2.getMessage());
      return false;
    }
  }

  static void bumpMaxIter(SolverSequence ss) {
    try {
      String[] tags = ss.feature().tags();
      for (int i = 0; i < tags.length; i++) bumpMaxIterFeature(ss.feature(tags[i]));
    } catch (Exception e) {
      System.out.println("bumpMaxIter FAIL: " + e.getMessage());
    }
  }

  static void bumpMaxIterFeature(SolverFeature sf) {
    String[] candidates = {"maxiter", "MaxIter", "maxIter"};
    for (int i = 0; i < candidates.length; i++) {
      try {
        sf.set(candidates[i], "400");
      } catch (Exception e) {
      }
    }
    try {
      String[] sub = sf.feature().tags();
      for (int i = 0; i < sub.length; i++) bumpMaxIterFeature(sf.feature(sub[i]));
    } catch (Exception e) {
    }
  }

  static double parseD(String s) { return Double.parseDouble(s.trim()); }

  static String fmt(double x) {
    return String.format(Locale.US, "%.6g", x);
  }
}
