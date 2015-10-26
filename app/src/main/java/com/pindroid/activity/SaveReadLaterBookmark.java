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

package com.pindroid.activity;

import java.util.Date;

import android.accounts.AccountManager;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.model.Bookmark;
import com.pindroid.service.SaveBookmarkService;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;

@EActivity
public class SaveReadLaterBookmark extends Activity {
	private static final String TAG = "SaveReadLaterBookmark";
    private String username;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

        if(!AccountHelper.isSingleAccount(this)) {
            Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
        } else {
            onChooseAccount(AccountHelper.getFirstAccount(this).name);
        }
	}

    @OnActivityResult(Constants.REQUEST_CODE_ACCOUNT_CHANGE)
	protected void onChooseAccount(@OnActivityResult.Extra(value = AccountManager.KEY_ACCOUNT_NAME) String username){
		this.username = username;
        handleIntent();
	}

	private void handleIntent(){
		if(username != null){
			Intent intent = getIntent();
	
			if((Intent.ACTION_SEND.equals(intent.getAction()) || Constants.ACTION_READLATER.equals(intent.getAction())) && intent.hasExtra(Intent.EXTRA_TEXT)){
				ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
				String url = StringUtils.getUrl(reader.getText().toString());

				if ("".equals(url)) {
					Toast.makeText(this, R.string.add_bookmark_invalid_url, Toast.LENGTH_LONG).show();
					finish();
				} else {
					saveBookmark(intent, reader, url);
				}
			}
		} else {
			Toast.makeText(this, R.string.login_no_account, Toast.LENGTH_SHORT).show();
			finish();
		}	
	}

	private void saveBookmark(Intent intent, ShareCompat.IntentReader reader, String url) {
		Bookmark bookmark = new Bookmark();
		bookmark.setUrl(url);
		bookmark.setDescription(reader.getSubject());
		bookmark.setShared(!intent.getBooleanExtra(Constants.EXTRA_PRIVATE, SettingsHelper.getPrivateDefault(this)));
		bookmark.setToRead(true);
		bookmark.setTime(new Date());
		bookmark.setTagString("");
		bookmark.setAccount(username);

		pushBookmarkToService(bookmark);

		Toast.makeText(this, R.string.save_later_saved, Toast.LENGTH_SHORT).show();
		finish();
	}

	private void pushBookmarkToService(Bookmark bookmark) {
		Intent intent = new Intent(this, SaveBookmarkService.class);
		intent.putExtra(Constants.EXTRA_BOOKMARK, bookmark);
		startService(intent);
	}
}
