package com.pindroid.activity;

import android.support.v7.app.ActionBarActivity;

import com.pindroid.R;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.providers.BookmarkContent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;

@EActivity(R.layout.activity_add_bookmark)
public class AddBookmark extends ActionBarActivity implements AddBookmarkFragment.OnBookmarkSaveListener {

    @FragmentById(R.id.addbookmark_fragment) AddBookmarkFragment addBookmarkFragment;

    @AfterViews
    protected void init() {
        addBookmarkFragment.setUsername(getIntent().getStringExtra("username"));
        addBookmarkFragment.loadBookmark((BookmarkContent.Bookmark) getIntent().getParcelableExtra("bookmark"), (BookmarkContent.Bookmark) getIntent().getParcelableExtra("oldBookmark"));
    }

    @Override
    public void onBookmarkSave(BookmarkContent.Bookmark b) {
        finish();
    }

    @Override
    public void onBookmarkCancel(BookmarkContent.Bookmark b) {
        finish();
    }
}
