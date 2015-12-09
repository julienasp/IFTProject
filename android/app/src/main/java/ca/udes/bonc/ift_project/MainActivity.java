package ca.udes.bonc.ift_project;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import ca.udes.bonc.ift_project.communication.QueryEventService;
import ca.udes.bonc.ift_project.communication.QueryIntentService;
import ca.udes.bonc.ift_project.communication.RestApiResultReceiver;
import ca.udes.bonc.ift_project.fragment.ListFragment;
import ca.udes.bonc.ift_project.fragment.MapFragment;
import ca.udes.bonc.ift_project.fragment.MyEventFragment;
import ca.udes.bonc.ift_project.fragment.NewEventFragment;
import ca.udes.bonc.ift_project.fragment.OnFragmentInteractionListener;
import ca.udes.bonc.ift_project.utils.AlertDialogManager;
import ca.udes.bonc.ift_project.utils.ConnectionDetector;


public class MainActivity extends FragmentActivity
implements NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener,
        RestApiResultReceiver.Receiver {

    public static FragmentManager fragmentManager;
    public RestApiResultReceiver mReceiver;
    private String TAG = "mainActivity";

    // Connection detector class
    private ConnectionDetector cd;
    // Alert Dialog Manager
    private AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReceiver = new RestApiResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        fragmentManager = getSupportFragmentManager();

        cd = new ConnectionDetector(getApplicationContext());
        // Check if Internet present
        if (! (cd.isConnectingToInternet())) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            //TODO : handle no connexion (cache / db etc..)
            return;
        }


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Create error
        //setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Fragment fragment = new MapFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });*/

        //test

        //TODO define category and get date from datepicker
        //QueryIntentService.startActionCreateEvent(this, mReceiver, "10.2", "23", "EVERYTHING IS AWSOME", "chill",12,"fun");
        QueryEventService.startActionGetMarkers(this, mReceiver, "10.2", "23");


    }
    //call when service send to receiver
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case QueryIntentService.STATUS_RUNNING:
                Log.i(TAG, "Runing status");
                //show progress
                break;
            case QueryIntentService.STATUS_FINISHED:
                String results = resultData.getString("results");
                Log.i(TAG, "result =");
                Log.i(TAG, results);
                // do something interesting
                // hide progress
                break;
            case QueryIntentService.STATUS_ERROR:
                //todo handl error
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Log.d(TAG, "error = " + error);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
        Fragment selectedFragment = null;

        switch (id){
            case R.id.nav_maps:
                selectedFragment = new MapFragment();
                break;
            case R.id.nav_list:
                selectedFragment = new ListFragment();
                break;
            case R.id.nav_newevent:
                selectedFragment = new NewEventFragment();
                break;
            case R.id.nav_my_event:
                selectedFragment = new MyEventFragment();
                break;
            default:
                break;
        }
        setTitle(item.getTitle());

        if((selectedFragment!= null) && (selectedFragment != currentFragment))
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, selectedFragment)
                    .commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onFragmentInteraction(Uri uri) {
        Log.i(this.toString(), uri.toString());
    }
}
