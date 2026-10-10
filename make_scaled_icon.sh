convert -size 512x512 xc:black \
  -stroke white -fill none -strokewidth 24 \
  -draw "stroke-linecap round stroke-linejoin round \
         path 'M 92,272 A 164,164 0 0,1 420,272' \
         path 'M 142,234 L 336,234 A 25,25 0 0,0 361,209 A 25,25 0 0,0 336,184 L 300,184' \
         path 'M 154,272 L 348,272 A 25,25 0 0,1 373,297 A 25,25 0 0,1 348,322 L 312,322' \
         line 208,360 176,404 \
         line 268,360 236,404 \
         line 328,360 296,404" \
  icon_master_scaled.png
ls -l icon_master_scaled.png
