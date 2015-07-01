package com.pindroid.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.model.Bookmark;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.StringUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OnActivityResult;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_add_bookmark)
public class AddBookmark extends AppCompatActivity implements AddBookmarkFragment.OnBookmarkSaveListener {

    @FragmentById(R.id.addbookmark_fragment) AddBookmarkFragment addBookmarkFragment;
	@Extra("username") String username;
	@Extra("bookmark") Bookmark bookmark;

    @AfterViews
    protected void init() {

        if(username == null || "".equals(username)) {
            requestAccount();
        } else {
            EventBus.getDefault().postSticky(new AccountChangedEvent(username));
            if(bookmark != null) {
                addBookmarkFragment.loadBookmark(bookmark);
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(AccountChangedEvent event) {
        getSupportActionBar().setSubtitle(event.getNewAccount());
    }

    private void handleIntent(){
        if(Intent.ACTION_SEND.equals(getIntent().getAction())){
            bookmark = findExistingBookmark(loadBookmarkFromShareIntent());
            addBookmarkFragment.loadBookmark(bookmark);
        }
    }

    private Bookmark loadBookmarkFromShareIntent() {
        Bookmark bookmark = new Bookmark();
        ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);

        if(reader.getText() != null){
            String url = StringUtils.getUrl(reader.getText().toString());
            bookmark.setUrl(url);
        }

        if(reader.getSubject() != null) {
            bookmark.setDescription(reader.getSubject());
        }

        bookmark.setToRead(SettingsHelper.getToReadDefault(this));
        bookmark.setShared(!SettingsHelper.getPrivateDefault(this));

        return bookmark;
    }

    private Bookmark findExistingBookmark(Bookmark bookmark) {

        try{
            Bookmark old = BookmarkManager.GetByUrl(bookmark.getUrl(), username, this);
            bookmark = old.copy();
        } catch(Exception e) {
        }

        return bookmark;
    }

    protected void requestAccount() {
        Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
    }

    @Override
    public void onBookmarkSave(Bookmark b) {
        finish();
    }

    @Override
    public void onBookmarkCancel() {
        finish();
    }

    @OnActivityResult(Constants.REQUEST_CODE_ACCOUNT_CHANGE)
    void onResult(int resultCode, @OnActivityResult.Extra(value = AccountManager.KEY_ACCOUNT_NAME) String username) {
        if (resultCode == Activity.RESULT_OK) {
            EventBus.getDefault().postSticky(new AccountChangedEvent(username));
            handleIntent();
        } else {
            finish();
        }
    }
}
