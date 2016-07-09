package shubhamjha33.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import shubhamjha33.sunshine.app.data.WeatherContract;
import shubhamjha33.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{

    public static String LOG_CLASS_NAME="MainActivity";

    private String mLocation;
    private static final String DETAILFRAGMENT_TAG="DFTAG";
    private static boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_CLASS_NAME, "onCreate Called");
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.weather_detail_container)!=null){
            mTwoPane=true;
            if(savedInstanceState==null){
                DetailFragment df=new DetailFragment();
                Uri dateUri=WeatherContract.WeatherEntry.buildWeatherLocationWithDate(Utility.getPreferredLocation(this),System.currentTimeMillis());
                Bundle b=new Bundle();
                b.putString("dateUri",dateUri.toString());
                df.setArguments(b);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.weather_detail_container,df,DETAILFRAGMENT_TAG).
                        commit();
            }
        }
        else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        ForecastFragment ff=(ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        ff.setUseTodayLayout(mTwoPane);
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.v(LOG_CLASS_NAME,"onDestroy Called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_CLASS_NAME, "onPause Called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(LOG_CLASS_NAME, "onRestart Called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_CLASS_NAME, "onResume Called");
        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                Log.v(LOG_CLASS_NAME,"calling onLocationChanged");
                ff.onLocationChanged();
            }
            else{
                Log.v(LOG_CLASS_NAME,"ForecastFrgament not found");
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_CLASS_NAME, "onStart called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_CLASS_NAME, "onStop called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId()==R.id.action_map){
            Intent mapIntent=new Intent(Intent.ACTION_VIEW);
            SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
            String query=sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
            Uri uri=Uri.parse("geo:0,0").buildUpon().appendQueryParameter("q",query).build();
            mapIntent.setData(uri);
            if(mapIntent.resolveActivity(getPackageManager())!=null){
                startActivity(mapIntent);
            }
            else{
                Toast toast=Toast.makeText(this,"No Map App available",Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri date) {
        if(mTwoPane){
            DetailFragment df= new DetailFragment();
            Bundle b=new Bundle();
            b.putString("dateUri",date.toString());
            df.setArguments(b);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.weather_detail_container, df, DETAILFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
        else{
            Intent intent=new Intent(this,DetailActivity.class);
            intent.setData(date);
            startActivity(intent);
        }
    }
}
