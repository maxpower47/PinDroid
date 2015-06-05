package com.pindroid.event;

import com.pindroid.model.FeedBookmark;

public class FeedBookmarkSelectedEvent {
    private FeedBookmark bookmark;

    public FeedBookmarkSelectedEvent(FeedBookmark bookmark) {
        this.bookmark = bookmark;
    }

    public FeedBookmark getBookmark() {
        return bookmark;
    }
}
