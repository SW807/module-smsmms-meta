package dk.aau.cs.psylog.sensor.smsmmsmeta;

import dk.aau.cs.psylog.module_lib.ScheduledService;

public class PsyLogService extends ScheduledService {

    public PsyLogService() {
        super("PsyLogIntentServiceSMS/MMS");
    }

    @Override
    public void setScheduledTask() {
        scheduledTask = new SMSListener(this);
    }
}
