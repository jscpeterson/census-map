package io.rs21.census.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import io.rs21.census.R;
import io.rs21.census.model.Tweet;
import java.util.List;

public class TweetAdapter extends ArrayAdapter<Tweet> {

  public TweetAdapter(@NonNull Context context,
      int resource, List<Tweet> objects) {
    super(context, resource, objects);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View v = convertView;

    if (v == null) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = inflater.inflate(R.layout.cardview_tweet, null);
    }

    TextView usernameView = v.findViewById(R.id.username_text);
    TextView timestampView = v.findViewById(R.id.timestamp_text);
    TextView tweetText = v.findViewById(R.id.tweet_text);

    usernameView.setText(getItem(position).getUsername());
    timestampView.setText(getItem(position).getTimestamp());
    tweetText.setText(getItem(position).getTweetText());

    return v;
  }
}
