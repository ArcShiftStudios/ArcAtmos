convert -size 512x512 xc:black \
  -stroke white -fill none -strokewidth 16 \
  -draw "stroke-linecap round stroke-linejoin round \
         path 'M 152,272 A 104,104 0 0,1 360,272' \
         path 'M 184,248 L 306,248 A 16,16 0 0,0 322,232 A 16,16 0 0,0 306,216 L 284,216' \
         path 'M 192,272 L 314,272 A 16,16 0 0,1 330,288 A 16,16 0 0,1 314,304 L 292,304' \
         line 226,328 206,356 \
         line 264,328 244,356 \
         line 302,328 282,356" \
  icon_master.png
ls -l icon_master.png
