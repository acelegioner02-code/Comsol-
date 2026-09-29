"""
MoS2 / F4TCNQ FET: drenaj kuchlanishi ta'sirida V_on siljishi uchun kompakt (kalibrlash) modeli.

Maqola: H. Kii, R. Nouchi, ACS Appl. Electron. Mater. 2025, 7, 5282-5289.
        doi:10.1021/acsaelm.5c00627

Bu fayl COMSOL'dan OLDIN ishlatiladi. Maqsadi:
  1) maqoladagi raqamlarni (V_on(V_D), to'g'rilash koeffitsiyenti, gisterezis) bitta
     fizik rasm bilan tushuntirish mumkinligini tekshirish;
  2) COMSOL modeliga kiritiladigan parametrlarni (beta, tau, t_hold, Phi_B) topish.

Fizik rasm (maqolaning 7-rasmi):
  - n-tip Schottky-barerli tranzistor. Oqim injeksiya qiluvchi kontakt bareri bilan cheklanadi:
      V_D > 0 da -> manba (S) bareri,  V_D < 0 da -> drenaj (D) bareri.
  - Suvdagi ionlar (H3O+ manbaga, OH- drenajga) elektrod sirtida qo'sh qatlam hosil qiladi:
      Phi_S = Phi_0 - x,   Phi_D = Phi_0 + x         (x, eV)
  - x sekin o'zgaradi (ionlar sekin):  dx/dt = (beta*V_D - x)/tau
  - Subporog sohada kontakt yonidagi zona egilishi psi = (V_G - V_FB)/m,  m = SS/(kT ln10).
    Shuning uchun bareri x ga pasaysa, V_on m*x ga siljiydi:  dV_on/dV_D = -m*beta.

Ishga tushirish:  python3 compact_model.py      (numpy, scipy, matplotlib kerak)
Natija: compact_model_results.png va konsolga jadval.
"""

import numpy as np
from scipy.optimize import brentq, least_squares

KT = 0.02585            # eV, 300 K
LN10 = np.log(10.0)
COX = 12.1e-9 * 1e4     # F/m^2  (12.1 nF/cm^2, 285 nm SiO2)
L = 5e-6                # m
I_ON_CRIT = 1e-9        # A, maqolada V_on = V_G(I_D = 1 nA)
T_HOLD = 0.5            # -1 V da ushlab turish vaqti (sweep vaqti birligida), taxmin

VD_LIST = np.array([0.2, 0.4, 0.6, 0.8, 1.0])

# --- Maqola rasmlaridan o'qilgan qiymatlar (taxminiy raqamlashtirish, +-1..2 V) ---
# Im1: |I_D| at V_D = -1 V, V_G = 80 V, oldinga sweep boshida (2c, 4c, S2 rasmlar).
# ratio = |I(+1)|/|I(-1)| (oldinga sweep), hyst = (|I(-1)|_oldinga - |I(-1)|_qaytish)/|I(-1)|_oldinga (matndan).
# SS: subporog qiyaligi, rasmlarning V_on atrofidagi qismidan ko'z bilan baholangan (V/dek).
DATA = {
    "Fig2_bare": dict(Von=[-68.0, -69.5, -70.0, -70.7, -71.0], SS=5.0,  mu=34.3, W=11.9e-6,
                      paper_slope=None, ratio=1.0,  hyst=0.07, Im1=48e-6),
    "Fig2_F4TCNQ": dict(Von=[-25.0, -34.0, -49.5, -56.0, -62.0], SS=12.0, mu=6.9,  W=11.9e-6,
                        paper_slope=46.3, ratio=0.37, hyst=0.32, Im1=13e-6),
    "Fig4_lowRH": dict(Von=[-51.0, -52.3, -54.0, -54.7, -55.0], SS=7.0,  mu=1.4,  W=12.8e-6,
                       paper_slope=5.3,  ratio=0.83, hyst=0.13, Im1=9.5e-6),
    "Fig4_highRH": dict(Von=[-4.0, -22.0, -36.0, -61.0, -78.0], SS=25.0, mu=0.8,  W=12.8e-6,
                        paper_slope=92.5, ratio=0.03, hyst=0.87, Im1=1.6e-6),
}


def softplus(z, w):
    return w * np.logaddexp(0.0, z / w)


class Device:
    """Bitta qurilma: V00 (x=0 dagi V_on), m, beta, mu, W."""

    def __init__(self, V00, SS, beta, mu_cm2, W, Vt=-100.0, Ic0=1.0, nc=1.0):
        self.V00 = V00
        self.SS = SS
        self.m = SS / (KT * LN10)
        self.beta = beta                 # eV/V
        self.mu = mu_cm2 * 1e-4          # m^2/Vs
        self.W = W
        # Kanalning o'zi "normally-on" (maqola, 6-bet): uning ostonasi V_on dan ancha past.
        # Subporog sohani kanal emas, kontakt bareri belgilaydi.
        self.Vt = Vt
        # On-holatda kontakt yonidagi zona egilishi kanal zaryadi bilan "qotadi", shuning uchun
        # kontakt oqimi faqat barerga bog'liq:  I_c = Ic0*exp(s*x/(nc*kT))   (Schottky diod ketma-ket)
        self.Ic0 = Ic0
        self.nc = nc

    def current(self, VG, VD, x):
        """|I_D| (A) berilgan ion holati x (eV) da. Belgisi V_D belgisiga teng."""
        if VD == 0.0:
            return 0.0
        s = np.sign(VD)
        # injeksiya qiluvchi kontakt bareri: Phi_0 - s*x  ->  V_on siljishi -m*s*x
        Von_inj = self.V00 - self.m * s * x
        log_inj = np.log(I_ON_CRIT) + (VG - Von_inj) * LN10 / self.SS
        I_inj = np.exp(min(log_inj, 50.0)) * (1.0 - np.exp(-abs(VD) / KT))
        I_c = self.Ic0 * np.exp(np.clip(s * x / (self.nc * KT), -50, 50))
        I_ch = self.mu * COX * self.W / L * softplus(VG - self.Vt, 5.0) * abs(VD) + 1e-15
        return s / (1.0 / I_inj + 1.0 / I_c + 1.0 / I_ch)

    def von(self, VD, x):
        f = lambda vg: np.log(abs(self.current(vg, VD, x))) - np.log(I_ON_CRIT)
        return brentq(f, -300.0, 300.0)

    def output_sweep(self, VG, tau, t_hold, n=201):
        """Maqoladagi tartib: -1 V da ushlab turish, keyin -1 -> 0 -> +1 -> 0 -> -1 V.
        Vaqt birligi: butun sweep davomiyligi = 1. tau va t_hold shu birlikda."""
        v = np.concatenate([np.linspace(-1, 1, n), np.linspace(1, -1, n)[1:]])
        dt = 1.0 / (len(v) - 1)
        x = -self.beta * (1.0 - np.exp(-t_hold / tau))       # ushlab turish oxiri (x(0)=0 dan)
        xs, I = [], []
        for vd in v:
            # aniq eksponensial integrator (turg'un)
            x = self.beta * vd + (x - self.beta * vd) * np.exp(-dt / tau)
            xs.append(x)
            I.append(self.current(VG, vd, x))
        return v, np.array(I), np.array(xs)


def metrics(v, I):
    n = (len(v) + 1) // 2
    i_start, i_end = 0, len(v) - 1                      # -1 V: oldinga va qaytish
    i_plus = np.argmin(abs(v[:n] - 1.0))                # +1 V (oldinga)
    ratio = abs(I[i_plus]) / abs(I[i_start])
    hyst = (abs(I[i_start]) - abs(I[i_end])) / abs(I[i_start])
    return ratio, hyst


def fit_transfer(name):
    d = DATA[name]
    von = np.array(d["Von"])
    slope, icpt = np.polyfit(VD_LIST, von, 1)
    m = d["SS"] / (KT * LN10)
    beta = -slope / m
    return icpt, slope, beta, m


def main(plot=True):
    print("=== 1. Transfer: V_on(V_D) chiziqli moslash va beta = -slope/m ===")
    print(f"{'qurilma':14s} {'slope,V/V':>10s} {'maqola':>8s} {'V00,V':>7s} {'m':>6s} {'beta,eV/V':>10s} {'dPhi(1V),eV':>12s}")
    devs = {}
    for name, d in DATA.items():
        V00, slope, beta, m = fit_transfer(name)
        devs[name] = Device(V00, d["SS"], max(beta, 0.0), d["mu"], d["W"])
        ps = d["paper_slope"] if d["paper_slope"] else float("nan")
        print(f"{name:14s} {slope:10.1f} {ps:8.1f} {V00:7.1f} {m:6.0f} {beta:10.3f} {beta*1.0:12.3f}")

    print(f"\n=== 2. Chiqish xarakteristikasi (V_G = 80 V): har qurilma uchun Ic0, mu_ch, tau moslash (nc = 1) ===")
    print(f"    (ushlab turish vaqti t_hold = {T_HOLD} x sweep vaqti deb qabul qilingan - maqolada berilmagan)")
    print(f"{'qurilma':14s} {'Ic0,uA':>8s} {'mu_ch':>5s} {'tau/t_sw':>9s} | {'|I(-1)|,uA':>10s} {'maq.':>5s} | "
          f"{'I(+1)/I(-1)':>11s} {'maq.':>5s} | {'gist.':>6s} {'maq.':>5s}")
    sweeps, fits = {}, {}
    for name, dev in devs.items():
        tgt = DATA[name]

        def resid(p, dev=dev, tgt=tgt):
            dev.Ic0, dev.mu, tau = np.exp(p)
            v, I, _ = dev.output_sweep(80.0, tau, T_HOLD)
            r, h = metrics(v, I)
            return [np.log(abs(I[0]) / tgt["Im1"]), np.log(r / tgt["ratio"]), (h - tgt["hyst"]) * 10]

        best = None
        for tau0 in [0.1, 0.5, 2.0, 10.0]:
            sol = least_squares(resid, x0=np.log([tgt["Im1"] * 2, dev.mu * 2, tau0]),
                                bounds=(np.log([1e-9, dev.mu / 20, 1e-3]), np.log([1e-2, dev.mu * 50, 1e3])))
            if best is None or sol.cost < best.cost:
                best = sol
        dev.Ic0, dev.mu, tau = np.exp(best.x)
        v, I, xs = dev.output_sweep(80.0, tau, T_HOLD)
        r, h = metrics(v, I)
        sweeps[name] = (v, I, xs)
        fits[name] = tau
        print(f"{name:14s} {dev.Ic0*1e6:8.2f} {dev.mu*1e4:5.1f} {tau:9.3f} | {abs(I[0])*1e6:10.2f} {tgt['Im1']*1e6:5.1f} | "
              f"{r:11.2f} {tgt['ratio']:5.2f} | {h*100:5.0f}% {tgt['hyst']*100:4.0f}%")

    if not plot:
        return devs, fits
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    fig, ax = plt.subplots(2, 3, figsize=(15, 9))
    # (a) V_on(V_D)
    a = ax[0, 0]
    for name, c in zip(DATA, ["gray", "C0", "C2", "C3"]):
        a.plot(VD_LIST, DATA[name]["Von"], "o", color=c, label=name + " (maqola)")
        vd = np.linspace(0.2, 1.0, 30)
        a.plot(vd, [devs[name].von(x, devs[name].beta * x) for x in vd], "-", color=c)
    a.set_xlabel("V_D (V)"); a.set_ylabel("V_on (V)"); a.set_title("V_on(V_D): nuqta = maqola, chiziq = model")
    a.legend(fontsize=7)

    # (b),(c) transfer curves
    for a, name in [(ax[0, 1], "Fig2_F4TCNQ"), (ax[0, 2], "Fig4_highRH")]:
        vg = np.linspace(-80, 80, 161)
        for vd in VD_LIST:
            a.semilogy(vg, [abs(devs[name].current(g, vd, devs[name].beta * vd)) + 1e-11 for g in vg],
                       label=f"V_D={vd:.1f} V")
        a.set_ylim(1e-11, 1e-4); a.set_xlabel("V_G (V)"); a.set_ylabel("|I_D| (A)")
        a.set_title(f"Transfer (model): {name}"); a.legend(fontsize=7)

    # (d),(e) output sweeps
    for a, name in [(ax[1, 0], "Fig2_F4TCNQ"), (ax[1, 1], "Fig4_highRH")]:
        v, I, _ = sweeps[name]
        n = (len(v) + 1) // 2
        a.plot(v[:n], I[:n] * 1e6, "b-", label="oldinga (-1 -> +1)")
        a.plot(v[n - 1:], I[n - 1:] * 1e6, "r--", label="qaytish (+1 -> -1)")
        a.axhline(0, color="k", lw=0.5); a.axvline(0, color="k", lw=0.5)
        a.set_xlabel("V_D (V)"); a.set_ylabel("I_D (uA)"); a.set_title(f"Chiqish, V_G=80 V (model): {name}")
        a.legend(fontsize=7)

    # (f) ion state
    a = ax[1, 2]
    for name in ["Fig4_lowRH", "Fig4_highRH", "Fig2_F4TCNQ"]:
        v, I, xs = sweeps[name]
        a.plot(np.linspace(0, 1, len(xs)), xs * 1e3, label=name)
    a.set_xlabel("t / t_sweep"); a.set_ylabel("x = dPhi (meV)"); a.set_title("Ion holati x(t) (Phi_S = Phi0 - x)")
    a.legend(fontsize=7)
    fig.tight_layout()
    fig.savefig("compact_model_results.png", dpi=120)
    print("\n-> compact_model_results.png saqlandi")
    return devs, fits


if __name__ == "__main__":
    main()
