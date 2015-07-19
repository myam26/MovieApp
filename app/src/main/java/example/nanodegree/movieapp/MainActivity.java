package example.nanodegree.movieapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    static final String TAG = MainActivity.class.getSimpleName();
    static final String IMAGE_BASE_PATH = "http://image.tmdb.org/t/p/w185/";
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Movie> listOfMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        initToolBar();
        setUpRecyclerView();

        if (savedInstanceState == null) {
            new GetMoviesTask().execute();
        }

        Log.d(TAG, "finally onCreate savedInstanceState null --> " + (savedInstanceState == null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        sortList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Const.KEY_MOVIE_LIST, listOfMovies);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listOfMovies = savedInstanceState.getParcelableArrayList(Const.KEY_MOVIE_LIST);
        setUpRecyclerView();

    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.movies);

        toolbar.inflateMenu(R.menu.menu_main);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.action_settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    return true;
                }


                return false;
            }
        });
    }

    private void setUpRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        int columns = Math.round(dpWidth / 120);

        // use a Grid layout manager
        mLayoutManager = new GridLayoutManager(this, columns);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new LazyAdapter(listOfMovies);
        mRecyclerView.setAdapter(mAdapter);

    }

    private void sortList() {
        String sortCriteria = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));

        if (sortCriteria != null)
            if (sortCriteria.matches(getString(R.string.pref_sort_default))) {
                sortListByPopularity();
            } else {
                sortListByHighestRated();
            }
    }

    private void sortListByPopularity() {
        Collections.sort(listOfMovies, new Comparator<Movie>() {
            @Override
            public int compare(Movie lhs, Movie rhs) {
                Double first = lhs.getPopularity();
                Double second = rhs.getPopularity();
                return first.compareTo(second);
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private void sortListByHighestRated() {
        Collections.sort(listOfMovies, new Comparator<Movie>() {
            @Override
            public int compare(Movie lhs, Movie rhs) {
                Double first = lhs.getUserRating();
                Double second = rhs.getUserRating();
                return first.compareTo(second);
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    public class LazyAdapter extends RecyclerView.Adapter<LazyAdapter.ViewHolder> {

        private List<Movie> mDataset;

        // Provide a suitable constructor (depends on the kind of dataset)
        public LazyAdapter(List<Movie> dataset) {
            mDataset = dataset;
        }


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageMoviePoster;

            public ViewHolder(View rootItemView) {
                super(rootItemView);
                // set view by Ids here
                imageMoviePoster = (ImageView) rootItemView.findViewById(R.id.imageView_movie_item);
            }
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_movies, viewGroup, false);
            // set the view's size, margins, paddings and layout parameters if needed

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.imageMoviePoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
                    intent.putExtra(Const.KEY_TITLE, listOfMovies.get(position).getTitle());
                    intent.putExtra(Const.KEY_IMAGE_URL, listOfMovies.get(position).getPosterImageUrl());
                    intent.putExtra(Const.KEY_RELEASE_DATE, listOfMovies.get(position).getReleaseDate());
                    intent.putExtra(Const.KEY_PLOT_SYNOPSIS, listOfMovies.get(position).getPlotSynopsis());
                    intent.putExtra(Const.KEY_USER_RATING, listOfMovies.get(position).getUserRating());
                    startActivity(intent);
                }
            });

            String imgUrl = mDataset.get(position).getPosterImageUrl();
            Picasso.with(getApplicationContext()).load(imgUrl).into(holder.imageMoviePoster);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    private class GetMoviesTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... params) {

            String url = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + Const.MOVIE_DB_API_KEY;
            String content = Utils.getStringRequest(url);
            JSONObject jsonObject;
            JSONArray jsonArray;


            String title, realeaseDate, plotSynopsis, posterPath;
            double userRating, popularity;
            int movieId;

            try {
                jsonObject = new JSONObject(content);
                jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    title = jsonArray.getJSONObject(i).getString("title");
                    realeaseDate = jsonArray.getJSONObject(i).getString("release_date");
                    plotSynopsis = jsonArray.getJSONObject(i).getString("overview");
                    posterPath = jsonArray.getJSONObject(i).getString("poster_path");
                    userRating = jsonArray.getJSONObject(i).getDouble("vote_average");
                    popularity = jsonArray.getJSONObject(i).getDouble("popularity");
                    movieId = jsonArray.getJSONObject(i).getInt("id");

                    Movie movie = new Movie();
                    movie.setTitle(title);
                    movie.setReleaseDate(realeaseDate);
                    movie.setPlotSynopsis(plotSynopsis);
                    movie.setPosterImageUrl(IMAGE_BASE_PATH + posterPath);
                    movie.setUserRating(userRating);
                    movie.setPopularity(popularity);
                    movie.setMovieId(movieId);

                    listOfMovies.add(movie);


//                    if (listOfMovies.get(i).getTitle().contains("Ant-Man")){
//
//                        Log.d(TAG, "finally title: " + listOfMovies.get(i).getTitle());
//                        Log.d(TAG, "finally date: " + listOfMovies.get(i).getReleaseDate());
//                        Log.d(TAG, "finally synopsis: " + listOfMovies.get(i).getPlotSynopsis());
//                        Log.d(TAG, "finally imageUrl: " + listOfMovies.get(i).getPosterImageUrl());
//                        Log.d(TAG, "finally rating: " + listOfMovies.get(i).getUserRating());
//                        Log.d(TAG, "finally popularity: " + listOfMovies.get(i).getPopularity());
//                        Log.d(TAG, "finally movieId: " + listOfMovies.get(i).getMovieId());
//                        Log.d(TAG, "finally .......................................... ");
//
//                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            sortList();

        }
    }

}
