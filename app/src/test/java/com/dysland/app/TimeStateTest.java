package com.dysland.app;
import com.dysland.app.data.TimeState;
import org.junit.Test;
import static org.junit.Assert.*;
public class TimeStateTest {
    @Test public void stopwatchUsesMonotonicElapsedTime() { assertEquals(65,TimeState.seconds(67000,2000,0)); }
    @Test public void countdownRoundsUp() { assertEquals(60,TimeState.seconds(2001,2000,60000)); }
    @Test public void countdownNeverNegative() { assertEquals(0,TimeState.seconds(999999,2000,60000)); }
    @Test public void clockBeforeStartIsClamped() { assertEquals(0,TimeState.seconds(1,2000,0)); }
    @Test public void hourFormatting() { assertEquals("01:01:01",TimeState.format(3661)); }
    @Test public void longTimersDoNotWrapHours() { assertEquals("100:00:00",TimeState.format(360000)); }
}
