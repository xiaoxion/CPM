package com.stratazima.grocerlist.processes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by esaur_000 on 11/13/2014.
 */
public class AlarmMangerPoller extends BroadcastReceiver {
    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext= context;

        context.sendBroadcast(new Intent("Connect"));
    }
}
