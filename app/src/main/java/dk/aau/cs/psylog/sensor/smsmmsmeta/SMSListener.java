package dk.aau.cs.psylog.sensor.smsmmsmeta;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import dk.aau.cs.psylog.module_lib.DBAccessContract;
import dk.aau.cs.psylog.module_lib.ISensor;

public class SMSListener implements ISensor {
    private ContentResolver resolver;

    public SMSListener(Context context) {
        resolver = context.getContentResolver();
    }

    public void startSensor() {
    }

    public void stopSensor() {
    }

    private void loadData() {
        Uri uri = Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + "smsmmsmeta");
        Cursor cursor = resolver.query(uri, new String[]{"MAX(time)"}, null, null, null);

        long last = 0;
        if (cursor.moveToNext())
            last = cursor.getLong(0);
    }

    @Override
    public void sensorParameters(Intent intent) {
    }
}
