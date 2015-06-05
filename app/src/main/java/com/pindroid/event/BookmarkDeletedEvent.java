package com.pindroid.event;

import com.pindroid.providers.BookmarkContent;

public class BookmarkDeletedEvent {
    private BookmarkContent.Bookmark bookmark;

    public BookmarkDeletedEvent(BookmarkContent.Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    public BookmarkContent.Bookmark getBookmark() {
        return bookmark;
    }
}
