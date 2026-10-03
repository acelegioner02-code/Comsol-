"""4-kun (C1-C6) B3 to'liq transfer natijasini chizadi. I_ON_CRIT (1 nA) chizig'i bilan
birga - V_on hech bir egri uchun yetilmaganini vizual tasdiqlaydi."""
import sys
sys.path.insert(0, r"C:\comsol_tranzistor\ish")
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from compact_model import I_ON_CRIT

a = np.loadtxt("natijalar/transfer_b3_full_d4.csv", delimiter=",", skiprows=1)
vds = np.unique(a[:, 0])

fig, ax = plt.subplots(figsize=(6, 4.5))
for vd in vds:
    m = a[:, 0] == vd
    vg = a[m, 1]
    idd = np.abs(a[m, 2])
    order = np.argsort(vg)
    ax.semilogy(vg[order], idd[order], marker="o", ms=3, label=f"VD={vd:.1f} V")

ax.axhline(I_ON_CRIT, color="k", ls="--", lw=1, label=f"1 nA (V_on mezoni)")
ax.set_xlabel("VG (V)")
ax.set_ylabel("|I_D| (A)")
ax.set_title("4-kun: D_it=3e13 to'liq yaqinlashdi, lekin V_on 1nA ga yetmaydi\n(tok ~5e-6..3e-5 A oralig'ida qoladi, kuchli Fermi-pinning tufayli)")
ax.legend(fontsize=8)
ax.grid(True, which="both", alpha=0.3)
fig.tight_layout()
fig.savefig("natijalar/fig4a_transfer_d4.png", dpi=150)
print("Saqlandi: natijalar/fig4a_transfer_d4.png")
print("ID oralig'i:", idd.min() if False else (a[:,2].min(), a[:,2].max()))
