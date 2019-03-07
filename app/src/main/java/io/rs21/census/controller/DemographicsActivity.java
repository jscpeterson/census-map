package io.rs21.census.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import io.rs21.census.R;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class DemographicsActivity extends AppCompatActivity {

  private static final String POPULATION_TOTAL_KEY = "ACS_13_5YR_B01001_with_ann_HD01_VD01";
  private static final String MALE_TOTAL_KEY = "ACS_13_5YR_B01001_with_ann_HD01_VD02";
  private static final String FEMALE_TOTAL_KEY = "ACS_13_5YR_B01001_with_ann_HD01_VD26";
  private static final String MALE_MEDIAN_KEY = "ACS_13_5YR_B01002_with_ann_HD01_VD03";
  private static final String FEMALE_MEDIAN_KEY = "ACS_13_5YR_B01002_with_ann_HD01_VD04";
  private static final String CENSUS_PROPERTIES_FORMAT = "ACS_13_5YR_%s_with_ann_%s";

  private static final int INDEX_PREFIX = 0;
  private static final int INDEX_LABEL = 1;
  private static final String AGE_DATA_PREFIX = "B01001";
  private static final String ESTIMATE_PREFIX = "HD01";

  private static final String EMPTY_STRING = "";
  private static final String MALE_LABEL_EXTRANEOUS_INFO = "Estimate; Male: - ";
  private static final String FEMALE_LABEL_EXTRANEOUS_INFO = "Estimate; Female: - ";

  private static final double MIN_CHART_BOUNDS = 1.0;
  private static final int ANIMATION_TIME_X = 1200;
  private static final int ANIMATION_TIME_Y = 1200;
  private static final float HOLE_RADIUS = 25f;
  private static final float TRANSPARENT_CIRCLE_RADIUS = 25f;

  private Bundle properties;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    properties = getIntent().getExtras();
    setContentView(R.layout.activity_demographics);
    setTitle(properties.getString(MapsActivity.GEO_DISPLAY_LABEL));

    // Initializes the pie objects.
    PieChart malePie = findViewById(R.id.pie);
    PieChart femalePie = findViewById(R.id.pie2);

    // Initializes the entry lists for the pie charts and parses the data into them with the
    // parsePopulationData method.
    List<PieEntry> maleEntries = new ArrayList<>();
    List<PieEntry> femaleEntries = new ArrayList<>();
    parsePopulationData(maleEntries, femaleEntries);

    // Initializes the dataset objects for the created entry lists.
    PieDataSet malePieDataSet = new PieDataSet(maleEntries, getString(R.string.pie_data_label_male));
    PieDataSet femalePieDataSet = new PieDataSet(femaleEntries, getString(R.string.pie_data_label_female));

    // Sets the color templates for the pie charts.
    malePieDataSet.setColors(
        getResources().getColor(R.color.male1), getResources().getColor(R.color.male2),
        getResources().getColor(R.color.male3), getResources().getColor(R.color.male4),
        getResources().getColor(R.color.male5));
    femalePieDataSet.setColors(
        getResources().getColor(R.color.female1), getResources().getColor(R.color.female2),
        getResources().getColor(R.color.female3), getResources().getColor(R.color.female4),
        getResources().getColor(R.color.female5));

    // Loads the data into the pie charts and formats them.
    bakePie(malePie, malePieDataSet);
    malePie.setCenterText("MALE");
    bakePie(femalePie, femalePieDataSet);
    femalePie.setCenterText("FEMALE");
    setDescriptions(malePie, femalePie);
  }

  /**
   * Sets up a pie object to be displayed based on a given pie object and dataset.
   *
   * @param pie the pie object
   * @param dataSet the PieDataSet collected
   * @return a new pie containing the dataset and with with formatting configurations.
   */
  private void bakePie(PieChart pie, PieDataSet dataSet) {
    PieData data = new PieData(dataSet);
    data.setValueTextColor(Color.WHITE);
    pie.setData(data);
    pie.setUsePercentValues(true);
    pie.setHoleRadius(HOLE_RADIUS);
    pie.setTransparentCircleRadius(TRANSPARENT_CIRCLE_RADIUS);
    pie.getLegend().setEnabled(false);
    pie.setEntryLabelColor(Color.BLACK);
    pie.animateXY(ANIMATION_TIME_X, ANIMATION_TIME_Y);
  }

  /**
   * Parses the population data from the B01001 file containing gender and age demographics. Stores
   * data in a mutable PieEntry list passed into the method.
   *
   * @param maleEntries A list of PieEntries for the male data
   * @param femaleEntries A list of PieEntries for the female data
   */
  private void parsePopulationData(List<PieEntry> maleEntries,
      List<PieEntry> femaleEntries) {
    try {
      InputStream inputStream = getApplicationContext().getResources()
          .openRawResource(R.raw.acs_13_5yr_b01001);
      CSVParser parser = new CSVParser(new InputStreamReader(inputStream), CSVFormat.DEFAULT);
      for (CSVRecord record : parser.getRecords()) {
        // Only get data which starts with the estimate prefix (as opposed to the margin of error prefix)
        // and does not end in a colon (as these indicate a total)
        if (record.get(INDEX_PREFIX).startsWith(ESTIMATE_PREFIX) && !record.get(INDEX_LABEL).endsWith(":")) {
          if (record.get(INDEX_LABEL).contains("Male")) {
            String format = String
                .format(CENSUS_PROPERTIES_FORMAT, AGE_DATA_PREFIX, record.get(INDEX_PREFIX));
            int value = Integer.valueOf(properties.getString(format));
            if (value >= MIN_CHART_BOUNDS) {
              String label = record.get(INDEX_LABEL).replace(MALE_LABEL_EXTRANEOUS_INFO, EMPTY_STRING);
              maleEntries.add(new PieEntry(value, label));
            }
          } else if (record.get(INDEX_LABEL).contains("Female")) {
            String format = String
                .format(CENSUS_PROPERTIES_FORMAT, AGE_DATA_PREFIX, record.get(INDEX_PREFIX));
            int value = Integer.valueOf(properties.getString(format));
            if (value >= MIN_CHART_BOUNDS) {
              String label = record.get(INDEX_LABEL).replace(FEMALE_LABEL_EXTRANEOUS_INFO, EMPTY_STRING);
              femaleEntries.add(new PieEntry(value, label));
            }
          }
        }
      }
    } catch (IOException e) {
      // TODO Handle exception.
    }
  }

  /**
   * Sets the descriptions for the male and female pie charts. See pie_description_format in Strings.
   *
   * @param malePie the male pie chart.
   * @param femalePie the female pie chart.
   */
  private void setDescriptions(PieChart malePie, PieChart femalePie) {
    // Calculates the male percentage of the population given.
    double malePercent = Double.valueOf(properties.getString(MALE_TOTAL_KEY)) / Double
        .valueOf(properties.getString(POPULATION_TOTAL_KEY));
    // Formats the description to display the male percentage and the male median age.
    malePie.getDescription().setText(String.format(getString(R.string.pie_description_format_male),
        malePercent * 100, properties.getString(MALE_MEDIAN_KEY)));

    // Calculates the female percentage of the population given.
    double femalePercent = Double.valueOf(properties.getString(FEMALE_TOTAL_KEY)) / Double
        .valueOf(properties.getString(POPULATION_TOTAL_KEY));
    // Formats the description to display the female percentage and the female median age.
    femalePie.getDescription().setText(String.format(getString(R.string.pie_description_format_female),
        femalePercent * 100, properties.getString(FEMALE_MEDIAN_KEY)));
  }

}