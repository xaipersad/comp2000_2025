public interface WeatherListener {
  void onWeatherUpdate(String timestamp, String weatherType, int x, int y, double value);
}
