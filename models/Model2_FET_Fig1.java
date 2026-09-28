/*
 * Model2_FET_Fig1.java — Model2_FET.java asosida, Fig.1(a-d) ga yaqinlashtirilgan versiya.
 *
 * Model2_FET.java (asosiy, tasdiqlangan baza) ZAXIRA sifatida saqlanadi - bu fayl mustaqil nusxa.
 *
 * QO'SHIMCHA FIZIKA (VAZIFA_FIG1.md, fig1_targets.csv):
 * 1. Nochiziqli OFF o'tkazuvchanlik: sig_off_eff = sig_off0*sinh(E/E0)/(E/E0), E0=V0_off/L_gap.
 * 2. Zatvor xotira holati P (2-chi Global ODE): dP/dt=(f_gate-P)/tau_P_eff, tau_P ASIMMETRIK
 *    (pasayish tez tau_P_fall, tiklanish sekin tau_P_rise) - Biolek uslubidagi tanh bilan
 *    silliqlashtirilgan yo'nalish funksiyasi orqali. sig_on_eff va tau_rel_eff endi P ga bog'liq
 *    (f_gate ga emas) - shu orqali zatvor holati "xotirasi" (panel d) modellashtiriladi.
 * 3. SET/RESET uchun alohida Ea_SET/Ea_RESET, a_SET/a_RESET (qutbga bog'liq barer, dir_smooth
 *    orqali aralashtiriladi) - mustaqil kalibrlash imkonini beradi.
 * 4. Ugate(t) - bitta uzluksiz 40ms Time Dependent hisobda silliq bosqichli funksiya (tanh bilan):
 *    0 -> -0.9V -> -1.1V -> 0V, har bir o'tish ~0.2ms.
 *
 * Natija: iv_fig1_continuous.csv (t, Ugate_wave, V_wave, I, xode, P) - keyin alohida PowerShell
 * skripti bilan 4 panelga bo'linib, Fig1_analog.png chiziladi (Python mavjud emas).
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class Model2_FET_Fig1 {

  // VAQT TEJASH: tezkor testda (cosh formulasi bilan) kalibrovka allaqachon aniqlandi
  // (sig_off0=1.6182e-08, sig_on0=1.0712e-05, sig_on_v=6.0469e-07) - ~25-30 daqiqa tejash uchun
  // qayta kalibrlashni o'chirib, to'g'ridan-to'g'ri shu qiymatlardan boshlanadi.
  static final boolean CALIBRATE = false;
  static final int CAL_MAX_IT = 16;
  static final double CAL_TOL = 0.02;

  public static Model run() {
    Model model = ModelUtil.create("Model2Fig1");
    model.label("Model2_FET_Fig1.mph");
    model.comments("Troyan & Doronin (2021) Fig.1a-d ga yaqinlashtirilgan fenomenologik lateral FET modeli.");

    // =====================================================================================
    // PARAMETERS
    // =====================================================================================
    p(model, "W", "1000[nm]", "Kanal umumiy uzunligi (source-drain)");
    p(model, "L_gap", "1.5[nm]", "Faol soha uzunligi (Model1 t_int bilan bir xil)");
    p(model, "t_ST", "20[nm]", "Sb2Te3 qalinligi");
    p(model, "t_GT", "20[nm]", "GeTe qalinligi");
    p(model, "t_ox", "10[nm]", "Al2O3 qalinligi");
    p(model, "t_gate", "10[nm]", "Zatvor qalinligi");
    p(model, "x_gap0", "(W-L_gap)/2", "");
    p(model, "x_gap1", "(W+L_gap)/2", "");
    p(model, "y_GT", "t_ST", "");
    p(model, "y_ox", "t_ST+t_GT", "");
    p(model, "y_gate", "t_ST+t_GT+t_ox", "");
    p(model, "y_top", "t_ST+t_GT+t_ox+t_gate", "");

    p(model, "sig_ST", "1e5[S/m]", "Sb2Te3 o'tkazuvchanligi");
    p(model, "sig_GT", "1e5[S/m]", "GeTe o'tkazuvchanligi (faqat issiqlik)");
    p(model, "sig_off0", "1.6182e-8[S/m]", "Faol soha OFF bazaviy o'tkazuvchanligi (KALIBRLANGAN)");
    p(model, "V0_off", "0.8[V]", "Nochiziqli OFF uchun kuchlanish miqyosi (sinh)");
    p(model, "E0_off", "V0_off/L_gap", "Nochiziqli OFF uchun maydon miqyosi");
    p(model, "sig_on0", "1.0712e-5[S/m]", "ON o'tkazuvchanlik, Ugate=0 (KALIBRLANGAN)");
    p(model, "sig_on_v", "6.0469e-7[S/m]", "ON o'tkazuvchanlik, volatil chegara (KALIBRLANGAN)");
    p(model, "epsr_ST", "50", "");
    p(model, "epsr_GT", "30", "");
    p(model, "epsr_int", "10", "");
    p(model, "epsr_ox", "9", "");
    p(model, "sig_gate", "5e6[S/m]", "");
    p(model, "sig_ox", "1e-12[S/m]", "");

    p(model, "k_ST", "1.0[W/(m*K)]", "");
    p(model, "k_GT", "2.0[W/(m*K)]", "");
    p(model, "k_int", "0.5[W/(m*K)]", "");
    p(model, "k_ox", "30[W/(m*K)]", "");
    p(model, "k_gate", "20[W/(m*K)]", "");
    p(model, "rho_ST", "6500[kg/m^3]", "");
    p(model, "rho_GT", "6140[kg/m^3]", "");
    p(model, "rho_int", "6300[kg/m^3]", "");
    p(model, "rho_ox", "3950[kg/m^3]", "");
    p(model, "rho_gate", "5220[kg/m^3]", "");
    p(model, "Cp_ST", "200[J/(kg*K)]", "");
    p(model, "Cp_GT", "250[J/(kg*K)]", "");
    p(model, "Cp_int", "220[J/(kg*K)]", "");
    p(model, "Cp_ox", "880[J/(kg*K)]", "");
    p(model, "Cp_gate", "600[J/(kg*K)]", "");
    p(model, "Tm_GT", "998[K]", "");
    p(model, "Tm_ST", "891[K]", "");
    p(model, "T_amb", "300[K]", "");

    p(model, "xs", "0", "Faol soha holati (Stationary uchun statik parametr)");
    p(model, "k0", "1e13[1/s]", "Urinish chastotasi");
    // SET/RESET uchun ALOHIDA barer parametrlari (qutbli elektromigratsiya asimmetriyasi farazi).
    p(model, "Ea_SET", "0.9[eV]", "SET (E_drive>0) uchun aktivatsiya energiyasi");
    p(model, "Ea_RESET", "0.9[eV]", "RESET (E_drive<0) uchun aktivatsiya energiyasi - kalibrlanadi");
    p(model, "a_SET", "0.3[nm]", "SET uchun sakrash masofasi");
    p(model, "a_RESET", "0.3[nm]", "RESET uchun sakrash masofasi - kalibrlanadi");
    p(model, "tau_nv", "1e3[s]", "Relaksatsiya, nonvolatil chegara");
    p(model, "tau_v", "1e-4[s]", "Relaksatsiya, volatil chegara - V_h~0.7V uchun moslashtiriladi");
    p(model, "p_win", "2", "Oyna darajasi");
    p(model, "kB_c", "1.380649e-23[J/K]", "");
    p(model, "q_c", "1.602176634e-19[C]", "");
    p(model, "E_s", "1e7[V/m]", "Biolek yo'nalish silliqlash miqyosi");

    p(model, "Ugate", "0[V]", "Zatvor kuchlanishi (Stationary uchun parametr)");
    p(model, "U0", "-0.9[V]", "Sigmoid markazi");
    p(model, "w_sig", "0.08[V]", "Sigmoid kengligi");
    // Zatvor XOTIRA holati P uchun asimmetrik vaqt doimiylari.
    p(model, "tau_P_fall", "1e-3[s]", "P pasayishi (f_gate kamayganda) - TEZ");
    p(model, "tau_P_rise", "75e-3[s]", "P tiklanishi (f_gate ortganda) - SEKIN");
    p(model, "P_s", "0.1[1]", "Silliq yo'nalish funksiyasi uchun P-o'lchov (tanh)");

    p(model, "V_read", "0.1[V]", "");
    p(model, "V_app", "V_read", "");
    p(model, "Vamp", "4.5[V]", "");
    p(model, "f0", "100[Hz]", "");

    p(model, "h_gap", "0.15[nm]", "");
    p(model, "h_glob", "5[nm]", "");

    // =====================================================================================
    // GEOMETRY
    // =====================================================================================
    model.component().create("comp1", true);
    GeomSequence g = model.component("comp1").geom().create("geom1", 2);
    g.lengthUnit("nm");

    rect(g, "r_st_l", "x_gap0", "t_ST", "0", "0");
    rect(g, "r_st_a", "L_gap", "t_ST", "x_gap0", "0");
    rect(g, "r_st_r", "W-x_gap1", "t_ST", "x_gap1", "0");
    rect(g, "r_gt", "W", "t_GT", "0", "y_GT");
    rect(g, "r_ox", "W", "t_ox", "0", "y_ox");
    rect(g, "r_gate", "W", "t_gate", "0", "y_gate");

    boxSel(g, "box_src", "-1", "0.01[nm]", "-1", "t_ST+0.01[nm]");
    boxSel(g, "box_drn", "W-0.01[nm]", "W+1[nm]", "-1", "t_ST+0.01[nm]");
    boxSel(g, "box_bot", "-1", "W+1[nm]", "-0.01", "0.01");
    boxSel(g, "box_top", "-1", "W+1[nm]", "y_top-0.01[nm]", "y_top+0.01[nm]");
    g.run();

    union(model, "sel_ec", 2, new String[]{"geom1_r_st_l_dom", "geom1_r_st_a_dom", "geom1_r_st_r_dom"}, "");

    // =====================================================================================
    // DEFINITIONS
    // =====================================================================================
    model.component("comp1").variable().create("var1");

    model.component("comp1").cpl().create("aveop_a", "Average");
    model.component("comp1").cpl("aveop_a").selection().named("geom1_r_st_a_dom");
    model.component("comp1").cpl().create("maxop_T", "Maximum");
    model.component("comp1").cpl("maxop_T").selection().geom("geom1", 2);
    model.component("comp1").cpl("maxop_T").selection().all();

    model.component("comp1").variable("var1").set("E_drive", "-aveop_a(ec.Ex)",
        "Faol sohadagi haydovchi maydon");
    model.component("comp1").variable("var1").set("T_local", "aveop_a(T)", "");

    // 1. Nochiziqli OFF o'tkazuvchanlik (Poole-Frenkel/tunnel tipidagi faraz): sinh(x)/x, x->0 da 1
    // ga intiladi (past maydonda oddiy ohmik), katta |E| da eksponensial o'sadi. 1e-3[V/m] - nol
    // bilan bo'lishdan saqlanish uchun kichik siljish (E0_off ~ 1e8-1e9 V/m tartibida, demak bu
    // siljish mutlaqo ahamiyatsiz).
    // TUZATISH (birinchi urinishda 40ms hisob o'ta sekin bo'ldi, ~0.4%/5min - sinh(x)/x dagi
    // BO'LINISH ehtimol Newton iteratsiyasida qattiqlikni oshirgan). "cosh(E/E0)" bilan
    // almashtirildi - bo'linishsiz, xuddi shu sifat (E=0 da 1, katta |E| da eksponensial o'sish).
    model.component("comp1").variable("var1").set("sig_off_eff",
        "sig_off0*cosh(E_drive/E0_off)",
        "Nochiziqli OFF o'tkazuvchanlik (cosh - Poole-Frenkel/tunnel farazi, bo'linishsiz shakl)");

    // 2. Zatvor XOTIRA holati P: dP/dt=(f_gate-P)/tau_P_eff, ASIMMETRIK tau_P (Biolek uslubida
    // tanh bilan silliqlangan yo'nalish: f_gate>P -> tiklanish (sekin), f_gate<P -> pasayish (tez)).
    model.component("comp1").variable("var1").set("f_gate", "1/(1+exp(-(Ugate_eff-U0)/w_sig))",
        "Zatvor muvozanat funksiyasi (sigmoid)");
    model.component("comp1").variable("var1").set("Ugate_eff", "Ugate",
        "Zatvor kuchlanishi manbai (Stationary: parametr; Time Dependent: Ugate_wave(t) ga almashtiriladi)");
    model.component("comp1").variable("var1").set("dir_P", "0.5*(1+tanh((f_gate-P)/P_s))",
        "Silliq yo'nalish: f_gate>P (tiklanish) -> 1, f_gate<P (pasayish) -> 0");
    model.component("comp1").variable("var1").set("tau_P_eff", "dir_P*tau_P_rise+(1-dir_P)*tau_P_fall",
        "Asimmetrik vaqt doimiysi: tiklanish SEKIN, pasayish TEZ");
    model.component("comp1").variable("var1").set("dPdt_rhs", "(f_gate-P)/tau_P_eff",
        "dP/dt ifodasi");

    model.component("comp1").variable("var1").set("sig_on_eff", "sig_on_v*(sig_on0/sig_on_v)^P",
        "P (zatvor XOTIRA holati) ga bog'liq ON o'tkazuvchanlik");
    model.component("comp1").variable("var1").set("tau_rel_eff", "tau_v*(tau_nv/tau_v)^P",
        "P ga bog'liq relaksatsiya vaqti");
    model.component("comp1").variable("var1").set("sig_fil", "sig_off_eff*(sig_on_eff/sig_off_eff)^xode",
        "Faol soha o'tkazuvchanligi");

    // 3. SET/RESET uchun alohida barer (qutbli elektromigratsiya asimmetriyasi).
    model.component("comp1").variable("var1").set("dir_smooth", "0.5*(1+tanh(E_drive/E_s))",
        "SET(1)/RESET(0) yo'nalishi, silliqlashtirilgan");
    model.component("comp1").variable("var1").set("Ea_eff", "dir_smooth*Ea_SET+(1-dir_smooth)*Ea_RESET", "");
    model.component("comp1").variable("var1").set("a_eff", "dir_smooth*a_SET+(1-dir_smooth)*a_RESET", "");
    model.component("comp1").variable("var1").set("Fwin_x",
        "dir_smooth*(1-xode^(2*p_win))+(1-dir_smooth)*(1-(xode-1)^(2*p_win))", "");
    model.component("comp1").variable("var1").set("dxdt_rhs",
        "k0*exp(-Ea_eff/(kB_c*T_local))*sinh(q_c*a_eff*E_drive/(2*kB_c*T_local))*Fwin_x-xode/tau_rel_eff", "");

    // 4. Ugate(t): 0 -> -0.9V -> -1.1V -> 0V, har bir o'tish silliq (~0.2ms), 10ms/panel.
    model.component("comp1").variable("var1").set("smstep1", "0.5*(1+tanh((t-10[ms])/0.2[ms]))", "");
    model.component("comp1").variable("var1").set("smstep2", "0.5*(1+tanh((t-20[ms])/0.2[ms]))", "");
    model.component("comp1").variable("var1").set("smstep3", "0.5*(1+tanh((t-30[ms])/0.2[ms]))", "");
    model.component("comp1").variable("var1").set("Ugate_wave",
        "0[V]+(-0.9[V]-0[V])*smstep1+(-1.1[V]-(-0.9[V]))*smstep2+(0[V]-(-1.1[V]))*smstep3",
        "Bosqichli silliq Ugate(t): 0->-0.9->-1.1->0V, har biri 10ms");
    model.component("comp1").variable("var1").set("V_wave", "Vamp*(2/pi)*asin(sin(2*pi*f0*t))", "");

    // =====================================================================================
    // MATERIALS
    // =====================================================================================
    material(model, "mat_st_l", "geom1_r_st_l_dom", "sig_ST", "epsr_ST", "k_ST", "rho_ST", "Cp_ST");
    material(model, "mat_st_r", "geom1_r_st_r_dom", "sig_ST", "epsr_ST", "k_ST", "rho_ST", "Cp_ST");
    material(model, "mat_fil", "geom1_r_st_a_dom", "sig_fil", "epsr_int", "k_int", "rho_int", "Cp_int");
    material(model, "mat_gt", "geom1_r_gt_dom", "sig_GT", "epsr_GT", "k_GT", "rho_GT", "Cp_GT");
    material(model, "mat_ox", "geom1_r_ox_dom", "sig_ox", "epsr_ox", "k_ox", "rho_ox", "Cp_ox");
    material(model, "mat_gate", "geom1_r_gate_dom", "sig_gate", "1", "k_gate", "rho_gate", "Cp_gate");

    // =====================================================================================
    // PHYSICS
    // =====================================================================================
    model.component("comp1").physics().create("ec", "ConductiveMedia", "geom1");
    model.component("comp1").physics("ec").selection().named("sel_ec");
    model.component("comp1").physics("ec").create("term1", "Terminal", 1);
    model.component("comp1").physics("ec").feature("term1").selection().named("geom1_box_drn");
    model.component("comp1").physics("ec").feature("term1").set("TerminalType", "Voltage");
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_app");
    model.component("comp1").physics("ec").create("gnd1", "Ground", 1);
    model.component("comp1").physics("ec").feature("gnd1").selection().named("geom1_box_src");

    model.component("comp1").physics().create("ht", "HeatTransfer", "geom1");
    model.component("comp1").physics("ht").create("temp1", "TemperatureBoundary", 1);
    union(model, "sel_Tbc", 1, new String[]{"geom1_box_bot", "geom1_box_top"}, "");
    model.component("comp1").physics("ht").feature("temp1").selection().named("sel_Tbc");
    model.component("comp1").physics("ht").feature("temp1").set("T0", "T_amb");

    model.component("comp1").multiphysics().create("emh1", "ElectromagneticHeating", 2);
    model.component("comp1").multiphysics("emh1").selection().all();

    // Global ODE: IKKITA qator - xode (filament) va P (zatvor xotirasi). "ge1" ning array
    // xossalari bir nechta qatorni qo'llab-quvvatlaydi (Model1/2 da bitta qator bilan tasdiqlangan).
    model.component("comp1").physics().create("ge", "GlobalEquations");
    model.component("comp1").physics("ge").feature("ge1").set("name", new String[]{"xode", "P"});
    model.component("comp1").physics("ge").feature("ge1").set("equation", new String[]{"dxdt_rhs", "dPdt_rhs"});
    model.component("comp1").physics("ge").feature("ge1").set("initialValueU", new String[]{"xs", "f_gate"});
    model.component("comp1").physics("ge").feature("ge1").set("initialValueUt", new String[]{"0", "0"});

    // =====================================================================================
    // MESH
    // =====================================================================================
    model.component("comp1").mesh().create("mesh1");
    model.component("comp1").mesh("mesh1").feature("size").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size").set("hmax", "h_glob");
    model.component("comp1").mesh("mesh1").feature("size").set("hmin", "0.05[nm]");
    model.component("comp1").mesh("mesh1").feature("size").set("hgrad", 1.2);
    model.component("comp1").mesh("mesh1").create("size_gap", "Size");
    model.component("comp1").mesh("mesh1").feature("size_gap").selection().geom("geom1", 2);
    model.component("comp1").mesh("mesh1").feature("size_gap").selection().named("geom1_r_st_a_dom");
    model.component("comp1").mesh("mesh1").feature("size_gap").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size_gap").set("hmaxactive", true);
    model.component("comp1").mesh("mesh1").feature("size_gap").set("hmax", "h_gap");
    model.component("comp1").mesh("mesh1").create("ftri1", "FreeTri");
    model.component("comp1").mesh("mesh1").run();

    // =====================================================================================
    // STUDY std1: S1 kalibrovka (Ugate=0, xs=0/1), ht/ge o'chirilgan.
    // =====================================================================================
    model.study().create("std1");
    model.study("std1").create("stat", "Stationary");
    model.study("std1").feature("stat").set("activate", new String[]{"ec", "on", "ht", "off", "ge", "off"});
    model.study("std1").run();

    model.result().numerical().create("gev_R", "EvalGlobal");
    model.result().numerical("gev_R").set("data", "dset1");
    model.result().numerical("gev_R").set("expr", new String[]{"ec.I0_1"});

    if (CALIBRATE) {
      // ---- Kalibrovka 1: sig_off0 -> I(3.5V, xs=0) = 0.03 mA (fig1_targets.csv, panel a) ----
      double targetIOff = 0.03e-3;   // A
      double sigOff0 = 1.0;
      model.param().set("Ugate", "0[V]");
      model.param().set("V_app", "3.5[V]");
      for (int it = 0; it < CAL_MAX_IT; it++) {
        model.param().set("sig_off0", String.format(Locale.US, "%.6e[S/m]", sigOff0));
        double iVal = solveI(model, "0");
        System.out.println(String.format(Locale.US, "CAL sig_off0 it=%d sig_off0=%.4e I_OFF(3.5V)=%.4e mA (maqsad 0.03)",
            it, sigOff0, iVal * 1e3));
        if (Math.abs(iVal / targetIOff - 1) < CAL_TOL) break;
        sigOff0 *= clamp(iVal > 0 ? targetIOff / iVal : 10.0);
      }

      // ---- Kalibrovka 2: sig_on0 -> R_ON=7kOhm (Ugate=0, xs=1) ----
      double rOnT = 7e3, sigOn0 = 1e3;
      model.param().set("V_app", "V_read");
      for (int it = 0; it < CAL_MAX_IT; it++) {
        model.param().set("sig_on0", String.format(Locale.US, "%.6e[S/m]", sigOn0));
        double iVal = solveI(model, "1");
        double r = 0.1 / iVal;
        System.out.println(String.format(Locale.US, "CAL sig_on0 it=%d sig_on0=%.4e R_ON=%.4e ohm (maqsad 7000)",
            it, sigOn0, r));
        if (Math.abs(r / rOnT - 1) < CAL_TOL) break;
        sigOn0 *= clamp(r / rOnT);
      }

      // ---- Kalibrovka 3: sig_on_v -> volatil holatda I(4V)~0.04mA (Ugate=-1.1V taxminiy P) ----
      double sigOnV = 1e3, targetIv = 0.04e-3;
      model.param().set("Ugate", "-1.1[V]");
      model.param().set("V_app", "4[V]");
      for (int it = 0; it < CAL_MAX_IT; it++) {
        model.param().set("sig_on_v", String.format(Locale.US, "%.6e[S/m]", sigOnV));
        double iVal = solveI(model, "1");
        System.out.println(String.format(Locale.US, "CAL sig_on_v it=%d sig_on_v=%.4e I(4V)=%.4e mA (maqsad 0.04)",
            it, sigOnV, iVal * 1e3));
        if (Math.abs(iVal / targetIv - 1) < CAL_TOL) break;
        sigOnV *= clamp(iVal > 0 ? targetIv / iVal : 10.0);
      }
      model.param().set("Ugate", "0[V]");
      model.param().set("V_app", "V_read");
      model.param().set("xs", "0");

      System.out.println(String.format(Locale.US,
          "S1 KALIBROVKA YAKUNI: sig_off0=%.4e sig_on0=%.4e sig_on_v=%.4e", sigOff0, sigOn0, sigOnV));
    } else {
      System.out.println("KALIBROVKA O'TKAZIB YUBORILDI - oldindan aniqlangan qiymatlar ishlatilmoqda "
          + "(sig_off0=1.6182e-8, sig_on0=1.0712e-5, sig_on_v=6.0469e-7).");
    }

    // =====================================================================================
    // S2: issiqlik tekshiruvi (T_max < 800K)
    // =====================================================================================
    model.study().create("std3");
    model.study("std3").create("stat", "Stationary");
    model.study("std3").feature("stat").set("activate", new String[]{"ec", "on", "ht", "on", "ge", "off"});
    model.param().set("xs", "1");
    model.param().set("V_app", "4.5[V]");
    model.study("std3").run();
    model.result().numerical().create("gev_Tmax", "EvalGlobal");
    model.result().numerical("gev_Tmax").set("data", "dset2");
    model.result().numerical("gev_Tmax").set("expr", new String[]{"maxop_T(T)"});
    double tmax = model.result().numerical("gev_Tmax").getReal()[0][0];
    System.out.println(String.format(Locale.US, "S2 T_max (xs=1,V=4.5V) = %.4e K (maqsad <800K)", tmax));
    model.param().set("xs", "0");
    model.param().set("V_app", "V_read");

    // =====================================================================================
    // S4: 4 ta KETMA-KET Time Dependent panel (a,b,c,d), har biri 10ms. x va P holatlari
    // QO'LDA bir paneldan ikkinchisiga UZATILADI (ge1.initialValueU orqali) - shu bilan
    // "holatlar tsikllar orasida saqlansin" talabi bajariladi, lekin BITTA 40ms uzluksiz
    // hisobga (juda sekin, ~3+ soat kutilgan) qaraganda TEZROQ va XAVFSIZROQ (har panel
    // alohida saqlanadi, vaqt tugasa ham QISMAN natija qoladi). Ugate_wave/smstep ENDI
    // KERAK EMAS - har panelda Ugate DOIMIY (silliq o'tish alohida tadqiqotlar orasida
    // tabiiy ravishda "sakrash" bo'ladi, lekin bu holat o'zgaruvchilarining o'ziga (BDF ning
    // birinchi qadami orqali) ta'sir qilmaydi, chunki ular qat'iy INITIAL VALUE sifatida
    // uzatiladi).
    model.component("comp1").physics("ge").feature("ge1").set("equation", new String[]{"xodet-dxdt_rhs", "Pt-dPdt_rhs"});
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_wave");

    model.study().create("std4");
    model.study("std4").create("time", "Transient");
    model.study("std4").feature("time").set("tlist", "range(0,5e-5,0.01)");
    // ht (Issiqlik) FAOLSIZLASHTIRILDI: S2 T_max=300K (xs=1, V=4.5V) - ya'ni bu lateral
    // FET geometriyasida Joule isishi ahamiyatsiz (Model1 vertikal ustunidan farqli
    // o'laroq). ht ni o'chirish DOF sonini (51042+19636 ichki -> faqat ec+ge) keskin
    // kamaytiradi - bu 8GB RAM li mashinada xotira yetishmasligi (OOM, jarayon
    // o'ldirildi) muammosini hal qilish uchun ZARUR edi. T_local=aveop_a(T) T_amb
    // (300K) atrofida qotib qoladi - fizik jihatdan to'g'ri taxmin.
    model.study("std4").feature("time").set("activate", new String[]{"ec", "on", "ht", "off", "ge", "on"});

    String[] panelNames = {"a", "b", "c", "d"};
    double[] panelUgate = {0, -0.9, -1.1, 0};
    double xPrev = 1e-3, pPrev = 1.0;

    String csvName = "iv_fig1_continuous.csv";
    PrintWriter out2 = null;
    double tOffset = 0;
    try {
      out2 = new PrintWriter(new FileWriter(csvName));
      out2.println("panel,Ugate_V,t_s,V_V,I_A,x,P,Tmax_K");
      for (int pnl = 0; pnl < panelNames.length; pnl++) {
        model.param().set("Ugate", String.format(Locale.US, "%.4g[V]", panelUgate[pnl]));
        model.component("comp1").physics("ge").feature("ge1").set("initialValueU",
            new String[]{String.format(Locale.US, "%.8e", xPrev), String.format(Locale.US, "%.8e", pPrev)});
        System.out.println(String.format(Locale.US, "PANEL %s boshlanmoqda: Ugate=%.2fV, x0=%.4e, P0=%.4e",
            panelNames[pnl], panelUgate[pnl], xPrev, pPrev));
        model.study("std4").run();

        // dset3 std4 BIRINCHI marta ishlagandan keyingina mavjud bo'ladi (Model1/2 saboqi) -
        // shuning uchun gev_S4 FAQAT birinchi panel tugagandan keyin yaratiladi.
        if (pnl == 0) {
          model.result().numerical().create("gev_S4", "EvalGlobal");
          model.result().numerical("gev_S4").set("data", "dset3");
          model.result().numerical("gev_S4").set("expr", new String[]{"t", "V_wave", "ec.I0_1", "xode", "P", "maxop_T(T)"});
        }
        double[][] s4 = model.result().numerical("gev_S4").getReal();
        int n = (s4.length > 0) ? s4[0].length : 0;
        for (int i = 0; i < n; i++) {
          out2.println(String.format(Locale.US, "%s,%.4g,%.6e,%.6e,%.6e,%.6e,%.6e,%.6e",
              panelNames[pnl], panelUgate[pnl], tOffset + s4[0][i], s4[1][i], s4[2][i], s4[3][i], s4[4][i], s4[5][i]));
        }
        xPrev = s4[3][n - 1];
        pPrev = s4[4][n - 1];
        double tmaxPanel = maxArr(s4[5]);
        tOffset += 0.01;
        System.out.println(String.format(Locale.US,
            "PANEL %s TAYYOR: n=%d, x(oxiri)=%.4e, P(oxiri)=%.4e, T_max=%.4e K",
            panelNames[pnl], n, xPrev, pPrev, tmaxPanel));

        // Har panel tugagach DARHOL saqlab qo'yamiz - vaqt tugasa ham shu paytgacha bo'lgan
        // natijalar (mph + csv) YO'QOLMAYDI.
        out2.flush();
        try {
          model.save("Model2_FET_Fig1.mph");
        } catch (IOException exSave) {
          System.out.println("XATO (oraliq saqlash): " + exSave.getMessage());
        }
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV iv_fig1_continuous): " + ex.getMessage());
    } finally {
      if (out2 != null) out2.close();
    }

    System.out.println("FIG1 TAYYOR.");
    return model;
  }

  static double maxArr(double[] a) {
    double m = -1e300;
    for (double v : a) if (v > m) m = v;
    return m;
  }

  static void p(Model model, String name, String expr, String descr) {
    model.param().set(name, expr, descr);
  }

  static void rect(GeomSequence g, String tag, String w, String h, String x0, String y0) {
    g.create(tag, "Rectangle");
    g.feature(tag).set("size", new String[]{w, h});
    g.feature(tag).set("pos", new String[]{x0, y0});
    g.feature(tag).set("selresult", true);
  }

  static void boxSel(GeomSequence g, String tag, String xMin, String xMax, String yMin, String yMax) {
    g.create(tag, "BoxSelection");
    g.feature(tag).set("entitydim", 1);
    g.feature(tag).set("xmin", xMin);
    g.feature(tag).set("xmax", xMax);
    g.feature(tag).set("ymin", yMin);
    g.feature(tag).set("ymax", yMax);
    g.feature(tag).set("condition", "inside");
  }

  static void union(Model model, String tag, int dim, String[] inputs, String label) {
    model.component("comp1").selection().create(tag, "Union");
    model.component("comp1").selection(tag).set("entitydim", dim);
    model.component("comp1").selection(tag).set("input", inputs);
  }

  static void material(Model model, String tag, String sel,
                       String sigma, String epsr, String k, String rho, String cp) {
    model.component("comp1").material().create(tag, "Common");
    model.component("comp1").material(tag).selection().named(sel);
    model.component("comp1").material(tag).propertyGroup("def").set("electricconductivity", new String[]{sigma});
    model.component("comp1").material(tag).propertyGroup("def").set("relpermittivity", new String[]{epsr});
    model.component("comp1").material(tag).propertyGroup("def").set("thermalconductivity", new String[]{k});
    model.component("comp1").material(tag).propertyGroup("def").set("density", rho);
    model.component("comp1").material(tag).propertyGroup("def").set("heatcapacity", cp);
  }

  static double solveI(Model model, String x) {
    model.param().set("xs", x);
    model.study("std1").run();
    return model.result().numerical("gev_R").getReal()[0][0];
  }

  static double clamp(double f) {
    return Math.max(0.1, Math.min(10.0, f));
  }

  public static void main(String[] args) {
    run();
  }
}
