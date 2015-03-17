package dk.aau.cs.psylog.sensor.smsmmsmeta;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import dk.aau.cs.psylog.module_lib.DBAccessContract;
import dk.aau.cs.psylog.module_lib.ISensor;

public class SMSListener implements ISensor {
    private SmsManager smsManager;

    public SMSListener(Context context) {
        smsManager = SmsManager.getDefault();
    }

    public void startSensor() {
    }

    public void stopSensor() {
    }

    @Override
    public void sensorParameters(Intent intent) {
    }
}
