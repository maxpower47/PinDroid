package com.pindroid.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PinboardFeedClient {
    private static PinboardFeedService REST_CLIENT;
    private static final String ROOT = "https://feeds.pinboard.in/";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    static {
        setupRestClient();
    }

    private PinboardFeedClient() { }

    public static PinboardFeedService get() {
        return REST_CLIENT;
    }

    private static void setupRestClient() {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    @Override
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        try {
                            return new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(json.getAsString());
                        } catch (ParseException e) {
                        }
                        throw new JsonParseException("Unparseable date: \"" + json.getAsString()
                                + "\". Supported format: " + DATE_FORMAT);
                    }
                })
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("User-Agent", "PinDroid")
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();

        Retrofit prodAdapter = new Retrofit.Builder()
                .baseUrl(ROOT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        REST_CLIENT = prodAdapter.create(PinboardFeedService.class);
    }
}
