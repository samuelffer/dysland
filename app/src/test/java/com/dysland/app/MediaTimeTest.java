package com.dysland.app;
import com.dysland.app.data.MediaTime;
import org.junit.Test;
import static org.junit.Assert.*;
public class MediaTimeTest {
    @Test public void advancesFromMonotonicTimestamp() { assertEquals(7000,MediaTime.position(3000,1000,5000,1,true,20000)); }
    @Test public void pausedPositionDoesNotAdvance() { assertEquals(3000,MediaTime.position(3000,1000,5000,1,false,20000)); }
    @Test public void clampsToDuration() { assertEquals(4000,MediaTime.position(3000,1000,5000,2,true,4000)); }
    @Test public void unknownPositionIsNonNegative() { assertEquals(0,MediaTime.position(-1,0,5000,1,true,0)); }
    @Test public void scrubbingSupportsLongTracks() { assertEquals(36000000,MediaTime.fromProgress(500,72000000)); }
    @Test public void liveStreamsCannotBeScrubbed() { assertEquals(0,MediaTime.fromProgress(500,0)); }
    @Test public void timeFormatting() { assertEquals("1:05",MediaTime.label(65000));assertEquals("1:00:00",MediaTime.label(3600000)); }
}
