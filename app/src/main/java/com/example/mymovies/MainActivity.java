package com.example.mymovies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymovies.adapters.MovieAdapter;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.utils.JsonUtils;
import com.example.mymovies.utils.NetworkUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject>{
    private RecyclerView recyclerViewPosters;
    private Switch switchSort;
    private TextView textViewPopularity;
    private TextView textViewTopRated;
    private ProgressBar progressBarLoading;


    private MovieAdapter adapter;
    private  ArrayList<Movie> movies;
    private MainViewModel viewModel;

    private static final int LOADER_ID=133; //идентиф. загрузчика
    private LoaderManager loaderManager;

    private static int page=1;
    private static boolean isLoading=false;
    private static int sortMethod;
    private static String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel= ViewModelProviders.of(this).get(MainViewModel.class);
        recyclerViewPosters=findViewById(R.id.recyclerViewPosters);
        switchSort=findViewById(R.id.switchSort);
        textViewPopularity=findViewById(R.id.textViewPopularity);
        textViewTopRated=findViewById(R.id.textViewTopRated);
        progressBarLoading=findViewById(R.id.progressBarLoading);

        lang= Locale.getDefault().getLanguage();


        loaderManager=LoaderManager.getInstance(this); //синглтон на загрузчик

        switchSort.setChecked(true);
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                page=1;
                setMethodOfSort(isChecked);

            }
        });

        adapter=new MovieAdapter();
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this,3));//getColumnCount()));
        recyclerViewPosters.setAdapter(adapter);
        adapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                //Toast.makeText(MainActivity.this,"Clicked "+position,Toast.LENGTH_SHORT).show();
                Movie movie=adapter.getMovies().get(position);
                Intent intent=new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id",movie.getId());
                startActivity(intent);
            }
        });
        adapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                if(!isLoading){
                    downloadData(sortMethod,page);
                    //Toast.makeText(MainActivity.this,"End Reached",Toast.LENGTH_SHORT).show();
                }

            }
        });
        switchSort.setChecked(false);
        LiveData<List<Movie>> moviesFromLiveData=viewModel.getMovies();
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if(page==1){
                    adapter.setMovies(movies);
                }
            }
        });

    }
    private void downloadData(int methodOfSort, int page){
        URL url=NetworkUtils.buildURL(methodOfSort,page,lang);
        Bundle bundle=new Bundle();
        bundle.putString("url",url.toString());
        loaderManager.restartLoader(LOADER_ID,bundle,this);
    }
    private void setMethodOfSort(boolean isTopRated){
        if(isTopRated){
            sortMethod=NetworkUtils.TOP_RATED;
            textViewTopRated.setTextColor(getResources().getColor(R.color.colorAccent));
            textViewPopularity.setTextColor(getResources().getColor(R.color.color_white));
        }else{
            sortMethod=NetworkUtils.POPULARITY;
            textViewPopularity.setTextColor(getResources().getColor(R.color.colorAccent));
            textViewTopRated.setTextColor(getResources().getColor(R.color.color_white));
        }
        downloadData(sortMethod,page);
    }
    public void onSetPopularity(View view) {
        switchSort.setChecked(false);
        setMethodOfSort(false);
    }

    public void onSetTopRated(View view) {
        switchSort.setChecked(true);
        setMethodOfSort(true);
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

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader=new NetworkUtils.JSONLoader(this,args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                isLoading=true;
                progressBarLoading.setVisibility(View.VISIBLE);
            }
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        movies= JsonUtils.getMoviesFromJSON(data);
        //adapter.setMovies(movies);
        if(movies!=null&&!movies.isEmpty()){
            if(page==1) {
                viewModel.deleteAllMovies();
                adapter.clear();
            }
            for(Movie movie:movies){
                viewModel.insertMovie(movie);
            }
            adapter.addMovies(movies);
            page++;
        }
        isLoading=false;
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }
    //расчет количества колонок в зависимости от ширины экрана
    private int getColumnCount(){
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int width=(int)(displayMetrics.widthPixels/displayMetrics.density);
        return width/185>2?width/185:2;
    }
}
