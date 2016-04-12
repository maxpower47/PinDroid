package com.pindroid.client;

import com.pindroid.model.FeedBookmark;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PinboardFeedService {

	@GET("json/recent")
    Call<List<FeedBookmark>> getRecent();

	@GET("json/popular")
    Call<List<FeedBookmark>> getPopular();

	@GET("json/u:{user}")
    Call<List<FeedBookmark>> getUserRecent(@Path("user") String user);

	@GET("json/u:{user}/t:{tag}")
    Call<List<FeedBookmark>> getUserRecent(@Path("user") String user, @Path("tag") String tag);

	@GET("json/secret:{token}/u:{username}/network")
    Call<List<FeedBookmark>> getNetworkRecent(@Path("token") String token, @Path("username") String username);

	@GET("json/t:{tag}")
    Call<List<FeedBookmark>> searchGlobalTags(@Path("tag") String tag);
}
