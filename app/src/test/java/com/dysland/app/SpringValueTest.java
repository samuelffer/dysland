package com.dysland.app;
import com.dysland.app.motion.SpringValue;
import org.junit.Test;
import static org.junit.Assert.*;
public class SpringValueTest {
    @Test public void convergesWithoutOscillatingForever() {
        SpringValue s=new SpringValue(16,.44,.84); s.target(220);
        for(int i=0;i<240;i++) s.step(1.0/60);
        assertEquals(220,s.value(),.01); assertTrue(s.settled());
    }
    @Test public void retargetPreservesVelocityAndPosition() {
        SpringValue s=new SpringValue(16,.44,.84); s.target(220); s.step(.08);
        double p=s.value(),v=s.velocity(); s.target(16);
        assertEquals(p,s.value(),0); assertEquals(v,s.velocity(),0);
    }
    @Test public void independentOfFrameRate() {
        SpringValue a=new SpringValue(16,.44,.84),b=new SpringValue(16,.44,.84);
        a.target(220);b.target(220);
        for(int i=0;i<12;i++) a.step(1.0/60);
        for(int i=0;i<24;i++) b.step(1.0/120);
        assertEquals(a.value(),b.value(),.00001); assertEquals(a.velocity(),b.velocity(),.00001);
    }
    @Test public void reducedMotionCanSnap() {
        SpringValue s=new SpringValue(220,.44,.84);s.target(16);s.snap();
        assertEquals(16,s.value(),0);assertEquals(0,s.velocity(),0);
    }
}
