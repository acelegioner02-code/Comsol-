# plot_fig1.ps1 - Fig1_analog.png ni iv_fig1_continuous.csv va fig1_targets.csv dan chizadi.
# Python mavjud emas, shuning uchun .NET System.Drawing orqali qo'lda 2x2 panel chiziladi.

Add-Type -AssemblyName System.Drawing

$dataPath = "C:\comsol_ish\models\iv_fig1_continuous.csv"
$targetPath = "C:\comsol_ish\models\fig1_targets.csv"
$outPath = "C:\comsol_ish\models\Fig1_analog.png"

$data = Import-Csv $dataPath
$targets = Import-Csv $targetPath

$panels = @("a","b","c","d")
$panelTitles = @{a="(a) Ugate=0"; b="(b) Ugate=-0.9V"; c="(c) Ugate=-1.1V"; d="(d) Ugate->0"}
$panelYRange = @{a=0.5; b=0.5; c=0.05; d=0.06}

$W = 1600
$Hgrid = 1400
$H = $Hgrid + 40
$bmp = New-Object System.Drawing.Bitmap($W,$H)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.Clear([System.Drawing.Color]::White)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

$penOff = New-Object System.Drawing.Pen([System.Drawing.Color]::Blue, 2)
$penOn = New-Object System.Drawing.Pen([System.Drawing.Color]::DarkOrange, 2)
$brushTarget = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Gray)
$penAxis = New-Object System.Drawing.Pen([System.Drawing.Color]::Black, 1.5)
$fontLabel = New-Object System.Drawing.Font("Arial", 14, [System.Drawing.FontStyle]::Bold)
$fontAxis = New-Object System.Drawing.Font("Arial", 11)
$brushText = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::Black)

$cellW = $W / 2
$cellH = $Hgrid / 2
$margin = 90

for ($idx = 0; $idx -lt 4; $idx++) {
    $panel = $panels[$idx]
    $col = $idx % 2
    $row = [math]::Floor($idx / 2)
    $ox = $col * $cellW
    $oy = $row * $cellH
    $plotX0 = $ox + $margin
    $plotY0 = $oy + $margin
    $plotW = $cellW - 2*$margin
    $plotH = $cellH - 2*$margin

    $yMax = $panelYRange[$panel]
    $xMin = -5.0; $xMax = 5.0
    $yMin = -$yMax

    function ToPx($v, $lo, $hi, $px0, $pw) { return $px0 + ($v - $lo) / ($hi - $lo) * $pw }
    function ToPy($v, $lo, $hi, $py0, $ph) { return $py0 + $ph - ($v - $lo) / ($hi - $lo) * $ph }

    # Axes (V=0 va I=0 chiziqlari)
    $x0px = ToPx 0 $xMin $xMax $plotX0 $plotW
    $y0px = ToPy 0 $yMin $yMax $plotY0 $plotH
    $g.DrawLine($penAxis, $plotX0, $y0px, $plotX0+$plotW, $y0px)
    $g.DrawLine($penAxis, $x0px, $plotY0, $x0px, $plotY0+$plotH)
    $g.DrawRectangle($penAxis, $plotX0, $plotY0, $plotW, $plotH)

    # Panel sarlavhasi
    $g.DrawString($panelTitles[$panel], $fontLabel, $brushText, $ox+10, $oy+10)
    $g.DrawString("Voltage [V]", $fontAxis, $brushText, $ox+$cellW/2-30, $oy+$cellH-25)
    $gsState = $g.Save()
    $g.TranslateTransform($ox+20, $oy+$cellH/2+30)
    $g.RotateTransform(-90)
    $g.DrawString("Current [mA]", $fontAxis, $brushText, 0, 0)
    $g.Restore($gsState)

    # Model ma'lumotlari: shu panelga tegishli qatorlar, OFF (x<0.5) / ON (x>=0.5) ga bo'lingan
    $rows = $data | Where-Object { $_.panel -eq $panel } | Sort-Object { [double]$_.t_s }
    $offPts = New-Object System.Collections.Generic.List[System.Drawing.PointF]
    $onPts = New-Object System.Collections.Generic.List[System.Drawing.PointF]
    $lastX = -1
    foreach ($r in $rows) {
        $v = [double]$r.V_V
        $i_mA = [double]$r.I_A * 1000.0
        $xv = [double]$r.x
        if ($v -lt $xMin -or $v -gt $xMax) { continue }
        $ivClamp = [math]::Max($yMin, [math]::Min($yMax, $i_mA))
        $px = ToPx $v $xMin $xMax $plotX0 $plotW
        $py = ToPy $ivClamp $yMin $yMax $plotY0 $plotH
        $pt = New-Object System.Drawing.PointF($px, $py)
        if ($xv -lt 0.5) { $offPts.Add($pt) } else { $onPts.Add($pt) }
    }
    if ($offPts.Count -gt 1) { $g.DrawLines($penOff, $offPts.ToArray()) }
    if ($onPts.Count -gt 1) { $g.DrawLines($penOn, $onPts.ToArray()) }

    # Maqsad nuqtalari (kulrang marker)
    $tgtRows = $targets | Where-Object { $_.panel -eq $panel }
    foreach ($tr in $tgtRows) {
        $v = [double]$tr.V_V
        $i_mA = [double]$tr.I_mA
        if ($v -lt $xMin -or $v -gt $xMax -or $i_mA -lt $yMin -or $i_mA -gt $yMax) { continue }
        $px = ToPx $v $xMin $xMax $plotX0 $plotW
        $py = ToPy $i_mA $yMin $yMax $plotY0 $plotH
        $g.FillEllipse($brushTarget, $px-5, $py-5, 10, 10)
    }

    # O'q belgilari (min/max qiymatlar)
    $g.DrawString("$xMin", $fontAxis, $brushText, $plotX0-10, $plotY0+$plotH+3)
    $g.DrawString("$xMax", $fontAxis, $brushText, $plotX0+$plotW-15, $plotY0+$plotH+3)
    $g.DrawString([string]$yMax, $fontAxis, $brushText, $plotX0-$margin+15, $plotY0-8)
    $g.DrawString([string]$yMin, $fontAxis, $brushText, $plotX0-$margin+15, $plotY0+$plotH-8)
}

# Umumiy sarlavha va izoh
$fontTitle = New-Object System.Drawing.Font("Arial", 16, [System.Drawing.FontStyle]::Bold)
$g.DrawString("Model2_FET vs Fig.1 (Troyan & Doronin 2021) - havo rangi=OFF, to'q sariq=ON, kulrang=maqola nishoni",
    $fontAxis, $brushText, 10, $Hgrid+12)

$bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
Write-Output "Saqlandi: $outPath"
