package org.phone_lab.jouler.joulerdemo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IJoulerPolicy;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.phone_lab.jouler.joulerdemo.R;
import org.phone_lab.jouler.joulerdemo.services.DemoService;
import org.phone_lab.jouler.joulerbase.IJoulerBaseService;

import java.util.Iterator;


public class MainActivity extends Activity {
    public final static String TAG = "JoulerDemo";

    private static boolean joulerBaseExist;
    private static boolean permissionGranted;
    private boolean mBound;
    private boolean iJoulerBaseServiceBound;
    private DemoService mService;
    private IJoulerBaseService iJoulerBaseService;

    private ServiceConnection joulerBaseConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            iJoulerBaseService = IJoulerBaseService.Stub.asInterface(service);
            iJoulerBaseServiceBound = true;
            try {
                if (!iJoulerBaseService.checkPermission()) {
                    // ask to get permission;
                    startJoulerBase();
//                } else {
//                    iJoulerBaseService.test("Demo", "app");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            iJoulerBaseService = null;
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            DemoService.LocalBinder binder = (DemoService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        joulerBaseExist = checkJoulerBaseExist();
        if (joulerBaseExist) {
            permissionGranted = checkJoulerBasePermission();
            if (permissionGranted) {
//                Toast.makeText(this, "Got permission", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_main);
            } else {
                Toast.makeText(this, "No permission, please reinstall this app!", Toast.LENGTH_SHORT).show();
            }
        } else {
            setContentView(R.layout.no_jouler_base_install);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!joulerBaseExist) { return; }
        if (!permissionGranted) { return; }
        if (!mBound) {
            Intent intent = new Intent(this, DemoService.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
        if (!iJoulerBaseServiceBound) {
            Intent intent = new Intent(IJoulerBaseService.class.getName());
            bindService(intent, joulerBaseConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!joulerBaseExist) { return; }
        if (!permissionGranted) { return; }
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        if (iJoulerBaseServiceBound) {
            unbindService(joulerBaseConnection);
            iJoulerBaseServiceBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void parseJSON(String src) {
        try {
            JSONObject json = new JSONObject(src);
            Iterator<String> e = json.keys();
            while (e.hasNext()) {
                String key = e.next();
                String onePackage = json.get(key).toString();
                JSONObject uidStats = new JSONObject(onePackage);
                Iterator<String> uidStatsE = uidStats.keys();
                while (uidStatsE.hasNext()) {
                    String attribute = uidStatsE.next();
                    Log.d(TAG, "Package name: " + key + "; " + attribute + ": " + uidStats.get(attribute));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void click(View view) {
        if (!iJoulerBaseServiceBound) {
            Toast.makeText(this, "iJoulerBaseService haven't bounded!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int uid = 0;
            try {
                PackageManager pm = getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
                uid = packageInfo.applicationInfo.uid;
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "Get uid error: " + e.toString());
            }

            Log.d(TAG, "my uid is: " + uid);
            int priority = 0;

            switch (view.getId()) {
                case R.id.getStatistics:
                    String result = iJoulerBaseService.getStatistics();
                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                    parseJSON(result);
                    break;
                case R.id.controlCpuMaxFrequency:
                    int freq = 1024;
//                    iJoulerBaseService.controlCpuMaxFrequency(freq);
                    break;
                case R.id.getAllCpuFrequencies:
                    int[] cpuFreqs =iJoulerBaseService.getAllCpuFrequencies();
                    break;
                case R.id.getPriority:
                    priority = iJoulerBaseService.getPriority(uid);
                    break;
                case R.id.resetPriority:
                    iJoulerBaseService.resetPriority(uid, priority);
                    break;
                case R.id.addRateLimitRule:
                    iJoulerBaseService.addRateLimitRule(uid);
                    break;
                case R.id.delRateLimitRule:
                    iJoulerBaseService.delRateLimitRule(uid);
                    break;
                default:
                    Toast.makeText(this, "Impossible button.", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (RemoteException e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // this will catch Exception for double run add/del RateLimitRule.
            Log.d(TAG, e.toString());
            e.printStackTrace();
        }
    }

    private boolean checkJoulerBasePermission() {
        PackageManager packageManager = getPackageManager();
        String permissionName = getString(R.string.permission_name);
        if (PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(permissionName, getPackageName())) {
            // got permission skip
            return true;
        }
        // Please reinstall this app.
        return false;
//        finish();
    }

    private boolean checkJoulerBaseExist() {
        if (isPackageExisted(getResources().getString(R.string.jouler_base_packagename))) {
            //Alread installed, skip
            return true;
        }
        Toast.makeText(this, "Not install Jouler Base. Please go to install it.", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.no_jouler_base_app_title)
                .setMessage(R.string.no_jouler_base_app_content);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("market://details?id=" + getString(R.string.jouler_base_packagename)));
                startActivity(goToMarket);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    private boolean isPackageExisted(String targetPackage) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void startJoulerBase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.not_selected_in_jouler_base_title)
                .setMessage(R.string.not_selected_in_jouler_base_content);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(getResources().getString(R.string.jouler_base_packagename));
                LaunchIntent.putExtra(getString(R.string.call_baseapp_extra_source_name), getApplicationInfo().packageName);
                startActivity(LaunchIntent);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        return;
    }
}
