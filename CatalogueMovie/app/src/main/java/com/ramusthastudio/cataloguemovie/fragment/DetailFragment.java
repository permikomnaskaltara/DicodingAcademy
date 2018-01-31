package com.ramusthastudio.cataloguemovie.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.ramusthastudio.cataloguemovie.BuildConfig;
import com.ramusthastudio.cataloguemovie.Genre;
import com.ramusthastudio.cataloguemovie.R;
import com.ramusthastudio.cataloguemovie.model.Result;
import com.ramusthastudio.cataloguemovie.repo.DatabaseContract;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.provider.BaseColumns._ID;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.CONTENT_URI;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.BACKDROP;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.GENRE;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.OVERVIEW;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.POPULARITY;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.POSTER;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.RATING;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.RELEASE_DATE;
import static com.ramusthastudio.cataloguemovie.repo.DatabaseContract.MovieColumns.TITLE;

public class DetailFragment extends Fragment {
  protected static final String ARG_PARAM = "result";
  private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("E, dd-MM-yyyy", Locale.getDefault());
  private ImageView fItemImageView;
  private TextView fItemTitleView;
  private TextView fItemDateView;
  private TextView fItemGenreView;
  private TextView fItemRatingView;
  private TextView fItemDescView;
  private TextView fItemPopularityView;
  private Result fResult;
  private ImageView fFavoriteView;
  private StringBuilder fGenreBuilder;
  private boolean fIsFavorited;
  private AppBarLayout fAppBarView;
  private CollapsingToolbarLayout fCollapsingToolbarView;

  public DetailFragment() {
    // Required empty public constructor
  }

  @Override
  public void onAttach(final Context context) {
    super.onAttach(context);

    if (getArguments() == null) {
      throw new NullPointerException("Args null");
    }

    fResult = (Result) getArguments().getSerializable(ARG_PARAM);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        Log.d("DetailFragment", "Backhome");
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_detail, container, false);

    if (getActivity() != null) {
      final AppCompatActivity ctx = (AppCompatActivity) getActivity();

      ctx.setSupportActionBar((Toolbar) view.findViewById(R.id.toolbar));
      if (ctx.getSupportActionBar() != null) {
        ctx.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }
    }

    fItemImageView = view.findViewById(R.id.detailImage);
    fItemTitleView = view.findViewById(R.id.detailTitle);
    fItemDateView = view.findViewById(R.id.detailDate);
    fItemGenreView = view.findViewById(R.id.detailGenre);
    fItemRatingView = view.findViewById(R.id.detailRating);
    fItemDescView = view.findViewById(R.id.detailDesc);
    fItemPopularityView = view.findViewById(R.id.detailPopularity);
    fFavoriteView = view.findViewById(R.id.detailFav);
    fAppBarView = view.findViewById(R.id.detailAppBar);
    fCollapsingToolbarView = view.findViewById(R.id.detailImageCollapsingToolbar);
    fCollapsingToolbarView.setExpandedTitleColor(Color.parseColor("#00FFFFFF"));

    if (fResult != null) {
      if (getActivity() != null) {
        if (fResult.getBackdropPath() != null) {
          Glide
              .with(getActivity())
              .load(BuildConfig.IMAGE_URL + "/w500" + fResult.getBackdropPath())
              .into(fItemImageView)
              .clearOnDetach();
        }

        fCollapsingToolbarView.setTitle(fResult.getTitle());
        fItemTitleView.setText(fResult.getTitle());

        if (fResult.getReleaseDate() != null) {
          fItemDateView.setText(sDateFormat.format(fResult.getReleaseDate()));
        } else {
          fItemDateView.setText("Unknown");
        }
        fItemPopularityView.setText(String.valueOf(fResult.getPopularity()));

        StringBuilder b = new StringBuilder();
        fGenreBuilder = new StringBuilder();
        for (int genre : fResult.getGenreIds()) {
          fGenreBuilder.append(genre).append(" ");
          b.append(Genre.displayName(genre)).append(" ");
        }
        fItemGenreView.setText(b.toString());
        fItemRatingView.setText(String.valueOf(fResult.getVoteAverage()));
        fItemDescView.setText(String.valueOf(fResult.getOverview()));

        final Cursor cursor = getActivity().
            getContentResolver()
            .query(Uri.parse(CONTENT_URI + "/" + fResult.getId()), null, null, null, null);

        if (cursor != null) {
          if (cursor.moveToFirst()) {
            final int dbId = DatabaseContract.getColumnInt(cursor, _ID);
            if (dbId == fResult.getId()) {
              fIsFavorited = true;
              fFavoriteView.setImageResource(android.R.drawable.star_big_on);
            } else {
              fIsFavorited = false;
              fFavoriteView.setImageResource(android.R.drawable.star_big_off);
            }
          }
          cursor.close();
        }
      }
    }

    fAppBarView.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
      @Override
      public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
          fItemRatingView.setVisibility(View.GONE);
          fFavoriteView.setVisibility(View.GONE);
        } else {
          fItemRatingView.setVisibility(View.VISIBLE);
          fFavoriteView.setVisibility(View.VISIBLE);
        }
      }
    });

    fFavoriteView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        if (fIsFavorited) {
          showSnackbarMessage(getString(R.string.already_saved));
          return;
        }

        if (fResult != null) {
          ContentValues values = new ContentValues();
          values.put(_ID, fResult.getId());
          values.put(TITLE, fResult.getTitle());
          values.put(POSTER, fResult.getPosterPath());
          values.put(BACKDROP, fResult.getBackdropPath());
          values.put(RELEASE_DATE, fResult.getReleaseDate().getTime());
          values.put(GENRE, fGenreBuilder.toString());
          values.put(RATING, fResult.getVoteAverage());
          values.put(OVERVIEW, fResult.getOverview());
          values.put(POPULARITY, fResult.getPopularity());

          if (getActivity() != null) {
            final Uri createdUri = getActivity()
                .getContentResolver()
                .insert(CONTENT_URI, values);

            if (createdUri != null) {
              fIsFavorited = true;
              fFavoriteView.setImageResource(android.R.drawable.star_big_on);
              showSnackbarMessage(getString(R.string.save_fav_success));
            }
          }
        }
      }
    });

    return view;
  }

  private void showSnackbarMessage(String message) {
    if (getView() != null) {
      Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }
  }

  public static DetailFragment newInstance(Result aResult) {
    DetailFragment fragment = new DetailFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_PARAM, aResult);
    fragment.setArguments(args);
    return fragment;
  }
}