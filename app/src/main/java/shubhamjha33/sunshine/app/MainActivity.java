package shubhamjha33.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import shubhamjha33.sunshine.app.data.WeatherContract;

public class MainActivity extends AppCompatActivity{

    public static String LOG_CLASS_NAME="MainActivity";

    private String mLocation;
    private static final String DETAILFRAGMENT_TAG="DFTAG";
    private static boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_CLASS_NAME, "onCreate Called");
        setContentView(R.layout.activity_main);
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
        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }
            mLocation = location;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_CLASS_NAME,"onStart called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_CLASS_NAME,"onStop called");
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

}
