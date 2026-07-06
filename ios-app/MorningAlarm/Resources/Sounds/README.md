# Alarm sound files

Drop your alarm sound files here as `.caf` audio (Core Audio Format), named to
match the `soundName` values used in `Alarm.soundName` (default: `classic_alarm.caf`).

Local notifications on iOS only support bundled sounds up to 30 seconds; the
in-app looping (via `AlarmAudioPlayer`) plays the same file on a loop once the
ringing screen is open, so a shorter, seamlessly-loopable clip (3-8s) works best.

Convert any WAV/MP3 to `.caf` with `afconvert`, e.g.:

```
afconvert -f caff -d ios_max input.wav classic_alarm.caf
```

Add the resulting file(s) to this folder before building — without at least
one `.caf` file here, `AlarmAudioPlayer` will fail to find a sound to play.
