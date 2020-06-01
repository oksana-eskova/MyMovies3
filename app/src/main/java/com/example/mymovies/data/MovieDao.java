package com.example.mymovies.data;

import com.example.mymovies.FavoriteMovie;
import com.example.mymovies.Movie;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MovieDao {
    @Query("SELECT * FROM movies")
    LiveData<List<Movie>> getAllMovies();
    @Query("SELECT * FROM movies WHERE id== :movieId")
    Movie getMovieById(int movieId);
    @Query("DELETE FROM movies")
    void deleteAllMovies();
    @Insert
    void insertMovie(Movie movie);
    @Delete
    void deleteMovie(Movie movie);
    @Query("SELECT * FROM favorite_movies")
    LiveData<List<FavoriteMovie>> getAllFavoriteMovies();
    @Query("SELECT * FROM favorite_movies WHERE id== :movieId")
    FavoriteMovie getFavoriteMovieById(int movieId);
    @Insert
    void insertFavoriteMovie(FavoriteMovie favoriteMovie);
    @Delete
    void deleteFavoriteMovie(FavoriteMovie favoriteMovie);
}

