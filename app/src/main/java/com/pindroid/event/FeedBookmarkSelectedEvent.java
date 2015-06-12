package com.pindroid.event;

import com.pindroid.model.FeedBookmark;

public class FeedBookmarkSelectedEvent {
    private final FeedBookmark bookmark;

    public FeedBookmarkSelectedEvent(FeedBookmark bookmark) {
        this.bookmark = bookmark;
    }

    public FeedBookmark getBookmark() {
        return bookmark;
    }
}
