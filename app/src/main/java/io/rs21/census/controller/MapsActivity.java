package io.rs21.census.controller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPolygon;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;
import io.rs21.census.model.FacebookPlace;
import io.rs21.census.R;
import io.rs21.census.model.Tweet;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final double DEFAULT_LAT = 35.0844;
  private static final double DEFAULT_LONG = -106.6504;
  private static final float DEFAULT_ZOOM = 15.0f;
  static final String GEO_DISPLAY_LABEL = "ACS_13_5YR_B11001_with_ann_GEO.display-label";
  private static final String CATEGORY_GENERIC = "Local business";
  private static final String CATEGORY_RESTAURANT = "Restaurant/cafe";
  private static final String CATEGORY_SPAS = "Spas/beauty/personal care";
  private static final String CATEGORY_SHOPPING = "Shopping/retail";
  private static final String CATEGORY_AUTOMOTIVE = "Automotive";

  private GoogleMap map;
  private GeoJsonFeature selectedFeature;
  private GeoJsonPolygonStyle unselectedStyle = new GeoJsonPolygonStyle();
  private GeoJsonPolygonStyle selectedStyle = new GeoJsonPolygonStyle();
  private ArrayList<Tweet> tweets;
  private ArrayList<FacebookPlace> places;
  private ArrayList<Marker> markers = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    parseData();
    setContentView(R.layout.activity_maps);
    setSelectionStyles();
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  /**
   * Attempts to parse the place and tweet data to a local variable and exits the application if
   * an error occurs.
   */
  private void parseData() {
    try {
      places = parsePlaces();
      tweets = parseTweets();
    } catch (IOException e) {
      // TODO Handle exception.
      System.out.println("Failed to parse data.");
      System.exit(1);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.options, menu);
    return true;
  }

  /**
   * Handles a click event on the options item menu for the Android toolbar.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean handled = true;
    if (selectedFeature == null) {
      Toast.makeText(this, R.string.no_feature_selected, Toast.LENGTH_SHORT).show();
      return handled;
    }
    Intent intent;
    hideMarkers();
    switch (item.getItemId()) {
      default:
        handled = super.onOptionsItemSelected(item);
        break;

      // ICON MENU OPTIONS
      case R.id.menu_tweets:
        TweetsFragment tweetsFragment = new TweetsFragment();
        tweetsFragment.show(getSupportFragmentManager(), "tweets");
        break;
      case R.id.menu_demographics:
        intent = new Intent(MapsActivity.this, DemographicsActivity.class);
        for (String key : selectedFeature.getPropertyKeys()) {
          intent.putExtra(key, selectedFeature.getProperty(key));
        }
        startActivity(intent);
        break;
      case R.id.menu_transportation:
        intent = new Intent(MapsActivity.this, TransportationActivity.class);
        for (String key : selectedFeature.getPropertyKeys()) {
          intent.putExtra(key, selectedFeature.getProperty(key));
        }
        startActivity(intent);
        break;

      // DROPDOWN MENU OPTIONS
      // TODO Find a better way to organize category data.
//    Hiding this category as it is too generic, although 8770 entries are listed as this.
//      case R.id.menu_cat_local:
//        filterMapMarkers(selectedFeature, CATEGORY_GENERIC);
//        break;
      case R.id.menu_cat_restaurant:
        filterMapMarkers(selectedFeature, CATEGORY_RESTAURANT);
        break;
      case R.id.menu_cat_spas:
        filterMapMarkers(selectedFeature, CATEGORY_SPAS);
        break;
      case R.id.menu_cat_shopping:
        filterMapMarkers(selectedFeature, CATEGORY_SHOPPING);
        break;
      case R.id.menu_cat_auto:
        filterMapMarkers(selectedFeature, CATEGORY_AUTOMOTIVE);
        break;
    }
    return handled;
  }

  /**
   * Manipulates the map once available. This callback is triggered when the map is ready to be
   * used.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    map = googleMap;

    // Prebuilds all the map markers for FacebookPlace data so they only need to be flagged as
    // visible when needed.
    for (FacebookPlace place : places) {
      Marker marker = map.addMarker(new MarkerOptions()
          .position(place.getLatLng())
          .title(place.getTitle())
          .snippet(place.getCategory()));
      markers.add(marker);
      marker.setTag(place);
      marker.setVisible(false);
    }
    try {
      // Creates layer of polygons from census block data
      GeoJsonLayer layer = new GeoJsonLayer(map, R.raw.bernallio_census_blocks,
          getApplicationContext());
      // Applies the layer to the map
      layer.addLayerToMap();

      // Initial state of MapCamera - moves the camera to Albuquerque and zooms in to city level.
      // TODO Find user location and set map camera and selected feature to the user's census block.
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LAT, DEFAULT_LONG),
          DEFAULT_ZOOM));

      // ClickListener for a polygon map feature.
      layer.setOnFeatureClickListener(this::updateSelectedBlock);

      // ClickListener for a map marker. If this is not set the Feature click listener is called
      // which throws an exception.
      map.setOnMarkerClickListener(marker -> {
        marker.showInfoWindow();
        return true;
      });

    } catch (IOException | JSONException e) {
      // TODO Handle exception.
    }
  }

  /**
   * Filters the Markers of the MapActivity by markers within a given geoJsonFeature and a String
   * category. Displays a toast if no markers were found.
   *
   * @param geoJsonFeature the GeoJsonFeature polygon of a given map area.
   * @param category a String such as "Local business" as associated with the FacebookPlace.
   */
  private void filterMapMarkers(GeoJsonFeature geoJsonFeature, String category) {
    boolean nothingFound = true;
    for (Marker marker : markers) {
      GeoJsonPolygon polygon = (GeoJsonPolygon) geoJsonFeature.getGeometry();
      FacebookPlace taggedPlace = (FacebookPlace) marker.getTag();
      if (PolyUtil.containsLocation(marker.getPosition(), polygon.getCoordinates().get(0), true)
          && taggedPlace.getCategory().equals(category)) {
        if (nothingFound) {
          nothingFound = false;
        }
        marker.setVisible(true);
      }
    }
    if (nothingFound) {
      String noResultsFoundMessage = String.format(getString(R.string.no_results_format),
          category, geoJsonFeature.getProperty(GEO_DISPLAY_LABEL));
      Toast.makeText(this, noResultsFoundMessage, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Builds the LatLngBounds from a GeoJsonFeature associated with a polygon.
   *
   * @param geoJsonFeature a GeoJsonFeature associated with a map polygon.
   * @return the LatLngBounds of the polygon.
   */
  private LatLngBounds getLatLngBounds(GeoJsonFeature geoJsonFeature) {
    GeoJsonPolygon polygon = (GeoJsonPolygon) geoJsonFeature.getGeometry();
    Builder builder = new Builder();
    for (LatLng latLng : polygon.getCoordinates().get(0)) {
      builder.include(latLng);
    }
    return builder.build();
  }

  /**
   * Getter to retrieve the selected feature from an outside class.
   * @return the selected feature.
   */
  GeoJsonFeature getSelectedFeature() {
    return selectedFeature;
  }

  /**
   * Getter to retreive the tweet data from an outside class.
   * @return an ArrayList of locally saved tweets.
   */
  ArrayList<Tweet> getTweets() {
    return tweets;
  }

  /**
   * Hides all map markers in the MapsActivity.
   */
  private void hideMarkers() {
    for (Marker marker : markers) {
      marker.setVisible(false);
    }
  }

  /**
   * Parses data from the facebookplaces_albuquerque.csv and returns the resulting data as an
   * ArrayList of FacebookPlace objects. Record parsing is done in the constructor of the
   * FacebookPlace object.
   *
   * @return an ArrayList of FacebookPlaces.
   */
  private ArrayList<FacebookPlace> parsePlaces() throws IOException {
    ArrayList<FacebookPlace> places = new ArrayList<>();
    InputStream placesInputStream = getApplicationContext().getResources()
        .openRawResource(R.raw.facebookplaces_albuquerque);
    CSVParser csvParser = new CSVParser(new InputStreamReader(placesInputStream),
        CSVFormat.DEFAULT);
    for (CSVRecord record : csvParser.getRecords()) {
      try {
        places.add(new FacebookPlace(record));
      } catch (NumberFormatException e) {
        System.out.printf("BAD DATA: %s", record.toString());
        throw new IOException();
      }
    }
    return places;
  }

  /**
   * Parses data from the twitter_141103.csv and returns the resulting data as an
   * ArrayList of Tweet objects. Record parsing is done in the constructor of the
   * Tweet object.
   *
   * @return an ArrayList of FacebookPlaces.
   */
  private ArrayList<Tweet> parseTweets() throws IOException {
    ArrayList<Tweet> tweets = new ArrayList<>();
    InputStream tweetsInputStream = getApplicationContext().getResources()
        .openRawResource(R.raw.twitter_141103);
    CSVParser csvParser = new CSVParser(new InputStreamReader(tweetsInputStream),
        CSVFormat.DEFAULT);
    for (CSVRecord record : csvParser.getRecords()) {
      try {
        tweets.add(new Tweet(record));
      } catch (NumberFormatException e) {
        System.out.printf("BAD DATA: %s", record.toString());
        throw new IOException();
      }
    }
    return tweets;
  }

  /**
   * Sets styles for unselected/selected polygon map states.
   */
  private void setSelectionStyles() {
    unselectedStyle.setFillColor(Color.TRANSPARENT);
    selectedStyle.setFillColor(getResources().getColor(R.color.colorSelectedPolygon));
  }

  /**
   * Updates the MapsActivity selectedFeature to a new GeoJsonFeature. If the GeoJsonFeature is
   * already the selectedFeature (the user clicked an already selected feature), method does nothing.
   *
   * @param geoJsonFeature a new GeoJsonFeature to update as the selected block.
   */
  private void updateSelectedBlock(GeoJsonFeature geoJsonFeature) {
    if (selectedFeature == null) {
      // Do nothing if there is no selected feature.
    } else if (geoJsonFeature.equals(selectedFeature)) {
      // Ignore user click if the user clicked on the selected feature.
      return;
    } else {
      // Remove styling from the previously selected feature.
      selectedFeature.setPolygonStyle(unselectedStyle);
      hideMarkers();
    }

    // Change title to selected census block label.
    String censusBlockLabel = geoJsonFeature.getProperty(GEO_DISPLAY_LABEL);
    setTitle(censusBlockLabel);

    // Change styling of polygon on map.
    geoJsonFeature.setPolygonStyle(selectedStyle);

    // Moves camera to center (more or less) of polygon.
    LatLngBounds bounds = getLatLngBounds(geoJsonFeature);
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), DEFAULT_ZOOM));

    // Update the locally defined selectedFeature to the new geoJsonFeature.
    selectedFeature = geoJsonFeature;
  }

}