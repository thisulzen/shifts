package app.shifts;

import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

class Cal {
    static final String URL = "https://drive.google.com/uc?export=download&id=1WNVxJV6F6zKBrmSmIQ3jpyskLf7cnIcV";
    static final int KITCHEN = 0xFFE8890C, JB = 0xFF2F6FDE, BG = 0xFF1B1B1F;

    // blocking network call, run off the main thread
    static void fetch(Context c) {
        try {
            HttpURLConnection h = (HttpURLConnection) new URL(URL).openConnection();
            h.setConnectTimeout(8000);
            h.setReadTimeout(8000);
            BufferedReader r = new BufferedReader(new InputStreamReader(h.getInputStream()));
            StringBuilder b = new StringBuilder();
            String l;
            while ((l = r.readLine()) != null) b.append(l);
            r.close();
            String j = b.toString();
            new JSONObject(j); // reject junk such as a Drive error page
            c.getSharedPreferences("s", 0).edit().putString("j", j).apply();
        } catch (Exception e) { /* keep the cached copy */ }
    }

    static Bitmap draw(Context c, int w, int h, int off) {
        // date -> job letters, plus a sorted list of shifts for the footer
        Map<String, String> jobs = new HashMap<>();
        List<String[]> all = new ArrayList<>();
        try {
            JSONArray a = new JSONObject(c.getSharedPreferences("s", 0)
                    .getString("j", "{\"shifts\":[]}")).getJSONArray("shifts");
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                String d = o.getString("d"), j = o.getString("j");
                String prev = jobs.get(d);
                jobs.put(d, prev == null ? j : prev + j);
                all.add(new String[]{d, o.getString("s"), o.getString("e")});
            }
        } catch (Exception e) { /* empty calendar */ }
        Collections.sort(all, (x, y) -> (x[0] + x[1]).compareTo(y[0] + y[1]));

        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(BG);
        cv.drawRoundRect(0, 0, w, h, w * 0.06f, w * 0.06f, p);

        Calendar now = Calendar.getInstance();
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = iso.format(now.getTime());
        Calendar m = Calendar.getInstance();
        m.set(Calendar.DAY_OF_MONTH, 1);
        m.add(Calendar.MONTH, off);
        int lead = (m.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Monday first
        int days = m.getActualMaximum(Calendar.DAY_OF_MONTH);
        int rows = (lead + days + 6) / 7;

        float pad = w * 0.04f, cw = (w - 2 * pad) / 7f;
        float titleH = h * 0.10f, headH = h * 0.07f, footH = h * 0.09f;
        float ch = (h - 2 * pad - titleH - headH - footH) / rows;
        float r = Math.min(cw, ch) * 0.40f;

        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(Color.WHITE);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(titleH * 0.6f);
        cv.drawText(new SimpleDateFormat("MMMM yyyy", Locale.US).format(m.getTime()),
                w / 2f, pad + titleH * 0.7f, p);

        p.setTypeface(Typeface.DEFAULT);
        p.setTextSize(headH * 0.65f);
        p.setColor(0xFF8A8A93);
        String[] dn = {"M", "T", "W", "T", "F", "S", "S"};
        for (int i = 0; i < 7; i++)
            cv.drawText(dn[i], pad + cw * (i + 0.5f), pad + titleH + headH * 0.75f, p);

        float top = pad + titleH + headH;
        String ym = new SimpleDateFormat("yyyy-MM-", Locale.US).format(m.getTime());
        for (int d = 1; d <= days; d++) {
            int cell = lead + d - 1;
            float cx = pad + cw * (cell % 7 + 0.5f), cy = top + ch * (cell / 7 + 0.5f);
            String key = ym + (d < 10 ? "0" : "") + d;
            String j = jobs.get(key);
            boolean mine = j != null;
            if (mine) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(j.contains("k") ? KITCHEN : JB);
                cv.drawCircle(cx, cy, r, p);
                if (j.contains("k") && j.contains("jb")) { // double shift gets a blue ring
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(r * 0.18f);
                    p.setColor(JB);
                    cv.drawCircle(cx, cy, r, p);
                }
            }
            if (key.equals(today)) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(r * 0.12f);
                p.setColor(Color.WHITE);
                cv.drawCircle(cx, cy, r + r * 0.12f, p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setColor(mine ? Color.WHITE : 0xFFC9C9D1);
            p.setTypeface(mine ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            p.setTextSize(r * 0.95f);
            cv.drawText(String.valueOf(d), cx, cy + r * 0.33f, p);
        }

        String foot = "No upcoming shifts";
        for (String[] s : all) {
            if (s[0].compareTo(today) >= 0) {
                try {
                    foot = new SimpleDateFormat("EEE d MMM", Locale.US).format(iso.parse(s[0]))
                            + "  " + s[1] + "-" + s[2];
                } catch (Exception e) { foot = s[0] + "  " + s[1] + "-" + s[2]; }
                break;
            }
        }
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.DEFAULT);
        p.setColor(Color.WHITE);
        p.setTextSize(footH * 0.5f);
        cv.drawText(foot, w / 2f, h - pad - footH * 0.25f, p);
        return bm;
    }
}
