"""
GEOMETRIYA.md dagi koordinatalarni chizadi. COMSOL'da qurilgan geometriya bilan solishtirish uchun.
Uch ko'rinish: (a) to'liq, masshtab 1:1; (b) y o'qi cho'zilgan; (c) source kontakt qirrasi yaqindan.

    python geometry_preview.py      ->  geometry_preview.png
"""
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Polygon, Rectangle

# ---- GEOMETRIYA.md, 2-jadval (um) ----
L, L_ov, L_pad = 5.0, 1.5, 2.0
t_mos, c, t_Au = 0.020, 0.001, 0.060
t_ox, t_Si, t_w = 0.285, 0.5, 0.003
H_air = 2.0
xS0 = -L_ov - L_pad
xD1 = L - xS0
DOMES = [(0.30 * L, 0.30 * L + 0.8, 0.40), (0.68 * L, 0.32 * L + 0.8, 0.55)]  # (xc, a, b), markaz y = t_mos


def cr_src():
    return [(xS0, 0), (-L_ov, 0), (-L_ov, t_mos), (0, t_mos), (0, t_mos + c),
            (-L_ov - c, t_mos + c), (-L_ov - c, c), (xS0, c)]


def au_src():
    return [(xS0, c), (-L_ov - c, c), (-L_ov - c, t_mos + c), (0, t_mos + c), (0, t_mos + c + t_Au),
            (-L_ov - c - t_Au, t_mos + c + t_Au), (-L_ov - c - t_Au, c + t_Au), (xS0, c + t_Au)]


def mirror(poly):
    return [(L - x, y) for x, y in poly]


def f4_outline(n=400):
    """Gumbazlar birlashmasining yuqori chegarasi (y >= t_mos)."""
    xs = np.linspace(min(xc - a for xc, a, b in DOMES), max(xc + a for xc, a, b in DOMES), n)
    ys = np.full_like(xs, t_mos)
    for xc, a, b in DOMES:
        inside = np.abs(xs - xc) < a
        ys[inside] = np.maximum(ys[inside], t_mos + b * np.sqrt(1 - ((xs[inside] - xc) / a) ** 2))
    return list(zip(xs, ys)) + [(xs[-1], t_mos), (xs[0], t_mos)]


def draw(ax):
    ax.add_patch(Rectangle((xS0, -t_ox - t_Si), xD1 - xS0, t_Si, fc="#333333", label="p++ Si (gate)"))
    ax.add_patch(Rectangle((xS0, -t_ox), xD1 - xS0, t_ox, fc="#b3f5c6", label="SiO2 285 nm"))
    ax.add_patch(Polygon(f4_outline(), fc="#d40000", alpha=0.85, label="F4TCNQ"))
    ax.add_patch(Rectangle((-L_ov, 0), L + 2 * L_ov, t_mos, fc="#ff9900", label="MoS2"))
    ax.add_patch(Rectangle((0, t_mos), L, t_w, fc="#1f77ff", label="suv plyonkasi"))
    for p, lab in [(au_src(), "Au"), (mirror(au_src()), None)]:
        ax.add_patch(Polygon(p, fc="#d4c600", ec="k", lw=0.3, label=lab))
    for p, lab in [(cr_src(), "Cr 1 nm"), (mirror(cr_src()), None)]:
        ax.add_patch(Polygon(p, fc="#777777", label=lab))


fig, axs = plt.subplots(3, 1, figsize=(11, 12))
for ax, (xl, yl, aspect, title) in zip(axs, [
        ((xS0, xD1), (-t_ox - t_Si, H_air), "equal", "(a) to'liq, 1:1 masshtab: MoS2 chiziq bo'lib ko'rinadi"),
        ((xS0, xD1), (-t_ox - 0.05, 0.7), "auto", "(b) y o'qi cho'zilgan"),
        ((-L_ov - 0.15, 0.25), (-0.03, 0.12), "auto", "(c) source kontakt qirrasi (Cr, MoS2 uchi, suv)")]):
    draw(ax)
    ax.set_xlim(*xl); ax.set_ylim(*yl); ax.set_aspect(aspect)
    ax.axhline(H_air, color="gray", ls=":", lw=0.8)
    ax.set_xlabel("x (um)"); ax.set_ylabel("y (um)"); ax.set_title(title)
axs[1].legend(loc="upper right", fontsize=8, ncol=4)
fig.tight_layout()
fig.savefig("geometry_preview.png", dpi=130)
print("geometry_preview.png saqlandi")
