package app.shifts;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;

public class Main extends Activity {
    int off = 0;
    ImageView v;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        v = new ImageView(this);
        v.setBackgroundColor(Cal.BG);
        v.setScaleType(ImageView.ScaleType.FIT_START);
        // tap left half = previous month, right half = next
        v.setOnTouchListener((x, e) -> {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                off += e.getX() < x.getWidth() / 2f ? -1 : 1;
                draw();
            }
            return true;
        });
        setContentView(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        draw();
        new Thread(() -> {
            Cal.fetch(this);
            runOnUiThread(this::draw);
        }).start();
    }

    void draw() {
        DisplayMetrics d = getResources().getDisplayMetrics();
        v.setImageBitmap(Cal.draw(this, d.widthPixels, (int) (d.widthPixels * 1.25f), off));
    }
}
