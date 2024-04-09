public class FormattedDouble extends Number {
    private static final long serialVersionUID = 1L;

    // ThreadLocal because DecimalFormat is not thread-safe
    private static final ThreadLocal<DecimalFormat> format =
            // Specify DecimalFormatSymbols to make code independent from default Locale
            ThreadLocal.withInitial(() -> new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)));

    private final Double delegate;

    public FormattedDouble(Double value) {
        Objects.requireNonNull(value, "Value should not be null");
        this.delegate = value;
    }

    @Override
    public byte byteValue() {
        return delegate.byteValue();
    }

    @Override
    public short shortValue() {
        return delegate.shortValue();
    }

    @Override
    public int intValue() {
        return delegate.intValue();
    }

    @Override
    public long longValue() {
        return delegate.longValue();
    }

    @Override
    public float floatValue() {
        return delegate.floatValue();
    }

    @Override
    public double doubleValue() {
        return delegate.doubleValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof FormattedDouble) {
            return ((FormattedDouble) obj).delegate.equals(delegate);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * delegate.hashCode() + format.get().hashCode();
    }

    @Override
    public String toString() {
        return format.get().format((double) delegate);
    }
}