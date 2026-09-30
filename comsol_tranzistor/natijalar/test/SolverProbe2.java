import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

public class SolverProbe2 {
  public static void main(String[] args) {
    try {
      Model model = ModelUtil.load("d1", "C:\\comsol_tranzistor\\ish\\model\\MoS2Fet_Model.mph");
      SolverFeature fc1 = model.sol("sol1").feature("s1").feature("fc1");
      System.out.println("all fc1 props+values:");
      for (String p : fc1.properties()) {
        try { System.out.println("  " + p + " = " + fc1.getString(p)); } catch (Exception e) {
          try { System.out.println("  " + p + " (bool) = " + fc1.getBoolean(p)); } catch (Exception e2) {}
        }
      }
      try { fc1.set("reserrfact", "1e6"); System.out.println("reserrfact=1e6 OK, readback=" + fc1.getString("reserrfact")); }
      catch (Exception e) { System.out.println("reserrfact FAIL: " + e.getMessage()); }
      try { fc1.set("maxiter", "400"); System.out.println("maxiter=400 OK, readback=" + fc1.getString("maxiter")); }
      catch (Exception e) { System.out.println("maxiter FAIL: " + e.getMessage()); }
    } catch (Exception e) {
      System.out.println("FATAL: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
