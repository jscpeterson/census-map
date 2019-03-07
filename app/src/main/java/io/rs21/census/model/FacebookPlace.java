package io.rs21.census.model;

import com.google.android.gms.maps.model.LatLng;
import org.apache.commons.csv.CSVRecord;

public class FacebookPlace {

  private static final int CSV_INDEX_TITLE = 0;
  private static final int CSV_INDEX_CATEGORY = 1;
  private static final int CSV_INDEX_CHECKINS = 2;
  private static final int CSV_INDEX_LAT = 3;
  private static final int CSV_INDEX_LONG = 4;

  private String title;
  private String category;
  private LatLng latLng;

  /**
   * Constructor for a new FacebookPlace based on a given CSVRecord assumed to be from the
   * facebookplaces_albuquerque.csv file. Attempts to parse for incorrect commas.
   * @param csvRecord A CSV Record for a Facebook place.
   */
  public FacebookPlace(CSVRecord csvRecord) {
    int indexOffset = 0;
    StringBuilder title = new StringBuilder(csvRecord.get(CSV_INDEX_TITLE));
    // Some data in facebookplaces_albuquerque.csv is broken up due to commas in the title of the business.
    // This while loop attempts to correctly parse this data - if the next CSV value starts with a
    // space, it appends that to the title StringBuilder and increments indexOffset to offset the
    // index of the CSV values.
    while (csvRecord.get(CSV_INDEX_TITLE + indexOffset + 1).charAt(0) == ' ') {
      title.append(csvRecord.get(CSV_INDEX_TITLE + indexOffset));
      indexOffset++;
    }
    this.title = title.toString();
    category = csvRecord.get(CSV_INDEX_CATEGORY + indexOffset);
    double latCoordinate = Double.valueOf(csvRecord.get(CSV_INDEX_LAT + indexOffset));
    double longCoordinate = Double.valueOf(csvRecord.get(CSV_INDEX_LONG + indexOffset));
    latLng = new LatLng(latCoordinate, longCoordinate);
  }

  /**
   * Constructor for a FacebookPlace if given basic input values.
   * @param title Title of the business.
   * @param category Category of the business.
   * @param latCoordinate Latitude coordinate.
   * @param longCoordinate Longitude coordinate.
   */
  FacebookPlace(String title, String category, double latCoordinate, double longCoordinate) {
    this.title = title;
    this.category = category;
    latLng = new LatLng(latCoordinate, longCoordinate);
  }

  @Override
  public String toString() {
    return String.format("Title: %s, Category: %s, Coordinates: %f, %f",
        title, category, latLng.latitude, latLng.longitude);
  }

  public String getTitle() {
    return title;
  }

  public String getCategory() {
    return category;
  }

  public LatLng getLatLng() {
    return latLng;
  }

}
