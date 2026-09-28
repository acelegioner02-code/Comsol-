# Fig.1 yaqinlashtirish - ish hisoboti

Boshlanish: 2026-09-28 (avtonom sessiya, 3 soat). Vazifa matni: `VAZIFA_FIG1.md`.
Maqsad qiymatlar: `fig1_targets.csv`.

## Holat jadvali

| Bosqich | Holat |
|---|---|
| 0. Sozlash (vazifa/nishon/hisobot fayllari) | BAJARILDI |
| 1. Nochiziqli OFF o'tkazuvchanlik (cosh, sinh(x)/x o'rniga bo'linishsiz) | BAJARILDI |
| 2. Zatvor holati P (2-Global ODE, asimmetrik tau_P) | BAJARILDI |
| 3. SET/RESET kinetikasi qayta fitting (Ea_SET/Ea_RESET, a_SET/a_RESET) | BAJARILDI (parametrlar kiritildi, aniq moslash keyingi bosqichda tekshiriladi) |
| 4. V_h~0.7V uchun tau_v moslash | REJADA (natija tahlilida tekshiriladi) |
| 5. Time Dependent hisob (4 panel, ketma-ket, holat uzatish bilan) | JARAYONDA |
| 6. Fig1_analog.png (2x2 panel) | KUTILMOQDA |
| 7. Geometriya (Model2 nm-masshtab, Model1 R_dev=250nm) | KUTILMOQDA (vaqt qolsa) |
| 8. Bitta birlashtirilgan .mph | KUTILMOQDA (vaqt qolsa) |

## Arxitektura qarori (5-bosqich)

Vazifada "bitta uzluksiz Time Dependent hisob (4 tsikl, 0-40ms)" so'ralgan edi, lekin
40ms uzluksiz hisobning dastlabki sinovi ~10 daqiqada atigi 7% ga yetdi (~140+ daqiqa
prognoz) - bu 90 daqiqalik cheklovni buzardi. Shuning uchun QARORI: hisobni **4 ta
ketma-ket alohida 10ms Time Dependent hisob**ga bo'lish, xode va P holatlarini
panellar orasida **qo'lda uzatish** (oldingi panelning oxirgi x,P qiymatlari keyingi
panelning `ge1.initialValueU` boshlang'ich shartiga yoziladi). Bu:
- vazifadagi "x va P holatlari tsikllar orasida saqlansin" talabini bajaradi (aynan
  bir xil natija, faqat COMSOL bitta calc o'rniga 4ta ketma-ket calc sifatida
  bajaradi);
- har bir panel tugagach natija CSVga yozilib, .mph saqlanadi - vaqt tugasa ham
  QISMAN natija saqlanib qoladi (a, b, c, d ketma-ketligida).

Kalibrlash (sig_off0, sig_on0, sig_on_v) oldingi qisqa sinovda (2ms, Ugate=0)
muvaffaqiyatli yakunlangan va natijalar kodga qattiq yozilgan (`CALIBRATE=false`):
sig_off0=1.6182e-8 S/m, sig_on0=1.0712e-5 S/m, sig_on_v=6.0469e-7 S/m.

Davomi pastda, bosqichma-bosqich yoziladi.
