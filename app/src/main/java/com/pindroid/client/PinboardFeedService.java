package com.pindroid.client;

import com.pindroid.model.FeedBookmark;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public interface PinboardFeedService {

	@GET("/json/recent")
	List<FeedBookmark> getRecent();

	@GET("/json/popular")
	List<FeedBookmark> getPopular();

	@GET("/json/u:{user}")
	List<FeedBookmark> getUserRecent(@Path("user") String user);

	@GET("/json/u:{user}/t:{tag}")
	List<FeedBookmark> getUserRecent(@Path("user") String user, @Path("tag") String tag);

	@GET("/json/secret:{token}/u:{username}/network")
	List<FeedBookmark> getNetworkRecent(@Path("token") String token, @Path("username") String username);

	@GET("/json/t:{tag}")
	List<FeedBookmark> searchGlobalTags(@Path("tag") String tag);
}
