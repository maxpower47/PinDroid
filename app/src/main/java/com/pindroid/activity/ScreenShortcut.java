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

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;

public class ScreenShortcut extends ActionBarActivity {

	private String username = "";
    private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.small_widget_configure_activity);
        getSupportActionBar().setTitle(R.string.small_widget_configuration_description);

        listView = (ListView) findViewById(R.id.shortcut_list);

		Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
		startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);

        String[] MENU_ITEMS = new String[] {getString(R.string.small_widget_my_bookmarks),
                getString(R.string.small_widget_my_unread),
                getString(R.string.small_widget_my_notes),
                getString(R.string.small_widget_add_bookmark),
                getString(R.string.small_widget_search_bookmarks)};


        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.widget_configure_view, MENU_ITEMS));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Context context = ScreenShortcut.this;

                Intent shortcutIntent;
                String shortcutName;

                switch (position){
                    default:
                    case 0:
                        shortcutIntent = IntentHelper.ViewBookmarks(null, username, null, context);
                        shortcutName = getString(R.string.small_widget_my_bookmarks);
                        break;
                    case 1:
                        shortcutIntent = IntentHelper.ViewUnread(username, context);
                        shortcutName = getString(R.string.small_widget_my_unread);
                        break;
                    case 2:
                        shortcutIntent = IntentHelper.ViewNotes(username, context);
                        shortcutName = getString(R.string.small_widget_my_notes);
                        break;
                    case 3:
                        shortcutIntent = IntentHelper.AddBookmark(null, username, context);
                        shortcutName = getString(R.string.small_widget_add_bookmark);
                        break;
                    case 4:
                        shortcutIntent = IntentHelper.WidgetSearch(username, context);
                        shortcutName = getString(R.string.small_widget_search_bookmarks);
                        break;
                }


                final ShortcutIconResource iconResource = ShortcutIconResource.fromContext(context, R.drawable.ic_main);
                final Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){	
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == Constants.REQUEST_CODE_ACCOUNT_CHANGE){
			username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
		}
	}
}