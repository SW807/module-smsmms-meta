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
    private Uri dbUri;

    public SMSListener(Context context) {
        resolver = context.getContentResolver();
        dbUri = Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + "smsmmsmeta");
    }

    public void startSensor() {
    }

    public void stopSensor() {
    }

    private void loadData() {
        Cursor cursor = resolver.query(dbUri, new String[]{"MAX(time)"}, null, null, null);

        long last = 0;
        if (cursor.moveToNext())
            last = cursor.getLong(0);
    }

    @Override
    public void sensorParameters(Intent intent) {
    }
}
