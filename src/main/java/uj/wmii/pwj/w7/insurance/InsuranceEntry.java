package uj.wmii.pwj.w7.insurance;

public record InsuranceEntry(
        String country,
        double tiv_2011,
        double tiv_2012
) {
    public double increase() {
        return tiv_2012 - tiv_2011;
    }
}
