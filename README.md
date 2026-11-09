# Shifts

Android home-screen widget and small app that show a month calendar with your work shifts.
Kitchen shifts are orange, JB Hi-Fi shifts are blue, today has a white ring, and the
bottom line shows your next shift.

The app reads a JSON feed (`URL` in `Cal.java`) every 30 minutes and when opened.
Feed format: `{"shifts":[{"d":"2026-11-10","s":"11:00","e":"15:00","j":"jb"}]}` with `j` set to `k` or `jb`.

Build: push to GitHub, open the Actions tab, download the `shifts-apk` artifact.
