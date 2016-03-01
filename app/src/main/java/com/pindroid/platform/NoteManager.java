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

package com.pindroid.platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.model.Note;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

public class NoteManager {
	
	public static CursorLoader GetNotes(String account, String sortorder, Context context) {		
		final String[] projection = new String[] {Note._ID, Note.Title, Note.Text, Note.Hash, Note.Pid, Note.Account, Note.Added, Note.Updated};
		final String selection = Note.Account + "=?";
		final String[] selectionargs = new String[]{ account == null ? "" : account};
		
		return new CursorLoader(context, Note.CONTENT_URI, projection, selection, selectionargs, sortorder);
	}
	
	public static Note GetById(int id, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {BaseColumns._ID, Note.Account, Note.Title, Note.Text, Note.Pid, Note.Hash, Note.Added, Note.Updated};
		
		Uri uri = ContentUris.appendId(Note.CONTENT_URI.buildUpon(), id).build();
		
		Cursor c = context.getContentResolver().query(uri, projection, null, null, null);				
		
		if(c.moveToFirst()){
			Note n = new Note(c);
			c.close();
			return n;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static void BulkInsert(List<Note> list, String account, Context context) {
		int notesize = list.size();
		ContentValues[] ncv = new ContentValues[notesize];
		
		for(int i = 0; i < notesize; i++){	
			Note n = list.get(i);
            n.setAccount(account);
			ncv[i] = n.toContentValues();
		}
		
		context.getContentResolver().bulkInsert(Note.CONTENT_URI, ncv);
	}
	
	public static void TruncateNotes(String account, Context context){
		
		final String selection = Note.Account + "=?";
		final String[] selectionargs = new String[]{account};
		
		context.getContentResolver().delete(Note.CONTENT_URI, selection, selectionargs);
	}
	
	public static void AddNote(Note note, String account, Context context){
		context.getContentResolver().insert(Note.CONTENT_URI, note.toContentValues());
	}
	
	public static void UpdateNote(Note note, String account, Context context){
		final String selection = Note.Pid + "=? AND " + Note.Account + "=?";
		final String[] selectionargs = new String[]{note.getPid(), account};
		context.getContentResolver().update(Note.CONTENT_URI, note.toContentValues(), selection, selectionargs);
	}
	
	public static CursorLoader SearchNotes(String query, String username, Context context) {
		final String[] projection = new String[] {Note._ID, Note.Title, Note.Text, Note.Hash, Note.Pid, Note.Account, Note.Added, Note.Updated};
		String selection = null;
		
		final String sortorder = Note.Updated + " ASC";
		final ArrayList<String> selectionlist = new ArrayList<>();
		
		ArrayList<String> queryList = new ArrayList<>();
		
		for(String s : query.split(" ")) {
			queryList.add("(" + Note.Title + " LIKE ? OR " + 
					Note.Text + " LIKE ?)");
			selectionlist.add("%" + s + "%");
			selectionlist.add("%" + s + "%");
		}
		
		selectionlist.add(username);
		
		if(query != null && !"".equals(query)) {
			selection = "(" + TextUtils.join(" OR ", queryList) + ")" + 
				" AND " + Note.Account + "=?";
		} else {
			selection = Note.Account + "=?";
		}
		
		return new CursorLoader(context, Note.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), sortorder);
	}
}