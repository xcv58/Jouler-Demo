package org.phone_lab.jouler.joulerdemo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.phone_lab.jouler.joulerdemo.services.DemoService;
import org.phone_lab.jouler.joulerdemo.utils.Utils;

/**
 * Created by xcv58 on 2/3/15.
 */
public class StartStopReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || action.isEmpty() || action.equals("")) {
            Log.d(Utils.TAG, "Receive empty action.");
            return;
        }
        Log.d(Utils.TAG, "Receive action: " + action);
        Intent serviceIntent = new Intent(context, DemoService.class);
        if (action.equals(Utils.START_ACTION)) {
            context.startService(serviceIntent);
        } else if (action.equals(Utils.STOP_ACTION)) {
            context.stopService(serviceIntent);
        } else {
            Log.d(Utils.TAG, "Receive unknown action: " + action);
        }
    }
}
