package com.pindroid.client;

import com.pindroid.model.NoteList;
import com.pindroid.model.TagSuggestions;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;

import java.util.List;
import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

public interface PinboardService {

	@GET("/v1/posts/update")
	Update getUpdate(@Query("auth_token") String authToken);

	@GET("/v1/posts/add")
	PinboardApiResult addBookmark(@Query("auth_token") String authToken, @QueryMap Map<String, String> query);

	@GET("/v1/posts/all?meta=yes")
	List<Bookmark> getBookmarks(@Query("auth_token") String authToken, @Query("start") String start, @Query("results") String results);

	@GET("/v1/posts/delete")
	PinboardApiResult deleteBookmark(@Query("auth_token") String authToken, @Query("url") String url);

	@GET("/v1/tags/get")
	Map<String, Long> getTags(@Query("auth_token") String authToken);

	@GET("/v1/posts/suggest")
	List<TagSuggestions> getTagSuggestions(@Query("auth_token") String authToken, @Query("url") String url);

	@GET("/v1/notes/list")
	NoteList getNotes(@Query("auth_token") String authToken);

	@GET("/v1/notes/{id}")
	Note getNote(@Query("auth_token") String authToken, @Path("id") String id);

	@GET("/v1/user/secret")
	PinboardAuthToken getSecretToken(@Query("auth_token") String authToken);
}
