package com.pindroid.widget;

import com.pindroid.R;
import com.pindroid.Constants;

import com.pindroid.activity.BrowseBookmarks;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContentProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

public class UnreadWidgetProvider extends AppWidgetProvider {
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int n = appWidgetIds.length;
        
		AccountManager mAccountManager = AccountManager.get(context);
		Account mAccount = null;
		String username = "";
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
			mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
			username = mAccount.name;
		}

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < n; i++) {
            int appWidgetId = appWidgetIds[i];
    		
    		Intent unreadIntent = new Intent(context, BrowseBookmarks.class);
    		unreadIntent.setAction(Intent.ACTION_VIEW);
    		unreadIntent.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder data = new Uri.Builder();
    		data.scheme(Constants.CONTENT_SCHEME);
    		data.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
    		data.appendEncodedPath("bookmarks");
    		data.appendQueryParameter("unread", "1");
    		unreadIntent.setData(data.build());
    		
            PendingIntent unreadPendingIntent = PendingIntent.getActivity(context, 0, unreadIntent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.unread_appwidget);
            views.setOnClickPendingIntent(R.id.unread_widget, unreadPendingIntent);
            
            int count = BookmarkManager.GetUnreadCount(username, context);

            String countText = Integer.toString(count);
            if(count > 99) {
            	countText = "+";
            }

            if(count > 0) {
            	views.setViewVisibility(R.id.unread_widget_unread_count, View.VISIBLE);
                views.setViewVisibility(R.id.unread_widget_unread_count_background, View.VISIBLE);
            	
            	views.setTextViewText(R.id.unread_widget_unread_count, countText);
            } else {
            	views.setViewVisibility(R.id.unread_widget_unread_count, View.GONE);
            	views.setViewVisibility(R.id.unread_widget_unread_count_background, View.GONE);
            }

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}