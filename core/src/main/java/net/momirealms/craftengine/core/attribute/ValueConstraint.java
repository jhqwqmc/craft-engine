package net.momirealms.craftengine.core.attribute;

public interface ValueConstraint {

    static ValueConstraint clamp(double min, double max) {
        return new Clamp(min, max);
    }

    double limit(double value);

    class Clamp implements ValueConstraint {
        private final double min;
        private final double max;

        public Clamp(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public double limit(double value) {
            return Math.clamp(value, min, max);
        }
    }
}
