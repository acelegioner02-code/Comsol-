"""R_ON_vs_pressure.csv faylidan R_ON(p) grafigini chizadi.

Ishlatish:  python plot_R_ON.py [R_ON_vs_pressure.csv]
Talab:      pip install matplotlib
"""
import csv
import sys

import matplotlib.pyplot as plt

path = sys.argv[1] if len(sys.argv) > 1 else "R_ON_vs_pressure.csv"
data = {}
with open(path, newline="") as f:
    for row in csv.DictReader(f):
        k = int(row["k_state"])
        data.setdefault(k, ([], [], []))
        data[k][0].append(float(row["p_ext_MPa"]))
        data[k][1].append(float(row["R_ON_Ohm"]))
        data[k][2].append(float(row["R_fil_Ohm"]))

labels = {1: "Xotira holati (k_state = 1)", 0: "Kalitlash holati (k_state = 0)"}
fig, ax = plt.subplots(figsize=(6, 4))
for k, (p, r, rf) in sorted(data.items(), reverse=True):
    line, = ax.semilogy(p, r, marker="o", label=labels.get(k, f"k_state = {k}") + ": R_ON")
    ax.semilogy(p, rf, linestyle="--", color=line.get_color(), label="  filamentning o'zi: R_fil")
    print(f"k_state={k}: R_ON(0)/R_ON(max) = {r[0] / r[-1]:.3g},  R_fil(0)/R_fil(max) = {rf[0] / rf[-1]:.3g}")
ax.set_xlabel("Tashqi bosim p [MPa]")
ax.set_ylabel("Qarshilik [Ohm]")
ax.grid(True, which="both", alpha=0.3)
ax.legend()
fig.tight_layout()
fig.savefig("R_ON_vs_pressure.png", dpi=150)
print("Saqlandi: R_ON_vs_pressure.png")
plt.show()
