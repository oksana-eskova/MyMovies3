package com.example.mymovies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymovies.adapters.ReviewAdapter;
import com.example.mymovies.adapters.TrailerAdapter;
import com.example.mymovies.data.MainViewModel;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import static com.example.mymovies.utils.JsonUtils.getReviewsFromJSON;
import static com.example.mymovies.utils.JsonUtils.getTrailersFromJSON;
import static com.example.mymovies.utils.NetworkUtils.getJSONForReviews;
import static com.example.mymovies.utils.NetworkUtils.getJSONForVideos;

public class DetailActivity extends AppCompatActivity {
    private ImageView imageViewBigPoster;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewReleaseDate;
    private TextView textViewOverview;
    private ImageView imageViewAddToFavorite;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private TrailerAdapter trailerAdapter;
    private  ReviewAdapter reviewAdapter;
    private ScrollView scrollViewInfo;

    private Movie movie;
    FavoriteMovie favoriteMovie;

    private int id;
    private MainViewModel viewModel;
    private String lang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        imageViewBigPoster=findViewById(R.id.imageViewBigPoster);
        textViewTitle=findViewById(R.id.textViewTitle);
        textViewOriginalTitle=findViewById(R.id.textViewOriginalTitle);
        textViewRating=findViewById(R.id.textViewRating);
        textViewReleaseDate=findViewById(R.id.textViewReleaseData);
        textViewOverview=findViewById(R.id.textViewOverview);
        imageViewAddToFavorite=findViewById(R.id.imageViewAddToFavorite);
        scrollViewInfo=findViewById(R.id.scrollViewInfo);

        lang= Locale.getDefault().getLanguage();

        Intent intent=getIntent();
        if(intent!=null&&intent.hasExtra("id")){
            id=intent.getIntExtra("id",-1);
        }else {
            finish();//возврат к вызвавшей активности
        }
        viewModel= ViewModelProviders.of(this).get(MainViewModel.class);
        movie=viewModel.getMovieById(id);
        Picasso.get().load(movie.getBigPosterPath()).placeholder(R.drawable.no_picture).into(imageViewBigPoster);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewRating.setText(Double.toString(movie.getVoteAverage()));
        textViewReleaseDate.setText(movie.getReleaseDate());
        textViewOverview.setText(movie.getOverview());
        setFavorite();
        //трейлеры и отзывы
        recyclerViewTrailers=findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews=findViewById(R.id.reccyclerViewReviews);
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        trailerAdapter=new TrailerAdapter();
        reviewAdapter=new ReviewAdapter();
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewReviews.setAdapter(reviewAdapter);
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void onTrailerClick(String url) {
                Intent intentToTrailer=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentToTrailer);
            }
        });

        JSONObject jsonObjectTrailers=getJSONForVideos(movie.getId(), lang);
        List<Trailer> trailers= getTrailersFromJSON(jsonObjectTrailers);
        trailerAdapter.setTrailers(trailers);

        JSONObject jsonObjectReviews=getJSONForReviews(id, lang);
        List<Review> reviews= getReviewsFromJSON(jsonObjectReviews);
        reviewAdapter.setReviews(reviews);
        scrollViewInfo.smoothScrollTo(0,0);

    }

    public void onClickAddToFavorite(View view) {
        if(favoriteMovie==null){
            viewModel.insertFavoriteMovie(new FavoriteMovie(movie));
            Toast.makeText(this,"Добавлено в избранное",Toast.LENGTH_SHORT).show();
        }else{
            viewModel.deleteFavoriteMovie(favoriteMovie);
            Toast.makeText(this,"Удалено из избранного",Toast.LENGTH_SHORT).show();
        }
        setFavorite();
    }
    private void setFavorite(){
        favoriteMovie=viewModel.getFavoriteMovieById(id);
        if(favoriteMovie==null){
            imageViewAddToFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }else{
            imageViewAddToFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int id=item.getItemId();
        switch(id){
            case R.id.itemMain:
                intent=new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.itemFavourite:
                intent=new Intent(this,FavoriteActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
