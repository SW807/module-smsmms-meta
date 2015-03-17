package dk.aau.cs.psylog.sensor.smsmmsmeta;

import dk.aau.cs.psylog.module_lib.SensorService;

public class PsyLogService extends SensorService {
    @Override
    public void setSensor() {
        sensor = new SMSListener(this);
    }
}
