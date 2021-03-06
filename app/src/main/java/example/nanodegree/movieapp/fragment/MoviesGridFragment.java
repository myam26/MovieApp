package example.nanodegree.movieapp.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import example.nanodegree.movieapp.Const;
import example.nanodegree.movieapp.activity.MainActivity;
import example.nanodegree.movieapp.model.Movie;
import example.nanodegree.movieapp.R;
import example.nanodegree.movieapp.Utils;


public class MoviesGridFragment extends Fragment {
    View rootView;
    static final String TAG = MoviesGridFragment.class.getSimpleName();
    static final String IMAGE_BASE_PATH = "http://image.tmdb.org/t/p/w500/";
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Movie> listOfMovies = new ArrayList<>();
    List<Movie> favoritesList = new ArrayList<>();
    MoviesFragmentListener mCallBack;

    public MoviesGridFragment() {
    }

    public interface MoviesFragmentListener {
        void onMovieSelected(Bundle bundle);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallBack = (MoviesFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement MoviesFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_recycler, container, false);
        setUpRecyclerView();

        Log.d(TAG, "savedinstance is null -->" + (savedInstanceState == null));

        if (savedInstanceState == null) {

            if (Utils.isInternetConnected(getActivity()))
                new GetMoviesTask().execute();
            else
                Toast.makeText(getActivity(), R.string.check_internet_message, Toast.LENGTH_LONG).show();

        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        sortList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Const.KEY_MOVIE_LIST, listOfMovies);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null)
            listOfMovies = savedInstanceState.getParcelableArrayList(Const.KEY_MOVIE_LIST);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // mRecyclerView.setBackgroundColor(Color.RED);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        int columns = Math.round(dpWidth / 150);

        Log.d(TAG, "columns --> " + columns);

        if (((MainActivity) getActivity()).isDualPane()) {
            columns = columns / 2;
        }

        // use a Grid layout manager
        mLayoutManager = new GridLayoutManager(getActivity(), columns);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new LazyAdapter(listOfMovies);
        mRecyclerView.setAdapter(mAdapter);


    }


    private void setUpRecyclerViewFavorites() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // mRecyclerView.setBackgroundColor(Color.RED);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpWidth = outMetrics.widthPixels / density;
        int columns = Math.round(dpWidth / 150);

        Log.d(TAG, "columns --> " + columns);

        if (((MainActivity) getActivity()).isDualPane()) {
            columns = columns / 2;
        }

        // use a Grid layout manager
        mLayoutManager = new GridLayoutManager(getActivity(), columns);
        mRecyclerView.setLayoutManager(mLayoutManager);

        favoritesList = Utils.getFavoriteMovies(getActivity());

        // specify an adapter
        mAdapter = new LazyAdapter(Utils.getFavoriteMovies(getActivity()));
        mRecyclerView.setAdapter(mAdapter);


    }

    private void sortList() {
        String sortCriteria = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_sort_key), getString(R.string.most_popular));

        if (sortCriteria != null)
            if (sortCriteria.matches(getString(R.string.most_popular))) {
                sortListByPopularity();
            } else if (sortCriteria.matches(getString(R.string.highest_rated))) {
                sortListByHighestRated();
            } else if (sortCriteria.matches(getString(R.string.favorites))) {
                setUpRecyclerViewFavorites();
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
        setUpRecyclerView();
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
        setUpRecyclerView();
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

                    Bundle bundle = new Bundle();
                    String sortCriteria = PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .getString(getString(R.string.pref_sort_key), getString(R.string.most_popular));

                    if (sortCriteria != null) {
                        if (sortCriteria.matches(getString(R.string.favorites)))
                            bundle.putParcelable(Const.KEY_MOVIE, favoritesList.get(position));
                        else
                            bundle.putParcelable(Const.KEY_MOVIE, listOfMovies.get(position));
                    }

                    mCallBack.onMovieSelected(bundle);
                }
            });

            String imgUrl = mDataset.get(position).getPosterImageUrl();
            Picasso.with(getActivity()).load(imgUrl).into(holder.imageMoviePoster);
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

            if (content != null)
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

//    public class GridAdapter extends ArrayAdapter {
//
//        private List<Movie> mDataset;
//        private Context context;
//
//        public GridAdapter(Context context, int resource, List<Movie> objects) {
//            super(context, resource, objects);
//            this.context = context;
//            this.mDataset = objects;
//
//        }
//
//        public class ViewHolder {
//            public ImageView imageMoviePoster;
//        }
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//
//            ViewHolder holder;
//            if (convertView == null) {
//                holder = new ViewHolder();
//                convertView = LayoutInflater.from(context).inflate(R.layout.item_movies, parent, false);
//                holder.imageMoviePoster = (ImageView) convertView.findViewById(R.id.imageView_movie_item);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            holder.imageMoviePoster.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    Bundle bundle = new Bundle();
//                    String sortCriteria = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                            .getString(getString(R.string.pref_sort_key), getString(R.string.most_popular));
//
//                    if (sortCriteria != null) {
//                        if (sortCriteria.matches(getString(R.string.favorites)))
//                            bundle.putParcelable(Const.KEY_MOVIE, favoritesList.get(position));
//                        else
//                            bundle.putParcelable(Const.KEY_MOVIE, listOfMovies.get(position));
//                    }
//
//                    mCallBack.onMovieSelected(bundle);
//                }
//            });
//
//            String imgUrl = mDataset.get(position).getPosterImageUrl();
//            Picasso.with(getActivity()).load(imgUrl).into(holder.imageMoviePoster);
//
//
//            return convertView;
//        }
//    }

}
