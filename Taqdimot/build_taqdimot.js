const pptxgen = require("pptxgenjs");
const { applyTheme } = require("/root/.claude/skills/synced/a6e1bca6-6868-4ae7-9818-20ce4760799f_0e42528d-a348-4420-926a-2547a884290d/pptx/scripts/apply_theme.js");

const OUT = process.argv[2] || "deck.pptx";
const IMG = __dirname + "/img/";

const THEME = {
  name: "GeTe Sb2Te3 COMSOL",
  headFontFace: "Cambria",
  bodyFontFace: "Calibri",
  colors: {
    dk1: "1B2A41", lt1: "FFFFFF", dk2: "0F5E5A", lt2: "EEF2F6",
    accent1: "E8871E", accent2: "2F5FD0", accent3: "0F766E",
    accent4: "B42318", accent5: "1E7B3A", accent6: "5B6B7F",
    hlink: "2F5FD0", folHlink: "6B4FA0",
  },
};
const HEX = THEME.colors;

const pres = new pptxgen();
pres.layout = "LAYOUT_WIDE"; // 13.33 x 7.5
pres.title = "GeTe/Sb2Te3 xotira elementi: COMSOL modeli";
pres.author = "acelegioner02";
pres.theme = { headFontFace: THEME.headFontFace, bodyFontFace: THEME.bodyFontFace };
const C = pres.SchemeColor;
const W = 13.333;

// ---------- Layouts ----------
pres.defineSlideMaster({
  title: "TITLE_DARK",
  background: { color: C.text1 },
  objects: [
    { placeholder: { options: { name: "title", type: "title", align: "left", x: 0.8, y: 2.0, w: 11.7, h: 1.9, fontSize: 40, bold: true, color: C.background1, valign: "bottom", fontFace: THEME.headFontFace }, text: "" } },
    { placeholder: { options: { name: "body", type: "body", x: 0.8, y: 4.1, w: 11.0, h: 1.6, fontSize: 20, color: C.background2, valign: "top" }, text: "" } },
  ],
});
pres.defineSlideMaster({
  title: "CLOSING_DARK",
  background: { color: C.text1 },
  objects: [
    { placeholder: { options: { name: "title", type: "title", align: "left", x: 0.8, y: 0.7, w: 11.7, h: 1.1, fontSize: 40, bold: true, color: C.background1, valign: "middle", fontFace: THEME.headFontFace }, text: "" } },
    { placeholder: { options: { name: "body", type: "body", x: 0.8, y: 2.1, w: 11.7, h: 4.6, fontSize: 20, color: C.background2, valign: "top", paraSpaceAfter: 14 }, text: "" } },
  ],
  slideNumber: { x: 12.4, y: 6.95, w: 0.6, h: 0.35, fontSize: 11, color: C.background2, align: "right" },
});
pres.defineSlideMaster({
  title: "SECTION_DARK",
  background: { color: C.text1 },
  objects: [
    { placeholder: { options: { name: "kicker", type: "body", x: 0.8, y: 2.3, w: 11, h: 0.6, fontSize: 18, color: C.accent1, bold: true }, text: "" } },
    { placeholder: { options: { name: "title", type: "title", align: "left", x: 0.8, y: 2.9, w: 11.7, h: 1.4, fontSize: 40, bold: true, color: C.background1, valign: "top", fontFace: THEME.headFontFace }, text: "" } },
    { placeholder: { options: { name: "body", type: "body", x: 0.8, y: 4.4, w: 11, h: 1.2, fontSize: 18, color: C.background2, valign: "top" }, text: "" } },
  ],
  slideNumber: { x: 12.4, y: 6.95, w: 0.6, h: 0.35, fontSize: 11, color: C.background2, align: "right" },
});
pres.defineSlideMaster({
  title: "CONTENT",
  background: { color: C.background1 },
  objects: [
    { placeholder: { options: { name: "title", type: "title", align: "left", x: 0.6, y: 0.35, w: 12.1, h: 0.85, fontSize: 30, bold: true, color: C.text1, valign: "middle", fontFace: THEME.headFontFace }, text: "" } },
  ],
  slideNumber: { x: 12.4, y: 6.95, w: 0.6, h: 0.35, fontSize: 11, color: C.accent6, align: "right" },
});

// ---------- Helpers ----------
let cnt = 0;
const nm = (s) => `${s}_${++cnt}`;

// "σ_{off}^{x}" -> text runs with sub/superscript
function rich(str, base = {}) {
  const parts = str.split(/(_\{[^}]*\}|\^\{[^}]*\})/).filter((p) => p !== "");
  return parts.map((p) => {
    if (p.startsWith("_{")) return { text: p.slice(2, -1), options: { ...base, subscript: true } };
    if (p.startsWith("^{")) return { text: p.slice(2, -1), options: { ...base, superscript: true } };
    return { text: p, options: { ...base } };
  });
}

function title(slide, t) { slide.addText(t, { placeholder: "title" }); }

function box(slide, x, y, w, h, fill, opts = {}) {
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y, w, h, fill: { color: fill }, line: { color: opts.line || fill, width: opts.lw || 0.75 }, rectRadius: opts.r ?? 0.08, objectName: nm("box") });
}

function txt(slide, text, x, y, w, h, o = {}) {
  slide.addText(text, { x, y, w, h, isTextBox: true, fontSize: 15, color: C.text1, valign: "top", margin: 0.05, objectName: nm("text"), ...o });
}

function circleNum(slide, n, x, y, d = 0.5, fill = C.accent1, color = C.background1, fs = 16) {
  slide.addShape(pres.shapes.OVAL, { x, y, w: d, h: d, fill: { color: fill }, line: { color: fill }, objectName: nm("num") });
  slide.addText(String(n), { x, y, w: d, h: d, isTextBox: true, align: "center", valign: "middle", fontSize: fs, bold: true, color, margin: 0, objectName: nm("numtxt") });
}

function eqBox(slide, str, x, y, w, h, fs = 22) {
  box(slide, x, y, w, h, C.background2, { r: 0.06 });
  slide.addText(rich(str, { fontFace: "Cambria", fontSize: fs, color: C.text1 }), { x: x + 0.2, y, w: w - 0.4, h, isTextBox: true, align: "center", valign: "middle", margin: 0, objectName: nm("eq") });
}

function bullets(slide, items, x, y, w, h, fs = 16) {
  const runs = items.map((it, i) => {
    const o = { bullet: { indent: 18 }, paraSpaceAfter: 8, breakLine: i < items.length - 1 };
    if (Array.isArray(it)) {
      // [bold, rest]
      return [{ text: it[0], options: { ...o, bold: true, breakLine: false } }, { text: it[1], options: { breakLine: i < items.length - 1 } }];
    }
    return [{ text: it, options: o }];
  }).flat();
  slide.addText(runs, { x, y, w, h, isTextBox: true, fontSize: fs, color: C.text1, valign: "top", margin: 0.05, objectName: nm("bul") });
}

function arrowR(slide, x, y, w) {
  slide.addShape(pres.shapes.LINE, { x, y, w, h: 0, line: { color: C.accent6, width: 2, endArrowType: "triangle" }, objectName: nm("arrow") });
}

function content(sectionTitle, t) {
  const s = pres.addSlide({ masterName: "CONTENT", sectionTitle });
  title(s, t);
  return s;
}

function section(sectionTitle, kicker, t, sub) {
  pres.addSection({ title: sectionTitle });
  const s = pres.addSlide({ masterName: "SECTION_DARK", sectionTitle });
  s.addText(kicker, { placeholder: "kicker" });
  s.addText(t, { placeholder: "title" });
  if (sub) s.addText(sub, { placeholder: "body" });
  return s;
}

// =====================================================================
// 1. Title
// =====================================================================
pres.addSection({ title: "Kirish" });
let s = pres.addSlide({ masterName: "TITLE_DARK", sectionTitle: "Kirish" });
s.addText("GeTe/Sb₂Te₃ xotira elementini COMSOL Multiphysics'da modellashtirish", { placeholder: "title" });
s.addText("Troyan & Doronin (2021) maqolasidagi I–V egri chiziqlari va bosim effektini fenomenologik model bilan takrorlash", { placeholder: "body" });
s.addNotes("Assalomu alaykum. Bu ishda men Troyan va Doronin 2021-yildagi maqolasini o'rgandim va undagi tajriba natijalarini COMSOL Multiphysics dasturida model qurib takrorlashga harakat qildim. Taqdimot oddiydan murakkabga qarab boradi: avval maqola nima deydi, keyin model qanday qurilgan, har bir tenglama nima uchun kerak, va oxirida natijalar va cheklovlar.");

// =====================================================================
// 2. Summary
// =====================================================================
s = content("Kirish", "Qisqacha: nima qilindi va nima chiqdi");
const stats = [
  ["3", "ta COMSOL modeli", "Vertikal element, FET (Fig.1) va bosim modeli"],
  ["4", "panel takrorlandi", "Fig.1 (a)–(d): gate kuchlanishi 0, −0.9, −1.1, 0 V"],
  ["×130", "R_ON kamaydi", "2 GPa bosimda, xotira holatida (maqola: 2–3 tartib)"],
];
stats.forEach((st, i) => {
  const x = 0.6 + i * 4.1;
  box(s, x, 1.55, 3.8, 3.1, C.background2);
  txt(s, st[0], x + 0.3, 1.75, 3.2, 1.2, { fontSize: 60, bold: true, color: C.accent1, fontFace: "Cambria" });
  txt(s, st[1], x + 0.3, 2.95, 3.2, 0.45, { fontSize: 18, bold: true });
  txt(s, st[2], x + 0.3, 3.45, 3.2, 1.0, { fontSize: 14, color: C.accent6 });
});
box(s, 0.6, 5.0, 12.1, 1.6, C.background1, { line: HEX.accent3, lw: 1.25 });
txt(s, [
  { text: "Asosiy xulosa: ", options: { bold: true, color: C.accent3 } },
  { text: "model maqoladagi kuzatilgan xatti-harakatni (xotira ↔ kalitlash, bosimga sezgirlik) tenglamalar orqali taqlid qiladi. U fenomenologik: Veyl/Dirak topologik fizikasini hisoblamaydi va uni isbotlamaydi." },
], 0.9, 5.15, 11.5, 1.3, { fontSize: 17, valign: "middle" });
s.addNotes("Bir jumlada: men uchta COMSOL modelini qurdim. Ikkinchi model maqoladagi 1-rasmning to'rtta panelini takrorlaydi, uchinchi model esa bosim ostida qarshilik qanday kamayishini ko'rsatadi. Muhim: bu model fenomenologik, ya'ni atomlar darajasida hisoblamaydi, faqat tajribada ko'rilgan natijani tenglamalar bilan tasvirlaydi.");

// =====================================================================
// Section 1: paper
// =====================================================================
s = section("Maqola", "1-bo'lim", "Maqola nima haqida", "Troyan & Doronin, ICCS 2020, LNNS 186, 427–433 (2021)");
s.addNotes("Birinchi bo'limda maqolaning o'zi haqida gapiraman: element qanday tuzilgan va qanday natijalar olingan.");

// 4. Device structure
s = content("Maqola", "Element qanday tuzilgan");
const stack = [
  ["Yuqori elektrod (TiN)", C.accent6, C.background1, 0.55],
  ["GeTe — ferroelektrik", C.accent3, C.background1, 0.9],
  ["Interfeys: GeSbTe₄ filamenti", C.accent1, C.background1, 0.55],
  ["Sb₂Te₃ — topologik izolyator", C.accent2, C.background1, 0.9],
  ["Pastki elektrod (TiN)", C.accent6, C.background1, 0.55],
];
let yy = 1.6;
stack.forEach(([label, fill, col, h]) => {
  s.addShape(pres.shapes.RECTANGLE, { x: 0.8, y: yy, w: 5.2, h, fill: { color: fill }, line: { color: C.background1, width: 1 }, objectName: nm("layer") });
  txt(s, label, 0.9, yy, 5.0, h, { fontSize: 15, bold: true, color: col, valign: "middle", align: "center" });
  yy += h;
});
txt(s, "Sxema, masshtabsiz", 0.8, yy + 0.1, 5.2, 0.35, { fontSize: 11, color: C.accent6, italic: true, align: "center" });
const devItems = [
  ["Kalitlash interfeysda bo'ladi. ", "Maqola fikriga ko'ra elektr maydon GeTe va Sb₂Te₃ orasida yupqa o'tkazuvchan “filament” (GeSbTe₄) hosil qiladi."],
  ["Filament bor = ON, yo'q = OFF. ", "ON holatda qarshilik kichik, OFF holatda katta."],
  ["Qutbga bog'liq. ", "ON ga faqat bitta qutbda o'tadi, OFF ga qaytish uchun qutbni almashtirish kerak."],
  ["Mualliflarning talqini: ", "ON holat — Veyl yarimmetalli, kalitlash holati — Dirak/2D topologik faza. Bu talqin maqolada isbotlanmagan."],
];
bullets(s, devItems, 6.6, 1.6, 6.1, 4.8, 16);
s.addNotes("Element sendvich ko'rinishida: pastda va yuqorida elektrodlar, o'rtada ikki yupqa qatlam, Sb2Te3 va GeTe. Maqolaga ko'ra eng muhim joy ularning chegarasi, ya'ni interfeys. Kuchlanish berilganda u yerda o'tkazuvchan ip, filament paydo bo'ladi va element ON holatga o'tadi. Filament yo'qolsa OFF holatga qaytadi. Mualliflar buni topologik fizika bilan tushuntiradi, lekin buni o'zlari hisoblamagan.");

// 5. Two effects
s = content("Maqola", "Ikki effekt: xotira va ostona kalitlash");
const cols = [
  ["Xotira (nonvolatil)", C.accent1, [
    "ON holat kuchlanish olib tashlangandan keyin ham saqlanadi",
    "OFF ga qaytish uchun teskari qutbli kuchlanish kerak (RESET)",
    "Gate kuchlanishi 0 V bo'lganda kuzatiladi",
    "R_ON bosimga juda sezgir",
  ]],
  ["Ostona kalitlash (volatil)", C.accent2, [
    "ON holat faqat kuchlanish bor paytda turadi",
    "Kuchlanish kamaysa, element o'zi OFF ga qaytadi",
    "Gate kuchlanishi ≈ −1.1 V bo'lganda kuzatiladi",
    "R_ON bosimga deyarli sezgir emas",
  ]],
];
cols.forEach(([h, col, items], i) => {
  const x = 0.6 + i * 6.2;
  box(s, x, 1.55, 5.9, 4.9, C.background2);
  circleNum(s, i + 1, x + 0.3, 1.8, 0.55, col);
  txt(s, h, x + 1.05, 1.8, 4.6, 0.55, { fontSize: 20, bold: true, valign: "middle" });
  bullets(s, items, x + 0.3, 2.6, 5.3, 3.7, 16);
});
txt(s, "Bitta element ikkala effektni ko'rsatadi, ular orasida gate kuchlanishi almashtiradi. Modelning asosiy vazifasi shu.", 0.6, 6.6, 12.1, 0.4, { fontSize: 14, color: C.accent6, italic: true });
s.addNotes("Elementda ikki xil xatti-harakat bor. Xotira effektida element ON bo'lgach, kuchlanishni olib tashlasak ham ON qoladi, xuddi fleshka kabi. Ostona kalitlashda esa element faqat kuchlanish bor paytda ON bo'ladi, kuchlanish tushsa o'zi OFF ga qaytadi. Qiziq tomoni shuki, bitta element ikkalasini ham ko'rsatadi va qaysi biri bo'lishini gate kuchlanishi hal qiladi.");

// 6. Fig 1 panels
s = content("Maqola", "Maqoladagi Fig.1: gate kuchlanishi effektni almashtiradi");
const panels = [
  ["(a)", "U_gate = 0 V", "Toza xotira", C.accent1],
  ["(b)", "U_gate = −0.9 V", "Xotira kuchsizlanadi", C.accent1],
  ["(c)", "U_gate = −1.1 V", "Faqat ostona kalitlash", C.accent2],
  ["(d)", "U_gate → 0 V", "Xotira qaytadi", C.accent1],
];
panels.forEach(([p, ug, d, col], i) => {
  const x = 0.6 + i * 3.075;
  box(s, x, 1.7, 2.8, 2.9, C.background2);
  txt(s, p, x + 0.25, 1.9, 2.3, 0.7, { fontSize: 34, bold: true, color: col, fontFace: "Cambria" });
  txt(s, ug, x + 0.25, 2.75, 2.4, 0.45, { fontSize: 17, bold: true });
  txt(s, d, x + 0.25, 3.25, 2.4, 1.1, { fontSize: 15, color: C.accent6 });
  if (i < 3) arrowR(s, x + 2.82, 3.15, 0.23);
});
bullets(s, [
  ["Tajriba: ", "elementga 100 Hz chastotali o'zgaruvchan kuchlanish berilib, tok o'lchangan (I–V egri chizig'i)."],
  ["Model uchun nishon: ", "har bir panelning SET/RESET kuchlanishi, R_ON, OFF toki grafikdan ko'z bilan o'qib olindi (±20% aniqlik)."],
  ["Qaytarlik: ", "(d) panelda gate yana 0 V ga qaytganda xotira tiklanadi."],
], 0.6, 4.9, 12.1, 1.9, 16);
s.addNotes("Maqoladagi birinchi rasm to'rtta paneldan iborat. Gate kuchlanishini asta-sekin manfiy tomonga o'zgartirishgan: 0 voltda xotira, minus 0.9 da xotira kuchsizlanadi, minus 1.1 da faqat kalitlash qoladi, gate yana nolga qaytganda xotira qaytadi. Mening modelimning maqsadi aynan shu to'rt panelni takrorlash edi. Buning uchun rasmdan raqamlarni ko'z bilan o'qib oldim, shuning uchun taxminan 20 foiz noaniqlik bor.");

// 7. COMSOL can / cannot
s = content("Maqola", "COMSOL nimani qila oladi, nimani qila olmaydi");
const can = [
  "Tok va kuchlanish taqsimoti (Ohm qonuni)",
  "Joule qizishi va harorat",
  "Bosim → deformatsiya → qatlam qalinligi",
  "Vaqt bo'yicha o'zgaradigan holat (ODE)",
  "Real geometriyada hammasi birga",
];
const cannot = [
  "Elektronlarning bandlar strukturasi",
  "Veyl tugunlari, Fermi yoylari",
  "Topologik invariantlar, spin-orbita",
  "Atomlar migratsiyasi (atom darajasida)",
  "Bular uchun DFT kerak: Quantum ESPRESSO, VASP, Wannier90",
];
[[can, "Qila oladi (qurilma darajasi)", C.accent5, "✓"], [cannot, "Qila olmaydi (kvant darajasi)", C.accent4, "✕"]].forEach(([items, h, col, mark], i) => {
  const x = 0.6 + i * 6.2;
  txt(s, h, x, 1.55, 5.9, 0.5, { fontSize: 20, bold: true, color: col });
  items.forEach((it, j) => {
    const y = 2.2 + j * 0.82;
    circleNum(s, mark, x, y, 0.5, col, C.background1, 16);
    txt(s, it, x + 0.7, y, 5.2, 0.5, { fontSize: 16, valign: "middle" });
  });
});
s.addNotes("COMSOL bu kontinuum dastur, ya'ni materialni yaxlit muhit deb qaraydi. U tok, issiqlik, mexanik kuchlanishni juda yaxshi hisoblaydi. Lekin elektronlarning kvant holatlari, Veyl tugunlari kabi narsalarni hisoblay olmaydi, buning uchun DFT dasturlari kerak. Shuning uchun men maqolaning qurilma darajasidagi natijalarini modellashtirdim, topologik talqinni esa tekshirmadim.");

// =====================================================================
// Section 2: how the model was built
// =====================================================================
s = section("Model", "2-bo'lim", "Model qanday qurildi", "Fenomenologik yondashuv, uchta model, geometriya va COMSOL modullari");
s.addNotes("Ikkinchi bo'limda modelning umumiy tuzilishini tushuntiraman.");

// 9. Phenomenological
s = content("Model", "“Fenomenologik model” nima degani");
const steps = [
  ["Kuzatish", "Tajribada nima ko'rilgan: qachon ON bo'ladi, qachon OFF bo'ladi, tok qancha"],
  ["Tenglama", "Bu xatti-harakatni beradigan oddiy, fizik ma'noli tenglama tanlanadi"],
  ["Moslash (fitting)", "Parametrlar model egri chizig'i tajribaga yaqinlashguncha o'zgartiriladi"],
  ["Tekshirish", "Natija maqola raqamlari bilan solishtiriladi, farqlar yoziladi"],
];
steps.forEach(([h, d], i) => {
  const x = 0.6 + i * 3.1;
  box(s, x, 1.7, 2.8, 2.7, C.background2);
  circleNum(s, i + 1, x + 0.25, 1.95);
  txt(s, h, x + 0.25, 2.6, 2.4, 0.45, { fontSize: 18, bold: true });
  txt(s, d, x + 0.25, 3.1, 2.4, 1.25, { fontSize: 14, color: C.accent6 });
  if (i < 3) arrowR(s, x + 2.82, 3.05, 0.26);
});
box(s, 0.6, 4.8, 12.1, 1.8, C.background1, { line: HEX.accent3, lw: 1.25 });
txt(s, [
  { text: "Oddiy misol: ", options: { bold: true, color: C.accent3 } },
  { text: "termostat qanday ishlashini atomlardan boshlab hisoblamaymiz. “Harorat 22° dan oshsa o'chadi, 20° dan tushsa yonadi” degan qoida bilan yetarlicha aniq tasvirlaymiz. Bizning model ham shunday: filament qanday paydo bo'lishini atomlar darajasida emas, “maydon kuchli bo'lsa ON tomonga o'sadi” degan tenglama bilan tasvirlaydi." },
], 0.9, 4.95, 11.5, 1.5, { fontSize: 16, valign: "middle" });
s.addNotes("Fenomenologik degani: biz hodisaning ichki mexanizmini atomlardan boshlab hisoblamaymiz. Tajribada nima ko'rilganini olamiz, uni tasvirlaydigan tenglama tanlaymiz va tenglamadagi parametrlarni egri chiziq tajribaga mos kelguncha sozlaymiz. Bu ilmda keng tarqalgan usul, masalan memristor modellari shunday quriladi. Kamchiligi: parametrlar mustaqil bashorat emas, ular tajribaga moslab topilgan.");

// 10. Three models
s = content("Model", "Uchta model, uchta savol");
const models = [
  ["Model1_Vertical", "Element qizib ketadimi?", "Vertikal (o'qqa simmetrik) element. Joule qizishi va harorat ta'siri tekshirildi.", "Electric Currents · Heat Transfer · Electromagnetic Heating · Global ODE"],
  ["Model2_FET_Fig1", "Fig.1 ni takrorlay olamizmi?", "Gate'li lateral tranzistor. Fig.1 ning 4 paneli shu modelda olindi. Asosiy model.", "Electric Currents · Global ODE (x va P) · (Heat Transfer: faqat tekshiruv)"],
  ["Model3_Pressure", "Bosim R_ON ni qanday o'zgartiradi?", "Model1 geometriyasi + mexanika. Bosim qatlamni siqadi, R_ON kamayadi.", "Solid Mechanics · Electric Currents"],
];
models.forEach(([n, q, d, m], i) => {
  const x = 0.6 + i * 4.1;
  box(s, x, 1.55, 3.8, 5.1, C.background2);
  circleNum(s, i + 1, x + 0.3, 1.8, 0.55, i === 1 ? C.accent1 : C.accent3);
  txt(s, n, x + 1.0, 1.8, 2.7, 0.55, { fontSize: 17, bold: true, valign: "middle" });
  txt(s, q, x + 0.3, 2.55, 3.3, 0.8, { fontSize: 17, bold: true, color: C.accent3, italic: true });
  txt(s, d, x + 0.3, 3.4, 3.3, 1.6, { fontSize: 15 });
  txt(s, "Modullar:", x + 0.3, 5.05, 3.3, 0.35, { fontSize: 12, bold: true, color: C.accent6 });
  txt(s, m, x + 0.3, 5.4, 3.3, 1.1, { fontSize: 12, color: C.accent6 });
});
s.addNotes("Ish uchta modelga bo'lingan, har biri bitta savolga javob beradi. Birinchi model: element qizib ketadimi, ya'ni issiqlik muhimmi. Ikkinchi model eng asosiysi: u maqoladagi 1-rasmni takrorlaydi. Uchinchi model: bosim qarshilikni qanday kamaytiradi. Hammasi alohida Java fayl sifatida yozilgan va COMSOL'da ishga tushirilgan.");

// 11. Model2 geometry
s = content("Model", "Model2 geometriyasi: gate'li lateral tranzistor");
const gx = 0.7, gw = 7.4;
const gl = [
  ["Gate elektrodi, 10 nm", C.accent6, 0.5],
  ["Al₂O₃ dielektrik, 10 nm", "B9C3CF", 0.5],
  ["GeTe, 20 nm", C.accent3, 0.85],
];
yy = 1.7;
gl.forEach(([label, fill, h]) => {
  s.addShape(pres.shapes.RECTANGLE, { x: gx, y: yy, w: gw, h, fill: { color: fill }, line: { color: C.background1, width: 1 }, objectName: nm("glayer") });
  txt(s, label, gx, yy, gw, h, { fontSize: 14, bold: true, color: fill === "B9C3CF" ? C.text1 : C.background1, valign: "middle", align: "center" });
  yy += h;
});
const chH = 0.95, gapW = 0.55;
const leftW = (gw - gapW) / 2;
s.addShape(pres.shapes.RECTANGLE, { x: gx, y: yy, w: leftW, h: chH, fill: { color: C.accent2 }, line: { color: C.background1, width: 1 }, objectName: nm("ch") });
s.addShape(pres.shapes.RECTANGLE, { x: gx + leftW, y: yy, w: gapW, h: chH, fill: { color: C.accent1 }, line: { color: C.background1, width: 1 }, objectName: nm("gap") });
s.addShape(pres.shapes.RECTANGLE, { x: gx + leftW + gapW, y: yy, w: leftW, h: chH, fill: { color: C.accent2 }, line: { color: C.background1, width: 1 }, objectName: nm("ch") });
txt(s, "Sb₂Te₃ kanal, 20 nm", gx, yy, leftW, chH, { fontSize: 14, bold: true, color: C.background1, valign: "middle", align: "center" });
txt(s, "Sb₂Te₃ kanal, 20 nm", gx + leftW + gapW, yy, leftW, chH, { fontSize: 14, bold: true, color: C.background1, valign: "middle", align: "center" });
const yCh = yy;
yy += chH;
txt(s, "Source: Ground (0 V)", gx - 0.1, yy + 0.1, 3.0, 0.4, { fontSize: 13, bold: true, color: C.accent2 });
txt(s, "Drain: Terminal, V(t)", gx + gw - 3.0, yy + 0.1, 3.1, 0.4, { fontSize: 13, bold: true, color: C.accent2, align: "right" });
txt(s, "Faol soha L_gap = 1.5 nm (to'q sariq)", gx + leftW - 1.6, yy + 0.55, 3.8, 0.4, { fontSize: 13, bold: true, color: C.accent1, align: "center" });
txt(s, "Kanal uzunligi W = 250 nm. Sxema masshtabsiz.", gx, yy + 1.0, gw, 0.4, { fontSize: 12, italic: true, color: C.accent6 });
bullets(s, [
  ["Tok faqat Sb₂Te₃ kanalidan oqadi. ", "Electric Currents fizikasi faqat shu uchta domenga qo'yilgan."],
  ["Kalitlash faqat faol sohada. ", "Uning o'tkazuvchanligi σ_fil holat o'zgaruvchilariga bog'liq."],
  ["Gate ustida, GeTe ga tegib turadi. ", "Gate maydoni elektrostatik hisoblanmaydi, U_gate tenglamaga parametr sifatida kiradi."],
  ["L_gap = 1.5 nm muhim. ", "Kattaroq (3 nm, 200 nm) olinganda kalitlash umuman bo'lmadi."],
], 8.5, 1.7, 4.3, 5.2, 14);
s.addNotes("Bu Model2 ning geometriyasi, ya'ni COMSOL'da chizilgan kesim. Pastda Sb2Te3 kanal, uning o'rtasida 1.5 nanometrlik faol soha bor, kalitlash shu yerda bo'ladi. Chap uchi yerga ulangan, o'ng uchiga o'zgaruvchan kuchlanish berilgan. Ustida GeTe, dielektrik va gate. Bir narsani halol aytaman: gate maydoni elektrostatik hisoblanmagan, gate kuchlanishi tenglamaga oddiy parametr sifatida kiradi.");

// 12. Modules
s = content("Model", "Qaysi COMSOL modullari ishlatildi va nega");
const mods = [
  ["Electric Currents (ec)", "∇·(σ∇V) = 0", "Tok va kuchlanish taqsimotini hisoblaydi. Har bir hisobning asosi: I–V egri chizig'idagi tok shu yerdan olinadi (ec.I0_1)."],
  ["Heat Transfer + Electromagnetic Heating", "Q = J·E", "Joule qizishini hisoblaydi. Savol: kalitlash issiqlik tufayli emasmi? Model2 da T_max = 300 K chiqdi, shuning uchun asosiy hisobda o'chirildi (xotira tejash)."],
  ["Global ODEs and DAEs (ge)", "dx/dt = …,  dP/dt = …", "Vaqt bo'yicha o'zgaradigan ikki holat: filament x va gate xotirasi P. Bular elementning “xotirasi”."],
  ["Solid Mechanics (solid), Model3", "σ = C : ε", "Bosimdan deformatsiyani hisoblaydi. Interfeys qatlamining yangi qalinligi shu yerdan olinadi."],
];
mods.forEach(([n, eq, d], i) => {
  const y = 1.5 + i * 1.32;
  circleNum(s, i + 1, 0.6, y + 0.2, 0.55, C.accent3);
  txt(s, n, 1.35, y + 0.05, 4.3, 0.45, { fontSize: 16, bold: true });
  slideEqInline(s, eq, 1.35, y + 0.55, 4.3, 0.5);
  txt(s, d, 5.9, y + 0.05, 6.8, 1.15, { fontSize: 14, valign: "middle" });
});
function slideEqInline(sl, eq, x, y, w, h) {
  sl.addText(rich(eq, { fontFace: "Cambria", fontSize: 15, color: C.accent3, italic: true }), { x, y, w, h, isTextBox: true, margin: 0.05, valign: "middle", objectName: nm("eqi") });
}
s.addNotes("Har bir modul aniq bir savol uchun qo'shilgan. Electric Currents tokni hisoblaydi, bu har qanday I-V uchun kerak. Issiqlik moduli kalitlash qizish tufayli emasligini tekshirish uchun qo'shilgan; FET modelida harorat deyarli 300 K da qoldi, shuning uchun asosiy hisobda o'chirildi. Global ODE moduli elementning xotirasini, ya'ni vaqt bo'yicha o'zgaradigan holatni tasvirlaydi. Solid Mechanics faqat bosim modelida kerak.");

// =====================================================================
// Section 3: equations
// =====================================================================
s = section("Tenglamalar", "3-bo'lim", "Tenglamalar: bittadan, sodda qilib", "Har bir tenglama: nima qiladi, nega shunday tanlandi");
s.addNotes("Endi eng muhim qism: har bir tenglamani alohida tushuntiraman.");

// 14. state x
s = content("Tenglamalar", "1-g'oya: filament holati x");
eqBox(s, "σ_{fil} = σ_{off}^{ 1 − x} · σ_{on}^{ x}", 0.6, 1.5, 7.2, 1.2, 30);
// scale bar
s.addShape(pres.shapes.RECTANGLE, { x: 0.8, y: 3.35, w: 3.4, h: 0.45, fill: { color: C.accent2 }, line: { color: C.accent2 }, objectName: nm("bar") });
s.addShape(pres.shapes.RECTANGLE, { x: 4.2, y: 3.35, w: 3.4, h: 0.45, fill: { color: C.accent1 }, line: { color: C.accent1 }, objectName: nm("bar") });
txt(s, "x = 0: OFF, filament yo'q", 0.8, 3.9, 3.4, 0.4, { fontSize: 14, bold: true, color: C.accent2 });
txt(s, "x = 1: ON, filament to'liq", 4.2, 3.9, 3.4, 0.4, { fontSize: 14, bold: true, color: C.accent1, align: "right" });
txt(s, "x — 0 dan 1 gacha bo'lgan son. U elementning “qancha ON ekanini” bildiradi.", 0.8, 4.5, 6.8, 0.8, { fontSize: 15 });
bullets(s, [
  ["Nega bitta son? ", "Filament shaklini hisoblash juda murakkab. Uning ta'sirini bitta o'zgaruvchi bilan ifodalaymiz (memristor modellaridagi standart usul)."],
  ["Nega darajali (log) aralashtirish? ", "σ_on va σ_off bir-biridan ko'p tartibga farq qiladi. Darajali formula x o'zgarganda qarshilikni bir tekis, tartib-tartib bilan o'zgartiradi."],
  ["Oddiy aralashtirish bo'lsa ", "(1−x)σ_off + xσ_on, x = 0.01 da ham element deyarli ON bo'lib qolardi."],
], 8.2, 1.5, 4.6, 5.3, 15);
s.addNotes("Butun modelning asosi bitta son: x. U nol bo'lsa element OFF, bir bo'lsa ON. Faol sohaning o'tkazuvchanligi x ga qarab OFF qiymatdan ON qiymatgacha o'zgaradi. Formula darajali, chunki ON va OFF o'tkazuvchanliklar ming-million marta farq qiladi; darajali formula o'tishni silliq qiladi. Bu usul memristor modellarida keng ishlatiladi.");

// 15. kinetic equation
s = content("Tenglamalar", "2-tenglama: x qanchalik tez o'zgaradi");
eqBox(s, "dx/dt = k_{0}·exp(−E_{a}/k_{B}T) · sinh(q·a·E/2k_{B}T) · F(x) − x/τ_{rel}", 0.6, 1.45, 12.1, 1.1, 24);
const terms = [
  ["k₀·exp(−Eₐ/k_BT)", "Atom to'siqdan sakrab o'tishi", "k₀ = 10¹³ 1/s — atom sekundiga necha marta “urinishi”. Eₐ — to'siq balandligi (Arrhenius qonuni). Harorat oshsa tezlashadi."],
  ["sinh(q·a·E/2k_BT)", "Elektr maydon yordami", "E > 0: musbat, x o'sadi (SET). E < 0: manfiy, x kamayadi (RESET). E kichik bo'lsa deyarli nol. Shu tufayli element qutbga bog'liq."],
  ["F(x)", "Oyna funksiyasi", "x ni 0 va 1 orasida ushlab turadi: chegaraga yaqinlashganda o'sish to'xtaydi (Biolek oynasi)."],
  ["− x/τ_rel", "O'z-o'zidan so'nish", "Kuchlanish bo'lmasa filament qanchalik tez yo'qoladi. τ_rel katta: xotira; kichik: volatil."],
];
terms.forEach(([eq, h, d], i) => {
  const x = 0.6 + i * 3.075;
  box(s, x, 2.85, 2.85, 3.75, C.background2);
  txt(s, eq, x + 0.2, 3.0, 2.5, 0.5, { fontSize: 15, bold: true, color: C.accent3, fontFace: "Cambria" });
  txt(s, h, x + 0.2, 3.5, 2.5, 0.6, { fontSize: 16, bold: true });
  txt(s, d, x + 0.2, 4.15, 2.5, 2.4, { fontSize: 13, color: C.text1 });
});
txt(s, "Bu ion sakrashi (Mott–Gurney tipidagi) kinetikasi: maydon to'siqni bir tomonga pasaytiradi, boshqa tomonga oshiradi.", 0.6, 6.7, 12.1, 0.4, { fontSize: 13, italic: true, color: C.accent6 });
s.addNotes("Bu modelning yuragi. x vaqt bo'yicha qanday o'zgarishini ko'rsatadi. Birinchi qism: atomlar to'siqdan sakrab o'tishi, harorat oshsa tezlashadi. Ikkinchi qism, sinh: elektr maydon atomlarni bir tomonga itaradi. Maydon musbat bo'lsa x o'sadi, ya'ni ON ga o'tadi; manfiy bo'lsa kamayadi, ya'ni OFF ga qaytadi. Sinh kichik maydonda deyarli nol, katta maydonda esa juda tez o'sadi, shuning uchun kalitlash ma'lum bir ostona kuchlanishda keskin bo'ladi. Uchinchi qism x ni 0 va 1 orasida ushlab turadi. Oxirgi qism: kuchlanish bo'lmaganda filament o'z-o'zidan so'nadi.");

// 16. SET vs RESET
s = content("Tenglamalar", "Nega SET va RESET uchun parametrlar har xil");
const sr = [
  ["SET (OFF → ON)", C.accent1, [["Eₐ_SET = ", "0.9 eV"], ["a_SET = ", "0.3 nm"], ["Natija: ", "V_SET ≈ +3.65 V (maqola: +3.5 V)"]]],
  ["RESET (ON → OFF)", C.accent2, [["Eₐ_RESET = ", "1.4 eV"], ["z_eff · a_RESET = ", "2.42 · 0.3 = 0.73 nm"], ["Natija: ", "RESET keskin, kengligi ≈ 0.3 V"]]],
];
sr.forEach(([h, col, items], i) => {
  const x = 0.6 + i * 3.9;
  box(s, x, 1.55, 3.6, 3.4, C.background2);
  txt(s, h, x + 0.25, 1.75, 3.1, 0.5, { fontSize: 18, bold: true, color: col });
  bullets(s, items, x + 0.25, 2.4, 3.2, 2.4, 15);
});
eqBox(s, "dir = ½·(1 + tanh(E/E_{s}))", 0.6, 5.25, 7.5, 0.85, 20);
txt(s, "dir ≈ 1 bo'lsa SET parametrlari, dir ≈ 0 bo'lsa RESET parametrlari ishlaydi (silliq almashadi).", 0.6, 6.2, 7.5, 0.6, { fontSize: 13, color: C.accent6 });
bullets(s, [
  ["Sabab — maqolaning o'zi: ", "SET va RESET har xil qutbda, har xil kuchlanishda bo'ladi."],
  ["tanh nega? ", "Keskin “if E>0” COMSOL yechuvchisini to'xtatib qo'yadi. tanh xuddi shu ishni silliq bajaradi."],
  ["z_eff nima? ", "RESET ni keskinroq qilish uchun kiritilgan ko'paytiruvchi (“ko'p zaryadli ion” farazi). Bu mexanizm o'rganilmagan, sof fitting vositasi."],
], 8.4, 1.55, 4.4, 5.3, 15);
s.addNotes("Tajribada ON ga o'tish va OFF ga qaytish bir xil emas, shuning uchun ularga alohida parametrlar berildi. Yo'nalishni tanlash uchun tanh funksiyasi ishlatilgan: maydon musbat bo'lsa SET parametrlari, manfiy bo'lsa RESET parametrlari ishlaydi. Oddiy if shartini ishlatib bo'lmaydi, chunki COMSOL yechuvchisi keskin sakrashlarda to'xtab qoladi. z_eff RESET ni keskinroq qilish uchun qo'shilgan; bu mexanizm emas, fitting vositasi ekanini ochiq aytaman.");

// 17. gate memory P
s = content("Tenglamalar", "3-tenglama: gate “xotirasi” P");
eqBox(s, "f_{gate} = 1 / (1 + exp(−(U_{gate} − U_{0}) / w))", 0.6, 1.5, 6.6, 0.95, 21);
eqBox(s, "dP/dt = (f_{gate} − P) / τ_{P}", 0.6, 2.65, 6.6, 0.95, 21);
s.addTable([
  [{ text: "U_gate", options: { bold: true, color: C.background1, fill: { color: C.text1 } } }, { text: "f_gate", options: { bold: true, color: C.background1, fill: { color: C.text1 } } }, { text: "Ma'nosi", options: { bold: true, color: C.background1, fill: { color: C.text1 } } }],
  ["0 V", "≈ 1.00", "Gate ta'sir qilmaydi, xotira"],
  ["−0.9 V", "≈ 0.88", "Xotira biroz kuchsiz"],
  ["−1.1 V", "≈ 0.12", "Kuchli bostirish, volatil"],
], { x: 0.6, y: 3.9, w: 6.6, colW: [1.4, 1.4, 3.8], fontSize: 14, color: C.text1, border: { type: "solid", color: "C9D2DC", pt: 0.75 }, rowH: 0.42, objectName: nm("tbl") });
bullets(s, [
  ["f_gate — “gate nimani xohlaydi”. ", "Sigmoid (S-shakl) funksiya, U₀ = −1.0 V atrofida 1 dan 0 ga o'tadi (kenglik w = 0.05 V)."],
  ["P — “element hozir qayerda”. ", "P darhol f_gate ga sakramaydi, vaqt bilan yaqinlashadi."],
  ["Asimmetrik vaqt: ", "pasayish tez (τ = 1 ms), tiklanish sekin (τ = 75 ms)."],
  ["Nega kerak? ", "(d) panelda gate 0 V ga qaytdi, lekin natija (a) bilan bir xil emas: element oldingi gate holatini “eslaydi”. P aynan shu xotirani beradi."],
], 7.6, 1.5, 5.2, 5.3, 14);
s.addNotes("Ikkinchi holat o'zgaruvchisi P gate ta'sirini tasvirlaydi. f_gate gate qaysi holatni xohlashini ko'rsatadi: 0 voltda bir, minus 1.1 voltda deyarli nol. P esa elementning haqiqiy holati, u f_gate ga asta-sekin yaqinlashadi. Pasayish tez, tiklanish sekin. Bu nega kerak? Maqolaning d panelida gate nolga qaytgan, lekin natija a panel bilan bir xil emas. Demak element oldingi gate holatini eslab qoladi, P shu xotirani modellashtiradi.");

// 18. tau_rel(P)
s = content("Tenglamalar", "4-tenglama: xotira yoki volatil? τ_rel(P) hal qiladi");
eqBox(s, "τ_{rel} = τ_{v} + (τ_{nv} − τ_{v}) · S,     S = 1 / (1 + exp(−(P − P_{c}) / w_{P}))", 0.6, 1.5, 12.1, 1.0, 21);
const tr = [
  ["P > 0.17", "S ≈ 1  →  τ_rel ≈ 1000 s", "x amalda “muzlaydi”. Kuchlanish olib tashlansa ham ON qoladi.", "XOTIRA: panel (a), (b), (d)", C.accent1],
  ["P < 0.17", "S ≈ 0  →  τ_rel ≈ 0.15 ms", "x tez so'nadi. Kuchlanish tushishi bilan element OFF ga qaytadi.", "KALITLASH: panel (c)", C.accent2],
];
tr.forEach(([h, eq, d, res, col], i) => {
  const x = 0.6 + i * 6.2;
  box(s, x, 2.8, 5.9, 2.9, C.background2);
  txt(s, h, x + 0.3, 3.0, 5.3, 0.55, { fontSize: 24, bold: true, color: col, fontFace: "Cambria" });
  txt(s, eq, x + 0.3, 3.6, 5.3, 0.45, { fontSize: 17, bold: true });
  txt(s, d, x + 0.3, 4.1, 5.3, 0.8, { fontSize: 15 });
  txt(s, res, x + 0.3, 5.05, 5.3, 0.45, { fontSize: 16, bold: true, color: col });
});
txt(s, [
  { text: "Nega keskin (w_P = 0.0015)? ", options: { bold: true } },
  { text: "Avval silliq versiya sinaldi: (d) panel xotira o'rniga volatil chiqdi. Keskin chegara P_c = 0.17 tanlandi, chunki (c) va (d) panellardagi haqiqiy P qiymatlari aynan shu son atrofida ajraladi." },
], 0.6, 5.95, 12.1, 0.9, { fontSize: 14 });
s.addNotes("Bu tenglama elementning xotira rejimida yoki kalitlash rejimida ishlashini hal qiladi. Agar P 0.17 dan katta bo'lsa, relaksatsiya vaqti ming soniya, ya'ni filament amalda yo'qolmaydi, bu xotira. Agar P kichik bo'lsa, vaqt 0.15 millisoniya, filament darhol so'nadi, bu ostona kalitlash. Avval silliqroq formula sinalgan edi, lekin d panel noto'g'ri chiqdi, shuning uchun keskin chegara tanlandi.");

// 19. OFF/ON conductivities
s = content("Tenglamalar", "5-tenglama: OFF va ON o'tkazuvchanliklar");
eqBox(s, "σ_{off,eff} = σ_{off0} · cosh(E / E_{0}) · r_{off}^{ (1 − P)}", 0.6, 1.5, 7.3, 1.0, 21);
eqBox(s, "σ_{on,eff} = σ_{on,v} · (σ_{on0} / σ_{on,v})^{ P}", 0.6, 2.75, 7.3, 1.0, 21);
bullets(s, [
  ["cosh(E/E₀): ", "OFF holatda ham tok kuchlanish bilan nochiziqli o'sadi (tajribada shunday). E₀ = 0.8 V / 1.5 nm."],
  ["Nega cosh, sinh(x)/x emas? ", "Shakli bir xil, lekin bo'lish yo'q. Bo'lish yechuvchini juda sekinlashtirgan edi."],
  ["r_off^(1−P): ", "manfiy gate OFF tokini ham kamaytiradi (r_off = 0.05). Bu (c), (d) panellardagi OFF tokini to'g'riladi."],
  ["σ_on,eff: ", "P = 1 da σ_on0 (R_ON ≈ 7 kΩ), P kichik bo'lsa σ_on,v gacha kamayadi. Gate ON holatni ham zaiflashtiradi."],
], 8.2, 1.5, 4.6, 5.3, 14);
s.addTable([
  [{ text: "Parametr", options: { bold: true, color: C.background1, fill: { color: C.text1 } } }, { text: "Qiymat", options: { bold: true, color: C.background1, fill: { color: C.text1 } } }, { text: "Qanday topildi", options: { bold: true, color: C.background1, fill: { color: C.text1 } } }],
  ["σ_off0", "1.62·10⁻⁸ S/m", "I_OFF(3.5 V) ≈ 0.03 mA"],
  ["σ_on0", "1.07·10⁻⁵ S/m", "R_ON = 7 kΩ, panel (a)"],
  ["σ_on,v", "6.05·10⁻⁷ S/m", "I(4 V) ≈ 0.04 mA, panel (c)"],
], { x: 0.6, y: 4.1, w: 7.3, colW: [1.5, 2.3, 3.5], fontSize: 14, color: C.text1, border: { type: "solid", color: "C9D2DC", pt: 0.75 }, rowH: 0.42, objectName: nm("tbl") });
s.addNotes("Bu ikki formula OFF va ON holatlarda faol soha qanchalik tok o'tkazishini beradi. OFF holatda ham tajribada tok kuchlanish bilan nochiziqli oshadi, buni cosh beradi. Gate manfiy bo'lsa OFF ham, ON ham kamayadi, buni P orqali ifodaladik. Jadvaldagi uchta asosiy son COMSOL'ning Stationary hisobi bilan avtomatik kalibrlangan, ya'ni maqoladagi qarshilik va tok qiymatlariga moslab topilgan.");

// 20. voltage and field
s = content("Tenglamalar", "Kuchlanish va maydon qanday beriladi");
eqBox(s, "V(t) = V_{amp} · (2/π) · asin(sin(2π f_{0} t)),     f_{0} = 100 Hz", 0.6, 1.5, 12.1, 0.95, 21);
eqBox(s, "E_{drive} = −⟨E_{x}⟩_{faol soha} ≈ V / L_{gap}", 0.6, 2.65, 12.1, 0.95, 21);
const vb = [
  ["Uchburchak to'lqin", "asin(sin(…)) — kuchlanish 0 → +V_amp → 0 → −V_amp → 0 tekis o'zgaradi. Tajribadagi 100 Hz bilan bir xil. Bir davr = 10 ms."],
  ["Har panel o'z amplitudasi bilan", "V_amp: (a) 3.8 V, (b) 3.8 V, (c) 4.5 V, (d) 4.0 V — maqoladagi grafiklardagidek."],
  ["Maydon faol sohada o'rtacha", "COMSOL faol sohadagi E_x ni o'rtachalaydi (aveop). Shu son sinh tenglamasiga kiradi."],
  ["Nega L_gap = 1.5 nm muhim", "E ≈ V/L_gap, sinh esa eksponensial. L_gap 2 marta katta bo'lsa, tezlik millionlab marta kamayadi va kalitlash bo'lmaydi."],
];
vb.forEach(([h, d], i) => {
  const x = 0.6 + (i % 2) * 6.2, y = 3.85 + Math.floor(i / 2) * 1.55;
  circleNum(s, i + 1, x, y + 0.05, 0.5, C.accent3);
  txt(s, h, x + 0.7, y, 5.2, 0.45, { fontSize: 16, bold: true });
  txt(s, d, x + 0.7, y + 0.45, 5.2, 1.0, { fontSize: 14, color: C.text1 });
});
s.addNotes("Elementga beriladigan kuchlanish uchburchak shaklida, 100 Hz, xuddi tajribadagidek. Har bir panel uchun amplituda maqoladagi grafikdan olingan. Elektr maydon faol soha bo'ylab o'rtacha qilib olinadi va kinetik tenglamaga kiradi. Faol soha uzunligi 1.5 nanometr bo'lishi juda muhim: maydon kuchlanishni shu uzunlikka bo'lganiga teng, sinh esa eksponensial, shuning uchun uzunlik biroz katta bo'lsa kalitlash umuman bo'lmaydi.");

// =====================================================================
// Section 4: computation
// =====================================================================
s = section("Hisoblash", "4-bo'lim", "Hisoblash qanday bajarildi", "Kalibrlash, issiqlik tekshiruvi, 4 ta vaqt bo'yicha hisob, natijani eksport qilish");
s.addNotes("To'rtinchi bo'limda hisob qanday ketma-ketlikda bajarilganini ko'rsataman.");

// 22. Flow
s = content("Hisoblash", "Hisoblash ketma-ketligi (Model2_FET_Fig1.java)");
const flow = [
  ["Kalibrlash", "Study 1, Stationary", "σ_off0, σ_on0, σ_on,v avtomatik topildi"],
  ["Issiqlik tekshiruvi", "Stationary + Heat", "T_max = 300 K, qizish yo'q, issiqlik o'chirildi"],
  ["4 ta panel", "Study 4, Time Dependent", "Har biri 10 ms, U_gate va V_amp har xil"],
  ["Holatni uzatish", "x va P", "Oldingi panel oxiri keyingisining boshi"],
  ["Eksport va grafik", "CSV → rasm", "iv_fig1_continuous.csv → Fig1_analog.png"],
];
flow.forEach(([h, sub, d], i) => {
  const x = 0.6 + i * 2.5;
  box(s, x, 1.6, 2.25, 2.9, C.background2);
  circleNum(s, i + 1, x + 0.2, 1.8, 0.5);
  txt(s, h, x + 0.2, 2.4, 1.95, 0.75, { fontSize: 16, bold: true });
  txt(s, sub, x + 0.2, 3.1, 1.95, 0.45, { fontSize: 12, bold: true, color: C.accent3 });
  txt(s, d, x + 0.2, 3.5, 1.95, 0.95, { fontSize: 12, color: C.text1 });
  if (i < 4) arrowR(s, x + 2.27, 3.05, 0.2);
});
box(s, 0.6, 4.85, 12.1, 1.85, C.background1, { line: HEX.accent1, lw: 1.25 });
txt(s, [
  { text: "Nega .mph faylda faqat (d) panel bor? ", options: { bold: true, color: C.accent1, breakLine: true } },
  { text: "Bitta Study 4 to'rt marta ketma-ket ishga tushirilgan. Har safar yangi natija eskisining ustidan yozilgan, shuning uchun faylda oxirgi panel (d) qolgan. Barcha 4 panel natijasi har hisobdan keyin CSV faylga yozib borilgan va 4 panelli rasm shu CSV fayldan PowerShell skripti bilan chizilgan." },
], 0.9, 5.0, 11.5, 1.6, { fontSize: 15, valign: "middle" });
s.addNotes("Hisob besh qadamda bajarilgan. Avval statsionar hisob bilan uchta asosiy o'tkazuvchanlik maqoladagi qarshiliklarga moslab topildi. Keyin issiqlik tekshirildi: harorat 300 K da qoldi, shuning uchun issiqlik moduli o'chirildi va kompyuter xotirasi tejaldi. Keyin vaqt bo'yicha hisob to'rt marta ishga tushirildi, har bir panel uchun bir marta, va x hamda P holatlari bir paneldan keyingisiga uzatildi. Natijalar CSV faylga yozildi va rasm shu fayldan chizildi. Shu sababli COMSOL faylida faqat oxirgi panel ko'rinadi.");

// 23. Fitting table
s = content("Hisoblash", "Parametrlar qanday tanlandi (fitting)");
const hdr = (t) => ({ text: t, options: { bold: true, color: C.background1, fill: { color: C.text1 } } });
s.addTable([
  [hdr("Parametr"), hdr("Qiymat"), hdr("Qaysi natijaga moslangan")],
  ["σ_on0", "1.07·10⁻⁵ S/m", "R_ON = 7 kΩ, panel (a)"],
  ["Eₐ_SET, a_SET", "0.9 eV, 0.3 nm", "V_SET ≈ +3.5 V"],
  ["Eₐ_RESET, z_eff", "1.4 eV, 2.42", "RESET joyi −3.6…−3.8 V va keskinligi"],
  ["U₀, w", "−1.0 V, 0.05 V", "(b) xotira, (c) kalitlash"],
  ["τ_P,fall, τ_P,rise", "1 ms, 75 ms", "(d) panelda gate xotirasi"],
  ["τ_v", "0.15 ms", "(c) panelda V_h ≈ 0.7 V"],
  ["P_c, w_P", "0.17, 0.0015", "(d) panel xotira, (c) panel volatil"],
  ["r_off", "0.05", "(c), (d) panellarda OFF toki"],
  ["β_mem (Model3)", "30", "R_ON(2 GPa) / R_ON(0) ≈ 130"],
], { x: 0.6, y: 1.5, w: 8.2, colW: [2.2, 2.3, 3.7], fontSize: 14, color: C.text1, border: { type: "solid", color: "C9D2DC", pt: 0.75 }, rowH: 0.45, objectName: nm("tbl") });
box(s, 9.2, 1.5, 3.5, 4.5, C.background2);
txt(s, "8", 9.45, 1.7, 3.0, 1.1, { fontSize: 60, bold: true, color: C.accent1, fontFace: "Cambria" });
txt(s, "iteratsiya", 9.45, 2.8, 3.0, 0.45, { fontSize: 18, bold: true });
txt(s, "Har iteratsiyada model grafigi maqola bilan solishtirildi va bitta-ikkita parametr o'zgartirildi. Eng yaxshi natija saqlandi.", 9.45, 3.3, 3.0, 2.6, { fontSize: 14, color: C.accent6 });
s.addNotes("Bu jadvalda asosiy parametrlar va ular qaysi tajriba natijasiga moslab tanlangani ko'rsatilgan. Masalan, aktivatsiya energiyasi SET kuchlanishi 3.5 volt bo'lishi uchun tanlangan. Moslash taxminan sakkiz iteratsiyada bajarildi: har safar grafik maqola bilan solishtirildi va parametr biroz o'zgartirildi. Shuni aytish kerakki, bu parametrlar mustaqil o'lchangan emas, ular moslab topilgan.");

// =====================================================================
// Section 5: results
// =====================================================================
s = section("Natijalar", "5-bo'lim", "Natijalar", "Fig.1 solishtirish, (d) panel grafigini o'qish, bosim natijasi");
s.addNotes("Beshinchi bo'limda natijalarni ko'rsataman.");

// 25. Fig1 image
s = content("Natijalar", "Fig.1: model va maqola solishtirmasi");
s.addImage({ path: IMG + "Fig1_analog.png", x: 0.6, y: 1.35, w: 6.2, h: 5.58, altText: "Model I–V egri chiziqlari, 4 panel, maqola nuqtalari bilan", objectName: nm("img") });
bullets(s, [
  ["Havo rang chiziq: ", "OFF holat (x < 0.5)."],
  ["To'q sariq chiziq: ", "ON holat (x > 0.5)."],
  ["Kulrang nuqtalar: ", "maqoladagi grafikdan o'qilgan nishon qiymatlar."],
  ["(a), (b): ", "sirtmoq ikkala qutbda ochiq, ya'ni xotira."],
  ["(c): ", "ON faqat musbat tomonda, kuchlanish tushganda OFF ga qaytadi, ya'ni volatil."],
  ["(d): ", "gate 0 ga qaytgach xotira tiklanadi, lekin tok (a) dan ~10 marta kichik."],
  ["Diqqat: ", "(c), (d) da y o'qi ±0.05 mA, (a), (b) da ±0.5 mA."],
], 7.2, 1.5, 5.6, 5.4, 15);
s.addNotes("Bu yakuniy natija. Chiziq — model, kulrang nuqtalar — maqoladan olingan qiymatlar. Havo rang qism OFF, to'q sariq qism ON. A va b panellarda sirtmoq ikkala tomonda ochiq, bu xotira. C panelda ON faqat musbat tomonda va kuchlanish tushishi bilan yo'qoladi, bu ostona kalitlash. D panelda xotira qaytadi. Diqqat qiling: pastki ikki panelda shkala o'n marta kichik.");

// 26. Numbers table
s = content("Natijalar", "Raqamlarda solishtirish");
const ok = (t) => ({ text: t, options: { color: C.accent5, bold: true } });
const mid = (t) => ({ text: t, options: { color: C.accent1, bold: true } });
const bad = (t) => ({ text: t, options: { color: C.accent4, bold: true } });
s.addTable([
  [hdr("Panel"), hdr("Ko'rsatkich"), hdr("Maqola"), hdr("Model"), hdr("Baho")],
  ["(a)", "V_SET", "+3.5 V", "+3.65 V", ok("✓ 4%")],
  ["(a)", "R_ON", "7 kΩ", "7.15 kΩ", ok("✓ 2%")],
  ["(a)", "RESET joyi", "−3.6…−3.8 V", "−3.65…−3.8 V", ok("✓")],
  ["(a)", "I_OFF (+3.5 V)", "0.03 mA", "0.074 mA", bad("✕ 2.5×")],
  ["(b)", "R_ON", "9 kΩ", "9.73 kΩ", ok("✓ 8%")],
  ["(c)", "Ulanish kuchlanishi", "+3.7…+4.3 V", "+3.78…+3.96 V", ok("✓")],
  ["(c)", "V_h (OFF ga qaytish)", "≈ 0.7 V", "0.72 V", ok("✓ 3%")],
  ["(d)", "Xatti-harakat", "xotira", "xotira", ok("✓")],
  ["(d)", "R_ON", "≈ 70 kΩ", "≈ 80 kΩ", mid("△ 14%")],
  ["(d)", "Manfiy ON toki", "−0.05 mA", "−0.024…−0.028 mA", mid("△ ~50%")],
], { x: 0.6, y: 1.45, w: 9.0, colW: [0.9, 2.6, 1.9, 2.2, 1.4], fontSize: 14, color: C.text1, border: { type: "solid", color: "C9D2DC", pt: 0.75 }, rowH: 0.43, objectName: nm("tbl") });
box(s, 10.0, 1.45, 2.7, 4.75, C.background2);
txt(s, "16–18", 10.2, 1.65, 2.4, 0.9, { fontSize: 40, bold: true, color: C.accent5, fontFace: "Cambria" });
txt(s, "31 nishondan", 10.2, 2.55, 2.4, 0.4, { fontSize: 16, bold: true });
txt(s, "20–30% aniqlik bilan yoki sifat jihatidan to'g'ri chiqdi. Eng muhim yutuq: (d) panel xotirasi to'g'ri chiqdi.", 10.2, 3.0, 2.4, 3.1, { fontSize: 14, color: C.accent6 });
s.addNotes("Jadvalda aniq raqamlar. Yashil belgilar yaxshi mos kelgan joylar: SET kuchlanishi, ON qarshilik, c paneldagi qaytish kuchlanishi. To'q sariq qisman mos kelgan, qizil esa mos kelmagan: a paneldagi OFF toki 2.5 marta katta chiqdi. Umuman olganda 31 ta nishondan 16-18 tasi yaxshi mos keldi.");

// 27. Reading user's panel d
s = content("Natijalar", "COMSOL grafigini qanday o'qish kerak: (d) panel");
const iw = 6.03, ih = 5.5, ix = 0.6, iy = 1.35, sc = iw / 923;
s.addImage({ path: IMG + "user_paneld.png", x: ix, y: iy, w: iw, h: ih, altText: "COMSOL'dagi (d) panel I–V grafigi", objectName: nm("img") });
const dataPts = [[1.5, 0.008], [3.45, 0.025], [1.6, 0.032], [-1.6, -0.033], [-3.3, -0.02], [-2.0, 0.009]];
dataPts.forEach(([V, I], i) => {
  const px = 96 + (V + 4) * 100.75, py = 410 - I * 5280;
  const cx = ix + px * sc, cy = iy + py * sc;
  circleNum(s, i + 1, cx - 0.17, cy - 0.17, 0.34, C.accent1, C.background1, 12);
});
const read = [
  ["Boshlanish (OFF): ", "kuchlanish 0 dan oshadi, tok deyarli nol (~0.002 mA). Element (c) paneldan keyin OFF holatda."],
  ["SET: ", "+3.5…+4 V da tok keskin oshadi, filament paydo bo'ldi (x → 1)."],
  ["ON chizig'i: ", "kuchlanish kamayganda tok to'g'ri chiziq bo'ylab qaytadi (Ohm qonuni, R ≈ 80 kΩ)."],
  ["Xotira: ", "kuchlanish manfiy bo'lsa ham element ON qoladi, chiziq davom etadi (−0.045 mA gacha)."],
  ["RESET: ", "−3 V dan pastda tok kamayadi, filament yo'qoladi (x → 0)."],
  ["Qaytish (OFF): ", "−4 V dan 0 gacha tok yana deyarli nol."],
];
read.forEach(([h, d], i) => {
  const y = 1.35 + i * 0.92;
  circleNum(s, i + 1, 7.0, y + 0.05, 0.4, C.accent1, C.background1, 13);
  txt(s, [{ text: h, options: { bold: true } }, { text: d }], 7.55, y, 5.25, 0.88, { fontSize: 13 });
});
s.addNotes("Bu mening COMSOL'da olgan grafigim, d panel. Uni oltita qadamda o'qiymiz. Bir: kuchlanish noldan oshadi, tok deyarli yo'q, element OFF. Ikki: taxminan 3.5-4 voltda tok keskin oshadi, bu SET. Uch: kuchlanish kamayganda tok to'g'ri chiziq bo'ylab qaytadi, ya'ni element oddiy rezistor kabi ishlaydi, qarshilik taxminan 80 kiloom. To'rt: kuchlanish manfiy bo'lsa ham element ON qoladi, bu xotira. Besh: minus 3 voltdan pastda tok kamayadi, bu RESET. Olti: element yana OFF. Nol atrofidagi kichik uzilish boshlang'ich holatdan kelib chiqqan.");

// 28. Pressure
s = content("Natijalar", "Model3: bosim R_ON ni kamaytiradi");
const pGPa = ["0", "0.25", "0.5", "1.0", "1.5", "2.0"];
s.addChart(pres.charts.LINE, [
  { name: "R_ON, xotira (β = 30)", labels: pGPa, values: [6988.8, 3862.6, 2105.1, 614.1, 178.8, 53.5] },
  { name: "R_ON, volatil (β = 0)", labels: pGPa, values: [6988.8, 6988.8, 6988.8, 6988.8, 6988.8, 6988.8] },
  { name: "R_OFF", labels: pGPa, values: [99993, 99993, 99993, 99993, 99993, 99993] },
], {
  x: 0.6, y: 1.4, w: 6.9, h: 5.4,
  chartColors: [HEX.accent1, HEX.accent2, HEX.accent6],
  lineSize: 2.5, lineDataSymbol: "circle", lineDataSymbolSize: 7,
  valAxisLogScaleBase: 10, valAxisMinVal: 10, valAxisMaxVal: 1000000,
  valAxisTitle: "Qarshilik, Ω (log)", showValAxisTitle: true, catAxisTitle: "Bosim, GPa", showCatAxisTitle: true,
  valAxisTitleFontSize: 12, catAxisTitleFontSize: 12,
  catAxisLabelFontSize: 12, valAxisLabelFontSize: 12,
  catAxisLabelColor: HEX.accent6, valAxisLabelColor: HEX.accent6,
  valAxisTitleColor: HEX.accent6, catAxisTitleColor: HEX.accent6,
  catAxisLabelFontFace: "+mn-lt", valAxisLabelFontFace: "+mn-lt", legendFontFace: "+mn-lt",
  valGridLine: { color: "E3E8EE", size: 0.75 }, catGridLine: { style: "none" },
  showLegend: true, legendPos: "b", legendFontSize: 12,
  showTitle: false, objectName: nm("chart"),
});
eqBox(s, "d_{gap} = t_{int} · (1 + ε_{zz})", 7.9, 1.4, 4.8, 0.75, 18);
eqBox(s, "σ_{on}(p) = σ_{on0} · exp(β·(t_{int} − d_{gap}) / t_{int})", 7.9, 2.3, 4.8, 0.75, 16);
bullets(s, [
  ["Mexanika: ", "bosim interfeysni siqadi, 2 GPa da 1.5 nm → 1.25 nm."],
  ["Xotira (β = 30): ", "R_ON 6989 Ω → 53.5 Ω, ×130 (≈ 2 tartib)."],
  ["Volatil (β = 0): ", "R_ON o'zgarmaydi."],
  ["R_OFF: ", "o'zgarmaydi (≈ 100 kΩ)."],
  ["Diqqat: ", "β tanlangan (fitting). Effekt GPa darajasida, maqolada esa bosim qiymati berilmagan."],
], 7.9, 3.25, 4.8, 3.6, 14);
s.addNotes("Uchinchi model bosim haqida. Solid Mechanics bosimdan interfeys qanchalik siqilishini hisoblaydi: 2 gigapaskalda 1.5 nanometrdan 1.25 ga tushadi. ON o'tkazuvchanlik qalinlikka eksponensial bog'langan, xuddi tunnel effektidagidek. Natija: xotira holatida R_ON 130 marta kamayadi, bu maqoladagi 2-3 tartib degan da'voning pastki chegarasi. Volatil holatda va OFF holatda o'zgarmaydi. Muhim: maqolada bosim qiymati berilmagan, effekt esa faqat gigapaskal darajasida chiqadi. Bu maqolaga savol sifatida qo'yilishi mumkin.");

// 29. Model1 extras
s = content("Natijalar", "Qo'shimcha natijalar (Model1_Vertical)");
const ex = [
  ["≈ 460 K", "Joule qizishi", "Vertikal elementda (x = 1, 3.5 V) maksimal harorat. Analitik baho ~450 K bilan mos. Lateral FET'da esa ~300 K."],
  ["3.51→0.78 V", "Harorat ta'siri", "Atrof harorati 300 K dan 450 K ga oshganda V_SET kamayadi. Bu Arrhenius qonunidan kutilgan natija."],
  ["R ∝ 1/r_f²", "Sezgirlik tahlili", "R_ON eng ko'p filament radiusiga bog'liq (±50% → ×3.3 / ×0.46), keyin qalinlik va σ_on."],
];
ex.forEach(([big, h, d], i) => {
  const x = 0.6 + i * 4.1;
  box(s, x, 1.55, 3.8, 4.4, C.background2);
  txt(s, big, x + 0.3, 1.8, 3.3, 0.9, { fontSize: 28, bold: true, color: C.accent3, fontFace: "Cambria" });
  txt(s, h, x + 0.3, 2.8, 3.3, 0.45, { fontSize: 18, bold: true });
  txt(s, d, x + 0.3, 3.35, 3.3, 2.5, { fontSize: 14 });
});
txt(s, "Xulosa: lateral FET'da issiqlik ahamiyatsiz, shuning uchun Fig.1 dagi kalitlashni issiqlik bilan emas, maydon bilan tushuntirish mumkin. Bu maqola fikriga mos.", 0.6, 6.2, 12.1, 0.7, { fontSize: 15, italic: true, color: C.text1 });
s.addNotes("Birinchi modelning qo'shimcha natijalari. Vertikal elementda harorat 460 K gacha ko'tariladi, lekin tranzistor geometriyasida qizish deyarli yo'q. Bu muhim: demak Fig.1 dagi kalitlash issiqlik bilan emas, maydon bilan bog'liq bo'lishi mumkin, bu maqola mualliflari fikriga mos keladi. Harorat oshsa SET kuchlanishi kamayadi. Sezgirlik tahlili esa qarshilik eng ko'p filament radiusiga bog'liqligini ko'rsatdi.");

// =====================================================================
// Section 6: limitations & conclusion
// =====================================================================
s = section("Xulosa", "6-bo'lim", "Cheklovlar va xulosa");
s.addNotes("Oxirgi bo'lim: modelning cheklovlari va xulosa.");

// 31. Limitations
s = content("Xulosa", "Modelning cheklovlari");
const lim = [
  ["Fenomenologik", "Veyl/Dirak fazalari, Fermi yoylari modellashtirilmagan va isbotlanmagan."],
  ["Parametrlar moslangan", "Ular mustaqil o'lchanmagan, tajribaga fitting qilingan."],
  ["Gate soddalashtirilgan", "Gate maydoni elektrostatik yechilmagan, U_gate oddiy parametr."],
  ["(b) panelda kichik tarmoq yo'q", "Buning uchun uchinchi holat o'zgaruvchisi kerak bo'lardi."],
  ["OFF toki (a) panelda 2.5× katta", "Simmetrik cosh formula asimmetrik OFF tokini to'liq bera olmaydi."],
  ["Nishonlar ko'z bilan o'qilgan", "Maqola grafigidan ±20% aniqlik bilan. Bitta qurilma, statistika yo'q."],
];
lim.forEach(([h, d], i) => {
  const x = 0.6 + (i % 3) * 4.1, y = 1.55 + Math.floor(i / 3) * 2.6;
  box(s, x, y, 3.8, 2.3, C.background2);
  circleNum(s, "!", x + 0.25, y + 0.25, 0.45, C.accent4, C.background1, 16);
  txt(s, h, x + 0.85, y + 0.22, 2.8, 0.55, { fontSize: 16, bold: true, valign: "middle" });
  txt(s, d, x + 0.25, y + 0.9, 3.3, 1.3, { fontSize: 14 });
});
s.addNotes("Cheklovlarni ochiq aytish kerak. Birinchi va eng asosiysi: model fenomenologik, topologik fizikani isbotlamaydi. Ikkinchi: parametrlar tajribaga moslab topilgan. Uchinchi: gate soddalashtirilgan. Bundan tashqari b paneldagi kichik qo'shimcha tarmoq chiqmadi va a paneldagi OFF toki 2.5 marta katta. Va nishon qiymatlar rasmdan ko'z bilan o'qilgan.");

// 32. Q&A
s = content("Xulosa", "Ustoz so'rashi mumkin bo'lgan savollar");
const qa = [
  ["Model Veyl fazasini isbotlaydimi?", "Yo'q. Model faqat I–V xatti-harakatini takrorlaydi. Topologik fazani tekshirish uchun DFT hisobi kerak."],
  ["Nega sinh funksiyasi?", "Ion sakrash nazariyasidan: maydon to'siqni bir tomonga pasaytiradi, boshqasiga oshiradi. Natijada keskin ostona va qutbga bog'liqlik."],
  ["Nega issiqlik o'chirildi?", "FET geometriyasida T_max = 300 K chiqdi, issiqlik ta'siri yo'q. O'chirish hisobni tezlashtirdi va xotira tanqisligini (OOM) hal qildi."],
  ["Nega ikkita holat (x va P)?", "Bitta x xotira va kalitlashni ajrata olmaydi. P gate tarixini eslab, (d) panelni to'g'ri beradi."],
  ["Natijalar ishonchlimi?", "Ko'p ko'rsatkich 20% ichida. Lekin bu mos kelish, bashorat emas: parametrlar shu natijaga moslangan."],
  ["Keyingi qadam nima?", "DFT bilan σ(t), gate'ni elektrostatik yechish, (b) panel uchun qo'shimcha holat."],
];
qa.forEach(([q, a], i) => {
  const x = 0.6 + (i % 2) * 6.2, y = 1.5 + Math.floor(i / 2) * 1.8;
  box(s, x, y, 5.9, 1.6, C.background2);
  circleNum(s, "?", x + 0.2, y + 0.2, 0.42, C.accent3, C.background1, 16);
  txt(s, q, x + 0.75, y + 0.15, 5.0, 0.5, { fontSize: 15, bold: true, valign: "middle" });
  txt(s, a, x + 0.75, y + 0.65, 5.0, 0.9, { fontSize: 13 });
});
s.addNotes("Bu slaydni o'zim uchun tayyorladim: ustoz berishi mumkin bo'lgan savollar va qisqa javoblar. Eng muhimi birinchi savol: model topologik fazani isbotlamaydi, faqat tajriba natijasini takrorlaydi.");

// 33. Conclusion
s = pres.addSlide({ masterName: "CLOSING_DARK", sectionTitle: "Xulosa" });
s.addText("Xulosa", { placeholder: "title" });
s.addText([
  { text: "COMSOL'da fenomenologik model maqoladagi Fig.1 ning to'rt panelini sifat jihatidan to'g'ri takrorladi.", options: { bullet: true, breakLine: true } },
  { text: "Model ikkita holat bilan ishlaydi: filament x va gate xotirasi P. Xotira yoki kalitlashni τ_rel(P) hal qiladi.", options: { bullet: true, breakLine: true } },
  { text: "Bosim modeli R_ON ning ~130 marta kamayishini berdi, lekin buning uchun GPa darajasidagi bosim kerak.", options: { bullet: true, breakLine: true } },
  { text: "Topologik talqinni tekshirish uchun keyingi qadam: DFT + COMSOL.", options: { bullet: true } },
], { placeholder: "body" });
s.addNotes("Xulosa qilib aytganda: COMSOL'da qurilgan fenomenologik model maqoladagi tajriba natijalarini sifat jihatidan to'g'ri takrorladi. Buning uchun ikki holat o'zgaruvchisi yetarli bo'ldi. Bosim modeli qarshilikning 130 marta kamayishini berdi. Maqoladagi topologik talqinni tekshirish uchun esa DFT hisoblari kerak, bu keyingi bosqich. E'tiboringiz uchun rahmat.");

(async () => {
  await pres.writeFile({ fileName: OUT });
  await applyTheme(OUT, THEME);
  console.log("written", OUT);
})();
