package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AiPlanEntity
import com.example.data.database.BookingEntity
import com.example.data.database.ChatMessageEntity
import com.example.data.repository.TajribahRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TajribahRepository(application)
    private val TAG = "MainViewModel"

    // --- Navigation State ---
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Splash))
    val screenStack: StateFlow<List<Screen>> = _screenStack.asStateFlow()

    private val _currentTab = MutableStateFlow(DashboardTab.Home)
    val currentTab: StateFlow<DashboardTab> = _currentTab.asStateFlow()

    // --- Back Navigation Handlers ---
    fun pushScreen(screen: Screen) {
        _screenStack.value = _screenStack.value + screen
    }

    fun popScreen(): Boolean {
        val stack = _screenStack.value
        if (stack.size > 1) {
            _screenStack.value = stack.dropLast(1)
            return true
        }
        return false // reached root
    }

    fun setTab(tab: DashboardTab) {
        _currentTab.value = tab
    }

    // Exit application dialog visibility state
    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    fun setShowExitDialog(show: Boolean) {
        _showExitDialog.value = show
    }

    // --- User Profile State ---
    val userName = MutableStateFlow("أحمد الغامدي")
    val userEmail = MutableStateFlow("ahmed.traveler@tajribah.com")
    val userWalletBalance = MutableStateFlow(2450.0)
    val userPoints = MutableStateFlow(480)

    // --- Search, Filtering & Categories ---
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل") // الكل, نحل, فخار, صيد, طهي, تخييم, جبال, خيل
    val maxPriceFilter = MutableStateFlow(1000.0)

    // --- Available Experiences (Pre-seeded Arabic Content) ---
    private val _experiencesList = MutableStateFlow<List<ExperienceItem>>(emptyList())
    val experiencesList: StateFlow<List<ExperienceItem>> = _experiencesList.asStateFlow()

    // --- Trips (Pre-seeded) ---
    private val _tripsList = MutableStateFlow<List<TripItem>>(emptyList())
    val tripsList: StateFlow<List<TripItem>> = _tripsList.asStateFlow()

    // --- Guides (Pre-seeded) ---
    private val _guidesList = MutableStateFlow<List<GuideItem>>(emptyList())
    val guidesList: StateFlow<List<GuideItem>> = _guidesList.asStateFlow()

    // --- Cars (Pre-seeded) ---
    private val _carsList = MutableStateFlow<List<CarItem>>(emptyList())
    val carsList: StateFlow<List<CarItem>> = _carsList.asStateFlow()

    // --- Gifts (Pre-seeded) ---
    private val _giftsList = MutableStateFlow<List<GiftItem>>(emptyList())
    val giftsList: StateFlow<List<GiftItem>> = _giftsList.asStateFlow()

    // --- Room Database Backed Lists (Bookings, AI Plans, Active Chat Partner) ---
    val bookingsList: StateFlow<List<BookingEntity>> = repository.getBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedAiPlansList: StateFlow<List<AiPlanEntity>> = repository.getAiPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatPartner = MutableStateFlow("العم صالح (حرفي الفخار)")
    val activeChatPartner: StateFlow<String> = _activeChatPartner.asStateFlow()

    val activeChatMessages: StateFlow<List<ChatMessageEntity>> = _activeChatPartner
        .flatMapLatest { partner -> repository.getChatMessages(partner) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- In-App Alerts / Notification Center ---
    private val _notificationsList = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notificationsList: StateFlow<List<NotificationItem>> = _notificationsList.asStateFlow()

    private val _activeBannerNotification = MutableStateFlow<NotificationItem?>(null)
    val activeBannerNotification: StateFlow<NotificationItem?> = _activeBannerNotification.asStateFlow()

    // --- AI Assistant Travel Planner Input & State ---
    val plannerCity = MutableStateFlow("أبها")
    val plannerBudget = MutableStateFlow("2000 ريال")
    val plannerPeopleCount = MutableStateFlow("2")
    val plannerDuration = MutableStateFlow("3")
    val plannerInterests = MutableStateFlow("عسل نحل، صعود الجبال، الأكلات الشعبية")

    private val _plannerState = MutableStateFlow<PlannerState>(PlannerState.Idle)
    val plannerState: StateFlow<PlannerState> = _plannerState.asStateFlow()

    sealed interface PlannerState {
        object Idle : PlannerState
        object Loading : PlannerState
        data class Success(val planText: String) : PlannerState
        data class Error(val errorMessage: String) : PlannerState
    }

    // --- Coupon & Discount State ---
    val appliedCoupon = MutableStateFlow("")
    val couponDiscountPercent = MutableStateFlow(0) // e.g. 15%

    // --- Technical Support Tickets ---
    private val _supportMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val supportMessages: StateFlow<List<ChatMessageEntity>> = _supportMessages.asStateFlow()

    // --- Admin Dashboard Management ---
    private val _adminComplaints = MutableStateFlow<List<ComplaintItem>>(emptyList())
    val adminComplaints: StateFlow<List<ComplaintItem>> = _adminComplaints.asStateFlow()

    private val _adminRegisteredUsers = MutableStateFlow<List<String>>(listOf("أحمد الغامدي", "العم فهد", "العم صالح", "سارة العسيري", "الكابتن يوسف"))
    val adminRegisteredUsers: StateFlow<List<String>> = _adminRegisteredUsers.asStateFlow()

    init {
        // Pre-seed static high-quality content
        seedInitialData()
        // Register standard initial notification
        addNotification("أهلاً بك في تِجربة!", "اكتشف أروع التجارب المحلية والحرف التراثية وعيش تِجربة سفر ذكية لا تُنسى.")
    }

    private fun seedInitialData() {
        // Seeding experiences with reviews and host replies (Premium Feature)
        _experiencesList.value = listOf(
            ExperienceItem(
                id = 1,
                title = "يوم في منحل العم فهد لإنتاج عسل السدر",
                hostName = "العم فهد (نحال)",
                category = "نحل",
                rating = 4.9f,
                reviewCount = 124,
                price = 150.0,
                duration = "4 ساعات",
                city = "عسير",
                description = "استمتع برحلة تعليمية وتفاعلية في منحل العم فهد التقليدي في جبال عسير الخضراء. ارتدي ملابس النحال الواقية، واقترب من الخلايا، وتعلم كيف ينتج عسل السدر والسمرة الطبيعي، مع جلسة تذوق فريدة وهدية عبوة عسل فاخرة.",
                isFavorite = true,
                reviews = listOf(
                    ReviewItem(1, "خالد العتيبي", 5.0f, "تجربة رائعة ولذيذة جداً، العم فهد رجل طيب ومضياف كرمه يفوق الوصف!", "2026-06-15", 14, "شكراً لك يا خالد، شرفتنا ونورنا المنحل في أي وقت!"),
                    ReviewItem(2, "نورة الشمراني", 4.8f, "ممتع ومثقف، تعلمنا الكثير عن مجتمع النحل وتذوقنا عسل السدر الطازج.", "2026-07-02", 9, "أهلاً بك يا نورة، نسعد دائماً بنشر ثقافة إنتاج العسل الأصيل.")
                )
            ),
            ExperienceItem(
                id = 2,
                title = "تعلم فن وتشكيل الفخار بالطين الطبيعي",
                hostName = "العم صالح (حرفي الفخار)",
                category = "فخار",
                rating = 4.8f,
                reviewCount = 89,
                price = 120.0,
                duration = "3 ساعات",
                city = "القطيف",
                description = "عش متعة التشكيل بيدك على العجلة الدوارة مع الحرفي القدير العم صالح في ورشته التراثية. ستتعلم أسرار تماسك الطين وتشكيله وصقله وحرقه بالفرن، وستصنع قطعتك الفخارية بنفسك وتأخذها معك كتذكار باقٍ.",
                isFavorite = false,
                reviews = listOf(
                    ReviewItem(1, "سلطان الحربي", 5.0f, "أفضل تجربة يدوية عشتها على الإطلاق! العم صالح صبور ومعلم محترف جداً.", "2026-05-18", 22, "سعيد بوجودك يا سلطان، شغفك بالصلصال كان مذهلاً!"),
                    ReviewItem(2, "منى أحمد", 4.5f, "تجربة جميلة جداً ومريحة للأعصاب، بالتأكيد سأكرر الزيارة.", "2026-07-10", 7, "أهلاً بك منى، صناعة الفخار علاج حقيقي للروح.")
                )
            ),
            ExperienceItem(
                id = 3,
                title = "رحلة صيد بحري تقليدية وطبخ في البحر الأحمر",
                hostName = "الكابتن يوسف (صياد)",
                category = "صيد",
                rating = 4.9f,
                reviewCount = 202,
                price = 350.0,
                duration = "يوم كامل",
                city = "جدة",
                description = "انطلق على متن قارب الكابتن يوسف الخشبي التراثي في أعماق البحر الأحمر. تعلم طرق الصيد التقليدية بالصنارة والشباك، واصطد سمك الهامور والناجل بنفسك، ثم استمتع بوجبة غداء صيادية طازجة مطبوخة على متن القارب وسط مياه البحر الفيروزية.",
                isFavorite = true,
                reviews = listOf(
                    ReviewItem(1, "بندر القحطاني", 5.0f, "الكابتن يوسف بحار حقيقي وخبير، صيدنا هامور طازج وطبخناه على القارب، طعم خيالي!", "2026-06-20", 31, "صيد عافية يا بندر، البحر جاد بكرمه معنا ذلك اليوم!"),
                    ReviewItem(2, "فيصل الدوسري", 4.9f, "رحلة متكاملة وصيد وفير وأمان تام. الكابتن مجهز لكل شيء.", "2026-07-08", 18, "تسلم يا فيصل، السلامة والصيد الطيب هما شعارنا دائماً.")
                )
            ),
            ExperienceItem(
                id = 4,
                title = "خبز التنور والطهي الشعبي مع أم سعود",
                hostName = "أم سعود (طاهية شعبية)",
                category = "طهي",
                rating = 4.7f,
                reviewCount = 65,
                price = 90.0,
                duration = "3 ساعات",
                city = "القصيم",
                description = "افتحي أسرار المطبخ السعودي القديم مع الوالدة أم سعود في مجلسها الدافئ. تعلمي عجن وخبز الرقاق والفتيت والقرصان وتجهيز كبسة لحم الحاشي بالبهارات القصيمية السرية على الحطب، تليها جلسة غداء دافئة للأكلات المصنوعة.",
                isFavorite = false,
                reviews = listOf(
                    ReviewItem(1, "فاطمة محمد", 5.0f, "أم سعود عسل وكلامها عسل وأكلها لا يقاوم! الخبز الساخن مع الزبدة كان خرافياً.", "2026-05-30", 11, "بالعافية على قلبك يا بنتي فاطمة، حياك الله في أي وقت.")
                )
            ),
            ExperienceItem(
                id = 5,
                title = "ركوب خيل عربية أصيلة على شواطئ جازان",
                hostName = "أبو ماجد (مدرب فروسية)",
                category = "خيل",
                rating = 4.8f,
                reviewCount = 74,
                price = 180.0,
                duration = "ساعتين",
                city = "جازان",
                description = "اركب صهوة جواد عربي مطيع ومدرب بجانب الشواطئ الذهبية لجازان في لحظة الغروب الساحرة. مناسب للمبتدئين والمحترفين بإشراف مدربين مؤهلين لحماية وسلامة الفرسان مع التقاط جلسة تصوير احترافية.",
                isFavorite = false
            ),
            ExperienceItem(
                id = 6,
                title = "تخييم فلكي ورصد النجوم في صحراء العلا",
                hostName = "عبد الله السهلي (هاوي فلك)",
                category = "تخييم",
                rating = 4.9f,
                reviewCount = 143,
                price = 280.0,
                duration = "يوم كامل",
                city = "العلا",
                description = "انعزل عن صخب الحياة في مخيم فاخر ومفتوح بوسط صحراء وتشكيلات العلا الصخرية الساحرة. استمتع بعشاء شواء فاخر ثم رصد الكواكب والمجرات البعيدة بالتلسكوبات الاحترافية مع شرح شيق لخرائط النجوم التاريخية.",
                isFavorite = true
            ),
            ExperienceItem(
                id = 7,
                title = "صعود ومسار الهايكنج في قمة جبل السودة",
                hostName = "سليمان العسيري (دليل جبال)",
                category = "جبال",
                rating = 4.9f,
                reviewCount = 96,
                price = 130.0,
                duration = "5 ساعات",
                city = "عسير",
                description = "استكشف أطول قمة جبلية بالسعودية عبر مسار هايكنج مميز يمر بين أشجار العرعر العتيقة والضباب الكثيف. الدليل سليمان مجهز بوجبات طاقة وحقائب إسعاف وخرائط جغرافية لعيش مغامرة آمنة ومبهجة.",
                isFavorite = false
            )
        )

        _tripsList.value = listOf(
            TripItem(1, "رحلة تراث رجال ألمع التاريخية", "جولة سياحية شاملة في قرية رجال ألمع التراثية تشمل دخول المتاحف والقصور الطينية التاريخية وتناول الغداء العسيري.", "عسير", "يوم كامل", 220.0, 4.9f, false),
            TripItem(2, "رحلة استكشاف معالم جبل القارة والأحساء", "زيارة واحة الأحساء المليئة بالنخيل ومغارات جبل القارة الباردة ومصنع الفخار وسوق القيصرية التراثي.", "الأحساء", "يوم كامل", 180.0, 4.8f, false),
            TripItem(3, "مغامرة غامضة: رحلة الوادي السري المفاجئة", "رحلة مفاجئة استثنائية! لا تكشف عن تفاصيل مسارها إلا في صباح يوم الانطلاق. تشمل هايكنج مائي وموقع مخفي ساحر تحت النجوم.", "العلا", "يومين", 550.0, 5.0f, true),
            TripItem(4, "رحلة مزارع الورد الطائفي العطرة", "تعرف على كيفية قطف وتقطير الورد الطائفي الثمين في مزارع الشفا التقليدية وتجربة عطور فريدة وجلسة ريفية.", "الطائف", "6 ساعات", 120.0, 4.7f, false),
            TripItem(5, "مغامرة غامضة: رحلة الكهف المخفي المفقود", "استعد لتحدي مفاجئ لاستكشاف أحد أسرار الطبيعة الصحراوية المدفونة وسط الرمال الذهبية مع إقامة بدوية تقليدية.", "الرياض", "يوم كامل", 380.0, 4.9f, true)
        )

        _guidesList.value = listOf(
            GuideItem(1, "أحمد العسيري", "التاريخ والآثار والتسلق الجبلي", "عسير", "مرشد سياحي مرخص بخبرة 8 سنوات في مرتفعات عسير ومسارات الهايكنج الأثرية وقرى رجال ألمع.", 250.0, 4.9f, 210),
            GuideItem(2, "سارة الغامدي", "السياحة الثقافية والمتاحف النسائية", "جدة", "متخصصة في تاريخ جدة التاريخية وسوق البلد والتراث الاجتماعي، متحدثة بطلاقة بثلاث لغات.", 300.0, 4.8f, 154)
        )

        _carsList.value = listOf(
            CarItem(1, "تويوتا لاندكروزر دفع رباعي SUV", "SUV عائلية", 450.0, "أوتوماتيك", "بنزين", 4.9f),
            CarItem(2, "هيونداي سانتا في عائلية مريحة", "عائلية مريحة", 280.0, "أوتوماتيك", "بنزين", 4.7f),
            CarItem(3, "لكزس LX600 فاخرة ومتميزة", "Luxury", 950.0, "أوتوماتيك", "بنزين", 5.0f)
        )

        _giftsList.value = listOf(
            GiftItem(1, "صندوق عسل سدر جبلي فاخر", "عسل سدر طبيعي 100% من مزارع عسير الشهيرة مغلف في صندوق خشبي تراثي مع ملعقة عسل تقليدية.", 320.0, 300),
            GiftItem(2, "مبخرة فخارية منقوشة باليد", "مبخرة تراثية مصنوعة يدوياً بطين المنطقة الشرقية مع نقوش ذهبية مستوحاة من البيئة العسيرية والقطيفية.", 110.0, 120),
            GiftItem(3, "بطاقة إهداء لتجربة محلية بقيمة 200 ريال", "بطاقة رقمية أنيقة تتيح لصديقك حجز أي تجربة من اختياره داخل التطبيق وعيش المغامرة الأصيلة.", 200.0, 180)
        )

        _adminComplaints.value = listOf(
            ComplaintItem(1, "سعد العتيبي", "saad@mail.com", "تأخر تفعيل كود الحجز", "لقد قمت بحجز تجربة منحل العم فهد وتم سحب المبلغ، ولكن الكود استغرق 10 دقائق للظهور في لوحة الحجوزات الخاصة بي.", "2026-07-15", "Resolved"),
            ComplaintItem(2, "نورة اليوسف", "noura@mail.com", "استفسار عن تأجير السيارات", "أريد التأكد هل أسعار تأجير السيارات تشمل التأمين الشامل ومضخة الإطارات في حال صعود الجبال الوعرة؟", "2026-07-17", "Pending")
        )

        _supportMessages.value = listOf(
            ChatMessageEntity(sender = "guide", message = "مرحباً بك في مركز الدعم الفني لتطبيق تِجربة 🌟 كيف يمكننا مساعدتك اليوم؟", timestamp = System.currentTimeMillis() - 60000, chatPartner = "الدعم الفني")
        )
    }

    // --- Actions ---

    // Toggle Favorite
    fun toggleFavorite(expId: Int) {
        _experiencesList.value = _experiencesList.value.map {
            if (it.id == expId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    // Add Booking & update balance / points
    fun bookExperience(itemTitle: String, category: String, price: Double, quantity: Int) {
        val totalCost = price * quantity
        // Verify balance
        if (userWalletBalance.value >= totalCost) {
            userWalletBalance.value -= totalCost
            // Earn points: 10% of price as points
            userPoints.value += (totalCost * 0.1).toInt()

            viewModelScope.launch {
                repository.createBooking(
                    itemTitle = itemTitle,
                    category = category,
                    date = "غداً، 09:00 صباحاً",
                    price = price,
                    quantity = quantity
                )
                addNotification("تم تأكيد الحجز بنجاح! 🎉", "حجزك لـ '$itemTitle' مؤكد الآن. يمكنك استعراض رمز QR Code وتفاصيل التجربة في شاشة الحجوزات.")
                Log.d(TAG, "Booking saved successfully")
            }
        } else {
            addNotification("فشل الحجز - الرصيد غير كافٍ ❌", "يرجى شحن محفظتك الرقمية لتتمكن من حجز تجربة '$itemTitle'.")
        }
    }

    fun refundBooking(bookingId: Int, itemTitle: String, refundAmount: Double) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId)
            userWalletBalance.value += refundAmount
            addNotification("تم استرداد المبلغ بنجاح 💰", "تم إلغاء حجزك لـ '$itemTitle' وإعادة مبلغ $refundAmount ريال لمحفظتك بنجاح.")
        }
    }

    // Direct Messaging system (Includes instant notification preview & automated replies)
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val partner = _activeChatPartner.value
        viewModelScope.launch {
            repository.sendChatMessage(partner, "user", text)
            
            // Auto reply simulate
            delay(1500)
            val replyText = getAutomatedHostReply(partner, text)
            repository.sendChatMessage(partner, "host", replyText)

            // Trigger simulated push & in-app banner alert
            addNotification("رسالة جديدة من $partner", replyText)
        }
    }

    private fun getAutomatedHostReply(partner: String, userMsg: String): String {
        return when {
            partner.contains("صالح") -> {
                if (userMsg.contains("موعد") || userMsg.contains("وقت")) {
                    "أهلاً بك يا غالي! ورشتي مفتوحة من السبت إلى الخميس، من الساعة 4 عصراً وحتى 9 مساءً. شرفنا في أي وقت!"
                } else if (userMsg.contains("سعر") || userMsg.contains("تكلفة")) {
                    "التجربة بـ 120 ريال فقط للبالغين، وتشمل جميع المواد الخام (الطين، عجلة التشكيل، الفرن) وقطعة فخار تأخذها معك."
                } else {
                    "حياك الله يا طيب! أنا هنا بانتظارك في الورشة لنصنع سوياً عملاً فخارياً رائعاً يعيش معك طويلاً ✨"
                }
            }
            partner.contains("فهد") -> {
                if (userMsg.contains("عسل") || userMsg.contains("نوع")) {
                    "نعم متوفر لدينا عسل سدر جبلي فاخر وعسل سمرة طازج ومضمون 100%. وكل زائر يحصل على عبوة تذوق هدية!"
                } else {
                    "يا أهلاً بك يا أخي! جبال عسير ترحب بك، والمنحل جاهز والملابس الواقية معقمة وجاهزة لعيش تجربة فريدة."
                }
            }
            partner.contains("يوسف") -> {
                "أهلاً وسهلاً بك في عروس البحر الأحمر! القارب مجهز بكافة أدوات السلامة والصنارات الفاخرة، والصيد ممتاز هذه الأيام 🎣"
            }
            else -> "مرحباً بك! يسعدني جداً اهتمامك بتجربتنا المحلية وسأكون مسروراً بالإجابة على أي استفسارات تخص الحجز."
        }
    }

    // Technical Support Messages (Direct typing)
    fun sendSupportMessage(text: String) {
        if (text.isBlank()) return
        val currentMsgs = _supportMessages.value
        val userMsg = ChatMessageEntity(sender = "user", message = text, timestamp = System.currentTimeMillis(), chatPartner = "الدعم الفني")
        _supportMessages.value = currentMsgs + userMsg

        // Auto support agent reply
        viewModelScope.launch {
            delay(1200)
            val supportReply = ChatMessageEntity(
                sender = "guide",
                message = "تم تسجيل طلبك وتمريره للمهندسة رغد وقسم الجودة لخدمتك فوراً. شكراً لتواصلك معنا ورقم تذكرتك هو #TAJ-${(1000..9999).random()}.",
                timestamp = System.currentTimeMillis(),
                chatPartner = "الدعم الفني"
            )
            _supportMessages.value = _supportMessages.value + supportReply
            addNotification("تحديث من الدعم الفني", "تم تحديث حالة تذكرتك بنجاح.")
        }
    }

    // --- AI Travel Assistant (Invoking Gemini API) ---
    fun runAiTravelPlanner() {
        val city = plannerCity.value
        val budget = plannerBudget.value
        val people = plannerPeopleCount.value
        val duration = plannerDuration.value
        val interests = plannerInterests.value

        _plannerState.value = PlannerState.Loading
        pushScreen(Screen.AiPlanner) // Open planner screen

        viewModelScope.launch {
            try {
                val planResult = repository.generateAndSaveAiPlan(city, budget, people, duration, interests)
                _plannerState.value = PlannerState.Success(planResult)
                addNotification("تم توليد خطتك الذكية بنجاح! 🤖", "قام المساعد الذكي بتخطيط رحلتك لـ '$city' وتوزيع الأنشطة والمطاعم.")
            } catch (e: Exception) {
                _plannerState.value = PlannerState.Error(e.message ?: "خطأ غير معروف")
            }
        }
    }

    fun resetPlanner() {
        _plannerState.value = PlannerState.Idle
    }

    // --- Coupons and Promo codes ---
    fun applyPromoCode(code: String): Boolean {
        return if (code.trim().uppercase() == "TAJRIBAH20") {
            appliedCoupon.value = "TAJRIBAH20"
            couponDiscountPercent.value = 20
            addNotification("تم تطبيق الكوبون! 🏷️", "مبروك، حصلت على خصم 20% على جميع حجوزاتك القادمة.")
            true
        } else {
            false
        }
    }

    fun removePromoCode() {
        appliedCoupon.value = ""
        couponDiscountPercent.value = 0
    }

    // --- Notifications Management ---
    fun addNotification(title: String, body: String) {
        val id = (1..10000).random()
        val notif = NotificationItem(id, title, body, System.currentTimeMillis(), false)
        _notificationsList.value = listOf(notif) + _notificationsList.value
        _activeBannerNotification.value = notif

        // Auto dismiss banner after 5 seconds
        viewModelScope.launch {
            delay(5000)
            if (_activeBannerNotification.value?.id == id) {
                _activeBannerNotification.value = null
            }
        }
    }

    fun clearBanner() {
        _activeBannerNotification.value = null
    }

    fun markNotificationsAsRead() {
        _notificationsList.value = _notificationsList.value.map { it.copy(isRead = true) }
    }

    // --- Admin Dashboard Operations (Managing content in real-time) ---
    fun addNewExperience(title: String, hostName: String, category: String, price: Double, city: String, description: String) {
        val newId = (_experiencesList.value.maxOfOrNull { it.id } ?: 0) + 1
        val newExp = ExperienceItem(
            id = newId,
            title = title,
            hostName = hostName,
            category = category,
            rating = 5.0f,
            reviewCount = 1,
            price = price,
            duration = "يوم كامل",
            city = city,
            description = description,
            isFavorite = false
        )
        _experiencesList.value = listOf(newExp) + _experiencesList.value
        addNotification("تمت إضافة تجربة جديدة! 📢", "قام مشرف النظام بإضافة تجربة '$title' في مدينة '$city' بنجاح.")
    }

    fun resolveComplaint(complaintId: Int) {
        _adminComplaints.value = _adminComplaints.value.map {
            if (it.id == complaintId) it.copy(status = "Resolved") else it
        }
        addNotification("تم حل الشكوى", "تم وضع علامة 'محلولة' على الشكوى رقم #$complaintId.")
    }

    fun selectChatPartner(partnerName: String) {
        _activeChatPartner.value = partnerName
    }
}
