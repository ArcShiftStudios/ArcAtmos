SRC="/app/applet/icon_master_scaled.png"
BASE="/app/applet/app/src/main/res"
cp "$SRC" "$BASE/drawable/arc_weather_logo.png"

for spec in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  dir="$BASE/mipmap-${spec%:*}"
  size="${spec#*:}"
  rm -f "$dir/ic_launcher.webp" "$dir/ic_launcher_round.webp"
  convert "$SRC" -resize "${size}x${size}!" "PNG32:$dir/ic_launcher.png"
  radius=$((size / 2))
  convert "$SRC" -resize "${size}x${size}!" \
    \( -size "${size}x${size}" xc:none -fill white \
       -draw "circle $radius,$radius $radius,0" \) \
    -alpha set -compose DstIn -composite "PNG32:$dir/ic_launcher_round.png"
done
ls -la $BASE/mipmap-*/ic_launcher*
