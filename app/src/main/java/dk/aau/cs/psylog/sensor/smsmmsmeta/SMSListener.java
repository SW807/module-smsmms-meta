package dk.aau.cs.psylog.sensor.smsmmsmeta;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Part;

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
    private static final String MODULE_NAME = "smsmmsmeta";
    private static final String TABLE_NAME = "smsmmsmeta";

    private static final Uri PART_CONTENT_URI = Uri.withAppendedPath(Mms.CONTENT_URI, "part");

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

    private Cursor getCursor(Uri uri, String[] columns, String selectionColumn, long lastdate) {
        return resolver.query(uri, columns, selectionColumn + " > ?", new String[]{String.valueOf(lastdate)}, null);
    }

    private void loadSmsInbox(long lastdate) {
        String[] columns = new String[]{Sms.Inbox.ADDRESS, Sms.Inbox.BODY, Sms.Inbox.DATE};

        Cursor inbox = getCursor(Sms.Inbox.CONTENT_URI, columns, Sms.Inbox.DATE, lastdate);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(CONTACT, inbox.getString(0));
            values.put(LENGTH, inbox.getString(1).length());
            values.put(DATE, inbox.getLong(2));
            values.put(INCOMING, true);
            resolver.insert(dbUri, values);
        }
    }

    private void loadSmsSent(long lastdate) {
        String[] columns = new String[]{Sms.Sent.ADDRESS, Sms.Sent.BODY, Sms.Sent.DATE};

        Cursor sent = getCursor(Sms.Sent.CONTENT_URI, columns, Sms.Sent.DATE, lastdate);
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
        String[] columns = new String[]{Mms.Inbox.DATE, Mms.Inbox._ID};

        Cursor inbox = getCursor(Mms.Inbox.CONTENT_URI, columns, Mms.Inbox.DATE, lastdate);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            String id = inbox.getString(1);

            values.put(CONTACT, getMmsContact(id));
            values.put(LENGTH, getMmsLength(id));
            values.put(DATE, inbox.getLong(0));
            values.put(INCOMING, true);
            resolver.insert(dbUri, values);
        }
    }

    private void loadMmsSent(long lastdate) {
        String[] columns = new String[]{Mms.Sent.DATE, Mms.Sent._ID};

        Cursor inbox = getCursor(Mms.Sent.CONTENT_URI, columns, Mms.Sent.DATE, lastdate);
        while (inbox.moveToNext()) {
            ContentValues values = new ContentValues();
            String id = inbox.getString(1);

            values.put(CONTACT, getMmsContact(id));
            values.put(LENGTH, getMmsLength(id));
            values.put(DATE, inbox.getLong(0));
            values.put(INCOMING, false);
            resolver.insert(dbUri, values);
        }
    }

    private int getMmsLength(String mmsId) {
        String[] columns = new String[]{Part.MSG_ID, Part.CONTENT_TYPE, Part._DATA, Part.TEXT};
        Cursor cursor = resolver.query(PART_CONTENT_URI, columns, Part.MSG_ID + " = ?", new String[]{mmsId}, null);

        int length = 0;

        if (cursor.moveToFirst()) {
            do {
                if ("text/plain".equals(cursor.getString(1))) {
                    String data = cursor.getString(2);
                    if (data != null) length += getMmsTextLength(cursor.getString(0));
                    else length += cursor.getString(3).length();
                }
            } while (cursor.moveToNext());
        }

        return length;
    }

    private int getMmsTextLength(String id) {
        Uri partURI = Uri.withAppendedPath(PART_CONTENT_URI, id);
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
        return sb.length();
    }

    private String getMmsContact(String messageId) {
        return null;
    }

    @Override
    public void doTask() {
        loadData();
    }

    @Override
    public void setParameters(Intent i) {

    }
}
