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
import com.pindroid.activity.SmallWidgetConfigure;
import com.pindroid.platform.BookmarkManager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

public class SmallWidgetProvider extends AppWidgetProvider {
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int n = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < n; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

		String username = SmallWidgetConfigure.loadAccountPref(context, appWidgetId);
    	String button = SmallWidgetConfigure.loadButtonPref(context, appWidgetId);

		PendingIntent bookmarkPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.ViewBookmarks(null, username, null, context), 0);
        PendingIntent unreadPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.ViewUnread(username, context), 0);
        PendingIntent tagPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.ViewTags(username, context), 0);
        PendingIntent notePendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.ViewNotes(username, context), 0);
        PendingIntent searchPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.WidgetSearch(username, context), 0);
        PendingIntent addPendingIntent = PendingIntent.getActivity(context, 0, IntentHelper.AddBookmark(null, username, context), 0);

        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.small_appwidget);
        views.setOnClickPendingIntent(R.id.small_widget_bookmarks_button, bookmarkPendingIntent);
        views.setOnClickPendingIntent(R.id.small_widget_unread_button, unreadPendingIntent);
        views.setOnClickPendingIntent(R.id.small_widget_tags_button, tagPendingIntent);
        views.setOnClickPendingIntent(R.id.small_widget_notes_button, notePendingIntent);
        views.setOnClickPendingIntent(R.id.small_widget_search_button, searchPendingIntent);
        views.setOnClickPendingIntent(R.id.small_widget_add_button, addPendingIntent);
        
        int count = BookmarkManager.GetUnreadCount(username, context);
        
        String countText = Integer.toString(count);
        if(count > 99) {
        	countText = "+";
        }

        if(count > 0) {
        	views.setViewVisibility(R.id.small_widget_unread_count_layout, View.VISIBLE);
        	
        	views.setTextViewText(R.id.small_widget_unread_count, countText);
        } else {
        	views.setViewVisibility(R.id.small_widget_unread_count_layout, View.GONE);
        }
    	
    	if(button.equals("bookmark")){
    		hideAllButtons(views);
    		views.setViewVisibility(R.id.small_widget_bookmarks_button, View.VISIBLE);
    	} else if(button.equals("unread")){
    		hideAllButtons(views);
    		views.setViewVisibility(R.id.small_widget_unread_layout, View.VISIBLE);
    	} else if(button.equals("tags")){
    		hideAllButtons(views);
    		views.setViewVisibility(R.id.small_widget_tags_button, View.VISIBLE);
    	} else if(button.equals("notes")){
    		hideAllButtons(views);
    		views.setViewVisibility(R.id.small_widget_notes_button, View.VISIBLE);
    	} else if(button.equals("add")){
    		hideAllButtons(views);
    		views.setViewVisibility(R.id.small_widget_add_button, View.VISIBLE);
    	} else if(button.equals("search")){
    		hideAllButtons(views);
    		views.setViewVisibility(R.id.small_widget_search_button, View.VISIBLE);
    	}

        // Tell the widget manager
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            SmallWidgetConfigure.deleteTitlePref(context, appWidgetIds[i]);
        }
    }
    
    private static void hideAllButtons(RemoteViews views){
    	views.setViewVisibility(R.id.small_widget_bookmarks_button, View.GONE);
    	views.setViewVisibility(R.id.small_widget_unread_layout, View.GONE);
    	views.setViewVisibility(R.id.small_widget_tags_button, View.GONE);
    	views.setViewVisibility(R.id.small_widget_notes_button, View.GONE);
    	views.setViewVisibility(R.id.small_widget_add_button, View.GONE);
    	views.setViewVisibility(R.id.small_widget_search_button, View.GONE);
    }
}