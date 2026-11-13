package com.dysland.app;
import com.dysland.app.data.NavigationText;
import org.junit.Test;
import static org.junit.Assert.*;
public class NavigationTextTest {
    @Test public void portugueseDistanceAndDirection() { assertEquals("200 m",NavigationText.distance("Vire à direita em 200 m"));assertEquals(1,NavigationText.turn("Vire à direita")); }
    @Test public void decimalDistance() { assertEquals("1,5 km",NavigationText.distance("Continue por 1,5 km")); }
    @Test public void missingDistanceDoesNotInventOne() { assertEquals("GPS",NavigationText.distance("Siga em frente"));assertEquals(0,NavigationText.turn("Siga em frente")); }
    @Test public void englishLeftTurn() { assertEquals(-1,NavigationText.turn("Turn left")); }
}
