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

package com.pindroid.action;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pindroid.client.PinboardApi;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;

public class MarkReadBookmarkTask extends AsyncTask<BookmarkTaskArgs, Integer, Boolean>{
	private Context context;
	private Bookmark bookmark;
	private Account account;
	
	@Override
	protected Boolean doInBackground(BookmarkTaskArgs... args) {
		context = args[0].getContext();
		bookmark = args[0].getBookmark();
		account = args[0].getAccount();
		
		bookmark.setToRead(false);
		
		try {
			Boolean success = PinboardApi.addBookmark(bookmark, account, context);
			if(success){
				BookmarkManager.UpdateBookmark(bookmark, account.name, context);
				return true;
			} else return false;
				
		} catch (IOException e) {
			return false;
		} catch (AuthenticationException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
	}

    protected void onPostExecute(Boolean result) {
		if(result){
			Toast.makeText(context, "Bookmark Updated Successfully", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
		}
    }
}