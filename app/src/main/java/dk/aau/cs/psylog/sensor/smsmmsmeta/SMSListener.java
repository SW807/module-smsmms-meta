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
import dk.aau.cs.psylog.module_lib.IScheduledTask;

public class SMSListener implements IScheduledTask {
    private ContentResolver resolver;
    private Uri dbUri;

    private static final String CONTACT = "contact";
    private static final String DATE = "date";
    private static final String LENGTH = "length";
    private static final String INCOMING = "incoming";
    private static final String MODULE_NAME = "smsmmsdata";
    private static final String TABLE_NAME = "smsmmsdata";

    public SMSListener(Context context) {
        resolver = context.getContentResolver();
        dbUri = Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + MODULE_NAME + "_" + TABLE_NAME);
    }

    private void loadData() {
        Cursor cursor = resolver.query(dbUri, new String[]{"MAX(" + DATE + ")"}, null, null, null);

        long last = 0;
        if (cursor.moveToNext())
            last = cursor.getLong(0);

        loadSmsInbox(last);
        loadSmsSent(last);
        loadMmsInbox(last);
        loadMmsSent(last);
    }

    private void loadSmsInbox(long lastdate) {
        String[] columns = new String[]{Sms.Inbox.ADDRESS, Sms.Inbox.BODY, Sms.Inbox.DATE};

        Cursor inbox = getCursor(Sms.Inbox.CONTENT_URI, columns, lastdate);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(CONTACT, inbox.getString(0));
            values.put(LENGTH, inbox.getString(1).length());
            values.put(DATE, inbox.getLong(2));
            values.put(INCOMING, true);
            resolver.insert(dbUri, values);
        }

    }

    private Cursor getCursor(Uri uri, String[] columns, long lastdate){
        return resolver.query(uri, columns, Sms.Inbox.DATE + " > ?", new String[]{String.valueOf(lastdate)}, null);
    }

    private void loadSmsSent(long lastdate) {
        String[] columns = new String[]{Sms.Sent.ADDRESS, Sms.Sent.BODY, Sms.Sent.DATE_SENT};

        Cursor sent = getCursor(Sms.Sent.CONTENT_URI, columns, lastdate);
        while (sent.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(CONTACT, sent.getString(0));
            values.put(LENGTH, sent.getString(1).length());
            values.put(DATE, sent.getLong(2));
            values.put(INCOMING, false);
            resolver.insert(dbUri, values);
        }
    }

    private void loadMmsInbox(long lastdate) {
        String[] columns = new String[]{Mms.Addr.ADDRESS, Mms.Inbox.DATE, Mms.Inbox.MESSAGE_ID};

        Cursor inbox = getCursor(Mms.Inbox.CONTENT_URI, columns, lastdate);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(CONTACT, inbox.getString(0));
            values.put(LENGTH, getMmsLength(inbox.getLong(2)));
            values.put(DATE, inbox.getLong(1));
            values.put(INCOMING, true);
            resolver.insert(dbUri, values);
        }
    }

    private void loadMmsSent(long lastdate) {
        String[] columns = new String[]{Mms.Addr.ADDRESS, Mms.Sent.DATE_SENT, Mms.Sent.MESSAGE_ID};

        Cursor inbox = getCursor(Mms.Sent.CONTENT_URI, columns, lastdate);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(CONTACT, inbox.getString(0));
            values.put(LENGTH, getMmsLength(inbox.getLong(2)));
            values.put(DATE, inbox.getLong(1));
            values.put(INCOMING, false);
            resolver.insert(dbUri, values);
        }
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
    public void doTask() {
        loadData();
    }

    @Override
    public void setParameters(Intent i) {

    }
}
