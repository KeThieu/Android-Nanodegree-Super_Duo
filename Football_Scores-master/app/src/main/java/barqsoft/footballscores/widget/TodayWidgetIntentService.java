package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.PagerFragment;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;
import barqsoft.footballscores.Utilites;

/**
 * Created by Kenneth on 3/11/2016.
 */
public class TodayWidgetIntentService extends IntentService {

    private static final String LOG_TAG = TodayWidgetIntentService.class.getSimpleName();
    private static final String [] SCORES_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL,
    };

    private static final int COL_MATCH_ID = 0;
    private static final int COL_HOME_NAME = 1;
    private static final int COL_AWAY_NAME = 2;
    private static final int COL_HOME_GOALS = 3;
    private static final int COL_AWAY_GOALS = 4;
    private static final int COL_MATCH_DATE = 5;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayWidgetProvider.class));

        Date fragmentdate = new Date(System.currentTimeMillis() + ((-2) * 86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = mformat.format(fragmentdate);

        //query for the 1st entry of today's scores
        Cursor cursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                SCORES_COLUMNS, null, new String[] {todayDate}, DatabaseContract.scores_table.HOME_GOALS_COL + " ASC");

        if(cursor == null) {
            emptyView(appWidgetManager, appWidgetIds, true);
            return;
        }
        if(!cursor.moveToFirst()) {
            //Log.v(LOG_TAG, "Empty Cursor!");
            cursor.close();
            emptyView(appWidgetManager, appWidgetIds, true);
            return;
        }

        emptyView(appWidgetManager, appWidgetIds, false);

        //Log.v(LOG_TAG, "We have something in the cursor! Proceed");
        String home_name = cursor.getString(COL_HOME_NAME);
        String away_name = cursor.getString(COL_AWAY_NAME);
        int home_score = cursor.getInt(COL_HOME_GOALS);
        int away_score = cursor.getInt(COL_AWAY_GOALS);
        String match_time = cursor.getString(COL_MATCH_DATE);

        cursor.close();

        //iterate through all today widgets
        for(int id : appWidgetIds) {
            int layoutId = R.layout.widget_today;
            RemoteViews views = new RemoteViews(this.getPackageName(), layoutId);

            views.setImageViewResource(R.id.widget_today_home_crest, Utilites.getTeamCrestByTeamName(home_name));
            views.setImageViewResource(R.id.widget_today_away_crest, Utilites.getTeamCrestByTeamName(away_name));
            /*
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, home_name, away_name);
            }
            */
            views.setTextViewText(R.id.widget_today_home_team, home_name);
            views.setTextViewText(R.id.widget_today_away_team, away_name);
            views.setTextViewText(R.id.widget_today_score_textview, Utilites.getScores(home_score, away_score));
            views.setTextViewText(R.id.widget_today_data_textview, match_time);

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent
                    .getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_today_main, pendingIntent);
            appWidgetManager.updateAppWidget(id, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String homeDescription, String awayDescription) {
        views.setContentDescription(R.id.widget_today_home_crest, homeDescription);
        views.setContentDescription(R.id.widget_today_away_crest, awayDescription);
    }

    public void emptyView(AppWidgetManager appWidgetManager, int[] AppWidgetIds, boolean onOff) {
        for(int id : AppWidgetIds) {
            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_today);
            if(onOff) {
                views.setViewVisibility(R.id.widget_today_empty, View.VISIBLE);
                Intent launchIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent
                        .getActivity(this, 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget_today_main, pendingIntent);
                appWidgetManager.updateAppWidget(id, views);
            } else {
                views.setViewVisibility(R.id.widget_today_empty, View.INVISIBLE);
            }
        }
    }
}
