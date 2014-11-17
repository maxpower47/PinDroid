package com.pindroid.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import com.pindroid.R;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.providers.BookmarkContent;

public class AddBookmark extends ActionBarActivity implements AddBookmarkFragment.OnBookmarkSaveListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bookmark);

        AddBookmarkFragment frag = (AddBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.addbookmark_fragment);
        frag.setUsername(getIntent().getStringExtra("username"));
        frag.loadBookmark((BookmarkContent.Bookmark) getIntent().getParcelableExtra("bookmark"), (BookmarkContent.Bookmark) getIntent().getParcelableExtra("oldBookmark"));
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
