package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilites;

/**
 * Created by Kenneth on 3/13/2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListWidgetRemoteViewsService extends RemoteViewsService {
    private static final String LOG_TAG = ListWidgetRemoteViewsService.class.getSimpleName();
    private static final String POSITION_LABEL = "POSITION";
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

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            private Resources resources = getApplicationContext().getResources();

            @Override
            public void onCreate() {}

            @Override
            public void onDataSetChanged() {
                //called when notifyAppWidgetViewDataChanged is called on the remote Adapter
                //basically, clears and reqeuries the data from the database

                if(data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                //gets Today in current time and SimpleDateFormat
                Date fragmentdate = new Date(System.currentTimeMillis()+ ((-2) * 86400000));
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String todayDate = mformat.format(fragmentdate);

                //query for the 1st entry of today's scores
                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        SCORES_COLUMNS, null, new String[]{todayDate}, DatabaseContract.scores_table.HOME_GOALS_COL + " ASC");

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                //we are at the right position, get the data now
                String home_name = data.getString(COL_HOME_NAME);
                String away_name = data.getString(COL_AWAY_NAME);
                int home_score = data.getInt(COL_HOME_GOALS);
                int away_score = data.getInt(COL_AWAY_GOALS);
                String match_time = data.getString(COL_MATCH_DATE);


                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);

                views.setImageViewResource(R.id.widget_list_home_crest, Utilites.getTeamCrestByTeamName(home_name));
                views.setImageViewResource(R.id.widget_list_away_crest, Utilites.getTeamCrestByTeamName(away_name));

                //adding content description to the crest icons
                views.setContentDescription(R.id.widget_list_home_crest,
                        String.format(resources.getString
                        (R.string.home_crest_content_description), home_name));
               views.setContentDescription(R.id.widget_list_away_crest, String.format(resources.getString
                        (R.string.away_crest_content_description), away_name));

                views.setTextViewText(R.id.widget_list_home_name, home_name);
                views.setTextViewText(R.id.widget_list_away_name, away_name);
                views.setTextViewText(R.id.widget_list_score_textview, Utilites.getScores(home_score, away_score));
                views.setTextViewText(R.id.widget_list_data_textview, match_time);

                //handle clicking intent here
                final Intent fillInIntent = new Intent(ListWidgetProvider.ACTION_DETAIL_CLICK);
                fillInIntent.putExtra(POSITION_LABEL, position);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(data.moveToPosition(position)) {
                    return data.getLong(COL_MATCH_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
