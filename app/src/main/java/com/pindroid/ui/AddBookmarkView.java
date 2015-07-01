package com.pindroid.ui;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pindroid.R;
import com.pindroid.client.NetworkUtilities;
import com.pindroid.client.PinboardClient;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.listadapter.TagAutoCompleteCursorAdapter;
import com.pindroid.model.Bookmark;
import com.pindroid.model.Tag;
import com.pindroid.model.TagSuggestions;
import com.pindroid.platform.TagManager;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.SpaceTokenizer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

@EViewGroup(R.layout.view_addbookmark)
public class AddBookmarkView extends LinearLayout {

    @ViewById(R.id.add_edit_url) EditText mEditUrl;
    @ViewById(R.id.add_edit_description) EditText mEditDescription;
    @ViewById(R.id.add_description_progress) ProgressBar mDescriptionProgress;
    @ViewById(R.id.add_edit_notes) EditText mEditNotes;
    @ViewById(R.id.add_edit_tags) MultiAutoCompleteTextView mEditTags;
    @ViewById(R.id.add_recommended_tags) TextView mRecommendedTags;
    @ViewById(R.id.add_popular_tags) TextView mPopularTags;
    @ViewById(R.id.add_edit_private) CompoundButton mPrivate;
    @ViewById(R.id.add_edit_toread) CompoundButton mToRead;

    @StringRes(R.string.add_bookmark_default_title) String defaultDescription;

    private Bookmark bookmark;

    private String username = EventBus.getDefault().getStickyEvent(AccountChangedEvent.class).getNewAccount();

    public AddBookmarkView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @AfterViews
    public void init(){
        mRecommendedTags.setMovementMethod(LinkMovementMethod.getInstance());
        mPopularTags.setMovementMethod(LinkMovementMethod.getInstance());
        mPrivate.setChecked(SettingsHelper.getPrivateDefault(getContext()));
        mToRead.setChecked(SettingsHelper.getToReadDefault(getContext()));

        CursorAdapter autoCompleteAdapter = new TagAutoCompleteCursorAdapter(getContext(), R.layout.autocomplete_view, null,
                new String[]{Tag.Name, Tag.Count}, new int[]{R.id.autocomplete_name, R.id.autocomplete_count}, 0);
        mEditTags.setAdapter(autoCompleteAdapter);
        mEditTags.setTokenizer(new SpaceTokenizer());
    }

    /**
     * Set the form fields based on an existing bookmark, used for editing a bookmark.
     * @param b Existing bookmark
     */
    public void bind(@NonNull Bookmark b) {
        bookmark = b;

        mEditUrl.setText(b.getUrl());

        if(b.getDescription() != null) {
            mEditDescription.setText(b.getDescription());
        }

        if(b.getNotes() != null) {
            mEditNotes.setText(b.getNotes());
        }

        if(b.getTagString() != null) {
            mEditTags.setText(b.getTagString());
        }
        else {
            mEditTags.setText("");
        }

        mPrivate.setChecked(!b.getShared());
        mToRead.setChecked(b.getToRead());

        if(mEditDescription.getText().toString().equals("")) {
            getWebpageTitle(b.getUrl());
        }

        getTagSuggestions(b.getUrl());
    }

    /**
     * Get a Bookmark representation of the filled in fields.
     */
    public Bookmark getBookmark() {
        Bookmark b = new Bookmark();

        b.setUrl(mEditUrl.getText().toString());
        b.setDescription(mEditDescription.getText().toString());

        if(b.getDescription().equals("")) {
            b.setDescription(defaultDescription);
        }

        b.setNotes(mEditNotes.getText().toString());

        String[] tags = mEditTags.getText().toString().trim().split("\\s+");
        b.setTagString(TextUtils.join(" ", tags));

        if(bookmark != null && bookmark.getId() != 0) {
            b.setId(bookmark.getId());
        } else {
            b.setTime(new Date());
        }

        b.setAccount(username);
        b.setToRead(mToRead.isChecked());
        b.setShared(!mPrivate.isChecked());

        return b;
    }

    private void getWebpageTitle(@NonNull String url) {
        mDescriptionProgress.setVisibility(View.VISIBLE);
        loadWebpageTitle(url);
    }

    @Background
    void loadWebpageTitle(String url) {
        updateWebpageTitle(NetworkUtilities.getWebpageTitle(url));
    }

    @UiThread
    void updateWebpageTitle(String title) {
        mEditDescription.setText(Html.fromHtml(title));
        mDescriptionProgress.setVisibility(View.GONE);
    }

    private void getTagSuggestions(@NonNull String url) {
        mRecommendedTags.setVisibility(View.GONE);
        mPopularTags.setVisibility(View.GONE);
        loadTagSuggestions(url);
    }

    @Background
    void loadTagSuggestions(String url) {
        try {
            Account account = AccountHelper.getAccount(username, getContext());

            if(!url.startsWith("http")){
                url = "http://" + url;
            }

            updateTagSuggestions(PinboardClient.get().getTagSuggestions(AccountHelper.getAuthToken(getContext(), account), url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void updateTagSuggestions(List<TagSuggestions> suggestions) {
        if(suggestions != null) {
            SpannableStringBuilder recommendedBuilder = new SpannableStringBuilder();
            SpannableStringBuilder popularBuilder = new SpannableStringBuilder();

            for(TagSuggestions ts : suggestions) {
                for(String t : ts.getPopular()) {
                    addTag(popularBuilder, t);
                }

                for(String t : ts.getRecommended()) {
                    addTag(recommendedBuilder, t);
                }
            }

            mRecommendedTags.setText(recommendedBuilder);
            mPopularTags.setText(popularBuilder);

            mRecommendedTags.setVisibility(View.VISIBLE);
            mPopularTags.setVisibility(View.VISIBLE);
        }
    }

    private void addTag(@NonNull SpannableStringBuilder builder, @NonNull String t) {
        int flags = 0;

        if (builder.length() != 0) {
            builder.append("   ");
        }

        int start = builder.length();
        builder.append(t);
        int end = builder.length();

        TagSpan span = new TagSpan(t);
        span.setOnTagClickListener(tagOnClickListener);

        builder.setSpan(span, start, end, flags);
    }

    final TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
            String currentTagString = mEditTags.getText().toString();

            ArrayList<String> currentTags = new ArrayList<>();
            Collections.addAll(currentTags, currentTagString.split(" "));

            if(tag != null && !tag.equals("")) {
                if(!currentTags.contains(tag)) {
                    currentTags.add(tag);
                } else {
                    currentTags.remove(tag);
                }
                mEditTags.setText(TextUtils.join(" ", currentTags.toArray()).trim());
                mEditTags.setSelection(mEditTags.getText().length());
            }
        }
    };

    @FocusChange(R.id.add_edit_url)
    public void urlFocusChanged(View urlView, boolean hasFocus) {
        if(!hasFocus){
            String url = mEditUrl.getText().toString().trim();

            if(!url.equals("")) {
                if(mEditDescription.getText().toString().equals("")) {
                    getWebpageTitle(url);
                }
                getTagSuggestions(url);
            }
        }
    }

    public void onEvent(AccountChangedEvent event) {
        username = event.getNewAccount();

        if(mEditTags.getAdapter() != null) {
            ((CursorAdapter)mEditTags.getAdapter()).setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence constraint) {
                    return TagManager.GetTagsAsCursor((constraint != null ? constraint.toString() : ""),
                            username, Tag.Count + " DESC, " + Tag.Name + " ASC", getContext());
                }
            });
        }
    }
}
