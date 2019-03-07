package io.rs21.census.model;

import com.google.android.gms.maps.model.LatLng;
import java.time.LocalDateTime;
import org.apache.commons.csv.CSVRecord;

public class Tweet {

  private static final int CSV_INDEX_TEXT = 0;
  private static final int CSV_INDEX_USERNAME = 1;
  private static final int CSV_INDEX_LAT = 2;
  private static final int CSV_INDEX_LONG = 3;
  private static final int CSV_INDEX_TIMESTAMP = 4;

  private String tweetText;
  private String username;
  private LatLng latLng;
  private String timestamp; // TODO Change to LocalDateTime

  /**
   * Constructor for a Tweet object based on a given CSVRecord assumed to be from twitter_141103.csv
   * @param record CSV Record
   */
  public Tweet(CSVRecord record) {
    tweetText = record.get(CSV_INDEX_TEXT);
    username = record.get(CSV_INDEX_USERNAME);
    latLng = new LatLng(Double.valueOf(record.get(CSV_INDEX_LAT)),
        Double.valueOf(record.get(CSV_INDEX_LONG)));
    timestamp = record.get(CSV_INDEX_TIMESTAMP);
  }

  public String getTweetText() {
    return tweetText;
  }

  public String getUsername() {
    return username;
  }

  public LatLng getLatLng() {
    return latLng;
  }

  public String getTimestamp() {
    return timestamp;
  }

}
