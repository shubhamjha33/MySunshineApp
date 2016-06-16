package shubhamjha33.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import shubhamjha33.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ForecastAdapter mForecastAdapter;
    public static final int LOADER_ID=101;
    private static Callback mCallback;
    private ListView mListView;
    private int mPosition;

    public ForecastFragment() {
    }

    public interface Callback{
        public void onItemSelected(Uri date);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback=(Callback)context;
        }catch (ClassCastException ex){
            throw new ClassCastException(context.toString()
                    + " must implement Callback");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mPosition=0;
        setHasOptionsMenu(true);
    }

    public void setUseTodayLayout(boolean mTwoPane){
        if(mForecastAdapter!=null)
            mForecastAdapter.setmUseTodayLayout(!mTwoPane);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_main, container, false);
        String locationSetting=Utility.getPreferredLocation(getActivity());
        String sortOrder= WeatherContract.WeatherEntry.COLUMN_DATE+" ASC";
        Uri weatherForLocationUri= WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        Cursor cur=getActivity().getContentResolver().query(weatherForLocationUri,WeatherContract.FORECAST_COLUMNS,null,null,sortOrder);
        mForecastAdapter =new ForecastAdapter(getActivity(),cur,0);
        mListView=(ListView)rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        if(savedInstanceState!=null&&savedInstanceState.containsKey("scrollPosition")) {
            mPosition=savedInstanceState.getInt("scrollPosition",0);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri dateUri=WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(WeatherContract.COL_WEATHER_DATE));
                    mCallback.onItemSelected(dateUri);
                    //savedInstanceState.putInt("scrollPosition",position);
                    mPosition=position;
                    /*Log.v("PhoneDebug",dateUri.toString());
                    Intent intent=new Intent(getActivity(),DetailActivity.class);
                    intent.setData(dateUri);
                    startActivity(intent);*/
                }
            }
        });
        return rootView;
    }

    public void updateWeather(){
        Log.v("TabDebug","ForecastFragment: updateWeather");
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        String setLocation=sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        String setMeasureUnit=sharedPreferences.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));
        Log.v("Sunshine_ForecastFrag", setMeasureUnit);
        new FetchWeatherTask(getContext()).execute(setLocation, setMeasureUnit);
    }

    void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID,null,this);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting=Utility.getPreferredLocation(getActivity());
        String sortOrder= WeatherContract.WeatherEntry.COLUMN_DATE+" ASC";
        Uri weatherForLocationUri= WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),weatherForLocationUri,WeatherContract.FORECAST_COLUMNS,null,null,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        mListView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollPosition",mPosition);
    }

}
