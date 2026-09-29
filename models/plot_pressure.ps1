# plot_pressure.ps1 - Fig_pressure.png: R_ON(p) va R_OFF(p) log shkalada,
# xotira (beta_mem) va volatil (beta=0) holatlari Ron_p.csv dan.

Add-Type -AssemblyName System.Drawing

$dataPath = "C:\comsol_ish\models\Ron_p.csv"
$outPath = "C:\comsol_ish\models\Fig_pressure.png"

$data = Import-Csv $dataPath

$W = 1200
$H = 900
$bmp = New-Object System.Drawing.Bitmap($W,$H)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::White)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

$penOffLine = New-Object System.Drawing.Pen([System.Drawing.Color]::Gray, 2)
$penOnMem = New-Object System.Drawing.Pen([System.Drawing.Color]::DarkOrange, 3)
$penOnVol = New-Object System.Drawing.Pen([System.Drawing.Color]::Blue, 3)
$penAxis = New-Object System.Drawing.Pen([System.Drawing.Color]::Black, 2)
$penGrid = New-Object System.Drawing.Pen([System.Drawing.Color]::LightGray, 1)
$brushMem = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::DarkOrange)
$brushVol = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Blue)
$brushOff = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Gray)
$fontLabel = New-Object System.Drawing.Font("Arial", 20, [System.Drawing.FontStyle]::Bold)
$fontAxis = New-Object System.Drawing.Font("Arial", 14)
$fontLegend = New-Object System.Drawing.Font("Arial", 13)
$brushText = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Black)

$margin = 110
$plotX0 = $margin
$plotY0 = 80
$plotW = $W - 2*$margin - 60
$plotH = $H - 220

$pMin = 0.0; $pMax = 2.0
$rMin = 10.0; $rMax = 200000.0   # log shkala, ohm

function ToPx($v) { return $plotX0 + ($v - $pMin) / ($pMax - $pMin) * $plotW }
function ToPyLog($v) {
    $lv = [math]::Log10($v)
    $lmin = [math]::Log10($rMin)
    $lmax = [math]::Log10($rMax)
    return $plotY0 + $plotH - ($lv - $lmin) / ($lmax - $lmin) * $plotH
}

# Panjara (dekada chiziqlari)
for ($dec = 1; $dec -le 5; $dec++) {
    $rv = [math]::Pow(10, $dec)
    $py = ToPyLog $rv
    $g.DrawLine($penGrid, $plotX0, $py, $plotX0+$plotW, $py)
    $g.DrawString(("{0:0}" -f $rv), $fontAxis, $brushText, $plotX0-95, $py-10)
}
for ($pv = 0.0; $pv -le 2.0; $pv += 0.5) {
    $px = ToPx $pv
    $g.DrawLine($penGrid, $px, $plotY0, $px, $plotY0+$plotH)
    $g.DrawString(("{0:0.0}" -f $pv), $fontAxis, $brushText, $px-15, $plotY0+$plotH+8)
}

$g.DrawRectangle($penAxis, $plotX0, $plotY0, $plotW, $plotH)
$g.DrawString("Bosim p [GPa]", $fontAxis, $brushText, $plotX0+$plotW/2-60, $plotY0+$plotH+35)
$gsState = $g.Save()
$g.TranslateTransform(25, $plotY0+$plotH/2+80)
$g.RotateTransform(-90)
$g.DrawString("Qarshilik R [Ohm] (log shkala)", $fontAxis, $brushText, 0, 0)
$g.Restore($gsState)

$g.DrawString("Model3_Pressure: R_ON(p) va R_OFF(p) - xotira vs volatil holat", $fontLabel, $brushText, 10, 15)

# R_OFF (xs=0) - ikkala holatda ham bir xil (kulrang, uzuq chiziq simulyatsiya qilamiz nuqta bilan)
$rowsOff = $data | Where-Object { $_.xs -eq "0" -and $_.beta -like "*xotira*" } | Sort-Object { [double]$_.p_GPa }
$prevPt = $null
foreach ($r in $rowsOff) {
    $p = [double]$r.p_GPa; $rv = [double]$r.R_ohm
    $px = ToPx $p; $py = ToPyLog $rv
    $pt = New-Object System.Drawing.PointF($px, $py)
    if ($prevPt -ne $null) { $g.DrawLine($penOffLine, $prevPt, $pt) }
    $g.FillEllipse($brushOff, $px-5, $py-5, 10, 10)
    $prevPt = $pt
}

# R_ON xotira (beta_mem)
$rowsMem = $data | Where-Object { $_.xs -eq "1" -and $_.beta -like "*xotira*" } | Sort-Object { [double]$_.p_GPa }
$prevPt = $null
foreach ($r in $rowsMem) {
    $p = [double]$r.p_GPa; $rv = [double]$r.R_ohm
    $px = ToPx $p; $py = ToPyLog $rv
    $pt = New-Object System.Drawing.PointF($px, $py)
    if ($prevPt -ne $null) { $g.DrawLine($penOnMem, $prevPt, $pt) }
    $g.FillEllipse($brushMem, $px-6, $py-6, 12, 12)
    $prevPt = $pt
}

# R_ON volatil (beta=0)
$rowsVol = $data | Where-Object { $_.xs -eq "1" -and $_.beta -like "*volatil*" } | Sort-Object { [double]$_.p_GPa }
$prevPt = $null
foreach ($r in $rowsVol) {
    $p = [double]$r.p_GPa; $rv = [double]$r.R_ohm
    $px = ToPx $p; $py = ToPyLog $rv
    $pt = New-Object System.Drawing.PointF($px, $py)
    if ($prevPt -ne $null) { $g.DrawLine($penOnVol, $prevPt, $pt) }
    $g.FillEllipse($brushVol, $px-6, $py-6, 12, 12)
    $prevPt = $pt
}

# Legenda
$ly = $plotY0 + $plotH + 70
$g.FillEllipse($brushOff, $plotX0, $ly, 10, 10)
$g.DrawString("R_OFF(p) - o'zgarmaydi (~100 kOhm)", $fontLegend, $brushText, $plotX0+18, $ly-4)
$g.FillEllipse($brushMem, $plotX0+420, $ly, 12, 12)
$g.DrawString("R_ON(p), XOTIRA (beta_mem=30) - keskin pasayadi", $fontLegend, $brushText, $plotX0+438, $ly-4)
$ly2 = $ly + 28
$g.FillEllipse($brushVol, $plotX0, $ly2, 12, 12)
$g.DrawString("R_ON(p), VOLATIL (beta=0) - deyarli o'zgarmaydi", $fontLegend, $brushText, $plotX0+18, $ly2-4)

$bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
Write-Output "Saqlandi: $outPath"
