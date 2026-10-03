# Fig.1 I–V grafiklarini COMSOL Results'da ko'rish

## Nega bu grafik `.mph` fayllarda yo'q

`Fig1_analog.png` va `Fig_pressure.png` **COMSOL ichida chizilmagan**. Ular quyidagicha olingan:

1. `Model2_FET_Fig1.java` bitta Time Dependent tadqiqotni (`std4`) **4 marta ketma-ket** ishga tushirgan, har panel uchun bir marta (Ugate = 0, −0.9, −1.1, 0 V).
   Har bir yangi hisob avvalgisining ustidan yozilgan. Shuning uchun `.mph` faylda **faqat oxirgi panel (d)** qolgan.
2. Har bir hisobdan keyin natija `iv_fig1_continuous.csv` fayliga yozilgan.
3. Rasm shu CSV fayldan **PowerShell skripti** (`models/plot_fig1.ps1`) bilan chizilgan. Havo rang chiziq OFF holat (x < 0.5), to'q sariq chiziq ON holat.
4. `Model2_FET_Fig1.mph` dagi `pg_VI` grafigi I–V emas: u **vaqt bo'yicha** V(t) va I(t) ni ko'rsatadi, faqat panel (d) uchun.
5. `Model3_Pressure` ham xuddi shunday: R(p) Java siklida hisoblangan, natija `Ron_p.csv` ga yozilgan, rasm `plot_pressure.ps1` bilan chizilgan. `.mph` da faqat oxirgi holat (p = 2 GPa) qolgan.

Bu fayllar `claude/trusting-noether-ksc9q6` branchidagi `models/` papkasida.

---

## 1-usul: barcha 4 panelni CSV'dan COMSOL'ga import qilish (qayta hisoblashsiz)

Bu papkadagi `panel_a_IV.csv` … `panel_d_IV.csv` fayllar `iv_fig1_continuous.csv` dan ajratib olingan.
Ustunlar: 1) V [V], 2) I [mA], 3) x (filament holati).

Har bir panel uchun:

1. `Model2_FET_Fig1.mph` ni oching (yoki yangi bo'sh model).
2. **Results → Tables** ustida o'ng tugma → **Table**.
3. Table sozlamalarida **Import** bo'limini toping → *Filename* = `panel_a_IV.csv` → **Import** tugmasi.
4. Hosil bo'lgan *Table 1* ustida o'ng tugma → **Table Graph**. Avtomatik ravishda yangi *1D Plot Group* yaratiladi.
5. *Table Graph* sozlamalari:
   - *x-axis data* = **Column 1** (V)
   - *Plot columns* = **Manual** → faqat **Column 2** (I, mA)
6. **Plot** tugmasini bosing.
7. 1D Plot Group sozlamalarida: *Title* = `(a) Ugate = 0`, *x label* = `Voltage [V]`, *y label* = `Current [mA]`.
   *Axis* bo'limida *x* oralig'i −5…5. *y* oralig'i: a, b uchun ±0.5; c uchun ±0.05; d uchun ±0.06.

Qolgan b, c, d panellar uchun 2–7 qadamlarni takrorlang.

Bosim grafigi uchun `Ron_p.csv` ni xuddi shunday import qiling (1-qator sarlavha, kerak bo'lsa uni o'chiring):
- *x* = **Column 1** (p, GPa), *y* = **Column 5** (R, Ω).
- *y* o'qini logarifmik qiling: *Axis → y-axis log scale*.

## 2-usul: panel (d) ni `.mph` ning o'zidan I–V ko'rinishida chizish

`Model2_FET_Fig1.mph` da panel (d) yechimi saqlangan. Uni I–V ko'rinishiga keltirish:

1. **Results → pg_VI → g1** (Global) ni oching.
2. *y-Axis Data*: jadvalda faqat `ec.I0_1*1000` qatorini qoldiring (`V_wave` qatorini o'chiring).
3. *x-Axis Data* → *Parameter* = **Expression** → *Expression* = `V_wave`.
4. **Plot** tugmasini bosing. Natijada panel (d) ning I–V sirtmog'i chiqadi.

Panel a, b, c ning yechimlari `.mph` da yo'q. Ularni COMSOL'ning o'zida ko'rish uchun 1-usuldan foydalaning
yoki Java kodini o'zgartirib, qayta hisoblang (pastga qarang).

## `.mph.lock` fayllar haqida

`Model2_FET_Fig1.mph.lock` va `Model3_Pressure.mph.lock` fayllari model ochiq bo'lganda yoki COMSOL to'satdan yopilganda paydo bo'ladi.
Agar COMSOL "fayl band" deb ochmasa: COMSOL'ni to'liq yoping, keyin `.lock` faylni o'chiring.
