package io.rs21.census.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import io.rs21.census.R;
import io.rs21.census.controller.MapsActivity;
import java.util.ArrayList;

public class TransportationActivity extends AppCompatActivity {

  private static final String CAR_TRUCK_VAN_KEY = "ACS_13_5YR_B08301_with_ann_HD01_VD02";
  private static final String CAR_TRUCK_VAN_LABEL = "Car, truck, or van";
  private static final String PUBLIC_TRANSPORT_KEY = "ACS_13_5YR_B08301_with_ann_HD01_VD10";
  private static final String PUBLIC_TRANSPORT_LABEL = "Public transportation";
  private static final String TAXI_KEY = "ACS_13_5YR_B08301_with_ann_HD01_VD16";
  private static final String TAXI_LABEL = "Taxicab";
  private static final String MOTORCYCLE_KEY = "ACS_13_5YR_B08301_with_ann_HD01_VD17";
  private static final String MOTORCYCLE_LABEL = "Motorcycle";
  private static final String BICYCLE_KEY = "ACS_13_5YR_B08301_with_ann_HD01_VD18";
  private static final String BICYCLE_LABEL = "Bicycle";
  private static final String WALK_KEY = "ACS_13_5YR_B08301_with_ann_HD01_VD19";
  private static final String WALK_LABEL = "Walking";
  private static final int ANIMATION_TIME_X = 800;
  private static final int ANIMATION_TIME_Y = 2000;

  private Bundle properties;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    properties = getIntent().getExtras();
    setContentView(R.layout.activity_transportation);
    setTitle(properties.getString(MapsActivity.GEO_DISPLAY_LABEL));
    HorizontalBarChart barChart = findViewById(R.id.chart);
    // Animates the bar chart.
    barChart.animateXY(ANIMATION_TIME_X, ANIMATION_TIME_Y);
    // Sets the data of the bar chart.
    barChart.setData(getBarChartData());
    // Sets the "X Axis" to the appropriate labels.
    barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(getBarChartLabels()));
    // Orients the labels to the left of the graph
    barChart.getXAxis().setPosition(XAxisPosition.BOTTOM);
    // Disables the description
    barChart.getDescription().setEnabled(false);
    // Disables the legend.
    barChart.getLegend().setEnabled(false);
  }

  private BarData getBarChartData() {
    ArrayList<BarEntry> entries = new ArrayList<>();
    entries.add(new BarEntry(0, Integer.valueOf(properties.getString(WALK_KEY))));
    entries.add(new BarEntry(1, Integer.valueOf(properties.getString(BICYCLE_KEY))));
    entries.add(new BarEntry(2, Integer.valueOf(properties.getString(MOTORCYCLE_KEY))));
    entries.add(new BarEntry(3, Integer.valueOf(properties.getString(TAXI_KEY))));
    entries.add(new BarEntry(4, Integer.valueOf(properties.getString(PUBLIC_TRANSPORT_KEY))));
    entries.add(new BarEntry(5, Integer.valueOf(properties.getString(CAR_TRUCK_VAN_KEY))));
    BarDataSet dataSet = new BarDataSet(entries, "Values");
    dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
    return new BarData(dataSet);
  }

  private ArrayList<String> getBarChartLabels() {
    ArrayList<String> labels = new ArrayList<>();
    labels.add(WALK_LABEL);
    labels.add(BICYCLE_LABEL);
    labels.add(MOTORCYCLE_LABEL);
    labels.add(TAXI_LABEL);
    labels.add(PUBLIC_TRANSPORT_LABEL);
    labels.add(CAR_TRUCK_VAN_LABEL);
    return labels;
  }

}