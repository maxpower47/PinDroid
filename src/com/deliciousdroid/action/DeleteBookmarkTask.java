/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.action;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.deliciousdroid.client.DeliciousApi;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.platform.TagManager;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.TagContent.Tag;

public class DeleteBookmarkTask extends AsyncTask<BookmarkTaskArgs, Integer, Boolean>{
	private Context context;
	private Bookmark bookmark;
	private Account account;
	
	@Override
	protected Boolean doInBackground(BookmarkTaskArgs... args) {
		context = args[0].getContext();
		bookmark = args[0].getBookmark();
		account = args[0].getAccount();
		
		try {
			Boolean success = DeliciousApi.deleteBookmark(bookmark, account, context);
			if(success){
				BookmarkManager.DeleteBookmark(args[0].getBookmark(), context);
				return true;
			} else return false;
				
		} catch (IOException e) {
			return false;
		} catch (AuthenticationException e) {
			return false;
		}
	}

    protected void onPostExecute(Boolean result) {
		if(result){
			String[] tags = bookmark.getTagString().split(" ");
			for(String s:tags){
				Tag t = new Tag(s, 1);    				
				TagManager.UpleteTag(t, account.name, context);
			}

			Toast.makeText(context, "Bookmark Deleted Successfully", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
		}
    }
}