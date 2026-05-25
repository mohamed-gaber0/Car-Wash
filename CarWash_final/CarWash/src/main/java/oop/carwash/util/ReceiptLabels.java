package oop.carwash.util;

/**
 * ترجمة حالات الفاتورة وتصفية المدفوعات للعرض العربي.
 * القيم المخزنة في قاعدة البيانات تبقى بالإنجليزية (UNPAID، …).
 */
public final class ReceiptLabels {

    private ReceiptLabels() {
    }

    public static String statusArabic(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "UNPAID" -> "غير مدفوع";
            case "PARTIAL" -> "مدفوع جزئياً";
            case "PAID" -> "مدفوع بالكامل";
            default -> status;
        };
    }

    /** تسميات قائمة التصفية في شاشة المدفوعات (القيمة المعروضة في الـ ComboBox). */
    public static String[] paymentFilterOptionsArabic() {
        return new String[] { "الكل", "غير مدفوع", "مدفوع جزئياً", "مدفوع بالكامل" };
    }

    /** تحويل تسمية التصفية العربية إلى مفتاح التصفية الداخلي (ALL / UNPAID / …). */
    public static String filterKeyFromArabicLabel(String arabicLabel) {
        if (arabicLabel == null) {
            return "ALL";
        }
        return switch (arabicLabel) {
            case "الكل" -> "ALL";
            case "غير مدفوع" -> "UNPAID";
            case "مدفوع جزئياً" -> "PARTIAL";
            case "مدفوع بالكامل" -> "PAID";
            default -> "ALL";
        };
    }
}
