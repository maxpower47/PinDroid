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

import android.accounts.Account;
import android.content.Context;

import com.deliciousdroid.providers.BookmarkContent.Bookmark;

public class BookmarkTaskArgs{
	private Bookmark bookmark;
	private Account account;
	private Context context;
	
	public Bookmark getBookmark(){
		return bookmark;
	}
	
	public Account getAccount(){
		return account;
	}
	
	public Context getContext(){
		return context;
	}
	
	public BookmarkTaskArgs(Bookmark b, Account a, Context c){
		bookmark = b;
		account = a;
		context = c;
	}
}