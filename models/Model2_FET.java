/*
 * Model2_FET.java  —  2-MODEL: lateral "FET" (zatvor bilan boshqariladigan xotira/volatil element)
 *
 * Troyan & Doronin, ICCS 2020 (LNNS 186, 427-433, 2021, doi:10.1007/978-3-030-66093-2_41)
 * maqolasidagi Fig. 1a-d effektining (zatvor kuchlanishi orqali xotira <-> volatil rejim almashinuvi)
 * FENOMENOLOGIK modeli. Model1_Vertical.java dagi ishlaydigan API chaqiruvlari (COMSOL 6.0, TASDIQLANDI
 * belgili joylar) qayta ishlatilgan, mustaqil fayl sifatida.
 *
 * GEOMETRIYA (2D Kartezian, LATERAL kesim, substrat modellanmaydi - pastki chegara T=T_amb bilan
 * "issiqlik cho'kmasi" sifatida ifodalanadi):
 *   Sb2Te3 kanali (t_ST qalinlik, y=0..t_ST) source (chap)dan drain (o'ng)gacha lateral cho'zilgan.
 *   Kanal ichida, zatvor ostida, uzunligi L_gap bo'lgan "FAOL SOHA" bor - bu soha interfeys materiali
 *   bilan to'ldirilgan (sigma = sig_off^(1-x)*sig_on(Ug)^x), Model1 dagi filament/halqa mantig'iga
 *   o'xshab, lekin LATERAL yo'nalishda (radial emas). FARAZ (aniq PARAMETRLAR2.md da yozilgan): bu
 *   real qurilmaning L_gap uzunlikdagi "zaif bo'g'in" (nuqson/vdW cho'kish nohomogenligi) joylashgan
 *   qismini ifodalaydi - maqolada aniq geometriya berilmagan.
 *   Kanal ustida: GeTe (t_GT), Al2O3 zatvor dielektrigi (t_ox), zatvor elektrodi (t_gate) - FAQAT
 *   GEOMETRIK/ISSIQLIK maqsadida (quyidagi "ZATVOR TA'SIRI" ga qarang - elektrostatik yechilmaydi).
 *
 * ELEKTR: faqat Sb2Te3 qatlami (3 domen: chap/faol/o'ng) o'tkazuvchi hisoblanadi (ec fizikasi shu
 * domenlarda). GeTe/Al2O3/zatvor ec dan tashqarida (izolyator/faol emas), lekin Heat Transfer da bor
 * (issiqlik tarqalishi uchun).
 *
 * ZATVOR TA'SIRI (FAQAT FENOMENOLOGIK, elektrostatik yechilmaydi - topshiriqda aniq ko'rsatilgan):
 *   f(Ug) = 1/(1+exp(-(Ug-U0)/w)),  U0=-0.9 V, w=0.08 V
 *   tau_rel(Ug) = tau_v*(tau_nv/tau_v)^f(Ug)   (Ug=0 -> tau_nv=1e3s, nonvolatil; Ug<<U0 -> tau_v=1e-4s, volatil)
 *   sig_on(Ug)  = sig_on_v*(sig_on0/sig_on_v)^f(Ug)  (Ug=0 -> sig_on0 (R_ON=7kOhm kalibrlangan);
 *                                                       Ug<<U0 -> sig_on_v (I(4V)~0.04mA ga kalibrlanadi))
 *   Ugate - oddiy PARAMETR (Java/COMSOL param), maydon sifatida yechilmaydi - shuning uchun
 *   GeTe/Al2O3/zatvor domenlari ec dan mustasno (ular faqat issiqlik tarqalishi uchun saqlanadi).
 *
 * "// TEKSHIRILSIN" belgisi: Model1 da TASDIQLANGAN chaqiruvlar takror ishlatilgan (endi tekshirish
 * shart emas); FAQAT Model2 ga xos yangi joylar (masalan ec.Ex, aveop_active) TEKSHIRILSIN qilib
 * belgilangan.
 *
 * Ishga tushirish (Windows, fayl joylashgan papkada):
 *   "C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64\comsolcompile.exe" Model2_FET.java
 *   "C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64\comsolbatch.exe" -inputfile Model2_FET.class -outputfile Model2_FET.mph
 */

import com.comsol.model.*;
import com.comsol.model.util.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class Model2_FET {

  static final boolean CALIBRATE = true;
  static final int CAL_MAX_IT = 14;   // sig_on_v kalibrovkasi 8 iteratsiyada yaqinlashmadi, oshirildi
  static final double CAL_TOL = 0.02;   // 2% (FET kalibrovkasi uchun biroz bo'shroq)

  public static Model run() {
    Model model = ModelUtil.create("Model2");
    model.label("Model2_FET.mph");
    model.comments("Troyan & Doronin (2021) Fig.1a-d effektining (zatvor bilan xotira<->volatil "
        + "almashinuv) fenomenologik lateral FET modeli.");

    // =====================================================================================
    // PARAMETERS
    // =====================================================================================
    // --- Geometriya ---
    p(model, "W", "1000[nm]", "Kanal umumiy uzunligi (source-drain)");
    // MUHIM TUZATISH (birinchi ishga tushirishdan keyin): boshlang'ich L_gap=200nm bilan hech
    // qanday Ugate qiymatida SET/RESET sodir bo'lmadi - sabab, E_drive = V/L_gap ~ 100x kichikroq
    // Model1 dagi t_int=1.5nm bilan solishtirganda, demak sinh(qaE/2kBT) argumenti ~75x kichikroq,
    // sinh esa EKSPONENSIAL sezgir bo'lgani uchun dxdt_rhs amalda NOLGA teng bo'lib qoldi (haqiqiy
    // vaqt shkalasida svitching sodir bo'lmadi). Model1 da ISHLAGAN t_int=1.5nm bilan bir xil
    // tartibdagi uzunlik ishlatildi.
    // IKKINCHI TUZATISH: L_gap=3nm bilan ham SET sodir bo'lmadi (diagnostika: x faqat ~0.013 ga
    // yetdi, T_local esa umuman o'zgarmadi - demak issiqlik emas, balki SOF MAYDON yetarli emas).
    // Sabab: L_gap=3nm Model1 dagi t_int=1.5nm dan 2x katta -> E_drive 2x kichik -> sinh argumenti
    // YARMIGA (13.2 -> 6.6) -> sinh ~750x KICHIKROQ (eksponensial sezgirlik). L_gap ANIQ t_int
    // qiymatiga (1.5nm) tenglashtirildi - Model1 bilan to'g'ridan-to'g'ri solishtirish uchun.
    p(model, "L_gap", "1.5[nm]", "Faol soha (interfeys) uzunligi, zatvor ostida - FARAZ (Model1 t_int bilan AYNAN bir xil)");
    p(model, "t_ST", "20[nm]", "Sb2Te3 qalinligi");
    p(model, "t_GT", "20[nm]", "GeTe qalinligi");
    p(model, "t_ox", "10[nm]", "Al2O3 zatvor dielektrigi qalinligi");
    p(model, "t_gate", "10[nm]", "Zatvor elektrodi qalinligi (faraz)");
    p(model, "x_gap0", "(W-L_gap)/2", "Faol soha chap chegarasi");
    p(model, "x_gap1", "(W+L_gap)/2", "Faol soha o'ng chegarasi");
    p(model, "y_GT", "t_ST", "GeTe pastki chegarasi");
    p(model, "y_ox", "t_ST+t_GT", "Al2O3 pastki chegarasi");
    p(model, "y_gate", "t_ST+t_GT+t_ox", "Zatvor pastki chegarasi");
    p(model, "y_top", "t_ST+t_GT+t_ox+t_gate", "Strukturaning yuqori chegarasi");

    // --- Elektr (Sb2Te3 kanali va faol soha) ---
    p(model, "sig_ST", "1e5[S/m]", "Sb2Te3 o'tkazuvchanligi (Model1 bilan bir xil taxmin)");
    p(model, "sig_GT", "1e5[S/m]", "GeTe o'tkazuvchanligi (faqat issiqlik uchun, ec dan tashqarida)");
    p(model, "sig_off", "1[S/m]", "Faol soha OFF o'tkazuvchanligi (fitting: R_OFF, Ugate=0 boshlang'ich taxmin)");
    p(model, "sig_on0", "1e3[S/m]", "Faol soha ON o'tkazuvchanligi Ugate=0 da (fitting: R_ON)");
    p(model, "sig_on_v", "1e3[S/m]", "Faol soha ON o'tkazuvchanligi volatil rejimda (Ugate<<U0, fitting: I(4V)~0.04mA)");
    p(model, "epsr_ST", "50", "Sb2Te3 epsilon_r (stationar EC ga ta'sir qilmaydi)");
    p(model, "epsr_GT", "30", "GeTe epsilon_r");
    p(model, "epsr_int", "10", "Faol soha epsilon_r");
    p(model, "epsr_ox", "9", "Al2O3 epsilon_r (faqat formal, ec dan tashqarida)");
    p(model, "sig_gate", "5e6[S/m]", "Zatvor elektrodi o'tkazuvchanligi (faqat issiqlik uchun, faraz TiN)");
    p(model, "sig_ox", "1e-12[S/m]", "Al2O3 o'tkazuvchanligi (deyarli izolyator, faqat formal)");

    // --- Issiqlik ---
    p(model, "k_ST", "1.0[W/(m*K)]", "Sb2Te3 issiqlik o'tkazuvchanligi");
    p(model, "k_GT", "2.0[W/(m*K)]", "GeTe issiqlik o'tkazuvchanligi");
    p(model, "k_int", "0.5[W/(m*K)]", "Faol soha issiqlik o'tkazuvchanligi (vdW, taxmin)");
    p(model, "k_ox", "30[W/(m*K)]", "Al2O3 issiqlik o'tkazuvchanligi");
    p(model, "k_gate", "20[W/(m*K)]", "Zatvor elektrodi issiqlik o'tkazuvchanligi (TiN, taxmin)");
    p(model, "rho_ST", "6500[kg/m^3]", "Sb2Te3 zichligi");
    p(model, "rho_GT", "6140[kg/m^3]", "GeTe zichligi");
    p(model, "rho_int", "6300[kg/m^3]", "Faol soha zichligi (faraz)");
    p(model, "rho_ox", "3950[kg/m^3]", "Al2O3 zichligi");
    p(model, "rho_gate", "5220[kg/m^3]", "Zatvor (TiN) zichligi");
    p(model, "Cp_ST", "200[J/(kg*K)]", "Sb2Te3 Cp");
    p(model, "Cp_GT", "250[J/(kg*K)]", "GeTe Cp");
    p(model, "Cp_int", "220[J/(kg*K)]", "Faol soha Cp (faraz)");
    p(model, "Cp_ox", "880[J/(kg*K)]", "Al2O3 Cp");
    p(model, "Cp_gate", "600[J/(kg*K)]", "Zatvor (TiN) Cp");
    p(model, "Tm_GT", "998[K]", "GeTe erish harorati");
    p(model, "Tm_ST", "891[K]", "Sb2Te3 erish harorati");
    p(model, "T_amb", "300[K]", "Atrof (substrat) harorati");

    // --- Filament kinetikasi ---
    p(model, "xs", "0", "Faol soha holati (Stationary kalibrovka/tekshiruv uchun statik parametr)");
    p(model, "k0", "1e13[1/s]", "Urinish chastotasi");
    p(model, "Ea", "0.9[eV]", "Migratsiya aktivatsiya energiyasi (Model1 bilan bir xil boshlang'ich)");
    p(model, "a_hop", "0.3[nm]", "Sakrash masofasi (Model1 bilan bir xil boshlang'ich)");
    p(model, "tau_nv", "1e3[s]", "Relaksatsiya vaqti, NONVOLATIL chegara (Ugate=0)");
    p(model, "tau_v", "1e-4[s]", "Relaksatsiya vaqti, VOLATIL chegara (Ugate<<U0)");
    p(model, "p_win", "2", "Oyna funksiyasi darajasi (Biolek, Model1 bilan bir xil)");
    p(model, "kB_c", "1.380649e-23[J/K]", "Boltsman doimiysi");
    p(model, "q_c", "1.602176634e-19[C]", "Elektron zaryadi");
    p(model, "E_s", "1e7[V/m]", "Silliq Biolek yo'nalish funksiyasi uchun maydon miqyosi (tanh)");

    // --- Zatvor (fenomenologik) ---
    p(model, "Ugate", "0[V]", "Zatvor kuchlanishi (parametr, elektrostatik yechilmaydi)");
    p(model, "U0", "-0.9[V]", "Sigmoid markazi (aralash rejim)");
    p(model, "w_sig", "0.08[V]", "Sigmoid kengligi");

    // --- Signal ---
    p(model, "V_read", "0.1[V]", "Stationar tekshiruv uchun o'qish kuchlanishi");
    p(model, "V_app", "V_read", "Drain kuchlanishi (Stationary uchun)");
    p(model, "Vamp", "4.5[V]", "Uchburchak signal amplitudasi");
    p(model, "f0", "100[Hz]", "Signal chastotasi");

    // --- Fitting maqsadlari ---
    p(model, "R_ON_t", "7[kohm]", "Maqsad R_ON (Ugate=0)");
    p(model, "R_OFF_t", "100[kohm]", "Maqsad R_OFF (Ugate=0)");
    p(model, "I_volatile_t", "0.04[mA]", "Maqsad tok, Ugate=-1.1V, V=4V da");

    // --- To'r ---
    p(model, "h_gap", "0.15[nm]", "Faol soha va uning atrofida maksimal element (L_gap=1.5nm bilan mos)");
    p(model, "h_glob", "5[nm]", "Qolgan joyda maksimal element");

    // =====================================================================================
    // GEOMETRY (2D Kartezian, LATERAL)
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

    boxSel(g, "box_src", "-1", "0.01[nm]", "-1", "t_ST+0.01[nm]");           // Source (chap, Sb2Te3)
    boxSel(g, "box_drn", "W-0.01[nm]", "W+1[nm]", "-1", "t_ST+0.01[nm]");    // Drain (o'ng, Sb2Te3)
    boxSel(g, "box_bot", "-1", "W+1[nm]", "-0.01", "0.01");                  // Substrat (T=T_amb)
    boxSel(g, "box_top", "-1", "W+1[nm]", "y_top-0.01[nm]", "y_top+0.01[nm]"); // Zatvor usti (T=T_amb)
    g.run();

    union(model, "sel_ec", 2, new String[]{"geom1_r_st_l_dom", "geom1_r_st_a_dom", "geom1_r_st_r_dom"},
        "Elektr o'tkazuvchi domen (Sb2Te3, faol soha bilan)");

    // =====================================================================================
    // DEFINITIONS
    // =====================================================================================
    model.component("comp1").variable().create("var1");
    model.component("comp1").variable("var1").label("Faol soha o'tkazuvchanligi va zatvor kinetikasi");

    // Zatvor sigmoidi va undan bog'liq kattaliklar (TASDIQLANDI naqshlar, Model1 dan).
    model.component("comp1").variable("var1").set("f_gate", "1/(1+exp(-(Ugate-U0)/w_sig))",
        "Zatvor bosqichma-bosqich bostirish funksiyasi: Ugate=0 -> ~1, Ugate<<U0 -> ~0");
    model.component("comp1").variable("var1").set("sig_on_eff", "sig_on_v*(sig_on0/sig_on_v)^f_gate",
        "Zatvorga bog'liq ON o'tkazuvchanlik");
    model.component("comp1").variable("var1").set("tau_rel_eff", "tau_v*(tau_nv/tau_v)^f_gate",
        "Zatvorga bog'liq relaksatsiya vaqti");
    model.component("comp1").variable("var1").set("sig_fil", "sig_off*(sig_on_eff/sig_off)^xode",
        "Faol soha o'tkazuvchanligi: sig_off^(1-x)*sig_on_eff(Ugate)^x");

    model.component("comp1").cpl().create("aveop_a", "Average");
    model.component("comp1").cpl("aveop_a").selection().named("geom1_r_st_a_dom");
    // TEKSHIRILSIN: 2D Kartezian geometriyada "axisym" xossasi yo'q/kerak emas (Model1 o'q-simmetrik
    // edi); shuning uchun bu yerda o'rnatilmaydi.
    model.component("comp1").cpl().create("maxop_T", "Maximum");
    model.component("comp1").cpl("maxop_T").selection().geom("geom1", 2);
    model.component("comp1").cpl("maxop_T").selection().all();

    // LATERAL qurilma: haydovchi maydon "ec.Ex" (Model1 da "ec.Ez" edi, chunki u vertikal edi).
    model.component("comp1").variable("var1").set("E_drive", "-aveop_a(ec.Ex)",   // TEKSHIRILSIN: ec.Ex
        "Faol sohadagi haydovchi maydon (drain musbat -> oqim o'ngga -> SET yo'nalishi)");
    model.component("comp1").variable("var1").set("T_local", "aveop_a(T)",
        "Faol sohaning o'rtacha harorati");
    model.component("comp1").variable("var1").set("dir_smooth", "0.5*(1+tanh(E_drive/E_s))",
        "Silliq yo'nalish funksiyasi (Model1 dagi Biolek+tanh naqshi)");
    model.component("comp1").variable("var1").set("Fwin_x",
        "dir_smooth*(1-xode^(2*p_win))+(1-dir_smooth)*(1-(xode-1)^(2*p_win))",
        "Silliqlashtirilgan Biolek oynasi");
    model.component("comp1").variable("var1").set("dxdt_rhs",
        "k0*exp(-Ea/(kB_c*T_local))*sinh(q_c*a_hop*E_drive/(2*kB_c*T_local))*Fwin_x-xode/tau_rel_eff",
        "dx/dt ifodasi (zatvorga bog'liq tau_rel_eff bilan)");
    model.component("comp1").variable("var1").set("V_wave", "Vamp*(2/pi)*asin(sin(2*pi*f0*t))",
        "Uchburchak surish signali (Time Dependent uchun)");

    // =====================================================================================
    // MATERIALS
    // =====================================================================================
    material(model, "mat_st_l", "Sb2Te3 (chap)", "geom1_r_st_l_dom", "sig_ST", "epsr_ST", "k_ST", "rho_ST", "Cp_ST");
    material(model, "mat_st_r", "Sb2Te3 (o'ng)", "geom1_r_st_r_dom", "sig_ST", "epsr_ST", "k_ST", "rho_ST", "Cp_ST");
    material(model, "mat_fil", "Faol soha (sig_fil(x))", "geom1_r_st_a_dom", "sig_fil", "epsr_int", "k_int", "rho_int", "Cp_int");
    material(model, "mat_gt", "GeTe", "geom1_r_gt_dom", "sig_GT", "epsr_GT", "k_GT", "rho_GT", "Cp_GT");
    material(model, "mat_ox", "Al2O3", "geom1_r_ox_dom", "sig_ox", "epsr_ox", "k_ox", "rho_ox", "Cp_ox");
    material(model, "mat_gate", "Zatvor (TiN, faraz)", "geom1_r_gate_dom", "sig_gate", "1", "k_gate", "rho_gate", "Cp_gate");

    // =====================================================================================
    // PHYSICS: Electric Currents (faqat Sb2Te3+faol soha)
    // =====================================================================================
    model.component("comp1").physics().create("ec", "ConductiveMedia", "geom1");
    model.component("comp1").physics("ec").selection().named("sel_ec");

    model.component("comp1").physics("ec").create("term1", "Terminal", 1);
    model.component("comp1").physics("ec").feature("term1").label("Drain");
    model.component("comp1").physics("ec").feature("term1").selection().named("geom1_box_drn");
    model.component("comp1").physics("ec").feature("term1").set("TerminalType", "Voltage");
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_app");

    model.component("comp1").physics("ec").create("gnd1", "Ground", 1);
    model.component("comp1").physics("ec").feature("gnd1").label("Source");
    model.component("comp1").physics("ec").feature("gnd1").selection().named("geom1_box_src");

    // =====================================================================================
    // PHYSICS: Heat Transfer (barcha domenlar) + Electromagnetic Heating
    // =====================================================================================
    model.component("comp1").physics().create("ht", "HeatTransfer", "geom1");
    model.component("comp1").physics("ht").create("temp1", "TemperatureBoundary", 1);
    model.component("comp1").physics("ht").feature("temp1").label("T=T_amb (substrat+zatvor usti)");
    union(model, "sel_Tbc", 1, new String[]{"geom1_box_bot", "geom1_box_top"}, "T=T_amb chegaralari");
    model.component("comp1").physics("ht").feature("temp1").selection().named("sel_Tbc");
    model.component("comp1").physics("ht").feature("temp1").set("T0", "T_amb");

    model.component("comp1").multiphysics().create("emh1", "ElectromagneticHeating", 2);
    // TUZATILDI: "sel_ec" ga cheklash T_local ni doim ANIQ 300.000K qilib qo'ydi (issiqlik
    // manbai ishlamadi) - Model1 dagi TASDIQLANGAN ".all()" naqshiga qaytarildi (ec faol bo'lmagan
    // domenlarda J=0, demak issiqlik manbai baribir 0 bo'ladi - xavfsiz va sodda).
    model.component("comp1").multiphysics("emh1").selection().all();

    // =====================================================================================
    // PHYSICS: Global ODE (x kinetikasi)
    // =====================================================================================
    model.component("comp1").physics().create("ge", "GlobalEquations");
    model.component("comp1").physics("ge").feature("ge1").label("Faol soha holati x");
    model.component("comp1").physics("ge").feature("ge1").set("name", new String[]{"xode"});
    model.component("comp1").physics("ge").feature("ge1").set("equation", new String[]{"dxdt_rhs"});
    model.component("comp1").physics("ge").feature("ge1").set("initialValueU", new String[]{"xs"});
    model.component("comp1").physics("ge").feature("ge1").set("initialValueUt", new String[]{"0"});

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
    // STUDY: S1 kalibrovka (Ugate=0, xs=0/1) - Model1 dagi bir xil naqsh
    // =====================================================================================
    model.study().create("std1");
    model.study("std1").label("S1a: Stationary (kalibrovka, bitta holat)");
    model.study("std1").create("stat", "Stationary");
    model.study("std1").feature("stat").set("activate", new String[]{"ec", "on", "ht", "off", "ge", "off"});

    // Eslatma: Model1 dagi std2 (aux sweep, S1_R_on_off.csv uchun) bu yerda ISHLATILMAYDI - Model2
    // uchun alohida R_on_off jadvali kerak emas (FET_S1_calibration.csv yetarli). Ishlatilmaydigan
    // studyni yaratmaslik dset/sol raqamlanishida chalkashlikning oldini oladi (Model1 dagi
    // dset6->dset5 saboqiga qarang).

    model.study("std1").run();

    model.result().numerical().create("gev_R", "EvalGlobal");
    model.result().numerical("gev_R").set("data", "dset1");
    model.result().numerical("gev_R").set("expr", new String[]{"V_app/ec.I0_1"});

    double rOffT = 100e3, rOnT = 7e3;
    double sigOff = 1.0, sigOn0 = 1e3;
    String calCsv = "FET_S1_calibration.csv";
    PrintWriter out = null;
    try {
      out = new PrintWriter(new FileWriter(calCsv));
      out.println("step,state,sig_off_S_per_m,sig_on0_S_per_m,R_ohm,target_ohm");
      if (CALIBRATE) {
        model.param().set("Ugate", "0[V]");
        for (int it = 0; it < CAL_MAX_IT; it++) {
          double r = solveR(model, "0");
          out.println(it + ",OFF," + e(sigOff) + "," + e(sigOn0) + "," + e(r) + "," + e(rOffT));
          System.out.println(String.format(Locale.US, "FET CAL OFF it=%d sig_off=%.4g S/m R_OFF=%.4g ohm", it, sigOff, r));
          if (Math.abs(r / rOffT - 1) < CAL_TOL) break;
          sigOff *= clamp(r / rOffT);
          model.param().set("sig_off", String.format(Locale.US, "%.4g[S/m]", sigOff));
        }
        for (int it = 0; it < CAL_MAX_IT; it++) {
          double r = solveR(model, "1");
          out.println(it + ",ON," + e(sigOff) + "," + e(sigOn0) + "," + e(r) + "," + e(rOnT));
          System.out.println(String.format(Locale.US, "FET CAL ON  it=%d sig_on0=%.4g S/m R_ON=%.4g ohm", it, sigOn0, r));
          if (Math.abs(r / rOnT - 1) < CAL_TOL) break;
          sigOn0 *= clamp(r / rOnT);
          model.param().set("sig_on0", String.format(Locale.US, "%.4g[S/m]", sigOn0));
        }
      }
      double rOff = solveR(model, "0");
      double rOn = solveR(model, "1");
      out.println("final,OFF," + e(sigOff) + "," + e(sigOn0) + "," + e(rOff) + "," + e(rOffT));
      out.println("final,ON," + e(sigOff) + "," + e(sigOn0) + "," + e(rOn) + "," + e(rOnT));
      System.out.println(String.format(Locale.US,
          "FET S1 NATIJA: sig_off=%.4g, sig_on0=%.4g -> R_OFF=%.4g ohm, R_ON=%.4g ohm",
          sigOff, sigOn0, rOff, rOn));
    } catch (IOException ex) {
      System.out.println("XATO (CSV FET_S1_calibration): " + ex.getMessage());
    } finally {
      if (out != null) out.close();
    }

    // ---- Volatil rejim (Ugate=-1.1V) uchun sig_on_v kalibrlash: I(4V) ~ 0.04 mA ----
    // FARAZ: kalibrovka x=1 (to'liq ON) holatida, drain=4V, source=0V statik yechim bilan.
    double sigOnV = 1e3;
    double targetI = 0.04e-3;   // A
    model.param().set("Ugate", "-1.1[V]");
    model.param().set("V_app", "4[V]");
    for (int it = 0; it < CAL_MAX_IT; it++) {
      model.param().set("sig_on_v", String.format(Locale.US, "%.4g[S/m]", sigOnV));
      model.param().set("xs", "1");
      model.study("std1").run();
      double iCur = model.result().numerical("gev_R").getReal()[0][0];   // gev_R = V/I -> R; tokni alohida olamiz
      // I ni to'g'ridan olish uchun alohida eval:
      double iVal = evalCurrent(model);
      System.out.println(String.format(Locale.US, "FET CAL sig_on_v it=%d sig_on_v=%.4g S/m I(4V)=%.4g mA (maqsad 0.04 mA)",
          it, sigOnV, iVal * 1e3));
      if (Math.abs(iVal / targetI - 1) < CAL_TOL) break;
      sigOnV *= clamp(iVal > 0 ? targetI / iVal : 10.0);
    }
    model.param().set("sig_on_v", String.format(Locale.US, "%.4g[S/m]", sigOnV));
    model.param().set("V_app", "V_read");
    model.param().set("Ugate", "0[V]");
    model.param().set("xs", "0");

    // =====================================================================================
    // S2 (issiqlik tekshiruvi): xs=0/1, Ugate=0, V_app=+-3.5,4.5V -> T_max
    // =====================================================================================
    model.study().create("std3");
    model.study("std3").label("S2: Stationary (EC+HT)");
    model.study("std3").create("stat", "Stationary");
    model.study("std3").feature("stat").set("activate", new String[]{"ec", "on", "ht", "on", "ge", "off"});

    model.param().set("xs", "0");
    model.param().set("V_app", "3.5[V]");
    model.study("std3").run();
    model.result().numerical().create("gev_Tmax", "EvalGlobal");
    model.result().numerical("gev_Tmax").set("data", "dset2");
    model.result().numerical("gev_Tmax").set("expr", new String[]{"maxop_T(T)"});
    double[] xsVals2 = {0, 1, 1};
    double[] vVals2 = {3.5, 3.5, 4.5};
    String tmaxCsv2 = "FET_S2_Tmax.csv";
    PrintWriter outT2 = null;
    try {
      outT2 = new PrintWriter(new FileWriter(tmaxCsv2));
      outT2.println("xs,V_app_V,T_max_K");
      for (int i = 0; i < xsVals2.length; i++) {
        model.param().set("xs", String.format(Locale.US, "%.0f", xsVals2[i]));
        model.param().set("V_app", String.format(Locale.US, "%.4g[V]", vVals2[i]));
        model.study("std3").run();
        double tmax = model.result().numerical("gev_Tmax").getReal()[0][0];
        outT2.println(String.format(Locale.US, "%.0f,%.4g,%.6e", xsVals2[i], vVals2[i], tmax));
        System.out.println(String.format(Locale.US, "FET S2 T_max: xs=%.0f V_app=%.2f -> T_max=%.6e K",
            xsVals2[i], vVals2[i], tmax));
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV FET_S2_Tmax): " + ex.getMessage());
    } finally {
      if (outT2 != null) outT2.close();
      model.param().set("xs", "0");
      model.param().set("V_app", "V_read");
    }

    // =====================================================================================
    // S4: TIME DEPENDENT - Ugate sweep (har biri 1 tsikl, 0-10ms)
    // =====================================================================================
    model.component("comp1").physics("ge").feature("ge1").set("equation", new String[]{"xodet-dxdt_rhs"});
    model.component("comp1").physics("ge").feature("ge1").set("initialValueU", new String[]{"1e-3"});
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_wave");

    model.study().create("std4");
    model.study("std4").label("S4: Time Dependent (Ugate sweep, bitta davr)");
    model.study("std4").create("time", "Transient");
    model.study("std4").feature("time").set("tlist", "range(0,5e-5,0.01)");

    double[] ugateSweep = {0, -0.5, -0.9, -1.1, -1.5};
    String ivUgateCsv = "iv_ugate_sweep.csv";
    String fetN6Csv = "FET_N6_table.csv";
    PrintWriter outIvU = null, outN6f = null;
    try {
      outIvU = new PrintWriter(new FileWriter(ivUgateCsv));
      outIvU.println("Ugate_V,t_s,V_V,I_A,x,E_drive,T_local");
      outN6f = new PrintWriter(new FileWriter(fetN6Csv));
      outN6f.println("Ugate_V,V_SET_V,V_RESET_V,I_at_4V_mA,rejim");

      for (int k = 0; k < ugateSweep.length; k++) {
        double ug = ugateSweep[k];
        model.param().set("Ugate", String.format(Locale.US, "%.4g[V]", ug));
        model.study("std4").run();

        if (k == 0) {
          model.result().numerical().create("gev_S4", "EvalGlobal");
          model.result().numerical("gev_S4").set("data", "dset3");
          model.result().numerical("gev_S4").set("expr", new String[]{"t", "V_wave", "ec.I0_1", "xode", "E_drive", "T_local"});
        }
        double[][] s4 = model.result().numerical("gev_S4").getReal();
        int n = (s4.length > 0) ? s4[0].length : 0;

        double vSet = Double.NaN, vReset = Double.NaN, iAt4V = Double.NaN;
        for (int i = 1; i < n; i++) {
          double xPrev = s4[3][i - 1], xCur = s4[3][i];
          if (Double.isNaN(vSet) && xPrev < 0.5 && xCur >= 0.5) {
            double frac = (0.5 - xPrev) / (xCur - xPrev);
            vSet = s4[1][i - 1] + frac * (s4[1][i] - s4[1][i - 1]);
          }
          if (Double.isNaN(vReset) && xPrev > 0.5 && xCur <= 0.5) {
            double frac = (xPrev - 0.5) / (xPrev - xCur);
            vReset = s4[1][i - 1] + frac * (s4[1][i] - s4[1][i - 1]);
          }
          if (Double.isNaN(iAt4V) && s4[1][i - 1] < 4.0 && s4[1][i] >= 4.0) {
            double frac = (4.0 - s4[1][i - 1]) / (s4[1][i] - s4[1][i - 1]);
            iAt4V = s4[2][i - 1] + frac * (s4[2][i] - s4[2][i - 1]);
          }
        }
        for (int i = 0; i < n; i++) {
          outIvU.println(String.format(Locale.US, "%.4g,%.6e,%.6e,%.6e,%.6e,%.6e,%.6e", ug, s4[0][i], s4[1][i], s4[2][i], s4[3][i], s4[4][i], s4[5][i]));
        }
        String rejim = Double.isNaN(vReset) ? "VOLATIL (o'z-o'zidan qaytadi)" : "XOTIRA (gisterezis)";
        outN6f.println(String.format(Locale.US, "%.4g,%s,%s,%s,%s",
            ug,
            Double.isNaN(vSet) ? "NaN" : String.format(Locale.US, "%.4g", vSet),
            Double.isNaN(vReset) ? "NaN" : String.format(Locale.US, "%.4g", vReset),
            Double.isNaN(iAt4V) ? "NaN" : String.format(Locale.US, "%.4g", iAt4V * 1e3),
            rejim));
        System.out.println(String.format(Locale.US,
            "FET S4 SWEEP: Ugate=%.2fV V_SET=%s V_RESET=%s I(4V)=%s mA [%s]",
            ug, Double.isNaN(vSet) ? "?" : String.format(Locale.US, "%.4g", vSet),
            Double.isNaN(vReset) ? "TOPILMADI" : String.format(Locale.US, "%.4g", vReset),
            Double.isNaN(iAt4V) ? "?" : String.format(Locale.US, "%.4g", iAt4V * 1e3), rejim));

        // N2: har bir Ugate uchun alohida I-V PNG (Fig.1a-d ga o'xshab, 4 panel o'rniga
        // 5 ta alohida rasm - COMSOL Java API bitta rasmda ko'p panelni oson qo'llab-quvvatlamaydi).
        try {
          String tag = "pg_N2_" + k;
          model.result().create(tag, "PlotGroup1D");
          model.result(tag).label(String.format(Locale.US, "N2: I-V, Ugate=%.2fV", ug));
          model.result(tag).set("data", "dset3");
          model.result(tag).create("g1", "Global");
          model.result(tag).feature("g1").set("expr", new String[]{"ec.I0_1*1e3"});
          model.result(tag).feature("g1").set("xdata", "expr");
          model.result(tag).feature("g1").set("xdataexpr", "V_wave");
          model.result().export().create("exp_" + tag, "Image");
          model.result().export("exp_" + tag).set("plotgroup", tag);
          model.result().export("exp_" + tag).set("pngfilename", String.format(Locale.US, "C:/comsol_ish/models/N2_iv_ugate_%d.png", k));
          model.result().export("exp_" + tag).run();
        } catch (Exception ex) {
          System.out.println("XATO (N2 PNG, k=" + k + "): " + ex.getMessage());
        }
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV Ugate sweep): " + ex.getMessage());
    } finally {
      if (outIvU != null) outIvU.close();
      if (outN6f != null) outN6f.close();
    }

    // =====================================================================================
    // Fig.1d: Ugate(t) bosqichli 0 -> -1.1V -> 0V, har biri 1 tsikl (3 tsikl, jarayon qaytarligi)
    // =====================================================================================
    String fig1dCsv = "iv_fig1d_sequence.csv";
    PrintWriter outFig1d = null;
    double[] ugateSeq = {0, -1.1, 0};
    try {
      outFig1d = new PrintWriter(new FileWriter(fig1dCsv));
      outFig1d.println("step,Ugate_V,t_s,V_V,I_A,x");
      for (int k = 0; k < ugateSeq.length; k++) {
        model.param().set("Ugate", String.format(Locale.US, "%.4g[V]", ugateSeq[k]));
        model.study("std4").run();
        double[][] s4 = model.result().numerical("gev_S4").getReal();
        int n = (s4.length > 0) ? s4[0].length : 0;
        for (int i = 0; i < n; i++) {
          outFig1d.println(String.format(Locale.US, "%d,%.4g,%.6e,%.6e,%.6e,%.6e",
              k, ugateSeq[k], s4[0][i], s4[1][i], s4[2][i], s4[3][i]));
        }
        System.out.println(String.format(Locale.US, "FET Fig1d qadam %d: Ugate=%.2fV, x(oxiri)=%.4g",
            k, ugateSeq[k], s4[3][n - 1]));
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV fig1d): " + ex.getMessage());
    } finally {
      if (outFig1d != null) outFig1d.close();
      model.param().set("Ugate", "0[V]");
    }

    // N2 endi Ugate sweep siklining o'zida (har bir k uchun N2_iv_ugate_<k>.png) eksport qilindi.

    try {
      model.save("Model2_FET.mph");
    } catch (IOException ex) {
      throw new RuntimeException("MPH faylni saqlab bo'lmadi", ex);
    }
    System.out.println("FET TAYYOR.");
    return model;
  }

  // =====================================================================================
  // Yordamchi metodlar (Model1_Vertical.java bilan bir xil, mustaqil fayl uchun qayta yozilgan)
  // =====================================================================================

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

  static double solveR(Model model, String x) {
    model.param().set("xs", x);
    model.study("std1").run();
    return model.result().numerical("gev_R").getReal()[0][0];
  }

  static double evalCurrent(Model model) {
    model.result().numerical().create("gev_I_tmp", "EvalGlobal");
    model.result().numerical("gev_I_tmp").set("data", "dset1");
    model.result().numerical("gev_I_tmp").set("expr", new String[]{"ec.I0_1"});
    double val = model.result().numerical("gev_I_tmp").getReal()[0][0];
    model.result().numerical().remove("gev_I_tmp");
    return val;
  }

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
