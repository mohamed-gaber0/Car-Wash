package oop.carwash.util;

/**
 * تنسيق المبالغ بالجنيه المصري في الواجهة والطباعة.
 */
public final class MoneyFormat {

    private MoneyFormat() {
    }

    /** يعرض المبلغ مع لاحقة جنيه مصري (ج.م). */
    public static String egp(double amount) {
        return String.format("%.2f ج.م", amount);
    }
}
