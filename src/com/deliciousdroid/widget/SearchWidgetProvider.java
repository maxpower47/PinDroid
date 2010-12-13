package com.deliciousdroid.widget;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.activity.BrowseBookmarks;
import com.deliciousdroid.activity.Main;
import com.deliciousdroid.providers.BookmarkContentProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class SearchWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int n = appWidgetIds.length;
        
		AccountManager mAccountManager = AccountManager.get(context);
		Account mAccount = null;
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
			mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		}

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < n; i++) {
            int appWidgetId = appWidgetIds[i];


            Intent bookmarkIntent = new Intent(context, BrowseBookmarks.class);
            bookmarkIntent.setAction(Intent.ACTION_VIEW);
            bookmarkIntent.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder bookmarkData = new Uri.Builder();
    		bookmarkData.scheme(Constants.CONTENT_SCHEME);
    		bookmarkData.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
    		bookmarkData.appendEncodedPath("bookmarks");
    		bookmarkIntent.setData(bookmarkData.build());
    		
    		Intent tagIntent = new Intent();
    		tagIntent.setAction(Intent.ACTION_VIEW);
    		tagIntent.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder tagData = new Uri.Builder();
    		tagData.scheme(Constants.CONTENT_SCHEME);
    		tagData.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
    		tagData.appendEncodedPath("tags");
    		tagIntent.setData(tagData.build());
    		
            PendingIntent bookmarkPendingIntent = PendingIntent.getActivity(context, 0, bookmarkIntent, 0);
            PendingIntent tagPendingIntent = PendingIntent.getActivity(context, 0, tagIntent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_appwidget);
            views.setOnClickPendingIntent(R.id.search_widget_bookmarks_button, bookmarkPendingIntent);
            views.setOnClickPendingIntent(R.id.search_widget_tags_button, tagPendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}