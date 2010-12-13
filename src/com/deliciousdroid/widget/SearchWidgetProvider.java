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

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, BrowseBookmarks.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder data = new Uri.Builder();
    		data.scheme(Constants.CONTENT_SCHEME);
    		data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
    		data.appendEncodedPath("bookmarks");
    		intent.setData(data.build());
    		
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.search_appwidget);
            views.setOnClickPendingIntent(R.id.button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}