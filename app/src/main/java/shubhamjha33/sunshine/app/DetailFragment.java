package shubhamjha33.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import shubhamjha33.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static String mForecastStr;
    private static ShareActionProvider mShareActionProvider;
    private static Intent mShareIntent;
    private static ViewHolder mViewHolder;
    private static Uri mUri;
    private static int DETAIL_ID=101;

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_ID, null, this);
        }
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView dayView;
        public final TextView humidityView;
        public final TextView pressureView;
        public final TextView windView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            dayView = (TextView) view.findViewById(R.id.detail_day_textview);
            humidityView=(TextView)view.findViewById(R.id.detail_humidity_textview);
            pressureView=(TextView)view.findViewById(R.id.detail_pressure_textview);
            windView=(TextView)view.findViewById(R.id.detail_wind_textview);
        }
    }

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem=menu.findItem(R.id.action_share);
        mShareActionProvider=(ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mShareIntent=new Intent(Intent.ACTION_SEND);
        mShareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT,mForecastStr + " #SunshineApp");
        mShareActionProvider.setShareIntent(mShareIntent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_settings){
            Intent intent=new Intent(getActivity(),SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId()==R.id.action_share){
            startActivity(mShareIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_detail, container, false);
        mViewHolder=new ViewHolder(rootView);
        Intent intent=getActivity().getIntent();
        Bundle b=getArguments();
        if(intent==null||intent.getData()==null) {
            Log.v("TabDebug","From bundle");
            mForecastStr = b.getString("dateUri");
        }
        else {
            Log.v("TabDebug", "From intent");
            mForecastStr = intent.getDataString();
        }
        Log.v("TabDebug",mForecastStr);
        mUri=Uri.parse(mForecastStr);
        getLoaderManager().initLoader(DETAIL_ID,null,this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v("TabDebug","In onCreateLoader");
        return new CursorLoader(getContext(),mUri,WeatherContract.FORECAST_COLUMNS,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("TabDebug","Loading of data finished");
        if(data!=null&&data.moveToFirst()){
            Log.v("TabDebug","Updating view");
            mViewHolder.dayView.setText(Utility.getDayName(getActivity(),data.getLong(WeatherContract.COL_WEATHER_DATE)));
            String dateString=Utility.getFormattedMonthDay(getActivity(), data.getLong(WeatherContract.COL_WEATHER_DATE));
            mViewHolder.dateView.setText(dateString);
            String weatherString=data.getString(WeatherContract.COL_WEATHER_DESC);
            mViewHolder.descriptionView.setText(weatherString);
            boolean isMetric=Utility.isMetric(getActivity());
            String high=Utility.formatTemperature(getContext(), data.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP), isMetric);
            String low=Utility.formatTemperature(getContext(),data.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP),isMetric);
            mViewHolder.highTempView.setText(high);
            mViewHolder.lowTempView.setText(low);
            int weatherConditionId=data.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
            mViewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherConditionId));
            mViewHolder.pressureView.setText(getContext().getString(R.string.format_pressure,data.getDouble(WeatherContract.COL_PRESSURE)));
            int windSpeedFormat=R.string.format_wind_mph;
            if(isMetric){
                windSpeedFormat=R.string.format_wind_kmh;
            }
            mViewHolder.windView.setText(Utility.getFormattedWind(getActivity(),data.getFloat(WeatherContract.COL_WIND_SPEED),data.getFloat(WeatherContract.COL_DEGREES)));
            mViewHolder.humidityView.setText(getContext().getString(R.string.format_humidity,data.getDouble(WeatherContract.COL_HUMIDITY)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
