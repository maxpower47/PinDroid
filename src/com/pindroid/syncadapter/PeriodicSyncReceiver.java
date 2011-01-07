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

package com.pindroid.syncadapter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public final class PeriodicSyncReceiver extends BroadcastReceiver {

	private static final String KEY_AUTHORITY = "authority";

	private static final String KEY_USERDATA = "userdata";

	public static Intent createIntent(Context context, String authority, Bundle extras) {
		Intent intent = new Intent(context, PeriodicSyncReceiver.class);
		intent.putExtra(KEY_AUTHORITY, authority);
		intent.putExtra(KEY_USERDATA, extras);
		return intent;
	}

	public static PendingIntent createPendingIntent(Context context, String authority, Bundle extras) {
		int requestCode = 0;
		Intent intent = createIntent(context, authority, extras);
		int flags = 0;
		return PendingIntent.getBroadcast(context, requestCode, intent, flags);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String authority = intent.getStringExtra(KEY_AUTHORITY);
		Bundle extras = intent.getBundleExtra(KEY_USERDATA);

		ContentResolver.requestSync(null, authority, extras);
	}
}