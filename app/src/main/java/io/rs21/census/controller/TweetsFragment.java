package io.rs21.census.controller;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.geojson.GeoJsonPolygon;
import io.rs21.census.R;
import io.rs21.census.model.Tweet;
import io.rs21.census.view.TweetAdapter;
import java.util.ArrayList;

public class TweetsFragment extends DialogFragment {

  private MapsActivity parentActivity;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    super.onCreateDialog(savedInstanceState);
    // Initializes dialog fragment builder.
    AlertDialog.Builder builder = new Builder(getActivity());
    // Initializes view.
    View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_tweets, null, false);
    // Initializes ListView object.
    ListView listView = view.findViewById(R.id.tweets_list);
    // Initializes parent Maps activity.
    parentActivity = (MapsActivity) getActivity();
    // Runs the getLocalTweets method to populate a list of tweets.
    ArrayList<Tweet> localTweets = getLocalTweets();
    if (localTweets.isEmpty()) {
      // If no tweets were found enable the error message textview.
      TextView errorText = view.findViewById(R.id.no_tweets_found_message);
      errorText.setVisibility(View.VISIBLE);
    } else {
      // Otherwise, build the tweet adapter with all the local tweets found.
      TweetAdapter tweetAdapter = new TweetAdapter(getContext(), R.layout.cardview_tweet,
          getLocalTweets());
      listView.setAdapter(tweetAdapter);
      // Set the title of the Dialog fragment as it is valid..
      builder.setTitle(R.string.title_local_tweets);
    }
    // Sets the view.
    builder.setView(view);
    // Creates and returns the AlertDialog.
    return builder.create();
  }

  /**
   * Retrieves the local tweets from the selectedFragment and tweets in the parent MapsActivity as
   * defined locally.
   *
   * @return a list of tweets made within the census block.
   */
  private ArrayList<Tweet> getLocalTweets() {
    ArrayList<Tweet> tweets = new ArrayList<>();
    GeoJsonPolygon polygon = (GeoJsonPolygon) parentActivity.getSelectedFeature().getGeometry();
    for (Tweet tweet : parentActivity.getTweets()) {
      if (PolyUtil.containsLocation(tweet.getLatLng(), polygon.getCoordinates().get(0), true)) {
        tweets.add(tweet);
      }
    }
    return tweets;
  }

}