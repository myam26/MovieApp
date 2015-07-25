package example.nanodegree.movieapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import example.nanodegree.movieapp.Const;
import example.nanodegree.movieapp.R;


public class MovieDetailsFragment extends Fragment {
    View rootView;
    static final String TAG = MovieDetailsFragment.class.getSimpleName();
    String title, realeaseDate, plotSynopsis, imagePosterUrl;
    double userRating;
    TextView tvTitle, tvReleaseDate, tvPlotSynopsis, tvUserRating;
    ImageView imageViewPoster;

    public MovieDetailsFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.movie_details, container, false);
        rootView.findViewById(R.id.toolbar).setVisibility(View.GONE);
        getMovieDetails();
        initializeViews();


        return rootView;
    }

    private void getMovieDetails() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString(Const.KEY_TITLE);
            realeaseDate = bundle.getString(Const.KEY_RELEASE_DATE);
            plotSynopsis = bundle.getString(Const.KEY_PLOT_SYNOPSIS);
            imagePosterUrl = bundle.getString(Const.KEY_IMAGE_URL).replace("/w185/", "/w500/");
            userRating = bundle.getDouble(Const.KEY_USER_RATING);
        }
    }

    private void initializeViews() {
        tvTitle = (TextView) rootView.findViewById(R.id.movie_details_title);
        tvReleaseDate = (TextView) rootView.findViewById(R.id.movie_details_release_date);
        tvPlotSynopsis = (TextView) rootView.findViewById(R.id.movie_details_plot_synopsis);
        tvUserRating = (TextView) rootView.findViewById(R.id.movie_details_rating);
        imageViewPoster = (ImageView) rootView.findViewById(R.id.movie_details_image_poster);

        tvTitle.setText(title);
        tvReleaseDate.setText(getFormattedDate());
        tvPlotSynopsis.setText(plotSynopsis);
        tvUserRating.setText(userRating + "/" + getString(R.string.rating_total));
        Picasso.with(getActivity()).load(imagePosterUrl).into(imageViewPoster);
    }

    private String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = new Date();
        try {
            date = sdf.parse(realeaseDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);

        return sdf.format(date);
    }

}
