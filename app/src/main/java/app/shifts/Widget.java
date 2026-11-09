package app.shifts;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        push(c, m, ids); // cached data first
        final PendingResult pr = goAsync();
        new Thread(() -> {
            try {
                Cal.fetch(c);
                push(c, m, ids);
            } finally {
                pr.finish();
            }
        }).start();
    }

    static void push(Context c, AppWidgetManager m, int[] ids) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget);
        rv.setImageViewBitmap(R.id.img, Cal.draw(c, 480, 480, 0));
        rv.setOnClickPendingIntent(R.id.img, PendingIntent.getActivity(c, 0,
                new Intent(c, Main.class), PendingIntent.FLAG_IMMUTABLE));
        m.updateAppWidget(ids, rv);
    }
}
