/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.pindroid.util;

import com.pindroid.syncadapter.PeriodicSyncReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;

public class SyncUtils {

	public static void addPeriodicSync(String authority, Bundle extras, long frequency, Context context) {
		long pollFrequencyMsec = frequency * 60000;
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		int type = AlarmManager.ELAPSED_REALTIME_WAKEUP;
		long triggerAtTime = SystemClock.elapsedRealtime() + pollFrequencyMsec;
		long interval = pollFrequencyMsec;
		PendingIntent operation = PeriodicSyncReceiver.createPendingIntent(context, authority, extras);

		manager.setInexactRepeating(type, triggerAtTime, interval, operation);

	}
	
    public static void removePeriodicSync(String authority, Bundle extras, Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PeriodicSyncReceiver.createPendingIntent(context, authority, extras);
        manager.cancel(operation);
    }

}
