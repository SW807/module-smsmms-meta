package dk.aau.cs.psylog.sensor.smsmmsmeta;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms;

import dk.aau.cs.psylog.module_lib.DBAccessContract;
import dk.aau.cs.psylog.module_lib.ISensor;

public class SMSListener implements ISensor {
    private ContentResolver resolver;
    private Uri dbUri;

    private static final String CONTACT = "contact";
    private static final String TIME = "time";
    private static final String LENGTH = "length";
    private static final String INCOMING = "incoming";

    public SMSListener(Context context) {
        resolver = context.getContentResolver();
        dbUri = Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + "smsmmsmeta");
    }

    public void startSensor() {
    }

    public void stopSensor() {
    }

    private void loadData() {
        Cursor cursor = resolver.query(dbUri, new String[]{"MAX(" + TIME + ")"}, null, null, null);

        long last = 0;
        if (cursor.moveToNext())
            last = cursor.getLong(0);

        loadSms(last);
    }

    private void loadSms(long lastdate) {
        String dateFilter = Sms.Inbox.DATE + " > " + String.valueOf(lastdate);

        String[] inboxCols = new String[]{Sms.Inbox.ADDRESS, Sms.Inbox.BODY, Sms.Inbox.DATE};
        Cursor inbox = resolver.query(Sms.Inbox.CONTENT_URI, inboxCols, dateFilter, null, null);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(CONTACT, inbox.getString(0));
            values.put(LENGTH, inbox.getString(1).length());
            values.put(TIME, inbox.getLong(2));
            values.put(INCOMING, true);
            resolver.insert(dbUri, values);
        }

        String[] sentCols = new String[]{Sms.Inbox.ADDRESS, Sms.Inbox.BODY, Sms.Inbox.DATE_SENT};
        Cursor outbox = resolver.query(Sms.Sent.CONTENT_URI, sentCols, dateFilter, null, null);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(CONTACT, inbox.getString(0));
            values.put(LENGTH, inbox.getString(1).length());
            values.put(TIME, inbox.getLong(2));
            values.put(INCOMING, false);
            resolver.insert(dbUri, values);
        }
    }

    @Override
    public void sensorParameters(Intent intent) {
    }
}
