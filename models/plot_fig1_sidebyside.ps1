# plot_fig1_sidebyside.ps1 - Fig1_side_by_side.png: har bir panel uchun model va
# maqola nuqtalari BITTA katta o'qda, katta shrift bilan (1 ustun, 4 qator - har
# biri kengroq va balandroq, o'qish osonroq).

Add-Type -AssemblyName System.Drawing

$dataPath = "C:\comsol_ish\models\iv_fig1_continuous.csv"
$targetPath = "C:\comsol_ish\models\fig1_targets.csv"
$outPath = "C:\comsol_ish\models\Fig1_side_by_side.png"

$data = Import-Csv $dataPath
$targets = Import-Csv $targetPath

$panels = @("a","b","c","d")
$panelTitles = @{a="(a) Ugate=0 (Vamp=3.8V)"; b="(b) Ugate=-0.9V (Vamp=3.8V)"; c="(c) Ugate=-1.1V (Vamp=4.5V)"; d="(d) Ugate->0 (Vamp=4.0V)"}
$panelYRange = @{a=0.5; b=0.5; c=0.05; d=0.06}

$W = 1400
$cellH = 620
$Hgrid = $cellH * 4
$H = $Hgrid + 30
$bmp = New-Object System.Drawing.Bitmap($W,$H)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::White)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

$penOff = New-Object System.Drawing.Pen([System.Drawing.Color]::Blue, 3)
$penOn = New-Object System.Drawing.Pen([System.Drawing.Color]::DarkOrange, 3)
$penGrid = New-Object System.Drawing.Pen([System.Drawing.Color]::LightGray, 1)
$brushTarget = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Gray)
$penTargetRing = New-Object System.Drawing.Pen([System.Drawing.Color]::Black, 1.5)
$penAxis = New-Object System.Drawing.Pen([System.Drawing.Color]::Black, 2)
$fontLabel = New-Object System.Drawing.Font("Arial", 20, [System.Drawing.FontStyle]::Bold)
$fontAxis = New-Object System.Drawing.Font("Arial", 15)
$brushText = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Black)

$cellW = $W
$margin = 110

function ToPx($v, $lo, $hi, $px0, $pw) { return $px0 + ($v - $lo) / ($hi - $lo) * $pw }
function ToPy($v, $lo, $hi, $py0, $ph) { return $py0 + $ph - ($v - $lo) / ($hi - $lo) * $ph }

for ($idx = 0; $idx -lt 4; $idx++) {
    $panel = $panels[$idx]
    $ox = 0
    $oy = $idx * $cellH
    $plotX0 = $ox + $margin
    $plotY0 = $oy + 55
    $plotW = $cellW - 2*$margin - 40
    $plotH = $cellH - 130

    $yMax = $panelYRange[$panel]
    $xMin = -5.0; $xMax = 5.0
    $yMin = -$yMax

    # Panjara chiziqlari (o'qish osonligi uchun)
    for ($gv = -4; $gv -le 4; $gv += 2) {
        $gx = ToPx $gv $xMin $xMax $plotX0 $plotW
        $g.DrawLine($penGrid, $gx, $plotY0, $gx, $plotY0+$plotH)
    }
    $gyStep = $yMax/2
    for ($gy = -$yMax; $gy -le $yMax; $gy += $gyStep) {
        $gyy = ToPy $gy $yMin $yMax $plotY0 $plotH
        $g.DrawLine($penGrid, $plotX0, $gyy, $plotX0+$plotW, $gyy)
    }

    $x0px = ToPx 0 $xMin $xMax $plotX0 $plotW
    $y0px = ToPy 0 $yMin $yMax $plotY0 $plotH
    $g.DrawLine($penAxis, $plotX0, $y0px, $plotX0+$plotW, $y0px)
    $g.DrawLine($penAxis, $x0px, $plotY0, $x0px, $plotY0+$plotH)
    $g.DrawRectangle($penAxis, $plotX0, $plotY0, $plotW, $plotH)

    $g.DrawString($panelTitles[$panel], $fontLabel, $brushText, $ox+10, $oy+5)

    $rows = $data | Where-Object { $_.panel -eq $panel } | Sort-Object { [double]$_.t_s }
    $prevPt = $null
    foreach ($r in $rows) {
        $v = [double]$r.V_V
        $i_mA = [double]$r.I_A * 1000.0
        $xv = [double]$r.x
        $ivClamp = [math]::Max($yMin, [math]::Min($yMax, $i_mA))
        $vClamp = [math]::Max($xMin, [math]::Min($xMax, $v))
        $px = ToPx $vClamp $xMin $xMax $plotX0 $plotW
        $py = ToPy $ivClamp $yMin $yMax $plotY0 $plotH
        $pt = New-Object System.Drawing.PointF($px, $py)
        $pen = if ($xv -lt 0.5) { $penOff } else { $penOn }
        if ($prevPt -ne $null) { $g.DrawLine($pen, $prevPt, $pt) }
        $prevPt = $pt
    }

    $tgtRows = $targets | Where-Object { $_.panel -eq $panel }
    foreach ($tr in $tgtRows) {
        $v = [double]$tr.V_V
        $i_mA = [double]$tr.I_mA
        if ($v -lt $xMin -or $v -gt $xMax -or $i_mA -lt $yMin -or $i_mA -gt $yMax) { continue }
        $px = ToPx $v $xMin $xMax $plotX0 $plotW
        $py = ToPy $i_mA $yMin $yMax $plotY0 $plotH
        $g.FillEllipse($brushTarget, $px-7, $py-7, 14, 14)
        $g.DrawEllipse($penTargetRing, $px-7, $py-7, 14, 14)
    }

    $g.DrawString("V [V]", $fontAxis, $brushText, $plotX0+$plotW+5, $y0px-12)
    $g.DrawString("I [mA]", $fontAxis, $brushText, $x0px+5, $plotY0-30)
    $g.DrawString(("{0:0.###}" -f $xMin), $fontAxis, $brushText, $plotX0-15, $plotY0+$plotH+5)
    $g.DrawString(("{0:0.###}" -f $xMax), $fontAxis, $brushText, $plotX0+$plotW-20, $plotY0+$plotH+5)
    $g.DrawString(("{0:0.###}" -f $yMax), $fontAxis, $brushText, $plotX0-$margin+10, $plotY0-10)
    $g.DrawString(("{0:0.###}" -f $yMin), $fontAxis, $brushText, $plotX0-$margin+10, $plotY0+$plotH-10)
}

$g.DrawString("Model2_FET_Fig1 (ko'k=OFF, to'q sariq=ON tarmoq) vs Troyan & Doronin (2021) Fig.1 nishonlari (kulrang doiralar)",
    $fontAxis, $brushText, 10, $Hgrid+3)

$bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
Write-Output "Saqlandi: $outPath"
