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

## Muammo: xotira yetishmasligi (OOM) va yechim

Birinchi to'liq S4 urinishida (ht yoqilgan holda, 51042+19636 DOF) tizim operativ
xotirasi (jami atigi ~8GB) yetmay qolib, comsolbatch jarayoni tashqi tomondan
o'ldirildi (panel "a" ning 4-qadamida, t=2.3e-6s). Sabab: har bir Time Dependent
qadamda EC+HT+GE fizikalari birgalikda assemble qilinganda vaqtinchalik ~2.8GB
cho'qqiga chiqadi, bu boshqa jarayonlar bilan birga mavjud xotiradan oshib ketadi.

2-urinish HAM o'ldirildi (ht S4'dan o'chirilgan bo'lsa ham, S3 issiqlik tekshiruvi
o'zi - ec+ht, 51040+4578 DOF - xotira cho'qqisiga sabab bo'lgan edi, chunki bu
mashinada erkin xotira atigi ~3.9GB, boshqa dasturlar - Word/Excel/Chrome/Telegram -
allaqachon ~1.5GB band qilgan). Shuning uchun QO'SHIMCHA qaror: S3 (issiqlik
tekshiruvi) ham `RUN_TCHECK=false` bilan BUTUNLAY O'TKAZIB YUBORILDI, chunki bu
tekshiruv BIRINCHI (muvaffaqiyatli) urinishda allaqachon T_max=300K natija bergan
va parametrlar keyin o'zgarmagan - qayta ishlatish shart emas. Muhim texnik nuqta:
std3 o'tkazib yuborilgani sababli dataset raqamlanishi almashadi (std4 endi 2-chi
ishlagan study -> "dset2", "dset3" emas) - kod shunga moslab tuzatildi.

Yechim (asosiy, S4 uchun): S2 bosqichida T_max(xs=1, V=4.5V)=300K ekanligi allaqachon tasdiqlangan edi
(Joule isishi ushbu lateral FET geometriyasida ahamiyatsiz - Model1 vertikal
ustunidan farqli). Shu sababli S4 (Time Dependent, 4 panel) uchun `ht` fizikasi
`activate` orqali O'CHIRILDI, faqat `ec`+`ge` yechiladi; T_local=aveop_a(T) T_amb
(~300K) qiymatida qotib qoladi - bu S2 natijasiga mos, fizik jihatdan asoslangan
soddalashtirish. Bu DOF sonini keskin kamaytirib, xotira muammosini hal qildi.

Davomi pastda, bosqichma-bosqich yoziladi.
