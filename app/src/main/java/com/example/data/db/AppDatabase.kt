package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppointmentCategory
import com.example.data.model.AppointmentEntity
import com.example.data.model.AppointmentStatus
import com.example.data.model.BillCategory
import com.example.data.model.BillEntity
import com.example.data.model.BillStatus
import com.example.data.model.DocType
import com.example.data.model.DocumentEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseEntity
import com.example.data.model.FamilyMemberEntity
import com.example.data.model.FamilyRelation
import com.example.data.model.Frequency
import com.example.data.model.RecurringTaskEntity
import com.example.data.model.TaskFrequency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        ExpenseEntity::class,
        BillEntity::class,
        AppointmentEntity::class,
        RecurringTaskEntity::class,
        DocumentEntity::class,
        FamilyMemberEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun billDao(): BillDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun taskDao(): TaskDao
    abstract fun documentDao(): DocumentDao
    abstract fun familyDao(): FamilyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_remind_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val now = Calendar.getInstance()

            // 1. Family Members
            val familyMembers = listOf(
                FamilyMemberEntity(name = "Self", relation = FamilyRelation.SELF.name, emoji = "👤", colorHex = "#1D4ED8"),
                FamilyMemberEntity(name = "Spouse", relation = FamilyRelation.SPOUSE.name, emoji = "👩", colorHex = "#0D9488"),
                FamilyMemberEntity(name = "Dad", relation = FamilyRelation.DAD.name, emoji = "👨", colorHex = "#D97706"),
                FamilyMemberEntity(name = "Mom", relation = FamilyRelation.MOM.name, emoji = "👩‍🦰", colorHex = "#E11D48"),
                FamilyMemberEntity(name = "Son", relation = FamilyRelation.SON.name, emoji = "👦", colorHex = "#3B82F6"),
                FamilyMemberEntity(name = "Daughter", relation = FamilyRelation.DAUGHTER.name, emoji = "👧", colorHex = "#8B5CF6")
            )
            database.familyDao().insertAll(familyMembers)

            // 2. Bills (Due Today, Upcoming, Yearly)
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 18)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val todayMillis = todayCal.timeInMillis

            val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val in3DaysCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 3) }
            val in7DaysCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
            val in14DaysCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 14) }
            val in30DaysCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 30) }

            val initialBills = listOf(
                BillEntity(
                    name = "Electricity Bill",
                    amount = 2450.0,
                    dueDateMillis = todayMillis,
                    frequency = Frequency.MONTHLY.name,
                    category = BillCategory.ELECTRICITY.name,
                    reminderDaysBefore = 3,
                    status = BillStatus.PENDING.name,
                    notes = "Meter No: 884729 - TNEB board",
                    docPolicyNo = "TNEB-Consumer-4482",
                    familyMember = "Family",
                    autoExpenseOnPaid = true
                ),
                BillEntity(
                    name = "Mobile Recharge (Airtel)",
                    amount = 599.0,
                    dueDateMillis = todayMillis,
                    frequency = Frequency.MONTHLY.name,
                    category = BillCategory.MOBILE_RECHARGE.name,
                    reminderDaysBefore = 2,
                    status = BillStatus.PENDING.name,
                    notes = "Spouse number - 84-day plan renewal",
                    docPolicyNo = "",
                    familyMember = "Spouse",
                    autoExpenseOnPaid = true
                ),
                BillEntity(
                    name = "Broadband & OTT Subscription",
                    amount = 999.0,
                    dueDateMillis = in3DaysCal.timeInMillis,
                    frequency = Frequency.MONTHLY.name,
                    category = BillCategory.INTERNET_OTT.name,
                    reminderDaysBefore = 3,
                    status = BillStatus.PENDING.name,
                    notes = "Fiber 100 Mbps + Netflix Bundle",
                    docPolicyNo = "ACT-Acct-9921",
                    familyMember = "Family",
                    autoExpenseOnPaid = true
                ),
                BillEntity(
                    name = "LIC Life Insurance Premium",
                    amount = 12500.0,
                    dueDateMillis = in14DaysCal.timeInMillis,
                    frequency = Frequency.YEARLY.name,
                    category = BillCategory.LIC_PREMIUM.name,
                    reminderDaysBefore = 15,
                    status = BillStatus.PENDING.name,
                    notes = "Policy: Jeevan Anand - Dad policy",
                    docPolicyNo = "LIC-883920194",
                    renewalDateMillis = in14DaysCal.timeInMillis,
                    familyMember = "Dad",
                    autoExpenseOnPaid = true
                ),
                BillEntity(
                    name = "Health Insurance (Family Floater)",
                    amount = 24000.0,
                    dueDateMillis = in30DaysCal.timeInMillis,
                    frequency = Frequency.YEARLY.name,
                    category = BillCategory.HEALTH_INSURANCE.name,
                    reminderDaysBefore = 30,
                    status = BillStatus.PENDING.name,
                    notes = "Sum Insured: ₹10 Lakhs - Star Health",
                    docPolicyNo = "SH-POL-2024-9981",
                    renewalDateMillis = in30DaysCal.timeInMillis,
                    familyMember = "Family",
                    autoExpenseOnPaid = true
                ),
                BillEntity(
                    name = "Car Insurance Renewal",
                    amount = 8900.0,
                    dueDateMillis = in7DaysCal.timeInMillis,
                    frequency = Frequency.YEARLY.name,
                    category = BillCategory.CAR_INSURANCE.name,
                    reminderDaysBefore = 7,
                    status = BillStatus.PENDING.name,
                    notes = "Honda City - HDFC ERGO Zero Dep",
                    docPolicyNo = "HDFC-MOT-77312",
                    renewalDateMillis = in7DaysCal.timeInMillis,
                    familyMember = "Self",
                    autoExpenseOnPaid = true
                ),
                BillEntity(
                    name = "House Rent",
                    amount = 18000.0,
                    dueDateMillis = in7DaysCal.timeInMillis,
                    frequency = Frequency.MONTHLY.name,
                    category = BillCategory.RENT.name,
                    reminderDaysBefore = 2,
                    status = BillStatus.PENDING.name,
                    notes = "Transfer to landlord account via UPI",
                    docPolicyNo = "",
                    familyMember = "Family",
                    autoExpenseOnPaid = true
                )
            )
            database.billDao().insertAll(initialBills)

            // 3. Appointments & Meetings
            val initialAppointments = listOf(
                AppointmentEntity(
                    title = "Hospital Appointment - Dr. Kumar",
                    personOrLocation = "Apollo Clinic, Room 204",
                    category = AppointmentCategory.HOSPITAL.name,
                    dateTimeMillis = tomorrowCal.apply { set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 30) }.timeInMillis,
                    reminderHoursBefore = 24,
                    reminderMinutesBefore = 120,
                    familyMember = "Dad",
                    status = AppointmentStatus.UPCOMING.name,
                    notes = "Routine BP and Sugar checkup. Bring past 3 months lab reports."
                ),
                AppointmentEntity(
                    title = "School Parent-Teacher Meeting (PTM)",
                    personOrLocation = "City Public School, Class 7B",
                    category = AppointmentCategory.SCHOOL.name,
                    dateTimeMillis = in7DaysCal.apply { set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0) }.timeInMillis,
                    reminderHoursBefore = 48,
                    reminderMinutesBefore = 60,
                    familyMember = "Son",
                    status = AppointmentStatus.UPCOMING.name,
                    notes = "Quarterly assessment discussion with class teacher Ms. Sharma."
                ),
                AppointmentEntity(
                    title = "Car Periodic Service & Wheel Alignment",
                    personOrLocation = "Honda Authorized Service Center",
                    category = AppointmentCategory.VEHICLE_SERVICE.name,
                    dateTimeMillis = in14DaysCal.apply { set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 30) }.timeInMillis,
                    reminderHoursBefore = 72,
                    reminderMinutesBefore = 120,
                    familyMember = "Self",
                    status = AppointmentStatus.UPCOMING.name,
                    notes = "10,000 km general maintenance and air filter replacement."
                ),
                AppointmentEntity(
                    title = "Pediatric Vaccination Appointment",
                    personOrLocation = "Rainbow Children Hospital",
                    category = AppointmentCategory.HOSPITAL.name,
                    dateTimeMillis = in3DaysCal.apply { set(Calendar.HOUR_OF_DAY, 11); set(Calendar.MINUTE, 0) }.timeInMillis,
                    reminderHoursBefore = 24,
                    reminderMinutesBefore = 60,
                    familyMember = "Daughter",
                    status = AppointmentStatus.UPCOMING.name,
                    notes = "Booster dose schedule."
                )
            )
            database.appointmentDao().insertAll(initialAppointments)

            // 4. Recurring Tasks
            val initialTasks = listOf(
                RecurringTaskEntity(title = "Take milk delivery & check qty", frequency = TaskFrequency.DAILY.name, category = "Daily", familyMember = "Family", streakCount = 12, isCompletedToday = true),
                RecurringTaskEntity(title = "Morning Exercise & 30 min Walk", frequency = TaskFrequency.DAILY.name, category = "Health", familyMember = "Self", streakCount = 5, isCompletedToday = false),
                RecurringTaskEntity(title = "Water balcony & indoor plants", frequency = TaskFrequency.DAILY.name, category = "Home", familyMember = "Spouse", streakCount = 8, isCompletedToday = true),
                RecurringTaskEntity(title = "Weekly Grocery & Vegetable Restock", frequency = TaskFrequency.WEEKLY.name, category = "Home", familyMember = "Family", streakCount = 3, isCompletedToday = false),
                RecurringTaskEntity(title = "Vehicle tire pressure & fluid check", frequency = TaskFrequency.WEEKLY.name, category = "Vehicle", familyMember = "Self", streakCount = 2, isCompletedToday = false),
                RecurringTaskEntity(title = "Review monthly household budget & bills", frequency = TaskFrequency.MONTHLY.name, category = "Finance", familyMember = "Self", streakCount = 6, isCompletedToday = false),
                RecurringTaskEntity(title = "AC filter cleaning & water purifier service", frequency = TaskFrequency.QUARTERLY.name, category = "Maintenance", familyMember = "Family", streakCount = 1, isCompletedToday = false),
                RecurringTaskEntity(title = "Insurance & tax renewal audit", frequency = TaskFrequency.YEARLY.name, category = "Finance", familyMember = "Dad", streakCount = 2, isCompletedToday = false)
            )
            database.taskDao().insertAll(initialTasks)

            // 5. Initial Expenses
            val initialExpenses = listOf(
                ExpenseEntity(title = "Daily Organic Cow Milk (1.5L)", amount = 90.0, category = ExpenseCategory.MILK.name, dateMillis = todayMillis - 3600000 * 3, notes = "Daily delivery", familyMember = "Family"),
                ExpenseEntity(title = "Fresh Vegetables & Greens", amount = 340.0, category = ExpenseCategory.VEGETABLES.name, dateMillis = todayMillis - 3600000 * 5, notes = "Weekly mandi purchase", familyMember = "Spouse"),
                ExpenseEntity(title = "Supermarket Grocery Essentials", amount = 2450.0, category = ExpenseCategory.GROCERY.name, dateMillis = todayMillis - 86400000 * 2, notes = "D-Mart pantry refill", familyMember = "Family"),
                ExpenseEntity(title = "Petrol Refill (Car)", amount = 2500.0, category = ExpenseCategory.FUEL.name, dateMillis = todayMillis - 86400000 * 3, notes = "Full tank at Shell", familyMember = "Self"),
                ExpenseEntity(title = "Pharmacy Prescription Medicines", amount = 1150.0, category = ExpenseCategory.MEDICAL.name, dateMillis = todayMillis - 86400000 * 4, notes = "Dad BP & multivitamin refill", familyMember = "Dad"),
                ExpenseEntity(title = "Dining & Weekend Takeout", amount = 1420.0, category = ExpenseCategory.DINING.name, dateMillis = todayMillis - 86400000 * 5, notes = "Family Sunday dinner", familyMember = "Family"),
                ExpenseEntity(title = "School Books & Stationery", amount = 1850.0, category = ExpenseCategory.EDUCATION.name, dateMillis = todayMillis - 86400000 * 8, notes = "Term 2 notebooks & geometry set", familyMember = "Son")
            )
            database.expenseDao().insertAll(initialExpenses)

            // 6. Documents
            val initialDocs = listOf(
                DocumentEntity(
                    title = "Family Health Shield Policy",
                    docType = DocType.HEALTH_INSURANCE.name,
                    identifierOrPolicyNo = "SH-POL-2024-9981",
                    issuerOrProvider = "Star Health Insurance",
                    expiryDateMillis = in30DaysCal.timeInMillis,
                    notes = "Cashless card in Dad's wallet. TPA Helpline: 1800-425-2255",
                    familyMember = "Family",
                    linkedBillName = "Health Insurance"
                ),
                DocumentEntity(
                    title = "Honda City Comprehensive Insurance & RC",
                    docType = DocType.VEHICLE_INSURANCE.name,
                    identifierOrPolicyNo = "HDFC-MOT-77312 / DL8C-9912",
                    issuerOrProvider = "HDFC ERGO",
                    expiryDateMillis = in7DaysCal.timeInMillis,
                    notes = "Zero Dep cover with Roadside Assistance included.",
                    familyMember = "Self",
                    linkedBillName = "Car Insurance Renewal"
                ),
                DocumentEntity(
                    title = "Property & Municipal Tax Receipt",
                    docType = DocType.PROPERTY_TAX.name,
                    identifierOrPolicyNo = "ZONE-04-ASSMT-88392",
                    issuerOrProvider = "City Municipal Corporation",
                    expiryDateMillis = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 120) }.timeInMillis,
                    notes = "Half-yearly municipal property assessment paid up to March.",
                    familyMember = "Family",
                    linkedBillName = "Municipality Tax"
                ),
                DocumentEntity(
                    title = "Refrigerator 10-Year Inverter Warranty Card",
                    docType = DocType.WARRANTY_CARD.name,
                    identifierOrPolicyNo = "SAMSUNG-INV-RF4920",
                    issuerOrProvider = "Samsung India",
                    expiryDateMillis = (todayCal.clone() as Calendar).apply { add(Calendar.YEAR, 4) }.timeInMillis,
                    notes = "Compressor warranty valid till 2030. Serial No: SN-9938472",
                    familyMember = "Family",
                    linkedBillName = ""
                )
            )
            database.documentDao().insertAll(initialDocs)
        }
    }
}
