/*
 * Model1_Vertical.java  —  1-MODEL, 1-2-3(a) BOSQICH ("tutun testi")
 *
 * Troyan & Doronin, ICCS 2020 (LNNS 186, 427-433, 2021, doi:10.1007/978-3-030-66093-2_41)
 * maqolasidagi GeTe/Sb2Te3 interfeysli xotira elementining FENOMENOLOGIK modeli.
 *
 * Struktura (2D o'q-simmetrik, pastdan yuqoriga, r = 0 simmetriya o'qi):
 *   pastki elektrod (BE) / Sb2Te3 / interfeys qatlami [filament r<r_f | halqa r>r_f] / GeTe / yuqori elektrod (TE)
 *
 * Bu bosqichda: GEOMETRY + MATERIALS + Electric Currents (ec) + S1 Stationary (x=0/1, r_f=200nm,
 * R_dev=1um, Mapped to'r); + Heat Transfer (ht) + Electromagnetic Heating (emh1) + S2 Stationary
 * (x=0/1, V_app=+-3.5/4.5V -> T_max, hammasi Tm dan past); + Global ODE (ge, "xode") + S3 Stationary
 * (ec+ht+ge birgalikda, muvozanat xode qidiruvi) - natijalar va CHEKLOV pastda.
 *
 * S1/S2 o'zgarishsiz: std1/std2/std3 (S1a/S1/S2) da "ge" o'chirilgan ("activate"), sig_fil "xode"
 * ga bog'liq bo'lsa-da, xode ge1 ning initialValueU="xs" ifodasi orqali "xs" parametriga
 * muzlatiladi - eski xs-boshqaruvli mexanizm (kalibrovka, aux sweep) TO'LIQ saqlanadi.
 *
 * S3 NATIJASI VA MUHIM CHEKLOV (2026-09-27): Global ODE fizikasi (ec+ht+ge) MEXANIK jihatdan
 * to'g'ri ishlaydi (kompilyatsiya, yechim, S1/S2 ga ta'sir qilmaydi). LEKIN Stationary orqali
 * "muvozanat x" qidirish bu BISTABIL/qattiq-nochiziqli tizim uchun ISHONCHSIZ: Joglekar oynasi
 * Fwin(0)=Fwin(1)=0 qilib quriladi, shuning uchun x=0 (deyarli) HAR QANDAY V_app da dxdt_rhs ning
 * ILDIZI bo'lib qoladi (Newton boshlang'ich taxmin qanchalik yaqin bo'lsa ham o'sha yerda "qotib
 * qolishi" yoki, aksincha, kichik boshlang'ich siljish bilan (x0=0.01) V_read=0.1V da ham xode
 * ~0.95 ga "sakrab ketishi" mumkin - FIZIK JIHATDAN NOTO'G'RI, chunki o'qish kuchlanishi hech
 * qanday holatni o'zgartirmasligi kerak). Sabab: Stationary Newton iteratsiyasi shunchaki eng
 * yaqin ILDIZNI topadi (barqaror yoki beqaror farqlanmaydi), real vaqt evolyutsiyasini aks
 * ettirmaydi. XULOSA: Stationary "muvozanat x" tekshiruvi faqat MEXANIZM ishlashini tasdiqlash
 * uchun ishlatildi (S3_ODE_check.csv); haqiqiy x(t) dinamikasi FAQAT Time Dependent orqali to'g'ri
 * aniqlanadi (keyingi bosqich).
 *
 * Keyingi bosqich (hali YO'Q): Time Dependent - V(t)=Vamp*(2/pi)*asin(sin(2*pi*f0*t)), 0-30ms,
 * BDF, T_amb sweep (300/350/400/450K), to'liq natijalar (N1-N6, iv_ugate0.csv, xt_cycle.csv,
 * Tmax_t.csv va h.k.).
 *
 * Kerakli litsenziya: COMSOL Multiphysics (ConductiveMedia, HeatTransfer, GlobalEquations bazaviy
 * paketda bor).
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

  // Natija fayllari (CSV, MPH) comsolbatch ishga tushirilgan papkaga (nisbiy yo'l bilan) yoziladi.
  // TEKSHIRILSIN edi: System.getProperty("user.dir") comsolbatch xavfsizlik menejeri tomonidan
  // rad etiladi (AccessControlException: PropertyPermission "user.dir" "read"), shuning uchun
  // barcha chiqish fayllari uchun oddiy nisbiy fayl nomlari ishlatiladi (getAbsolutePath() ham
  // ichida yo'l hal qiluvchi kod orqali xuddi shu tekshiruvni chaqirishi mumkin).

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
    p(model, "R_dev", "1[um]", "Element radiusi (2-bosqich: T_max cheklovi uchun kattalashtirilgan)");
    p(model, "t_be", "10[nm]", "Pastki elektrod qalinligi (faraz)");
    p(model, "t_ST", "20[nm]", "Sb2Te3 qalinligi");
    p(model, "t_int", "1.5[nm]", "Interfeys (vdW) qatlami qalinligi");
    p(model, "t_GT", "20[nm]", "GeTe qalinligi");
    p(model, "t_te", "10[nm]", "Yuqori elektrod qalinligi (faraz)");
    p(model, "r_f", "200[nm]", "Filament radiusi (2-bosqich: T_max cheklovi uchun kattalashtirilgan)");
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

    // --- Filament kinetikasi (3-bosqich: Global ODE, xs endi statik PARAMETR sifatida S1/S2 uchun
    //     saqlanadi, dinamik holat esa alohida "xode" global o'zgaruvchisida - pastga qarang) ---
    p(model, "xs", "0", "Filament holati (S1/S2 uchun statik parametr; 3-bosqich: xode)");
    p(model, "k0", "1e13[1/s]", "Urinish chastotasi (fonon chastotasi tartibi)");
    p(model, "Ea", "0.9[eV]", "Migratsiya aktivatsiya energiyasi (fitting)");
    p(model, "a_hop", "0.3[nm]", "Sakrash masofasi (fitting)");
    p(model, "tau_rel", "1e3[s]", "Relaksatsiya vaqti (katta -> nonvolatil)");
    p(model, "p_win", "2", "Oyna funksiyasi darajasi (Biolek: Fwin = 1-x^(2p) yoki 1-(x-1)^(2p))");
    p(model, "E_s", "1e7[V/m]", "Silliq Biolek yo'nalish funksiyasi uchun maydon miqyosi (tanh)");
    p(model, "kB_c", "1.380649e-23[J/K]", "Boltsman doimiysi");
    p(model, "q_c", "1.602176634e-19[C]", "Elektron zaryadi");

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
    // h_int endi ishlatilmaydi: interfeys qatlami (r_f=200nm, R_dev=1um bilan juda yassi/keng domen)
    // Mapped to'r bilan mesh qilinadi (z bo'yicha aniq element soni, r bo'yicha r_f atrofida
    // zichlashtirilgan taqsimot) -- FreeTri + hmax=0.5nm butun radius bo'ylab juda ko'p element
    // hosil qilardi.
    p(model, "h_glob", "2[nm]", "Qolgan joyda maksimal element (FreeTri: elektrodlar, ST, GT)");

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

    // Interfeys qatlami uchun Mapped to'r qirralari (2-bosqich, r_f=200nm/R_dev=1um bilan):
    boxSel(g, "box_v_rf", "r_f-0.01[nm]", "r_f+0.01[nm]", "z_int-0.01[nm]", "z_int+t_int+0.01[nm]");   // umumiy filament/halqa qirrasi (z bo'yicha taqsimot)
    boxSel(g, "box_fil_bot", "-1", "r_f-0.01[nm]", "z_int-0.01[nm]", "z_int+0.01[nm]");                 // filament pastki qirrasi (r bo'yicha taqsimot)
    boxSel(g, "box_ring_bot", "r_f+0.01[nm]", "R_dev+1[nm]", "z_int-0.01[nm]", "z_int+0.01[nm]");       // halqa pastki qirrasi (r bo'yicha taqsimot)
    g.run();

    // Domen birlashmalari
    union(model, "sel_int", 2, new String[]{"geom1_r_fil_dom", "geom1_r_ring_dom"}, "Interfeys qatlami (filament + halqa)");
    union(model, "sel_el", 2, new String[]{"geom1_r_be_dom", "geom1_r_te_dom"}, "Elektrodlar");
    // Chegara birlashmasi: T = T_amb (yuqori + pastki elektrod tashqi yuzalari), 2-bosqich uchun.
    union(model, "sel_Tbc", 1, new String[]{"geom1_box_top", "geom1_box_bot"}, "T=T_amb chegaralari (yuqori+pastki elektrod)");

    // =====================================================================================
    // DEFINITIONS (o'zgaruvchilar va operatorlar)
    // =====================================================================================
    model.component("comp1").variable().create("var1");
    model.component("comp1").variable("var1").label("Filament o'tkazuvchanligi");
    // sig_off^(1-x)*sig_on^x ni birliklar bo'yicha to'g'ri ko'rinishda yozamiz:
    // sig_fil "xode" ga bog'liq (S1/S2/S3(issiqlik)da ge o'chirilgan bo'lsa, xode ge1 ning
    // initialValueU="xs" ifodasi orqali "xs" parametrining joriy qiymatiga "muzlatiladi" - shu
    // tarzda eski xs-boshqaruvli kalibrovka/aux sweep mexanizmi o'zgarishsiz ishlayveradi).
    model.component("comp1").variable("var1").set("sig_fil", "sig_off*(sig_on/sig_off)^xode",
        "Filament o'tkazuvchanligi: sig_off^(1-x)*sig_on^x");

    // 3-bosqich: Global ODE uchun haydovchi kattaliklar va Joglekar oynasi. "xode" quyida
    // "ge" (Global ODEs and DAEs) fizikasi orqali aniqlanadigan global bog'liq o'zgaruvchi
    // (aveop_fil/ec/ht dan oldin/keyin yaratilgan bo'lishidan qat'i nazar, COMSOL butun model
    // bo'yicha simvolik jadvalni model.build() vaqtida hal qiladi - 1-bosqichdagi ec.Ez -> aveop_fil
    // naqshiga o'xshab).
    model.component("comp1").variable("var1").set("E_drive", "-aveop_fil(ec.Ez)",
        "Filamentdagi haydovchi maydon (V_app>0 -> E_drive>0 -> SET yo'nalishi)");
    model.component("comp1").variable("var1").set("T_local", "aveop_fil(T)",
        "Filamentning o'rtacha harorati (Heat Transfer dan)");
    // OYNA TANLOVI (Time Dependent, 3-bosqich, 2-qism uchun EMPIRIK ravishda TUZATILDI):
    // dastlab Joglekar oynasi (1-(2x-1)^(2p)) + x(0)=1e-3 bilan sinaldi. Natija: SET to'g'ri ishladi
    // (V_SET topilgan = 3.705 V, maqsad 3.5 V), LEKIN RESET umuman topilmadi - Joglekar oynasi
    // Fwin(1)=0 QURILISHI BO'YICHA, shuning uchun x=1 ga yetgach, HATTO KUCHLI MANFIY E_drive
    // (V=-4.5V) da ham haydovchi had deyarli bostiriladi va x amalda "yopishib qoladi" (faqat juda
    // sekin -x/tau_rel relaksatsiyasi qoladi, tau_rel=1000s >> 30ms simulyatsiya). Bu Joglekar
    // (simmetrik) oynaning YAXSHI MA'LUM kamchiligi: ikkala chegara HAM yo'nalishidan qat'i nazar
    // "yopishqoq" bo'lib qoladi. YECHIM: Biolek oynasi (yo'nalishga bog'liq): SET paytida (E_drive>0)
    // oyna x=1 yaqinida yopiladi (o'sishni to'xtatadi, lekin PASTGA harakatni CHEKLAMAYDI), RESET
    // paytida (E_drive<0) oyna x=0 yaqinida yopiladi, ammo x=1 yaqinida OCHIQ qoladi - shu bilan
    // RESET signalini bostirmaydi. Yo'nalish sifatida haqiqiy dx/dt (o'z-o'ziga bog'liq bo'lardi)
    // o'rniga sign(E_drive) ishlatiladi - bu matematik jihatdan bir xil (sinh argumentning ishorasini
    // saqlaydi), lekin circular reference yo'q.
    // SILLIQLASHTIRISH (T_amb sweepdan oldin, hisoblash tezligi uchun): "if(E_drive>0, ...)" keskin
    // uzilish har bir nol-kesishmada (davrga 6 marta) BDF qadamini juda kichraytirib yubordi
    // (~45 daqiqa, bitta tsikl). "dir_smooth" endi tanh bilan silliq: E_drive >> E_s da ~1 (SET),
    // E_drive << -E_s da ~0 (RESET), oralig'ida silliq o'tish. E_s=1e7 V/m tanlovi: SET/RESET paytida
    // |E_drive| ~ 1e9 V/m tartibida (interfeys ustidagi V/t_int), demak E_s ancha kichik bo'lib,
    // o'tish V=0 atrofida ingichka (E_drive~E_s ga mos V juda kichik) qoladi va V_SET/V_RESET
    // qiymatlariga deyarli ta'sir qilmaydi.
    model.component("comp1").variable("var1").set("dir_smooth", "0.5*(1+tanh(E_drive/E_s))",
        "Silliq yo'nalish funksiyasi: ~1 SET (E_drive>0), ~0 RESET (E_drive<0)");
    model.component("comp1").variable("var1").set("Fwin_x",
        "dir_smooth*(1-xode^(2*p_win))+(1-dir_smooth)*(1-(xode-1)^(2*p_win))",
        "Silliqlashtirilgan Biolek oynasi: SET'da x=1 tomon, RESET'da x=0 tomon ochiq");
    model.component("comp1").variable("var1").set("dxdt_rhs",
        "k0*exp(-Ea/(kB_c*T_local))*sinh(q_c*a_hop*E_drive/(2*kB_c*T_local))*Fwin_x-xode/tau_rel",
        "dx/dt ifodasi (Global Equation da xode_t = dxdt_rhs sifatida ishlatiladi)");
    // 3-bosqich, Time Dependent: uchburchak surish signali. t=0 da asin(sin(0))=0 -> V(0)=0.
    model.component("comp1").variable("var1").set("V_wave", "Vamp*(2/pi)*asin(sin(2*pi*f0*t))",
        "Uchburchak surish signali V(t) (faqat Time Dependent tadqiqotda term1.V0 sifatida ishlatiladi)");

    // Filament bo'yicha hajmiy o'rtacha (keyingi bosqichda Global ODE uchun <Ez> va <T>)
    model.component("comp1").cpl().create("aveop_fil", "Average");
    model.component("comp1").cpl("aveop_fil").selection().named("geom1_r_fil_dom");
    model.component("comp1").cpl("aveop_fil").set("axisym", true);   // TEKSHIRILSIN: 2*pi*r og'irlik bilan o'rtacha
    model.component("comp1").cpl().create("intop_top", "Integration");
    model.component("comp1").cpl("intop_top").selection().geom("geom1", 1);
    model.component("comp1").cpl("intop_top").selection().named("geom1_box_top");
    model.component("comp1").cpl("intop_top").set("axisym", true);   // TEKSHIRILSIN

    // 2-bosqich: butun qurilma bo'yicha maksimal harorat (Heat Transfer qo'shilgach ishlatiladi).
    // TASDIQLANDI: "Average"/"Integration" kabi "Maximum" ham to'g'ri cpl operator turi.
    model.component("comp1").cpl().create("maxop_T", "Maximum");
    model.component("comp1").cpl("maxop_T").selection().geom("geom1", 2);
    model.component("comp1").cpl("maxop_T").selection().all();

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
    // PHYSICS: Heat Transfer in Solids (2-bosqich)
    // =====================================================================================
    // TASDIQLANDI (ishga tushirish orqali): "HeatTransferInSolids" COMSOL 6.0 da MAVJUD EMAS
    // ("Unknown physics interface"); to'g'ri tur nomi "HeatTransfer" (yagona bazaviy interfeys,
    // domenlarda default "Solid" feature qo'shiladi).
    model.component("comp1").physics().create("ht", "HeatTransfer", "geom1");
    // solid1 (domen) k/rho/Cp ni materiallardan oladi (default "from material").
    // Yon devor (geom1_box_out): chegara shart qo'yilmagan -> default Thermal Insulation (ec dagi
    // Electric Insulation kabi), alohida feature kerak emas.

    // TASDIQLANDI: "TemperatureBoundary" va "T0" birinchi urinishdayoq xatosiz ishladi.
    model.component("comp1").physics("ht").create("temp1", "TemperatureBoundary", 1);
    model.component("comp1").physics("ht").feature("temp1").label("T = T_amb (yuqori+pastki elektrod)");
    model.component("comp1").physics("ht").feature("temp1").selection().named("sel_Tbc");
    model.component("comp1").physics("ht").feature("temp1").set("T0", "T_amb");

    // =====================================================================================
    // MULTIPHYSICS
    // =====================================================================================
    // Electromagnetic Heating: ec dagi Joule isishi (J*E) ni ht ga issiqlik manbai sifatida qo'shadi.
    // TASDIQLANDI: "ElectromagneticHeating" tur nomi va dim=2 (2D domen darajasi) xatosiz ishladi.
    model.component("comp1").multiphysics().create("emh1", "ElectromagneticHeating", 2);
    model.component("comp1").multiphysics("emh1").selection().all();

    // =====================================================================================
    // PHYSICS: Global ODE (x kinetikasi, 3-bosqich)
    // =====================================================================================
    // "ge" (Global ODEs and DAEs) -- bitta skalyar (0D) bog'liq o'zgaruvchi "xode" uchun.
    // Tenglama: xode_t = dxdt_rhs (Stationary'da solver xode_t=0 deb hisoblab muvozanat xode'ni
    // topadi; Time Dependent'da to'liq integrallanadi). Fizik masala (ec/ht) bilan bog'liq
    // (E_drive, T_local orqali) va sig_fil ga ta'sir qilishi uchun (keyingi bosqichda) xode
    // sig_fil ifodasida xs o'rniga ishlatiladi.
    // TEKSHIRILSIN: "GlobalODEsAndDAEs"/"GlobalODE"/"GlobalODEs" (geom1 bilan) ishlamadi. Programming
    // Reference Manual: 0D (fazoga bog'liq bo'lmagan) interfeyslar geomtag ARGUMENTISIZ yaratiladi.
    model.component("comp1").physics().create("ge", "GlobalEquations");
    model.component("comp1").physics("ge").feature("ge1").label("Filament holati x");
    // TASDIQLANDI: "name"/"equation"/"initialValueU"/"initialValueUt" xossalari (jadval ustunlari
    // sifatida) birinchi urinishdayoq to'g'ri ishladi.
    model.component("comp1").physics("ge").feature("ge1").set("name", new String[]{"xode"});
    // TEKSHIRILSIN -> QISMAN TASDIQLANDI: "xode_t" Stationary tadqiqotda ANIQLANMAGAN ("Undefined
    // variable: xode_t. Global scope") - vaqt hosilasi faqat Time Dependent tadqiqotda mavjud bo'ladi.
    // Hozircha (Stationary, 3-bosqichning birinchi qismi) tenglama shunchaki muvozanat sharti
    // dxdt_rhs=0. Time Dependent bosqichida bu ifoda "xode_t-dxdt_rhs" ga almashtirilishi kerak
    // (o'sha paytda haqiqiy vaqt o'lchami mavjud bo'lgani uchun xode_t aniqlangan bo'lishi kutiladi).
    model.component("comp1").physics("ge").feature("ge1").set("equation", new String[]{"dxdt_rhs"});
    // initialValueU = "xs" (SON emas, IFODA): std1/std2/std3'da ge o'chirilgan bo'lgani uchun xode
    // "xs" parametrining joriy qiymatiga muzlatiladi (eski xs-boshqaruvli mexanizm bilan mos keladi);
    // std4'da esa bu shunchaki Newton iteratsiyasi uchun BOSHLANG'ICH TAXMIN bo'ladi (xode erkin
    // yechiladi). Shu sababli 3-bosqich tekshiruvida boshlang'ich taxminni "xs" orqali beramiz.
    model.component("comp1").physics("ge").feature("ge1").set("initialValueU", new String[]{"xs"});
    model.component("comp1").physics("ge").feature("ge1").set("initialValueUt", new String[]{"0"});

    // =====================================================================================
    // MESH
    // =====================================================================================
    model.component("comp1").mesh().create("mesh1");
    model.component("comp1").mesh("mesh1").feature("size").set("custom", "on");
    model.component("comp1").mesh("mesh1").feature("size").set("hmax", "h_glob");
    model.component("comp1").mesh("mesh1").feature("size").set("hmin", "0.02[nm]");
    model.component("comp1").mesh("mesh1").feature("size").set("hgrad", 1.2);

    // Interfeys (filament+halqa, t_int=1.5nm x R_dev=1um -- juda yassi/keng domen) uchun Mapped
    // (strukturaviy) to'r: FreeTri + hmax=0.5nm butun radius bo'ylab ishlatilsa mos kelmagan
    // darajada ko'p element hosil bo'lardi. sel_int (1-bosqichda yaratilgan) qayta ishlatiladi.
    // TASDIQLANDI: "Mapped" GUI da ("Operation cannot be created in this context" - "Mapped" ishlamadi),
    // ichki tur nomi "Map".
    model.component("comp1").mesh("mesh1").create("map1", "Map");
    model.component("comp1").mesh("mesh1").feature("map1").selection().geom("geom1", 2);
    model.component("comp1").mesh("mesh1").feature("map1").selection().named("sel_int");

    // z bo'yicha (t_int=1.5nm qalinlik): umumiy filament/halqa qirrasida taqsimot -- Mapped to'r
    // qarama-qarshi qirralarni avtomatik moslashtiradi, shu bilan ikkala domen ham 3 elementli bo'ladi.
    model.component("comp1").mesh("mesh1").feature("map1").create("dis_z", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_z").selection().named("geom1_box_v_rf");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_z").set("numelem", 3);

    // r bo'yicha: filament ichida r_f chegarasi tomon zichlashuv (oqim zichligi keskin o'zgaradigan joy).
    // TASDIQLANDI: "type"/"numelem"/"elemratio"/"reverse" xossalari birinchi urinishdayoq ishladi.
    model.component("comp1").mesh("mesh1").feature("map1").create("dis_rfil", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rfil").selection().named("geom1_box_fil_bot");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rfil").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rfil").set("numelem", 20);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rfil").set("elemratio", 20);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rfil").set("reverse", true);   // zich uchi r=r_f tomonda

    // halqada ham xuddi shu r_f chegarasidan tashqariga tomon siyraklashuv.
    model.component("comp1").mesh("mesh1").feature("map1").create("dis_rring", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rring").selection().named("geom1_box_ring_bot");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rring").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rring").set("numelem", 20);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis_rring").set("elemratio", 20);
    // reverse=false (default): zich uchi qirraning boshida, ya'ni r=r_f tomonda.

    // Qolgan domenlar (elektrodlar, ST, GT): sel_int mesh qilingandan keyin "qolgan" domenlar sifatida
    // avtomatik tanlanadi (hmax=h_glob, size feature orqali).
    model.component("comp1").mesh("mesh1").create("ftri1", "FreeTri");
    model.component("comp1").mesh("mesh1").run();

    // =====================================================================================
    // STUDY
    // =====================================================================================
    // std1: bitta Stationary (xs parametrining joriy qiymati bilan) -> kalibrovka sikli uchun.
    model.study().create("std1");
    model.study("std1").label("S1a: Stationary (kalibrovka, bitta holat)");
    model.study("std1").create("stat", "Stationary");
    // Faqat ec: S1 kalibrovkasi 1-bosqichdagidek toza elektr masala bo'lib qolishi uchun ht o'chirilgan.
    // TASDIQLANDI: "activate" 2D String[][] emas, balki tekis String[] (kalit,qiymat,...) kutadi.
    model.study("std1").feature("stat").set("activate", new String[]{"ec", "on", "ht", "off", "ge", "off"});

    // std2: Stationary + auxiliary sweep xs = 0, 1 -> R_OFF va R_ON bitta datasetda (GUI va eksport uchun).
    model.study().create("std2");
    model.study("std2").label("S1: Stationary, x = 0 (OFF) va x = 1 (ON)");
    model.study("std2").create("stat", "Stationary");
    model.study("std2").feature("stat").set("useparam", true);
    model.study("std2").feature("stat").set("pname", new String[]{"xs"});
    model.study("std2").feature("stat").set("plistarr", new String[]{"0 1"});
    model.study("std2").feature("stat").set("punit", new String[]{""});
    model.study("std2").feature("stat").set("activate", new String[]{"ec", "on", "ht", "off", "ge", "off"});

    // std3: Stationary, ec + ht birgalikda (Electromagnetic Heating orqali) -> 2-bosqich, S2_Tmax.csv.
    model.study().create("std3");
    model.study("std3").label("S2: Stationary (Electric Currents + Heat Transfer)");
    model.study("std3").create("stat", "Stationary");
    model.study("std3").feature("stat").set("activate", new String[]{"ec", "on", "ht", "on", "ge", "off"});

    // std4: Stationary, ec + ht + ge birgalikda -> 3-bosqich (Global ODE, muvozanat x).
    model.study().create("std4");
    model.study("std4").label("S3: Stationary (Electric Currents + Heat Transfer + Global ODE)");
    model.study("std4").create("stat", "Stationary");

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
    String calCsv = "S1_calibration.csv";
    PrintWriter out = null;
    try {
      out = new PrintWriter(new FileWriter(calCsv));
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
    } finally {
      if (out != null) out.close();
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
      model.result().export("exp_S1").set("filename", "S1_R_on_off.csv");
      model.result().export("exp_S1").run();
    } catch (Exception ex) {
      System.out.println("XATO (eksport S1_R_on_off.csv): " + ex.getMessage());
    }
    try {
      model.result().export().create("exp_Vz", "Plot");
      model.result().export("exp_Vz").set("plotgroup", "pg_Vz");
      model.result().export("exp_Vz").set("plot", "lngr1");
      model.result().export("exp_Vz").set("filename", "S1_V_axis.csv");
      model.result().export("exp_Vz").run();
    } catch (Exception ex) {
      System.out.println("XATO (eksport S1_V_axis.csv): " + ex.getMessage());
    }

    // =====================================================================================
    // S2: HEAT TRANSFER + ELECTROMAGNETIC HEATING (2-bosqich, "tutun testi")
    // =====================================================================================
    // x = 0 (OFF) va x = 1 (ON) holatlarida, V_app = +3.5 V va -3.5 V da (V_SET_t/V_RESET_t) T_max.
    // OGOHLANTIRISH: analitik baho bo'yicha x=1, |V_app|=3.5V da T_max ~1e4 K tartibida bo'lishi
    // mumkin (J ~ 5e12 A/m^2, r_f=5nm, sig_int past termik o'tkazuvchanlik bilan). Bu holda modelga
    // tegilmaydi -- natija xom holida hisobot qilinadi, chora tanlovi foydalanuvchiga qoldiriladi.
    // std3 birinchi marta ishga tushmaguncha uning dataseti (dsetN) mavjud bo'lmaydi ("Unknown
    // dataset"), shuning uchun baholash tugunini yaratishdan oldin bitta "priming" yechim kerak.
    // TASDIQLANDI: naqsh dset1->std1, dset2->std2 ga o'xshab, std3 -> dset3.
    model.param().set("xs", "0");
    model.param().set("V_app", "3.5[V]");
    model.study("std3").run();

    model.result().numerical().create("gev_Tmax", "EvalGlobal");
    model.result().numerical("gev_Tmax").label("S2: T_max (butun qurilma)");
    model.result().numerical("gev_Tmax").set("data", "dset3");
    model.result().numerical("gev_Tmax").set("expr", new String[]{"maxop_T(T)"});
    model.result().numerical("gev_Tmax").set("unit", new String[]{"K"});

    String tmaxCsv = "S2_Tmax.csv";
    PrintWriter outT = null;
    try {
      outT = new PrintWriter(new FileWriter(tmaxCsv));
      outT.println("xs,V_app_V,T_max_K,Tm_ST_K,Tm_GT_K,note");
      double[] xsVals = {0, 0, 0, 1, 1, 1};
      double[] vVals  = {3.5, -3.5, 4.5, 3.5, -3.5, 4.5};
      for (int i = 0; i < xsVals.length; i++) {
        model.param().set("xs", String.format(Locale.US, "%.0f", xsVals[i]));
        model.param().set("V_app", String.format(Locale.US, "%.4g[V]", vVals[i]));
        model.study("std3").run();
        double tmax = model.result().numerical("gev_Tmax").getReal()[0][0];
        String note = (tmax >= 998.0) ? "T_max >= Tm_GT (998 K)"
            : (tmax >= 891.0) ? "T_max >= Tm_ST (891 K)" : "OK (erish haroratidan past)";
        outT.println(String.format(Locale.US, "%.0f,%.4g,%.6e,891,998,%s", xsVals[i], vVals[i], tmax, note));
        System.out.println(String.format(Locale.US,
            "S2 T_max: xs=%.0f  V_app=%.2f V  ->  T_max=%.6e K  [%s]", xsVals[i], vVals[i], tmax, note));
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV S2_Tmax): " + tmaxCsv + " : " + ex.getMessage());
    } finally {
      if (outT != null) outT.close();
      model.param().set("xs", "0");
      model.param().set("V_app", "V_read");
    }

    // =====================================================================================
    // S3: GLOBAL ODE (3-bosqich, Stationary -- muvozanat x). sig_fil endi "xode" dan foydalanadi;
    // "xs" parametri esa (a) S1/S2/std3'da ge o'chirilgani uchun xode ni muzlatish, (b) std4'da
    // ge1 ning initialValueU="xs" orqali Newton iteratsiyasi uchun BOSHLANG'ICH TAXMIN berish
    // uchun ishlatiladi (haqiqiy qattiq bog'lanish emas -- std4'da xode erkin yechiladi).
    // =====================================================================================
    // Har bir holat uchun boshlang'ich taxmin (x0="xs") berilib, Stationary Newton iteratsiyasi
    // qaysi muvozanatga (agar bir nechta bo'lsa - bistabillik) yaqinlashishini ko'ramiz:
    //  1) V_read (0.1V), x0=0    -> kutilgan: xode ~ 0 (o'qishda o'zgarish bo'lmasligi kerak)
    //  2) V_SET_t (+3.5V), x0=0  -> kutilgan: xode -> 1 ga yaqin (SET, OFF dan)
    //  3) V_RESET_t (-3.5V), x0=1 -> kutilgan: xode -> 0 ga yaqin (RESET, ON dan)
    //  4) +4.5V, x0=0            -> kutilgan: xode -> 1 ga yaqin (kuchliroq SET)
    // MUHIM TOPILMA: Fwin(0)=Fwin(1)=0 (Joglekar oynasi qurilishi bo'yicha), shuning uchun x=0 HAR
    // QANDAY V_app da dxdt_rhs ning ANIQ ILDIZI (haydovchi had 0 ga ko'payadi) - agar boshlang'ich
    // taxmin ANIQ 0 bo'lsa, Newton "yaqinlashdi" deb 0 qatorida qotib qoladi (u beqaror muvozanat
    // bo'lsa ham). Shu sababli boshlang'ich taxminlar 0/1 chekkalaridan birmuncha ichkariga (0.5)
    // qo'yiladi -- Stationary'da bistabillikni to'g'ri qidirish uchun standart texnika.
    double[] vApp3   = {0.1, 3.5, -3.5, 4.5};
    double[] x0guess = {0.01, 0.5, 0.5, 0.5};
    String[] lbl3 = {"V_read,x0=0.01", "V_SET_t,x0=0.5", "V_RESET_t,x0=0.5", "V=4.5V,x0=0.5"};

    model.param().set("V_app", String.format(Locale.US, "%.4g[V]", vApp3[0]));
    model.param().set("xs", String.format(Locale.US, "%.4g", x0guess[0]));
    model.study("std4").run();

    model.result().numerical().create("gev_S3", "EvalGlobal");
    model.result().numerical("gev_S3").label("S3: xode, R, T_max (muvozanat)");
    model.result().numerical("gev_S3").set("data", "dset4");   // TASDIQLANDI: naqsh davom etadi, std4 -> dset4
    model.result().numerical("gev_S3").set("expr", new String[]{"xode", "V_app/ec.I0_1", "maxop_T(T)"});
    model.result().numerical("gev_S3").set("unit", new String[]{"1", "ohm", "K"});

    String odeCsv = "S3_ODE_check.csv";
    PrintWriter outX = null;
    try {
      outX = new PrintWriter(new FileWriter(odeCsv));
      outX.println("case,V_app_V,x0_guess,xode_final,R_ohm,T_max_K");
      for (int i = 0; i < vApp3.length; i++) {
        model.param().set("V_app", String.format(Locale.US, "%.4g[V]", vApp3[i]));
        model.param().set("xs", String.format(Locale.US, "%.4g", x0guess[i]));
        model.study("std4").run();
        double[][] res = model.result().numerical("gev_S3").getReal();
        double xodeFinal = res[0][0], rOhm = res[1][0], tmax = res[2][0];
        outX.println(String.format(Locale.US, "%s,%.4g,%.4g,%.6e,%.6e,%.6e", lbl3[i], vApp3[i], x0guess[i], xodeFinal, rOhm, tmax));
        System.out.println(String.format(Locale.US,
            "S3 ODE: [%s]  V_app=%.2f V  x0=%.2f  ->  xode=%.6e  R=%.6e ohm  T_max=%.6e K",
            lbl3[i], vApp3[i], x0guess[i], xodeFinal, rOhm, tmax));
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV S3_ODE_check): " + odeCsv + " : " + ex.getMessage());
    } finally {
      if (outX != null) outX.close();
      model.param().set("V_app", "V_read");
      model.param().set("xs", "0");
    }

    // =====================================================================================
    // S4: TIME DEPENDENT (3-bosqich, 2-qism) - BITTA TSIKL, T_amb=300K (foydalanuvchi so'roviga
    // ko'ra T_amb sweep va N1-N6 keyingi qadamga qoldirilgan).
    // =====================================================================================
    // OYNA TANLOVI (EMPIRIK ITERATSIYA - yuqoridagi Fwin_x izohiga qarang): dastlab Joglekar oynasi
    // + x(0)=1e-3 sinaldi - SET to'g'ri ishladi, lekin RESET UMUMAN topilmadi (x=1 da Fwin=0 bo'lgani
    // uchun x "yopishib qoldi"). YAKUNIY TANLOV: Biolek oynasi (yo'nalish = sign(E_drive), circular
    // reference YO'Q) - bu ham x(0)=1e-3 talab qilmaydi (Fwin_biolek(x=0, E_drive>0) = 1-0^(2p) = 1,
    // ya'ni x=0 SET yo'nalishida OCHIQ chegara), lekin xavfsizlik uchun baribir 1e-3 qoldirildi.
    model.component("comp1").physics("ge").feature("ge1").label("Filament holati x (dinamik, Time Dependent)");
    // TEKSHIRILSIN: "xode_t" (pastki chiziq bilan) Time Dependent'da ham ANIQLANMAGAN chiqdi
    // ("Undefined variable: xode_t"). Hujjatdagi "f(u,ut,utt,t)" yozuvida "u" bilan "t" orasida
    // pastki chiziq YO'Q - haqiqiy sintaksis "xodet" (qo'shilgan) bo'lishi mumkin.
    model.component("comp1").physics("ge").feature("ge1").set("equation", new String[]{"xodet-dxdt_rhs"});
    model.component("comp1").physics("ge").feature("ge1").set("initialValueU", new String[]{"1e-3"});

    // Terminal endi doimiy V_app o'rniga uchburchak signal V_wave bilan boshqariladi. Bu o'zgarish
    // S1/S2/S3 (std1-4, ular yuqorida allaqachon yechilgan va eksport qilingan) ga ta'sir qilmaydi.
    model.component("comp1").physics("ec").feature("term1").set("V0", "V_wave");

    model.study().create("std5");
    model.study("std5").label("S4: Time Dependent (T_amb=300K, uchburchak signal, bitta tsikl)");
    model.study("std5").create("time", "Transient");   // TEKSHIRILSIN: "Transient" - GUI "Time Dependent"
    // 0-30ms, chiqish qadami 5e-5s (max ichki qadam talabi 1/(400*f0)=2.5e-5s ga yaqin holda,
    // hisoblash vaqtini maqbul saqlash uchun; BDF solver ikkita chiqish nuqtasi orasida ham
    // moslashuvchan kichikroq ichki qadamlar oladi).
    model.study("std5").feature("time").set("tlist", "range(0,5e-5,0.03)");   // TEKSHIRILSIN
    model.study("std5").run();

    model.result().numerical().create("gev_S4", "EvalGlobal");
    model.result().numerical("gev_S4").label("S4: t, V, I, x, T_max (vaqt qatori)");
    model.result().numerical("gev_S4").set("data", "dset5");   // TEKSHIRILSIN: std5 -> dset5
    model.result().numerical("gev_S4").set("expr", new String[]{"t", "V_wave", "ec.I0_1", "xode", "maxop_T(T)"});
    model.result().numerical("gev_S4").set("unit", new String[]{"s", "V", "A", "1", "K"});
    double[][] s4 = model.result().numerical("gev_S4").getReal();   // s4[expr][nuqta]: 0=t,1=V,2=I,3=x,4=Tmax

    String xtCsv = "xt_cycle.csv";
    String ivCsv = "iv_ugate0.csv";
    PrintWriter outXt = null, outIv = null;
    try {
      outXt = new PrintWriter(new FileWriter(xtCsv));
      outXt.println("t_s,V_V,I_A,x,Tmax_K");
      outIv = new PrintWriter(new FileWriter(ivCsv));
      outIv.println("V_V,I_A");
      int n = (s4.length > 0) ? s4[0].length : 0;
      for (int i = 0; i < n; i++) {
        outXt.println(String.format(Locale.US, "%.6e,%.6e,%.6e,%.6e,%.6e", s4[0][i], s4[1][i], s4[2][i], s4[3][i], s4[4][i]));
        outIv.println(String.format(Locale.US, "%.6e,%.6e", s4[1][i], s4[2][i]));
      }

      // V_SET / V_RESET: x = 0.5 dan o'tish momentidagi V (chiziqli interpolyatsiya).
      double vSetFound = Double.NaN, vResetFound = Double.NaN;
      for (int i = 1; i < n; i++) {
        double xPrev = s4[3][i - 1], xCur = s4[3][i];
        if (Double.isNaN(vSetFound) && xPrev < 0.5 && xCur >= 0.5) {
          double frac = (0.5 - xPrev) / (xCur - xPrev);
          vSetFound = s4[1][i - 1] + frac * (s4[1][i] - s4[1][i - 1]);
        }
        if (Double.isNaN(vResetFound) && xPrev > 0.5 && xCur <= 0.5) {
          double frac = (xPrev - 0.5) / (xPrev - xCur);
          vResetFound = s4[1][i - 1] + frac * (s4[1][i] - s4[1][i - 1]);
        }
      }
      System.out.println(String.format(Locale.US,
          "S4 NATIJA: n=%d nuqta, x(oxirgi)=%.4g, T_max(oxirgi)=%.4g K, V_SET(topilgan)=%s V (maqsad +3.5), V_RESET(topilgan)=%s V (maqsad -3.5)",
          n, s4[3][n - 1], s4[4][n - 1],
          Double.isNaN(vSetFound) ? "TOPILMADI" : String.format(Locale.US, "%.4g", vSetFound),
          Double.isNaN(vResetFound) ? "TOPILMADI" : String.format(Locale.US, "%.4g", vResetFound)));
    } catch (IOException ex) {
      System.out.println("XATO (CSV xt_cycle/iv_ugate0): " + ex.getMessage());
    } finally {
      if (outXt != null) outXt.close();
      if (outIv != null) outIv.close();
    }

    // =====================================================================================
    // S5: T_amb SWEEP (Tungi vazifa, 1-topshiriq) - har bir T_amb uchun BITTA davr (0-10ms).
    // N1 (I-V), N3 (x/V/I(t)), N4 (T xaritasi+T_max(t)), N5 (|E|/|J| xaritalari), N6 (jadval).
    // =====================================================================================
    model.study().create("std6");
    model.study("std6").label("S5: Time Dependent (T_amb sweep, bitta davr)");
    model.study("std6").create("time", "Transient");
    model.study("std6").feature("time").set("tlist", "range(0,5e-5,0.01)");

    double[] tambSweep = {300, 350, 400, 450};
    String ivSweepCsv = "iv_Tamb_sweep.csv";
    String tmaxTCsv = "Tmax_t.csv";
    String n6Csv = "N6_table.csv";
    PrintWriter outIvSweep = null, outTmaxT = null, outN6 = null;
    double[][] s5_300 = null;   // 300K natijasi N1/N3/N4/N5 chizmalar uchun saqlanadi
    try {
      outIvSweep = new PrintWriter(new FileWriter(ivSweepCsv));
      outIvSweep.println("T_amb_K,t_s,V_V,I_A");
      outTmaxT = new PrintWriter(new FileWriter(tmaxTCsv));
      outTmaxT.println("T_amb_K,t_s,Tmax_K");
      outN6 = new PrintWriter(new FileWriter(n6Csv));
      outN6.println("T_amb_K,R_OFF_ohm,R_ON_ohm,V_SET_V,V_RESET_V,R_OFF_over_R_ON");

      for (int k = 0; k < tambSweep.length; k++) {
        double tamb = tambSweep[k];
        model.param().set("T_amb", String.format(Locale.US, "%.4g[K]", tamb));
        model.param().set("xs", "0");
        model.study("std6").run();

        if (k == 0) {
          // dset6 birinchi marta shu yerda paydo bo'ladi (S1/S2/S3 dagi dset1/dset2/dset3/dset4/dset5
          // naqshiga o'xshab).   // TEKSHIRILSIN: std6 -> dset6
          model.result().numerical().create("gev_S5", "EvalGlobal");
          model.result().numerical("gev_S5").label("S5: t, V, I, x, T_max (T_amb sweep)");
          model.result().numerical("gev_S5").set("data", "dset6");
          model.result().numerical("gev_S5").set("expr", new String[]{"t", "V_wave", "ec.I0_1", "xode", "maxop_T(T)"});
          model.result().numerical("gev_S5").set("unit", new String[]{"s", "V", "A", "1", "K"});
        }

        double[][] s5 = model.result().numerical("gev_S5").getReal();
        int n5 = (s5.length > 0) ? s5[0].length : 0;

        double vSet = Double.NaN, vReset = Double.NaN;
        int setIdxBefore = -1;
        for (int i = 1; i < n5; i++) {
          double xPrev = s5[3][i - 1], xCur = s5[3][i];
          if (Double.isNaN(vSet) && xPrev < 0.5 && xCur >= 0.5) {
            double frac = (0.5 - xPrev) / (xCur - xPrev);
            vSet = s5[1][i - 1] + frac * (s5[1][i] - s5[1][i - 1]);
            setIdxBefore = i - 1;
          }
          if (Double.isNaN(vReset) && xPrev > 0.5 && xCur <= 0.5) {
            double frac = (xPrev - 0.5) / (xPrev - xCur);
            vReset = s5[1][i - 1] + frac * (s5[1][i] - s5[1][i - 1]);
          }
        }

        for (int i = 0; i < n5; i++) {
          outIvSweep.println(String.format(Locale.US, "%.4g,%.6e,%.6e,%.6e", tamb, s5[0][i], s5[1][i], s5[2][i]));
          outTmaxT.println(String.format(Locale.US, "%.4g,%.6e,%.6e", tamb, s5[0][i], s5[4][i]));
        }
        // R_OFF/R_ON: bu modelda sig_off/sig_on T_amb ga bog'liq EMAS (faqat kinetika Arrhenius
        // orqali T ga bog'liq), shuning uchun x=0/x=1 holatlaridagi qarshilik S1 dan olingan doimiy
        // qiymatlar bilan bir xil (R_OFF=99.99 kOhm, R_ON=6.99 kOhm barcha T_amb uchun).
        double rOffConst = 9.999e4, rOnConst = 6.989e3;   // S1 NATIJA bilan mos (yuqorida hisoblangan)
        outN6.println(String.format(Locale.US, "%.4g,%.6e,%.6e,%s,%s,%.4g",
            tamb, rOffConst, rOnConst,
            Double.isNaN(vSet) ? "NaN" : String.format(Locale.US, "%.4g", vSet),
            Double.isNaN(vReset) ? "NaN" : String.format(Locale.US, "%.4g", vReset),
            rOffConst / rOnConst));
        System.out.println(String.format(Locale.US,
            "S5 SWEEP: T_amb=%.0fK  V_SET=%s V  V_RESET=%s V",
            tamb, Double.isNaN(vSet) ? "TOPILMADI" : String.format(Locale.US, "%.4g", vSet),
            Double.isNaN(vReset) ? "TOPILMADI" : String.format(Locale.US, "%.4g", vReset)));

        if (k == 0) {
          s5_300 = s5;   // 300K natijasini saqlab qolamiz

          // ---- N1: I-V egri chizig'i (chiziqli va log|I|) ----
          try {
            model.result().create("pg_N1_lin", "PlotGroup1D");
            model.result("pg_N1_lin").label("N1: I-V (chiziqli, T_amb=300K)");
            model.result("pg_N1_lin").set("data", "dset6");
            model.result("pg_N1_lin").create("g1", "Global");
            model.result("pg_N1_lin").feature("g1").set("expr", new String[]{"ec.I0_1*1e3"});
            model.result("pg_N1_lin").feature("g1").set("xdata", "expr");
            model.result("pg_N1_lin").feature("g1").set("xdataexpr", "V_wave");
            model.result().export().create("exp_N1_lin", "Image");
            model.result().export("exp_N1_lin").set("plotgroup", "pg_N1_lin");
            model.result().export("exp_N1_lin").set("filename", "N1_iv_linear.png");
            model.result().export("exp_N1_lin").run();
          } catch (Exception ex) {
            System.out.println("XATO (N1 chiziqli): " + ex.getMessage());
          }
          try {
            model.result().create("pg_N1_log", "PlotGroup1D");
            model.result("pg_N1_log").label("N1: log10|I| (T_amb=300K)");
            model.result("pg_N1_log").set("data", "dset6");
            model.result("pg_N1_log").create("g1", "Global");
            model.result("pg_N1_log").feature("g1").set("expr", new String[]{"log10(abs(ec.I0_1)/1[A]+1e-15)"});
            model.result("pg_N1_log").feature("g1").set("xdata", "expr");
            model.result("pg_N1_log").feature("g1").set("xdataexpr", "V_wave");
            model.result().export().create("exp_N1_log", "Image");
            model.result().export("exp_N1_log").set("plotgroup", "pg_N1_log");
            model.result().export("exp_N1_log").set("filename", "N1_iv_log.png");
            model.result().export("exp_N1_log").run();
          } catch (Exception ex) {
            System.out.println("XATO (N1 log): " + ex.getMessage());
          }

          // ---- N3: x(t), V(t), I(t) - alohida uchta PNG ----
          try {
            model.result().create("pg_N3_x", "PlotGroup1D");
            model.result("pg_N3_x").label("N3: x(t)");
            model.result("pg_N3_x").set("data", "dset6");
            model.result("pg_N3_x").create("g1", "Global");
            model.result("pg_N3_x").feature("g1").set("expr", new String[]{"xode"});
            model.result().export().create("exp_N3_x", "Image");
            model.result().export("exp_N3_x").set("plotgroup", "pg_N3_x");
            model.result().export("exp_N3_x").set("filename", "N3_x_t.png");
            model.result().export("exp_N3_x").run();
          } catch (Exception ex) {
            System.out.println("XATO (N3 x(t)): " + ex.getMessage());
          }
          try {
            model.result().create("pg_N3_V", "PlotGroup1D");
            model.result("pg_N3_V").label("N3: V(t)");
            model.result("pg_N3_V").set("data", "dset6");
            model.result("pg_N3_V").create("g1", "Global");
            model.result("pg_N3_V").feature("g1").set("expr", new String[]{"V_wave"});
            model.result().export().create("exp_N3_V", "Image");
            model.result().export("exp_N3_V").set("plotgroup", "pg_N3_V");
            model.result().export("exp_N3_V").set("filename", "N3_V_t.png");
            model.result().export("exp_N3_V").run();
          } catch (Exception ex) {
            System.out.println("XATO (N3 V(t)): " + ex.getMessage());
          }
          try {
            model.result().create("pg_N3_I", "PlotGroup1D");
            model.result("pg_N3_I").label("N3: I(t)");
            model.result("pg_N3_I").set("data", "dset6");
            model.result("pg_N3_I").create("g1", "Global");
            model.result("pg_N3_I").feature("g1").set("expr", new String[]{"ec.I0_1*1e3"});
            model.result().export().create("exp_N3_I", "Image");
            model.result().export("exp_N3_I").set("plotgroup", "pg_N3_I");
            model.result().export("exp_N3_I").set("filename", "N3_I_t.png");
            model.result().export("exp_N3_I").run();
          } catch (Exception ex) {
            System.out.println("XATO (N3 I(t)): " + ex.getMessage());
          }

          // ---- N4: SET paytidagi T xaritasi + T_max(t) (Tm_GT, Tm_ST chiziqlari bilan) ----
          // TEKSHIRILSIN: "sol6" (std6 -> 6-tadqiqot -> sol6 naqshi) va "looplevel" (vaqt indeksini
          // tanlash uchun, 1-asosli) - dset1..dset5 naqshiga o'xshab taxmin qilindi.
          try {
            if (setIdxBefore >= 0) {
              model.result().dataset().create("dset_SET", "Solution");
              model.result().dataset("dset_SET").set("solution", "sol6");
              model.result().dataset("dset_SET").set("looplevel", new int[]{setIdxBefore + 2});
              model.result().create("pg_N4_map", "PlotGroup2D");
              model.result("pg_N4_map").label("N4: T xaritasi (SET paytida)");
              model.result("pg_N4_map").set("data", "dset_SET");
              model.result("pg_N4_map").create("surf1", "Surface");
              model.result("pg_N4_map").feature("surf1").set("expr", "T");
              model.result().export().create("exp_N4_map", "Image");
              model.result().export("exp_N4_map").set("plotgroup", "pg_N4_map");
              model.result().export("exp_N4_map").set("filename", "N4_Tmap_SET.png");
              model.result().export("exp_N4_map").run();
            }
          } catch (Exception ex) {
            System.out.println("XATO (N4 T xaritasi): " + ex.getMessage());
          }
          try {
            model.result().create("pg_N4_Tmax", "PlotGroup1D");
            model.result("pg_N4_Tmax").label("N4: T_max(t) va erish haroratlari");
            model.result("pg_N4_Tmax").set("data", "dset6");
            model.result("pg_N4_Tmax").create("g1", "Global");
            model.result("pg_N4_Tmax").feature("g1").set("expr", new String[]{"maxop_T(T)", "Tm_ST", "Tm_GT"});
            model.result("pg_N4_Tmax").feature("g1").set("legend", true);
            model.result().export().create("exp_N4_Tmax", "Image");
            model.result().export("exp_N4_Tmax").set("plotgroup", "pg_N4_Tmax");
            model.result().export("exp_N4_Tmax").set("filename", "N4_Tmax_t.png");
            model.result().export("exp_N4_Tmax").run();
          } catch (Exception ex) {
            System.out.println("XATO (N4 Tmax(t)): " + ex.getMessage());
          }

          // ---- N5: SET dan oldin/keyin |E| va |J| xaritalari ----
          try {
            if (setIdxBefore >= 1) {
              model.result().dataset().create("dset_before", "Solution");
              model.result().dataset("dset_before").set("solution", "sol6");
              model.result().dataset("dset_before").set("looplevel", new int[]{setIdxBefore + 1});
              model.result().dataset().create("dset_after", "Solution");
              model.result().dataset("dset_after").set("solution", "sol6");
              model.result().dataset("dset_after").set("looplevel", new int[]{setIdxBefore + 2});

              String[][] n5jobs = {
                  {"pg_N5_Ebefore", "dset_before", "ec.normE", "N5_E_before.png", "N5: |E| SET dan oldin"},
                  {"pg_N5_Eafter", "dset_after", "ec.normE", "N5_E_after.png", "N5: |E| SET dan keyin"},
                  {"pg_N5_Jbefore", "dset_before", "ec.normJ", "N5_J_before.png", "N5: |J| SET dan oldin"},
                  {"pg_N5_Jafter", "dset_after", "ec.normJ", "N5_J_after.png", "N5: |J| SET dan keyin"}
              };
              for (String[] job : n5jobs) {
                model.result().create(job[0], "PlotGroup2D");
                model.result(job[0]).label(job[4]);
                model.result(job[0]).set("data", job[1]);
                model.result(job[0]).create("surf1", "Surface");
                model.result(job[0]).feature("surf1").set("expr", job[2]);
                model.result().export().create("exp_" + job[0], "Image");
                model.result().export("exp_" + job[0]).set("plotgroup", job[0]);
                model.result().export("exp_" + job[0]).set("filename", job[3]);
                model.result().export("exp_" + job[0]).run();
              }
            }
          } catch (Exception ex) {
            System.out.println("XATO (N5 E/J xaritalari): " + ex.getMessage());
          }
        }
      }
    } catch (IOException ex) {
      System.out.println("XATO (CSV T_amb sweep): " + ex.getMessage());
    } finally {
      if (outIvSweep != null) outIvSweep.close();
      if (outTmaxT != null) outTmaxT.close();
      if (outN6 != null) outN6.close();
      model.param().set("T_amb", "300[K]");
      model.param().set("V_app", "V_read");
      model.param().set("xs", "0");
    }

    try {
      model.save("Model1_Vertical.mph");
    } catch (IOException ex) {
      throw new RuntimeException("MPH faylni saqlab bo'lmadi", ex);
    }
    System.out.println("TAYYOR.");
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
