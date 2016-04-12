package com.pindroid.client;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.pindroid.Constants;
import com.pindroid.event.AuthenticationEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PinboardClient {
    private static Context context;
	private static PinboardService REST_CLIENT;
	private static final String ROOT = "https://api.pinboard.in/";
	private static final String[] DATE_FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ssZ",
			"yyyy-MM-dd HH:mm:ss"
	};

	static {
		setupRestClient();
	}

	private PinboardClient() {}

	public static PinboardService get() {
		return REST_CLIENT;
	}

	private static void setupRestClient() {

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Boolean.class, new JsonDeserializer<Boolean>() {
					@Override
					public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
						try {
							String value = json.getAsJsonPrimitive().getAsString();
							return value.toLowerCase().equals("yes");
						} catch (ClassCastException e) {
							throw new JsonParseException("Cannot parse json boolean '" + json.toString() + "'", e);
						}
					}
				})
				.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
					@Override
					public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
						for (String format : DATE_FORMATS) {
							try {
								return new SimpleDateFormat(format, Locale.US).parse(json.getAsString().replace("Z", "+0000"));
							} catch (ParseException e) {
							}
						}
						throw new JsonParseException("Unparseable date: \"" + json.getAsString()
								+ "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
					}
				})
                .setLenient()
				.create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        HttpUrl url = chain.request().url().newBuilder().addQueryParameter("format", "json").build();

                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("User-Agent", "PinDroid")
                                .url(url)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = chain.proceed(chain.request());

                        if (Constants.HTTP_STATUS_UNAUTHORIZED == response.code()) {
                            String authToken = chain.request().url().queryParameter("auth_token");
                            EventBus.getDefault().post(new AuthenticationEvent(authToken));
                        }

                        return response;
                    }
                })
                .build();

		Retrofit prodAdapter = new Retrofit.Builder()
				.baseUrl(ROOT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
				.build();

		REST_CLIENT = prodAdapter.create(PinboardService.class);
	}
}
