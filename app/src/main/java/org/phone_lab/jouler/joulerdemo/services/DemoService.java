package org.phone_lab.jouler.joulerdemo.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DemoService extends Service {

    public class LocalBinder extends Binder {
        public DemoService getService() {
            return DemoService.this;
        }
    }

    public DemoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new LocalBinder();
    }
}
