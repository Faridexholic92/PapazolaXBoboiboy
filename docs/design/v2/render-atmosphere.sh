#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p video-frames assets
python3 extract-wallpaper.py
python3 render-atmosphere.py
ffmpeg -hide_banner -loglevel error -y -i wallpaper.jpg -framerate 24 -i video-frames/%03d.png -filter_complex "[0:v]zoompan=z='1.012+0.006*(1-cos(2*PI*on/240))':x='iw/2-iw/zoom/2+4*sin(2*PI*on/240)':y='ih/2-ih/zoom/2+2*sin(2*PI*on/240)':d=240:s=1280x720:fps=24[base];[base][1:v]overlay=shortest=1,format=yuv420p[out]" -map '[out]' -frames:v 240 -c:v libx264 -preset slow -crf 25 -profile:v main -level 3.1 -movflags +faststart -an assets/papazola-boboiboy-atmosphere-loop.mp4
