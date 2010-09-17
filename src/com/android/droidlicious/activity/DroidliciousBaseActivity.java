package com.android.droidlicious.activity;

import com.android.droidlicious.R;

import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class DroidliciousBaseActivity extends ListActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_addbookmark:
			Intent addBookmark = new Intent(this, AddBookmark.class);
			startActivity(addBookmark);
			return true;
	    case R.id.menu_mybookmarks:
			Intent myBookmarks = new Intent(this, BrowseTags.class);
			startActivity(myBookmarks);
	        return true;
	    case R.id.menu_settings:
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
