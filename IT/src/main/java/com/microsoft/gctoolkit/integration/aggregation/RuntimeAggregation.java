package com.microsoft.gctoolkit.integration.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * An Aggregation that collates runtime data. This class is meant to be extended by other
 * implementations that need access to runtime data to perform calculations.
 * <p>
 * The following code shows recommended practice for using this Aggregation. The example
 * uses {@code RuntimeAggregation} to calculate the ratio of pause time to runtime duration
 * for a G1GC log.
 * <pre><code>
 * {@literal @}Collates(G1PauseTimeAggregator.class)
 * public abstract class G1PauseTimeAggregation extends RuntimeAggregation {
 *      public abstract void recordPause(double pauseTime);
 * }
 *
 * {@literal @}Aggregates({EventSource.G1GC)
 * public class G1PauseTimeAggregator extends RuntimeAggregator{@literal <}G1PauseTimeAggregation{@literal >} {
 *
 *     public G1PauseTimeAggregator(G1PauseTimeAggregation aggregation) {
 *         super(aggregation);
 *         register(G1RealPause.class, this::process);
 *     }
 *
 *      private void process(G1RealPause event) {
 *          aggregation().recordPause(event.getDuration());
 *      }
 * }
 *
 * public class G1PauseTimeRatio extends G1PauseTimeAggregation {
 *
 *     long totalPauseTime;
 *
 *     public MaxFullGCPauseTime() {}
 *
 *     {@literal @}Override
 *     public void recordPause(double pauseTime) {
 *         totalPauseTime += pauseTime;
 *     }
 *
 *     public double getPauseTimeRatio() {
 *         return getRuntimeDuration() {@literal >} 0.0 ? totalPauseTime / getRuntimeDuration() : 0.0;
 *     }
 *
 *     {@literal @}Override
 *     public boolean hasWarning() { return false; }
 *
 *     {@literal @}Override
 *     public boolean isEmpty() { return getRuntimeDuration() {@literal <}= 0.0; }
 * }
 * </code></pre>

 */
public class RuntimeAggregation  extends Aggregation {

    /**
     * This class is meant to be extended.
     */
    public RuntimeAggregation() {}

    /**
     * RuntimeAggregation collates the time of an event and the duration of the event.
     * @param eventTime The time a JVMEvent occurred.
     * @param duration The duration of the JVMEvent.
     */
    public void publish(DateTimeStamp eventTime, double duration) {
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
