package com.deliciousdroid.activity;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.providers.BookmarkContentProvider;
import com.deliciousdroid.util.StringUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;

public class BookmarkLiveFolder extends Activity {
	
	protected AccountManager mAccountManager;
	protected Account mAccount;
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + 
			BookmarkContentProvider.AUTHORITY + "/bookmark/livefolder");

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		mAccountManager = AccountManager.get(this);
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
			mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		}
        
        Intent tagIntent = new Intent();
        tagIntent.setAction(Intent.ACTION_PICK);
        tagIntent.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
		data.appendEncodedPath("tags");
		tagIntent.setData(data.build());

        startActivityForResult(tagIntent, 1);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String tag = data.getStringExtra("tagname");

        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
            setResult(RESULT_OK, 
            	createLiveFolder(this, Uri.parse(CONTENT_URI.toString() + "?tagname=" + tag), 
            		StringUtils.capitalize(tag), R.drawable.ic_bookmark_folder));
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
