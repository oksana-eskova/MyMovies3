package com.example.mymovies.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class NetworkUtils {

    private static final String BASE_URL="https://api.themoviedb.org/3/discover/movie";
    private static final String BASE_URL_VIDEOS="https://api.themoviedb.org/3/movie/%s/videos";
    private static final String BASE_URL_REVIEWS="https://api.themoviedb.org/3/movie/%s/reviews";

    private static final String PARAMS_API_KEY="api_key";
    private static final String PARAMS_LANGUAGE="language";
    private static final String PARAMS_SORT_BY="sort_by";
    private static final String PARAMS_PAGE="page";
    private static final String PARAMS_MIN_VOTE_COUNT="vote_count.gte";

    private static final String API_KEY="77ba4a73ffcb8622c3817dc87c608944";
    private static final String SORT_BY_POPULARITY="popularity.desc";
    private static final String SORT_BY_TOP_RATED="vote_average.desc";
    private static final String MIN_VOTE_COUNT_VALUE="1000";
    public static final int POPULARITY=0;
    public static final int TOP_RATED=1;

    //создание URL для получения JSON
    public static URL buildURL(int sort_by, int page, String lang){
        String methodOfSort;
        if(sort_by==POPULARITY){
            methodOfSort=SORT_BY_POPULARITY;
        }else{
            methodOfSort=SORT_BY_TOP_RATED;
        }
        Uri uri=Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY,API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE,lang)
                .appendQueryParameter(PARAMS_SORT_BY,methodOfSort)
                .appendQueryParameter(PARAMS_PAGE,Integer.toString(page))
                .appendQueryParameter(PARAMS_MIN_VOTE_COUNT,MIN_VOTE_COUNT_VALUE)
                .build();
        URL result=null;
        try {
            result=new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }
    //создание URL для получения видео
    public static URL buildURLForVideos(int id, String lang){
           Uri uri=Uri.parse(String.format(BASE_URL_VIDEOS,id)).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY,API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE,lang)
                .build();
        URL result=null;
        try {
            result=new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }
    //создание URL для получения отзывов
    public static URL buildURLForReviews(int id, String lang){
        Uri uri=Uri.parse(String.format(BASE_URL_REVIEWS,id)).buildUpon()
                .appendQueryParameter(PARAMS_API_KEY,API_KEY)
                .appendQueryParameter(PARAMS_LANGUAGE,lang)
                .build();
        URL result=null;
        try {
            result=new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }
    //класс управляет загрузкой в отдельном потоке. Продолжает загрузку дальше при повороте экрана
    public static class JSONLoader extends AsyncTaskLoader<JSONObject>{
        public interface OnStartLoadingListener{
            void onStartLoading();
        }

        private Bundle bundle;
        OnStartLoadingListener onStartLoadingListener;

        public void setOnStartLoadingListener(OnStartLoadingListener onStartLoadingListener) {
            this.onStartLoadingListener = onStartLoadingListener;
        }

        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if(onStartLoadingListener!=null){
                onStartLoadingListener.onStartLoading();
            }
            forceLoad(); //продолжить загрузку
        }

        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if(bundle==null){
                return null;
            }
            String urlAsString=bundle.getString("url");
            URL url=null;
            try {
                url=new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if(url==null){
                return null;
            }
            JSONObject result=null;
            HttpURLConnection connection=null;
            try {
                connection=(HttpURLConnection)url.openConnection();
                InputStream inputStream=connection.getInputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder=new StringBuilder();
                String line=reader.readLine();
                while(line!=null){
                    builder.append(line);
                    line=reader.readLine();
                }
                result=new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }catch(JSONException e){
                e.printStackTrace();
            }finally
            {
                if(connection!=null){
                    connection.disconnect();
                }
            }
            return result;
        }
    }
    //класс получает данные из сети в отдельном потоке
    private static class JSONLoadTask extends AsyncTask<URL,Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject result=null;
            if(urls==null|| urls.length==0){
                return result;
            }
            HttpURLConnection connection=null;
            try {
                connection=(HttpURLConnection)urls[0].openConnection();
                InputStream inputStream=connection.getInputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder=new StringBuilder();
                String line=reader.readLine();
                while(line!=null){
                    builder.append(line);
                    line=reader.readLine();
                }
                result=new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }catch(JSONException e){
                e.printStackTrace();
            }finally
            {
                if(connection!=null){
                    connection.disconnect();
                }
            }
            return result;
        }
    }
    //метод для получения данных из сети
    public static JSONObject getJSONFromNetwork(int sort_by,int page, String lang){
        JSONObject result=null;
        URL url=buildURL(sort_by,page,lang);
        try {
            result=new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
    //метод для получения данных о видео из сети
    public static JSONObject getJSONForVideos(int id, String lang){
        JSONObject result=null;
        URL url=buildURLForVideos(id,lang);
        try {
            result=new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
    //метод для получения отзывов из сети
    public static JSONObject getJSONForReviews(int id, String lang){
        JSONObject result=null;
        URL url=buildURLForReviews(id,lang);
        try {
            result=new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
