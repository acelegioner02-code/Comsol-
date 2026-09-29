"""
COMSOL'dan eksport qilingan natijalardan maqoladagi raqamlarni hisoblaydi va maqola bilan solishtiradi.

1) Transfer (Stationary, VD va VG bo'yicha sweep). CSV ustunlari: VD, VG, ID  (V, V, A)
     python3 extract_metrics.py transfer transfer.csv  [--device Fig4_highRH]
   -> har bir VD uchun V_on = VG(|ID| = 1 nA), dV_on/dV_D qiyaligi.

2) Chiqish (Time Dependent, VD(t) uchburchak signal). CSV ustunlari: t, VD, ID  (s, V, A)
     python3 extract_metrics.py output output.csv  [--device Fig4_highRH]
   -> |I(+1)|/|I(-1)| (oldinga sweep) va (|I(-1)|_boshi - |I(-1)|_oxiri)/|I(-1)|_boshi.

COMSOL'da eksport: Results > Tables (yoki Export > Data), bo'luvchi vergul yoki bo'shliq,
'%' bilan boshlangan sarlavha qatorlari avtomatik tashlab yuboriladi.
"""

import argparse
import numpy as np

from compact_model import DATA, VD_LIST, I_ON_CRIT


def load(path):
    rows = []
    with open(path) as f:
        for line in f:
            line = line.strip()
            if not line or line[0] in "%#" or line[0].isalpha():
                continue
            rows.append([float(v) for v in line.replace(",", " ").split()])
    return np.array(rows)


def von_from_curve(vg, idd, crit=I_ON_CRIT):
    order = np.argsort(vg)
    vg, lg = vg[order], np.log10(np.abs(idd[order]) + 1e-30)
    above = np.where(lg >= np.log10(crit))[0]
    if len(above) == 0 or above[0] == 0:
        return np.nan
    k = above[0]
    return vg[k - 1] + (np.log10(crit) - lg[k - 1]) * (vg[k] - vg[k - 1]) / (lg[k] - lg[k - 1])


def transfer(path, device):
    a = load(path)
    vds = np.unique(np.round(a[:, 0], 6))
    vons = np.array([von_from_curve(a[a[:, 0] == vd, 1], a[a[:, 0] == vd, 2]) for vd in vds])
    slope = np.polyfit(vds, vons, 1)[0]
    print(f"{'VD, V':>6s} {'Von model, V':>13s}" + (f" {'Von maqola, V':>14s}" if device else ""))
    for vd, v in zip(vds, vons):
        ref = ""
        if device and np.any(np.isclose(VD_LIST, vd)):
            ref = f" {DATA[device]['Von'][int(np.argmin(abs(VD_LIST - vd)))]:14.1f}"
        print(f"{vd:6.2f} {v:13.1f}{ref}")
    print(f"dVon/dVD = {slope:.1f} V/V" + (f"   (maqola: {DATA[device]['paper_slope']})" if device else ""))


def output(path, device):
    a = load(path)
    t, vd, idd = a[:, 0], a[:, 1], a[:, 2]
    k_max = int(np.argmax(vd))                   # +1 V ga yetgan nuqta (oldinga sweep)
    first = int(np.argmin(abs(vd[:k_max + 1] + 1.0)))           # sweep boshidagi -1 V
    last = k_max + int(np.argmin(abs(vd[k_max:] + 1.0)))        # sweep oxiridagi -1 V
    ratio = abs(idd[k_max]) / abs(idd[first])
    hyst = (abs(idd[first]) - abs(idd[last])) / abs(idd[first])
    print(f"|I(-1)| boshi = {abs(idd[first])*1e6:.3f} uA, |I(+1)| = {abs(idd[k_max])*1e6:.3f} uA, "
          f"|I(-1)| oxiri = {abs(idd[last])*1e6:.3f} uA")
    print(f"|I(+1)|/|I(-1)| = {ratio:.2f}" + (f"   (maqola: {DATA[device]['ratio']})" if device else ""))
    print(f"gisterezis     = {hyst*100:.0f}%" + (f"   (maqola: {DATA[device]['hyst']*100:.0f}%)" if device else ""))


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("kind", choices=["transfer", "output"])
    p.add_argument("csv")
    p.add_argument("--device", choices=list(DATA), default=None)
    args = p.parse_args()
    (transfer if args.kind == "transfer" else output)(args.csv, args.device)
