package dk.aau.cs.psylog.sensor.smsmmsmeta;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Mms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        loadMms(last);
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

    private void loadMms(long lastdate) {
    }

    private int getMmsLength(long mmsId) {
        String selectionPart = "mid=" + mmsId;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = resolver.query(uri, null,
                selectionPart, null, null);

        int length = 0;

        if (cursor.moveToFirst()) {
            do {
                String partId = cursor.getString(cursor.getColumnIndex("_id"));
                String type = cursor.getString(cursor.getColumnIndex("ct"));
                if ("text/plain".equals(type)) {
                    String data = cursor.getString(cursor.getColumnIndex("_data"));
                    String body;
                    if (data != null)
                        body = getMmsText(partId);
                    else
                        body = cursor.getString(cursor.getColumnIndex("text"));
                    length += body.length();
                }
            } while (cursor.moveToNext());
        }

        return length;
    }

    private String getMmsText(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = resolver.openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void sensorParameters(Intent intent) {
    }
}
