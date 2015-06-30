package com.pindroid.event;

import com.pindroid.model.Bookmark;

public class BookmarkSelectedEvent {
    private final Bookmark bookmark;

    public BookmarkSelectedEvent(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }
}
