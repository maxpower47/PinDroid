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
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.small_widget_configure_activity)
public class ScreenShortcut extends AppCompatActivity {

	private String username = "";
    @ViewById(R.id.shortcut_list) ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.small_widget_configuration_description);

		Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
		startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
    }

    @AfterViews
    void init() {
        String[] MENU_ITEMS = new String[] {getString(R.string.small_widget_my_bookmarks),
                getString(R.string.small_widget_my_unread),
                getString(R.string.small_widget_my_notes),
                getString(R.string.small_widget_add_bookmark),
                getString(R.string.small_widget_search_bookmarks)};

        listView.setAdapter(new ArrayAdapter<>(this, R.layout.widget_configure_view, MENU_ITEMS));
    }

    @ItemClick(R.id.shortcut_list)
    void listItemClicked(int position) {
        Intent shortcutIntent;
        String shortcutName;

        switch (position){
            default:
            case 0:
                shortcutIntent = IntentHelper.ViewBookmarks(null, username, null, this);
                shortcutName = getString(R.string.small_widget_my_bookmarks);
                break;
            case 1:
                shortcutIntent = IntentHelper.ViewUnread(username, this);
                shortcutName = getString(R.string.small_widget_my_unread);
                break;
            case 2:
                shortcutIntent = IntentHelper.ViewNotes(username, this);
                shortcutName = getString(R.string.small_widget_my_notes);
                break;
            case 3:
                shortcutIntent = IntentHelper.AddBookmark(null, username, this);
                shortcutName = getString(R.string.small_widget_add_bookmark);
                break;
            case 4:
                shortcutIntent = IntentHelper.WidgetSearch(username, this);
                shortcutName = getString(R.string.small_widget_search_bookmarks);
                break;
        }

        final ShortcutIconResource iconResource = ShortcutIconResource.fromContext(this, R.drawable.ic_main);
        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnActivityResult(Constants.REQUEST_CODE_ACCOUNT_CHANGE)
    protected void onChooseAccount(Intent data){
		username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	}
}