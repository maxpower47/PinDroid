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

package com.pindroid.widget;

import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.activity.LargeWidgetConfigure;

import com.pindroid.platform.BookmarkManager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

public class SearchWidgetProvider extends AppWidgetProvider {
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int n = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < n; i++) {
        	int appWidgetId = appWidgetIds[i];
        	updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    	
    	String username = LargeWidgetConfigure.loadAccountPref(context, appWidgetId);
    	
		PendingIntent bookmarkPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.ViewBookmarks(null, username, null, context), 0);
        PendingIntent unreadPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.ViewUnread(username, context), 0);
        PendingIntent addPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.AddBookmark(null, username, context), 0);

        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_appwidget);
        views.setOnClickPendingIntent(R.id.search_widget_bookmarks_button, bookmarkPendingIntent);
        views.setOnClickPendingIntent(R.id.search_widget_add_button, addPendingIntent);
        views.setOnClickPendingIntent(R.id.search_widget_unread_button, unreadPendingIntent);

        views.setTextViewText(R.id.search_widget_account_name, username);
        
        int count = BookmarkManager.GetUnreadCount(username, context);
        
        String countText = Integer.toString(count);
        if(count > 99) {
        	countText = "+";
        }

        if(count > 0) {
        	views.setViewVisibility(R.id.search_widget_unread_count_layout, View.VISIBLE);
        	
        	views.setTextViewText(R.id.search_widget_unread_count, countText);
        } else {
        	views.setViewVisibility(R.id.search_widget_unread_count_layout, View.GONE);
        }

        // Tell the AppWidgetManager to perform an update on the current App Widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            LargeWidgetConfigure.deleteAccountPref(context, appWidgetIds[i]);
        }
    }
}