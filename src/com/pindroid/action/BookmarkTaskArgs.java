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

import android.accounts.Account;
import android.content.Context;

import com.pindroid.providers.BookmarkContent.Bookmark;

public class BookmarkTaskArgs{
	private Bookmark bookmark;
	private Bookmark oldBookmark;
	private Account account;
	private Context context;
	private Boolean update;
	
	public Bookmark getBookmark(){
		return bookmark;
	}
	
	public Bookmark getOldBookmark(){
		return oldBookmark;
	}
	
	public Account getAccount(){
		return account;
	}
	
	public Context getContext(){
		return context;
	}
	
	public Boolean getUpdate(){
		return update;
	}
	
	public BookmarkTaskArgs(Bookmark b, Account a, Context c){
		bookmark = b;
		account = a;
		context = c;
	}
	
	public BookmarkTaskArgs(Bookmark b, Bookmark ob, Account a, Context c, Boolean u){
		bookmark = b;
		oldBookmark = ob;
		account = a;
		context = c;
		update = u;
	}
}