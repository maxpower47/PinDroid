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

package com.pindroid.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getData() != null && intent.getData().getSchemeSpecificPart().equals("com.pindroid.readlater")){
			if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)){
				ComponentName cn = new ComponentName("com.pindroid", "com.pindroid.activity.SaveReadLaterBookmark");
				context.getPackageManager().setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			}
			
			if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)){
				ComponentName cn = new ComponentName("com.pindroid", "com.pindroid.activity.SaveReadLaterBookmark");
				context.getPackageManager().setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
			}
		} else if(intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)){
			try {
				PackageInfo pi = context.getPackageManager().getPackageInfo("com.pindroid.readlater", 0);
				
				if(pi != null){
					ComponentName cn = new ComponentName("com.pindroid", "com.pindroid.activity.SaveReadLaterBookmark");
					context.getPackageManager().setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
			} catch (NameNotFoundException e) {
			}
		}
	}
}