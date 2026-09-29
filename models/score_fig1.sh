#!/bin/bash
# score_fig1.sh - iv_fig1_continuous.csv ni fig1_targets.csv bilan solishtirib,
# har bir nishon uchun |farq|<20% bo'lsa 1 ball beradi. Natijada jami ball chiqadi.
# Ishlatish: bash score_fig1.sh [iv_csv] [targets_csv]

IV=${1:-iv_fig1_continuous.csv}
TGT=${2:-fig1_targets.csv}

awk -F',' -v ivfile="$IV" '
BEGIN {
  # iv_fig1_continuous.csv ni xotiraga yuklash: panel,V,I,x
  while ((getline line < ivfile) > 0) {
    if (line ~ /^panel/) continue
    split(line, f, ",")
    n++
    P[n]=f[1]; V[n]=f[4]+0; I[n]=f[5]+0; X[n]=f[6]+0
  }
  close(ivfile)
}
NR==1{next}
{
  panel=$1; branch=$3; tv=$4+0; ti=$5+0; note=$6
  best=1e18; bi=-1
  for (k=1;k<=n;k++) {
    if (P[k]!=panel) continue
    isOn = (X[k]>=0.5)
    if (branch=="ON" && !isOn) continue
    if (branch=="OFF" && isOn) continue
    if (branch=="VOLATILE") continue  # alohida ko"rilmaydi (hozircha)
    d = V[k]-tv; if (d<0) d=-d
    if (d<best) { best=d; bi=k }
  }
  total++
  if (bi<0) {
    printf "[YO\x27Q] panel=%s branch=%s V=%.2f I_target=%.4fmA (%s) - MOS nuqta topilmadi\n", panel, branch, tv, ti, note
    next
  }
  simI_mA = I[bi]*1000.0
  if (ti == 0) {
    diffpct = (simI_mA<0?-simI_mA:simI_mA) * 1000  # 0 maqsad uchun mA->uA shkalada ko"rsatish
    ok = (diffpct < 20)  # <0.02mA farq bo"lsa OK deb hisoblanadi (juda kichik oqim)
    status = ok ? "OK" : "FARQ"
    if (ok) score++
    printf "[%s] panel=%s branch=%s V_target=%.2f (topilgan V=%.3f) I_target=0 I_model=%.5fmA (%s)\n", status, panel, branch, tv, V[bi], simI_mA, note
  } else {
    diffpct = (simI_mA-ti)/ti*100
    ad = diffpct<0?-diffpct:diffpct
    ok = (ad < 20)
    if (ok) score++
    printf "[%s] panel=%s branch=%s V_target=%.2f (topilgan V=%.3f) I_target=%.4fmA I_model=%.4fmA farq=%.1f%% (%s)\n", (ok?"OK":"FARQ"), panel, branch, tv, V[bi], ti, simI_mA, diffpct, note
  }
}
END {
  printf "\n=== JAMI: %d/%d nishon <20%% farq bilan mos (I_farq balli) ===\n", score, total
}
' "$TGT"
