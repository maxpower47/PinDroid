package com.pindroid.client;

import com.pindroid.model.NoteList;
import com.pindroid.model.TagSuggestions;
import com.pindroid.model.Bookmark;
import com.pindroid.model.Note;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;


public interface PinboardService {

	@GET("v1/posts/update")
    Call<Update> getUpdate(@Query("auth_token") String authToken) throws AuthenticationException;

	@GET("v1/posts/add")
    Call<PinboardApiResult> addBookmark(@Query("auth_token") String authToken, @QueryMap Map<String, String> query) throws AuthenticationException;

	@GET("v1/posts/all?meta=yes")
    Call<List<Bookmark>> getBookmarks(@Query("auth_token") String authToken, @Query("start") String start, @Query("results") String results) throws AuthenticationException;

	@GET("v1/posts/delete")
    Call<PinboardApiResult> deleteBookmark(@Query("auth_token") String authToken, @Query("url") String url) throws AuthenticationException;

	@GET("v1/tags/get")
    Call<Map<String, Long>> getTags(@Query("auth_token") String authToken) throws AuthenticationException;

	@GET("v1/posts/suggest")
    Call<List<TagSuggestions>> getTagSuggestions(@Query("auth_token") String authToken, @Query("url") String url) throws AuthenticationException;

	@GET("v1/notes/list")
    Call<NoteList> getNotes(@Query("auth_token") String authToken) throws AuthenticationException;

	@GET("v1/notes/{id}")
    Call<Note> getNote(@Query("auth_token") String authToken, @Path("id") String id) throws AuthenticationException;

	@GET("v1/user/secret")
    Call<PinboardAuthToken> getSecretToken(@Query("auth_token") String authToken) throws AuthenticationException;

    @GET("v1/user/api_token")
    Call<PinboardAuthToken> authenticate(@Header("Authorization") String authorization) throws AuthenticationException;
}
