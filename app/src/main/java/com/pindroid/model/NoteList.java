package com.pindroid.model;

import com.pindroid.providers.NoteContent.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteList {

	private List<Note> notes;
	private long count;

	public NoteList() {
		notes = new ArrayList<>();
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
