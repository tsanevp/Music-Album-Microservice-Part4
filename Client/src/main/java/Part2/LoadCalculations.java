package Part2;

import java.util.Collections;
import java.util.List;

public class LoadCalculations {
  private final List<Long> latencies;

  public LoadCalculations(List<Long> latencies) {
    Collections.sort(latencies);
    this.latencies = latencies;
  }

  public double getMeanResponseTime() {
    return this.latencies.stream().mapToDouble(latency -> latency).average().orElse(0.0);
  }

  public double getMedianResponseTime() {
    int size = this.latencies.size();
    if (size % 2 == 0) {
      return (this.latencies.get(size / 2) + this.latencies.get(size / 2)) / 2;
    }

    return this.latencies.get(size / 2);
  }

  public double getMax() {
    return this.latencies.get(this.latencies.size() - 1);
  }

  public double getMin() {
    return this.latencies.get(0);
  }

  public long getPercentile(int percentile) {
    int size = this.latencies.size();
    int percentileIndex = (int) Math.ceil((percentile / 100.0) * size);

    return this.latencies.get(percentileIndex);
  }
}
