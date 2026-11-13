package com.dysland.app.motion;

/** Exact underdamped spring integration. Retargeting preserves position and velocity. */
public final class SpringValue {
    private double position, velocity, target;
    private final double omega, damping;
    public SpringValue(double initial, double response, double damping) {
        position=target=initial;
        omega=2*Math.PI/response;
        this.damping=Math.max(.1,Math.min(.99,damping));
    }
    public void target(double target) { this.target=target; }
    public double value() { return position; }
    public double velocity() { return velocity; }
    public void snap() { position=target; velocity=0; }
    public boolean settled() { return Math.abs(position-target)<.25 && Math.abs(velocity)<.35; }
    public void step(double seconds) {
        if (seconds<=0) return;
        double q=position-target, decay=damping*omega;
        double frequency=omega*Math.sqrt(1-damping*damping);
        double b=(velocity+decay*q)/frequency;
        double angle=frequency*seconds, c=Math.cos(angle), s=Math.sin(angle);
        double envelope=Math.exp(-decay*seconds), oscillation=q*c+b*s;
        position=target+envelope*oscillation;
        velocity=envelope*(-decay*oscillation+frequency*(-q*s+b*c));
        if (settled()) snap();
    }
}
