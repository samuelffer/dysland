package com.dysland.app;
import com.dysland.app.presentation.IslandPresentation;
import org.junit.Test;
import static org.junit.Assert.*;
public class CompactGeometryTest {
    @Test public void zeroExtraFitsCamera() { assertEquals(16,IslandPresentation.compactHeight(16,1,0)); }
    @Test public void extraIsRelativeAndDensityAware() { assertEquals(66,IslandPresentation.compactHeight(48,3,6)); }
    @Test public void largerCutoutIsNotClipped() { assertEquals(90,IslandPresentation.compactHeight(90,3,0)); }
}
