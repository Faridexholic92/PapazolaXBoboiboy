# Atmospheric video-loop prototype

File: `assets/papazola-boboiboy-atmosphere-loop.mp4`.

- 10-second periodic camera/particle loop, 1280×720, 24 fps.
- H.264 Main, yuv420p, fast-start MP4, no audio stream.
- Built from the AI-edited background using a periodic zoom/pan and animated warm particles.
- **The characters are static artwork. This is not a generated running-character animation, animated 3D rig or a full-motion character video.** A supplied suitable animated video or character-animation pipeline is needed for that request.
- The prototype uses the MP4 as a wallpaper layer, with real UI text and icons on top, not burned into the movie.
- Reduced-motion and page-hidden states pause playback; a static background remains as a fallback.
- Native Android playback is not implemented yet. The Android port should loop muted, pause when hidden, handle decoder errors, preserve a still-image fallback and respect reduced motion. Physical-TV decoder/performance checks are required before any stable designation.

The video is kept as base64 text for MCP transport and decoded to the binary MP4 on the design branch by `.github/workflows/design-assets.yml`. `render-atmosphere.py` and `render-atmosphere.sh` reproduce the loop after extracting `wallpaper.jpg` from `index.html`.
