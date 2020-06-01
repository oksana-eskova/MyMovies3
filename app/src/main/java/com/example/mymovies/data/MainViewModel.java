package com.example.mymovies.data;

import android.app.Application;
import android.os.AsyncTask;

import com.example.mymovies.FavoriteMovie;
import com.example.mymovies.Movie;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {
    private static MovieDatabase database;
    private LiveData<List<Movie>> movies;
    private LiveData<List<FavoriteMovie>> favoriteMovies;
    public MainViewModel(@NonNull Application application) {
        super(application);
        database=MovieDatabase.getInstance(getApplication());
        movies=database.movieDao().getAllMovies();
        favoriteMovies=database.movieDao().getAllFavoriteMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public LiveData<List<FavoriteMovie>> getFavoriteMovies() {
        return favoriteMovies;
    }

    public Movie getMovieById(int movieId){
        Movie movie=null;
        try {
            movie=new GetMovieTask().execute(movieId).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return movie;
    }
    private static class GetMovieTask extends AsyncTask<Integer,Void,Movie>{

        @Override
        protected Movie doInBackground(Integer... integers) {
            Movie movie=null;
            if(integers!=null&&integers.length>0){
                movie=database.movieDao().getMovieById(integers[0]);
            }
            return movie;
        }
    }
    public void deleteAllMovies(){
        new DeleteAllMoviesTask().execute();
    }
    private static class DeleteAllMoviesTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            database.movieDao().deleteAllMovies();
            return null;
        }
    }
    public void insertMovie(Movie movie){
        new InsertMovieTask().execute(movie);
    }
    private static class InsertMovieTask extends AsyncTask<Movie,Void,Void>{

        @Override
        protected Void doInBackground(Movie... movies) {
            if(movies!=null&&movies.length>0){
                database.movieDao().insertMovie(movies[0]);
            }
            return null;
        }
    }
    public void deleteMovie(Movie movie){
            new DeleteMovieTask().execute(movie);
    }
    private static class DeleteMovieTask extends AsyncTask<Movie,Void,Void>{

        @Override
        protected Void doInBackground(Movie... movies) {
            if(movies!=null&&movies.length>0){
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }
    }
    public void insertFavoriteMovie(FavoriteMovie movie){
        new InsertFavoriteMovieTask().execute(movie);
    }
    private static class InsertFavoriteMovieTask extends AsyncTask<FavoriteMovie,Void,Void>{

        @Override
        protected Void doInBackground(FavoriteMovie... movies) {
            if(movies!=null&&movies.length>0){
                database.movieDao().insertFavoriteMovie(movies[0]);
            }
            return null;
        }
    }
    public void deleteFavoriteMovie(FavoriteMovie movie){
        new DeleteFavoriteMovieTask().execute(movie);
    }
    private static class DeleteFavoriteMovieTask extends AsyncTask<FavoriteMovie,Void,Void>{

        @Override
        protected Void doInBackground(FavoriteMovie... movies) {
            if(movies!=null&&movies.length>0){
                database.movieDao().deleteFavoriteMovie(movies[0]);
            }
            return null;
        }
    }
    public FavoriteMovie getFavoriteMovieById(int movieId){
        FavoriteMovie movie=null;
        try {
            movie=new GetFavoriteMovieTask().execute(movieId).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return movie;
    }
    private static class GetFavoriteMovieTask extends AsyncTask<Integer,Void,FavoriteMovie>{

        @Override
        protected FavoriteMovie doInBackground(Integer... integers) {
            FavoriteMovie movie=null;
            if(integers!=null&&integers.length>0){
                movie=database.movieDao().getFavoriteMovieById(integers[0]);
            }
            return movie;
        }
    }
}
