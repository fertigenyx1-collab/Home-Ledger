package com.example.data.model

enum class ExpenseCategory(val displayName: String, val iconEmoji: String) {
    GROCERY("Grocery", "🛒"),
    MILK("Milk", "🥛"),
    VEGETABLES("Vegetables", "🥦"),
    FUEL("Fuel", "⛽"),
    MEDICAL("Medical", "💊"),
    EDUCATION("Education", "📚"),
    SHOPPING("Shopping", "🛍️"),
    HOUSEHOLD("Household", "🏠"),
    DINING("Dining", "🍽️"),
    TRANSPORTATION("Transport", "🚕"),
    BILLS("Bills & Utilities", "⚡"),
    OTHER("Other", "📦");

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class BillCategory(val displayName: String, val iconEmoji: String) {
    ELECTRICITY("Electricity", "⚡"),
    LPG_GAS("LPG / Gas", "🔥"),
    MOBILE_RECHARGE("Mobile Recharge", "📱"),
    INTERNET_OTT("Internet / OTT", "🌐"),
    RENT("Rent / Housing", "🏠"),
    LIC_PREMIUM("LIC Premium", "🏦"),
    HEALTH_INSURANCE("Health Insurance", "🛡️"),
    CAR_INSURANCE("Car Insurance", "🚗"),
    BIKE_INSURANCE("Bike Insurance", "🏍️"),
    PROPERTY_TAX("Property Tax", "🏛️"),
    LOAN_EMI("Loan / EMI", "💳"),
    WATER("Water Bill", "💧"),
    OTHER("Other Bill", "📄");

    companion object {
        fun fromString(value: String): BillCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class Frequency(val displayName: String, val daysStep: Int) {
    ONE_TIME("One-time", 0),
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    HALF_YEARLY("Half-Yearly", 182),
    YEARLY("Yearly", 365);

    companion object {
        fun fromString(value: String): Frequency {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MONTHLY
        }
    }
}

enum class BillStatus(val displayName: String) {
    PENDING("Pending"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    PLANNED("Planned")
}

enum class AppointmentCategory(val displayName: String, val iconEmoji: String) {
    HOSPITAL("Hospital / Medical", "🏥"),
    SCHOOL("School / Education", "🏫"),
    WORK("Work / Business", "💼"),
    PERSONAL("Personal / Care", "🧘"),
    VEHICLE_SERVICE("Vehicle Service", "🔧"),
    OTHER("Other Appointment", "📅");

    companion object {
        fun fromString(value: String): AppointmentCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class AppointmentStatus(val displayName: String) {
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class TaskFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    HALF_YEARLY("Half-Yearly"),
    YEARLY("Yearly")
}

enum class DocType(val displayName: String, val iconEmoji: String) {
    HEALTH_INSURANCE("Health Insurance", "🛡️"),
    VEHICLE_INSURANCE("Vehicle Insurance", "🚗"),
    PROPERTY_TAX("Property / Municipal Tax", "🏛️"),
    RECEIPT("Receipt / Invoice", "🧾"),
    MEDICAL_PRESCRIPTION("Medical / Lab Report", "🩺"),
    WARRANTY_CARD("Warranty / Guarantee", "🏷️"),
    OTHER("Other Document", "📁");

    companion object {
        fun fromString(value: String): DocType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class FamilyRelation(val displayName: String, val iconEmoji: String) {
    SELF("Self", "👤"),
    SPOUSE("Spouse", "👩"),
    DAD("Dad", "👨"),
    MOM("Mom", "👩‍🦰"),
    SON("Son", "👦"),
    DAUGHTER("Daughter", "👧"),
    FAMILY_SHARED("All / Family", "🏠"),
    OTHER("Other", "👥");

    companion object {
        fun fromString(value: String): FamilyRelation {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: FAMILY_SHARED
        }
    }
}
