package com.pindroid.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.pindroid.listadapter.StableListItem;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedBookmark implements StableListItem {

	@SerializedName("u") private String url;
	@SerializedName("d") private String description;
	@SerializedName("n") private String notes;
	@SerializedName("t") private List<String> tags;
	@SerializedName("a") private String account;
	@SerializedName("dt") private Date time;

	public Bookmark toBookmark() {
		return new Bookmark(this);
	}

    public int getId() {
        return 0;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<TagContent.Tag> getTags() {
		List<TagContent.Tag> result = new ArrayList<>();

		if(tags != null){
			for(String s : tags) {
				if(!s.equals("")) {
					result.add(new TagContent.Tag(s));
				}
			}
		}

		return result;
	}

	public String getTagString() {
		return TextUtils.join(" ", tags);
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
