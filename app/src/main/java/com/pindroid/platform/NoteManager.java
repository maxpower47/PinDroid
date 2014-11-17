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

import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.NoteContent.Note;

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
		final String[] selectionargs = new String[]{account};
		
		return new CursorLoader(context, Note.CONTENT_URI, projection, selection, selectionargs, sortorder);
	}
	
	public static Note GetById(int id, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {Note.Account, Note.Title, Note.Text, Note.Pid, Note.Hash, Note.Added, Note.Updated};
		
		Uri uri = ContentUris.appendId(Note.CONTENT_URI.buildUpon(), id).build();
		
		Cursor c = context.getContentResolver().query(uri, projection, null, null, null);				
		
		if(c.moveToFirst()){
			final int accountColumn = c.getColumnIndex(Note.Account);
			final int titleColumn = c.getColumnIndex(Note.Title);
			final int textColumn = c.getColumnIndex(Note.Text);
			final int pidColumn = c.getColumnIndex(Note.Pid);
			final int hashColumn = c.getColumnIndex(Note.Hash);
			final int addedColumn = c.getColumnIndex(Note.Added);
			final int updatedColumn = c.getColumnIndex(Note.Updated);

			Note n = new Note(id, c.getString(titleColumn), c.getString(textColumn), 
				c.getString(accountColumn), c.getString(hashColumn), c.getString(pidColumn),
				c.getLong(addedColumn), c.getLong(updatedColumn));
			
			c.close();
			
			return n;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static void BulkInsert(ArrayList<Note> list, String account, Context context) {
		int notesize = list.size();
		ContentValues[] ncv = new ContentValues[notesize];
		
		for(int i = 0; i < notesize; i++){	
			Note n = list.get(i);
			
			ContentValues values = new ContentValues();
			
			values.put(Note.Title, n.getTitle());
			values.put(Note.Text, n.getText());
			values.put(Note.Account, account);
			values.put(Note.Hash, n.getHash());
			values.put(Note.Pid, n.getPid());
			values.put(Note.Added, n.getAdded());
			values.put(Note.Updated, n.getUpdated());
			
			ncv[i] = values;
		}
		
		context.getContentResolver().bulkInsert(Note.CONTENT_URI, ncv);
	}
	
	public static void TruncateNotes(String account, Context context){
		
		final String selection = Note.Account + "=?";
		final String[] selectionargs = new String[]{account};
		
		context.getContentResolver().delete(Note.CONTENT_URI, selection, selectionargs);
	}
	
	public static void TruncateOldNotes(ArrayList<String> accounts, Context context){
		
		final ArrayList<String> selectionList = new ArrayList<String>();
		
		for(String s : accounts) {
			selectionList.add(Note.Account + " <> '" + s + "'");
		}
		
		final String selection = TextUtils.join(" AND ", selectionList);
		
		context.getContentResolver().delete(Note.CONTENT_URI, selection, null);
	}
	
	public static void UpsertNote(Note note, String account, Context context){
		final String[] projection = new String[] {Note.Pid, Note.Account, Note.Hash};
		final String selection = Note.Pid + "=? AND " + Note.Account + "=?";
		final String[] selectionargs = new String[]{note.getPid(), account};
		
		final Cursor c = context.getContentResolver().query(Note.CONTENT_URI, projection, selection, selectionargs, null);
		
		if(c.moveToFirst()){
			final int hashColumn = c.getColumnIndex(Note.Hash);
			final String hash = c.getString(hashColumn);
			if(!hash.equals(note.getHash())){
				note.setHash(null);
			}
			
			UpdateNote(note, account, context);
		} else {
			AddNote(note, account, context);
		}
		c.close();
	}
	
	public static void AddNote(Note note, String account, Context context){
		final ContentValues values = new ContentValues();
		
		values.put(Note.Title, note.getTitle());
		values.put(Note.Hash, note.getHash());
		values.put(Note.Text, note.getText());
		values.put(Note.Pid, note.getPid());
		values.put(Note.Added, note.getAdded());
		values.put(Note.Updated, note.getUpdated());
		values.put(Note.Account, note.getAccount());
	
		context.getContentResolver().insert(Note.CONTENT_URI, values);
	}
	
	public static void UpdateNote(Note note, String account, Context context){
		
		final String selection = Note.Pid + "=? AND " + Note.Account + "=?";
		final String[] selectionargs = new String[]{note.getPid(), account};
		
		final ContentValues values = new ContentValues();
		values.put(Note.Title, note.getTitle());
		values.put(Note.Hash, note.getHash());
		values.put(Note.Text, note.getText());
		values.put(Note.Pid, note.getPid());
		values.put(Note.Added, note.getAdded());
		values.put(Note.Updated, note.getUpdated());
		values.put(Note.Account, note.getAccount());
		
		context.getContentResolver().update(Note.CONTENT_URI, values, selection, selectionargs);
	}
	
	public static Note CursorToNote(Cursor c) {
		Note n = new Note();
		n.setId(c.getInt(c.getColumnIndex(BaseColumns._ID)));
		n.setTitle(c.getString(c.getColumnIndex(Note.Title)));
		n.setText(c.getString(c.getColumnIndex(Note.Text)));

		if(c.getColumnIndex(Note.Account) != -1)
			n.setAccount(c.getString(c.getColumnIndex(Note.Account)));
		
		return n;
	}
	
	public static CursorLoader SearchNotes(String query, String username, Context context) {
		final String[] projection = new String[] {Note._ID, Note.Title, Note.Text, Note.Hash, Note.Pid, Note.Account, Note.Added, Note.Updated};
		String selection = null;
		
		final String sortorder = Note.Updated + " ASC";
		final ArrayList<String> selectionlist = new ArrayList<String>();
		
		ArrayList<String> queryList = new ArrayList<String>();
		
		for(String s : query.split(" ")) {
			queryList.add("(" + Note.Title + " LIKE ? OR " + 
					Note.Text + " LIKE ?)");
			selectionlist.add("%" + s + "%");
			selectionlist.add("%" + s + "%");
		}
		
		selectionlist.add(username);
		
		if(query != null && query != "") {
			selection = "(" + TextUtils.join(" OR ", queryList) + ")" + 
				" AND " + Note.Account + "=?";
		} else {
			selection = Note.Account + "=?";
		}
		
		return new CursorLoader(context, Note.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), sortorder);
	}
}