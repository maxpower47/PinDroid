package com.deliciousdroid.activity;

import com.deliciousdroid.R;
import com.deliciousdroid.providers.BookmarkContentProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;

public class TagLiveFolder extends Activity {
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + 
			BookmarkContentProvider.AUTHORITY + "/tag/livefolder");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
            setResult(RESULT_OK, createLiveFolder(this, CONTENT_URI, "Delicious Tags",
                    R.drawable.tag));
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }
    
    private Intent createLiveFolder(Context context, Uri uri, String name, int icon) {

        final Intent intent = new Intent();

        intent.setData(uri);
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, name);
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON,
                Intent.ShortcutIconResource.fromContext(context, icon));
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE, LiveFolders.DISPLAY_MODE_LIST);

        return intent;
    }
}
