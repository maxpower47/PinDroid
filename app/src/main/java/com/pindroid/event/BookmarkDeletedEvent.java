package com.pindroid.event;

import com.pindroid.model.Bookmark;

public class BookmarkDeletedEvent {
    private final Bookmark bookmark;

    public BookmarkDeletedEvent(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }
}
