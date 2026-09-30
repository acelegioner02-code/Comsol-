import com.comsol.model.*;
import com.comsol.model.util.*;
import java.util.*;

public class SolverProbe {
  public static void main(String[] args) {
    try {
      Model model = ModelUtil.load("d1", "C:\\comsol_tranzistor\\ish\\model\\MoS2Fet_Model.mph");
      String[] solTags = model.sol().tags();
      System.out.println("sol tags: " + Arrays.toString(solTags));
      for (String st : solTags) {
        dumpTree(model.sol(st), st);
      }
    } catch (Exception e) {
      System.out.println("FATAL: " + e.getMessage());
      e.printStackTrace();
    }
  }

  static void dumpTree(SolverSequence ss, String path) {
    try {
      String[] tags = ss.feature().tags();
      for (String t : tags) dumpFeature(ss.feature(t), path + "." + t);
    } catch (Exception e) {}
  }

  static void dumpFeature(SolverFeature sf, String path) {
    try {
      String type = sf.getType();
      System.out.println(path + " type=" + type);
      if (type.contains("Fully") || type.contains("Newton") || type.contains("Nonlinear") || type.contains("Stationary")) {
        String[] props = sf.properties();
        for (String p : props) {
          if (p.toLowerCase().contains("iter") || p.toLowerCase().contains("div") || p.toLowerCase().contains("tol") || p.toLowerCase().contains("term") || p.toLowerCase().contains("stag")) {
            try { System.out.println("    " + p + " = " + sf.getString(p)); } catch (Exception e) {}
          }
        }
      }
    } catch (Exception e) {}
    try {
      String[] sub = sf.feature().tags();
      for (String s : sub) dumpFeature(sf.feature(s), path + "." + s);
    } catch (Exception e) {}
  }
}
