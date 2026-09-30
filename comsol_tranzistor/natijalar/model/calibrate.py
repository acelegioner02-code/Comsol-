"""
B4: kalibrlash. params.txt ni yozadi, comsolbatch (MoS2Fet.class) ni chaqiradi,
transfer.csv dan V_on(V_D) va subporog qiyaligini o'qib, maqola qiymatlariga (T1,T2,T3,T8)
moslashtiradi.

Ishga tushirish: pytools\python.bat calibrate.py

Tartib (qollanma 5.10, PROMPT.md B4):
  1) Dit -> SS (T8)
  2) Phi_B0, sigma_F4, Nd_mos -> V_on(0.2V) (T1 boshlang'ich nuqtasi)
  3) beta -> qiyalik dVon/dVD (T1, T2)
  4) past RH uchun beta, Phi_B0 alohida (T3)

Har bir urinish calib_log.csv ga yoziladi.
"""

import csv
import os
import subprocess
import sys
import time

import numpy as np

HERE = os.path.dirname(os.path.abspath(__file__))
COMSOLBATCH = r"C:\Program Files\COMSOL\COMSOL60\Multiphysics\bin\win64\comsolbatch.exe"
CLASSFILE = os.path.join(HERE, "MoS2Fet.class")
PARAMS_PATH = os.path.join(HERE, "params.txt")
TRANSFER_CSV = os.path.join(HERE, "transfer.csv")
LOG_CSV = os.path.join(HERE, "calib_log.csv")

# Maqola nishonlari (T1, T2, T3, T8) - Fig4_highRH va Fig4_lowRH, PROMPT.md 2-qism
TARGET = {
    "highRH": dict(Von=[-4.0, -22.0, -36.0, -61.0, -78.0], slope=92.5, SS=25.0),
    "lowRH":  dict(Von=None, slope=5.3, SS=None),  # V_on ~ -51..-55, SS taxminan 7-12 V/dek
}
VD_LIST = [0.2, 0.4, 0.6, 0.8, 1.0]

DEFAULT_PARAMS = {
    "mode": "transfer",
    "use_traps": "1",
    "use_f4": "1",
    "Dit": "3e13[1/(cm^2*eV)]",
    "Phi_B0": "0.25[V]",
    "sigma_F4": "-1e12[1/cm^2]*e_const",
    "beta": "0.223[V/V]",
    "Nd_mos": "1e17[1/cm^3]",
    "VD_list": ",".join(str(v) for v in VD_LIST),
}


def write_params(overrides):
    """overrides ichida VG_list (masalan '80,60,40,20,10,0,-10,-20') bo'lsa, to'liq
    VG_start/stop/step range o'rniga faqat shu nuqtalar hisoblanadi - bu kalibrlashni
    (V_on faqat bir necha nuqta orqali interpolatsiya qilinadi) 5-10 baravar tezlashtiradi.
    VG_list bo'lmasa, standart to'liq range ishlatiladi (VG_start/stop/step ham berilishi kerak)."""
    p = dict(DEFAULT_PARAMS)
    p.update(overrides)
    with open(PARAMS_PATH, "w") as f:
        for k, v in p.items():
            f.write(f"{k} = {v}\n")
    return p


def run_comsol(timeout=7200):
    """DIQQAT: mashinada faqat 7.8 GB RAM bor. Bu funksiyani chaqirishdan oldin boshqa
    hech qanday comsolbatch jarayoni ishlamayotganiga ishonch hosil qiling
    (masalan: `tasklist | findstr comsolbatch` yoki PowerShell Get-Process comsolbatch)."""
    t0 = time.time()
    result = subprocess.run(
        [COMSOLBATCH, "-inputfile", CLASSFILE],
        cwd=HERE, capture_output=True, text=True, timeout=timeout,
    )
    dt = time.time() - t0
    return result.returncode, result.stdout, result.stderr, dt


def vg_window_for_target(von_guess, span_below=15, span_above=45, n=9):
    """von_guess atrofida sparse VG nuqtalar ro'yxati: pastda V_on ni chegaralash uchun,
    yuqorida subporog qiyaligini (SS) baholash uchun yetarli oraliq."""
    lo, hi = von_guess - span_below, von_guess + span_above
    pts = np.linspace(hi, lo, n)  # yuqoridan pastga (on-holatdan boshlab, yaqinlashish uchun yaxshi)
    return ",".join(f"{v:.1f}" for v in pts)


def quick_von(vd, von_guess, overrides, n=9):
    """Bitta VD uchun sparse VG_list bilan tezkor V_on/SS baholash."""
    p = dict(overrides)
    p["VD_list"] = str(vd)
    p["VG_list"] = vg_window_for_target(von_guess, n=n)
    write_params(p)
    rc, out, err, dt = run_comsol()
    print(f"[quick_von] VD={vd} vaqt={dt:.0f}s rc={rc}")
    a = load_transfer()
    if len(a) == 0:
        print("  OGOHLANTIRISH: transfer.csv bo'sh")
        return None
    vg, idd = a[:, 1], a[:, 2]
    von = von_from_curve(vg, idd)
    ss = subthreshold_slope(vg, idd, von) if np.isfinite(von) else np.nan
    print(f"  Von={von:.1f} V, SS={ss:.1f} V/dek")
    return dict(von=von, ss=ss, vg=vg, idd=idd, dt=dt)


def load_transfer():
    rows = []
    with open(TRANSFER_CSV) as f:
        for line in f:
            line = line.strip()
            if not line or line[0].isalpha():
                continue
            parts = line.split(",")
            if len(parts) != 3 or "NaN" in parts[2]:
                continue
            rows.append([float(x) for x in parts])
    return np.array(rows)


def von_from_curve(vg, idd, crit=1e-9):
    order = np.argsort(vg)
    vg, lg = vg[order], np.log10(np.abs(idd[order]) + 1e-30)
    above = np.where(lg >= np.log10(crit))[0]
    if len(above) == 0 or above[0] == 0:
        return np.nan
    k = above[0]
    return vg[k - 1] + (np.log10(crit) - lg[k - 1]) * (vg[k] - vg[k - 1]) / (lg[k] - lg[k - 1])


def subthreshold_slope(vg, idd, von):
    # von dan 10-20 V yuqorida, exp o'sish sohasidan chiziqli moslash (log10|I| vs VG)
    order = np.argsort(vg)
    vg, idd = vg[order], idd[order]
    mask = (vg > von) & (vg < von + 20) & (np.abs(idd) > 1e-13)
    if mask.sum() < 3:
        return np.nan
    lg = np.log10(np.abs(idd[mask]))
    slope, _ = np.polyfit(vg[mask], lg, 1)
    return 1.0 / slope  # V/dek


def analyze():
    a = load_transfer()
    if len(a) == 0:
        return None
    vds = np.unique(np.round(a[:, 0], 6))
    vons, sss = [], []
    for vd in vds:
        sel = a[:, 0] == vd
        vg, idd = a[sel, 1], a[sel, 2]
        von = von_from_curve(vg, idd)
        vons.append(von)
        sss.append(subthreshold_slope(vg, idd, von))
    vons = np.array(vons)
    slope = np.polyfit(vds, vons, 1)[0] if np.all(np.isfinite(vons)) else np.nan
    return dict(vds=vds, vons=vons, sss=sss, slope=slope)


def log_attempt(step, params, result):
    exists = os.path.isfile(LOG_CSV)
    with open(LOG_CSV, "a", newline="") as f:
        w = csv.writer(f)
        if not exists:
            w.writerow(["step", "Dit", "Phi_B0", "sigma_F4", "beta", "Nd_mos",
                        "Von_0.2", "slope", "SS_mean"])
        w.writerow([step, params.get("Dit"), params.get("Phi_B0"), params.get("sigma_F4"),
                    params.get("beta"), params.get("Nd_mos"),
                    result["vons"][0] if result else "NaN",
                    result["slope"] if result else "NaN",
                    np.nanmean(result["sss"]) if result else "NaN"])


if __name__ == "__main__":
    print("calibrate.py - qo'lda ishga tushirish uchun tayyor skelet.")
    print("Har bir bosqichni alohida ishga tushirish tavsiya etiladi (avval Dit->SS, keyin ...).")
