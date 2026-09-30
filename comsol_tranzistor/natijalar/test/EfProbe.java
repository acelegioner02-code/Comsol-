import com.comsol.model.*;
import com.comsol.model.util.*;
import com.comsol.model.physics.*;
import java.util.*;

/* MoS2 domenining eng soddalashtirilgan varianti: Equilibrium yechib, Efn/Ec/Ev/EFS kabi
 * o'zgaruvchilarni sinab ko'ramiz - qaysi nom haqiqatda mavjud va qaysi birlikda. */
public class EfProbe {
  public static void main(String[] args) {
    try {
      Model model = ModelUtil.create("Model");
      model.component().create("comp1", true);
      GeomSequence g = model.component("comp1").geom().create("geom1", 2);
      g.lengthUnit("um");
      g.create("r1", "Rectangle").set("pos", new String[]{"0","0"}).set("size", new String[]{"5","0.02"});
      g.run();

      model.param().set("Eg_mos", "1.23[V]");
      model.param().set("chi_mos", "4.0[V]");
      model.param().set("Nd_mos", "1e17[1/cm^3]");

      Physics semi = model.component("comp1").physics().create("semi", "Semiconductor", "geom1");
      semi.prop("d").set("d", "12.8[um]");
      PhysicsFeature smm1 = semi.feature("smm1");
      smm1.set("Eg0_mat", "userdef"); smm1.set("Eg0", "Eg_mos");
      smm1.set("chi0_mat", "userdef"); smm1.set("chi0", "chi_mos");
      smm1.set("epsilonr_mat", "userdef"); smm1.set("epsilonr", "7");
      smm1.set("Nc_mat", "userdef"); smm1.set("Nc", "1e19[1/cm^3]");
      smm1.set("Nv_mat", "userdef"); smm1.set("Nv", "1e19[1/cm^3]");
      smm1.set("mun_mat", "userdef"); smm1.set("mun", "30[cm^2/(V*s)]");
      smm1.set("mup_mat", "userdef"); smm1.set("mup", "10[cm^2/(V*s)]");

      PhysicsFeature adm1 = semi.create("adm1", "AnalyticDopingModel", 2);
      adm1.set("impurityType", "donor");
      adm1.set("NDc", "Nd_mos");
      adm1.set("impurityDistribution", "box");
      adm1.set("BaseOrCenter", "corner");
      adm1.set("rb", new String[]{"-1[um]", "-1[um]", "0[um]"});
      adm1.set("jwidth", "7[um]");
      adm1.set("jheight", "1000*0.02[um]");
      adm1.set("JunctionOrLength", "decay_length");
      adm1.set("ls", "1000*0.02[um]");

      model.component("comp1").mesh().create("mesh1");
      model.component("comp1").mesh("mesh1").create("tri1", "FreeTri");
      model.component("comp1").mesh("mesh1").run();

      model.study().create("std1");
      model.study("std1").create("eq", "SemiconductorEquilibrium");
      model.study("std1").run();
      System.out.println("Equilibrium OK");

      String[] candidates = {
        "semi.Efn", "semi.Efp", "semi.Ec", "semi.Ev", "semi.Ei", "semi.V",
        "semi.EFn", "semi.EFp", "semi.EFS", "semi.Emid",
        "semi.phin", "semi.phip"
      };
      model.result().numerical().create("pev1", "EvalPoint");
      model.result().numerical("pev1").set("data", "dset1");
      model.result().numerical("pev1").selection().set(new int[]{1}); // a point, will adjust if fails
      for (String c : candidates) {
        try {
          model.result().numerical("pev1").set("expr", new String[]{c});
          double[][] v = model.result().numerical("pev1").getReal();
          System.out.println(c + " = " + Arrays.deepToString(v));
        } catch (Exception e) {
          System.out.println(c + " FAIL: " + e.getMessage());
        }
      }
    } catch (Exception e) {
      System.out.println("FATAL: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
