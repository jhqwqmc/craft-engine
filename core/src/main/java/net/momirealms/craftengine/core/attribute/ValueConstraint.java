package net.momirealms.craftengine.core.attribute;

public interface ValueConstraint {

    static ValueConstraint noLimit() {
        return new NoLimit();
    }

    static ValueConstraint clamp(double min, double max) {
        return new Clamp(min, max);
    }

    static ValueConstraint min(double min) {
        return new Clamp(min, Double.MAX_VALUE);
    }

    static ValueConstraint max(double max) {
        return new Clamp(Double.MIN_VALUE, max);
    }

    double limit(double value);

    class NoLimit implements ValueConstraint {
        @Override
        public double limit(double value) {
            return value;
        }
    }

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
