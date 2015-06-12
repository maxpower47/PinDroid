package com.pindroid.event;

import com.pindroid.providers.BookmarkContent;

public class BookmarkSelectedEvent {
    private final BookmarkContent.Bookmark bookmark;

    public BookmarkSelectedEvent(BookmarkContent.Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    public BookmarkContent.Bookmark getBookmark() {
        return bookmark;
    }
}
