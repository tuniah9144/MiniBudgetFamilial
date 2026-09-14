package mg.budget.familial

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

private const val PREFS = "budget_v2"
private const val EXPENSES = "expenses"
private const val CASH = "cash"
private const val EXTRA_SAVINGS = "extra_savings"

data class Expense(
    val date: String,
    val category: String,
    val amount: Long,
    val note: String,
    val mode: String
)

private val categories = listOf(
    "Alimentation",
    "PPN",
    "Crédit / petites dettes",
    "Santé",
    "Hygiène",
    "Charbon / Kitay",
    "JIRAMA",
    "Fournitures",
    "Goûter",
    "Transport",
    "Colis / aide",
    "Loisirs / Volo",
    "Imprévus",
    "Loyer",
    "Karama",
    "Connexion",
    "Écolage",
    "Prêt bancaire",
    "Charité",
    "Dîme"
)

private val budget = linkedMapOf(
    "Alimentation" to 550000L,
    "PPN" to 150000L,
    "Crédit / petites dettes" to 10000L,
    "Santé" to 75000L,
    "Hygiène" to 80000L,
    "Charbon / Kitay" to 50000L,
    "JIRAMA" to 50000L,
    "Fournitures" to 50000L,
    "Goûter" to 50000L,
    "Transport" to 80000L,
    "Colis / aide" to 30000L,
    "Loisirs / Volo" to 30000L,
    "Imprévus" to 120000L,
    "Loyer" to 200000L,
    "Karama" to 158000L,
    "Connexion" to 99000L,
    "Écolage" to 25000L,
    "Prêt bancaire" to 704024L,
    "Charité" to 158927L,
    "Dîme" to 79464L
)

private val revenues = listOf(
    2911898L,
    4711425L,
    4483425L,
    2911898L,
    2911898L,
    10596580L,
    2911898L,
    4711425L,
    4483425L,
    2911898L,
    2911898L,
    10596580L
)

private val loanBalances = mapOf(
    "2026-09" to 23516927L,
    "2026-12" to 22227454L,
    "2027-12" to 16587647L,
    "2028-12" to 10089542L,
    "2029-07" to 5851462L,
    "2029-12" to 2602521L,
    "2030-01" to 1929402L,
    "2030-02" to 248290L,
    "2030-03" to 0L
)

private fun money(value: Long): String {
    return NumberFormat.getNumberInstance(Locale.FRANCE).format(value) + " Ar"
}

class Store(private val context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(): List<Expense> {
        val array = JSONArray(prefs.getString(EXPENSES, "[]"))

        return (0 until array.length()).map {
            val obj = array.getJSONObject(it)

            Expense(
                date = obj.getString("date"),
                category = obj.getString("category"),
                amount = obj.getLong("amount"),
                note = obj.optString("note"),
                mode = obj.optString("mode")
            )
        }
    }

    fun save(expenses: List<Expense>) {
        val array = JSONArray()

        expenses.forEach { expense ->
            array.put(
                JSONObject().apply {
                    put("date", expense.date)
                    put("category", expense.category)
                    put("amount", expense.amount)
                    put("note", expense.note)
                    put("mode", expense.mode)
                }
            )
        }

        prefs.edit()
            .putString(EXPENSES, array.toString())
            .apply()
    }

    fun cash(): Long {
        return prefs.getLong(CASH, 0L)
    }

    fun setCash(value: Long) {
        prefs.edit()
            .putLong(CASH, value)
            .apply()
    }

    fun extra(): Long {
        return prefs.getLong(EXTRA_SAVINGS, 0L)
    }

    fun setExtra(value: Long) {
        prefs.edit()
            .putLong(EXTRA_SAVINGS, value)
            .apply()
    }

    fun backup(expenses: List<Expense>): String {

        return JSONObject().apply {

            put("cash", cash())
            put("extraSavings", extra())

            put(
                "expenses",
                JSONArray().apply {

                    expenses.forEach { expense ->

                        put(
                            JSONObject().apply {
                                put("date", expense.date)
                                put("category", expense.category)
                                put("amount", expense.amount)
                                put("note", expense.note)
                                put("mode", expense.mode)
                            }
                        )
                    }
                }
            )

        }.toString()
    }

    fun restore(json: String): List<Expense> {

        val obj = JSONObject(json)

        setCash(obj.optLong("cash", 0L))
        setExtra(obj.optLong("extraSavings", 0L))

        val array =
            obj.optJSONArray("expenses") ?: JSONArray()

        return (0 until array.length()).map {

            val expense = array.getJSONObject(it)

            Expense(
                date = expense.getString("date"),
                category = expense.getString("category"),
                amount = expense.getLong("amount"),
                note = expense.optString("note"),
                mode = expense.optString("mode")
            )
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val store = remember {
        Store(context)
    }

    var expenses by remember {
        mutableStateOf(store.load())
    }

    var tab by remember {
        mutableIntStateOf(0)
    }

    var month by remember {
        mutableStateOf(YearMonth.now())
    }

    var cash by remember {
        mutableLongStateOf(store.cash())
    }

    var extra by remember {
        mutableLongStateOf(store.extra())
    }

    var message by remember {
        mutableStateOf("")
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    fun addExpense(expense: Expense) {

        expenses = listOf(expense) + expenses

        store.save(expenses)

        message = "Dépense enregistrée"
    }

    val backupLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri: Uri? ->

            uri?.let {
                writeUri(
                    context,
                    it,
                    store.backup(expenses)
                )

                message = "Sauvegarde créée"
            }
        }

    val restoreLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->

            uri?.let {

                try {

                    val input =
                        context.contentResolver
                            .openInputStream(it)

                    val json =
                        input?.bufferedReader()?.use { reader ->
                            reader.readText()
                        }

                    if (json != null) {

                        val restored =
                            store.restore(json)

                        expenses = restored
                        cash = store.cash()
                        extra = store.extra()

                        message = "Sauvegarde restaurée"
                    }

                } catch (e: Exception) {

                    message = "Fichier invalide"
                }
            }
        }

    LaunchedEffect(message) {

        if (message.isNotBlank()) {

            snackbarHostState.showSnackbar(message)

            message = ""
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF2563EB),
            secondary = androidx.compose.ui.graphics.Color(0xFF16A34A)
        )
    ) {

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        Text("Mini Budget Familial")
                    },

                    actions = {

                        IconButton(
                            onClick = {
                                backupLauncher.launch(
                                    "budget-familial-backup.json"
                                )
                            }
                        ) {

                            Icon(
                                Icons.Default.Backup,
                                contentDescription = "Sauvegarder"
                            )
                        }

                        IconButton(
                            onClick = {
                                restoreLauncher.launch(
                                    arrayOf("application/json")
                                )
                            }
                        ) {

                            Icon(
                                Icons.Default.Restore,
                                contentDescription = "Restaurer"
                            )
                        }
                    }
                }
            },

            bottomBar = {

                NavigationBar {

                    val navigationItems = listOf(
                        "Accueil" to Icons.Default.Home,
                        "Saisie" to Icons.Default.Add,
                        "Budget" to Icons.Default.AccountBalanceWallet,
                        "Prêt" to Icons.Default.CreditCard,
                        "Caisse" to Icons.Default.Payments
                    )

                    navigationItems.forEachIndexed { index, item ->

                        val title = item.first
                        val icon = item.second

                        NavigationBarItem(

                            selected = tab == index,

                            onClick = {
                                tab = index
                            },

                            icon = {
                                Icon(
                                    icon,
                                    contentDescription = title
                                )
                            },

                            label = {
                                Text(title)
                            }
                        )
                    }
                }
            },

            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }

        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                when (tab) {

                    0 -> Dashboard(
                        expenses,
                        month
                    ) {
                        month = it
                    }

                    1 -> ExpenseScreen(
                        month,
                        ::addExpense
                    )

                    2 -> BudgetScreen(
                        expenses,
                        month
                    )

                    3 -> LoanScreen(
                        extra
                    ) {
                        extra = it
                        store.setExtra(it)
                    }

                    4 -> CashScreen(
                        cash
                    ) {
                        cash = it
                        store.setCash(it)
                    }
                }
            }
        }
    }
}

fun writeUri(
    context: Context,
    uri: Uri,
    text: String
) {

    context.contentResolver
        .openOutputStream(uri)
        ?.use {
            it.write(text.toByteArray())
        }
}

@Composable
fun Dashboard(
    expenses: List<Expense>,
    month: YearMonth,
    onMonth: (YearMonth) -> Unit
) {

    val monthlyExpenses =
        expenses.filter {
            it.date.startsWith(month.toString())
        }

    val spent =
        monthlyExpenses.sumOf {
            it.amount
        }

    val index =
        (month.monthValue - 1)
            .coerceIn(0, revenues.lastIndex)

    val revenue =
        revenues[index]

    val totalBudget =
        budget.values.sum()

    val surplus =
        revenue - spent

    val rate =
        if (revenue > 0) {
            surplus * 100.0 / revenue
        } else {
            0.0
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                "Tableau de bord",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {

            MonthPicker(
                month,
                onMonth
            )
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Kpi(
                    "Revenus",
                    money(revenue),
                    Modifier.weight(1f)
                )

                Kpi(
                    "Dépenses",
                    money(spent),
                    Modifier.weight(1f)
                )
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Kpi(
                    "Solde",
                    money(surplus),
                    Modifier.weight(1f)
                )

                Kpi(
                    "Taux",
                    String.format(
                        Locale.FRANCE,
                        "%.1f %%",
                        rate
                    ),
                    Modifier.weight(1f)
                )
            }
        }

        item {

            Text(
                "Dépenses par catégorie",
                fontWeight = FontWeight.Bold
            )
        }

        item {

            CategoryChart(monthlyExpenses)
        }

        item {

            Text(
                "Évolution récente",
                fontWeight = FontWeight.Bold
            )
        }

        item {

            TrendChart(
                expenses,
                month
            )
        }

        item {

            Text(
                "Budget de référence mensuel : ${money(totalBudget)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun Kpi(
    title: String,
    value: String,
    modifier: Modifier
) {

    Card(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                title,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                value,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonthPicker(
    month: YearMonth,
    onChange: (YearMonth) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        IconButton(
            onClick = {
                onChange(
                    month.minusMonths(1)
                )
            }
        ) {

            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Précédent"
            )
        }

        Text(
            "${month.monthValue}/${month.year}",
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = {
                onChange(
                    month.plusMonths(1)
                )
            }
        ) {

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Suivant"
            )
        }
    }
}

@Composable
fun CategoryChart(
    expenses: List<Expense>
) {

    val values =
        expenses
            .groupBy {
                it.category
            }
            .mapValues {
                it.value.sumOf(Expense::amount)
            }
            .toList()
            .sortedByDescending {
                it.second
            }
            .take(8)

    val total =
        expenses.sumOf {
            it.amount
        }

    Column {

        values.forEach { item ->

            val category = item.first
            val amount = item.second

            val progress =
                if (total > 0) {
                    amount.toFloat() /
                        total.toFloat()
                } else {
                    0f
                }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    category,
                    modifier = Modifier.width(150.dp)
                )

                LinearProgressIndicator(
                    progress = {
                        progress
                    },
                    modifier = Modifier.weight(1f)
                )

                Text(
                    money(amount),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@Composable
fun TrendChart(
    expenses: List<Expense>,
    month: YearMonth
) {

    val data =
        (0..5)
            .map {
                month.minusMonths(5L - it)
            }
            .map { ym ->

                ym to expenses
                    .filter {
                        it.date.startsWith(
                            ym.toString()
                        )
                    }
                    .sumOf {
                        it.amount
                    }
            }

    val max =
        (data.maxOfOrNull {
            it.second
        } ?: 1L)
            .coerceAtLeast(1L)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
    ) {

        data.forEachIndexed { index, entry ->

            if (index > 0) {

                val previous =
                    data[index - 1].second

                val current =
                    entry.second

                val x1 =
                    (index - 1) *
                        size.width /
                        5f

                val x2 =
                    index *
                        size.width /
                        5f

                val y1 =
                    size.height -
                        (
                            previous.toFloat() /
                                max.toFloat()
                            ) *
                            size.height *
                            0.85f

                val y2 =
                    size.height -
                        (
                            current.toFloat() /
                                max.toFloat()
                            ) *
                            size.height *
                            0.85f

                drawLine(
                    color =
                        MaterialTheme.colorScheme.primary,

                    start = Offset(
                        x1,
                        y1
                    ),

                    end = Offset(
                        x2,
                        y2
                    ),

                    strokeWidth = 4f,

                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun ExpenseScreen(
    month: YearMonth,
    onAdd: (Expense) -> Unit
) {

    var category by remember {
        mutableStateOf(
            categories.first()
        )
    }

    var amount by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    var mode by remember {
        mutableStateOf("Espèces")
    }

    var menuOpen by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(
                rememberScrollState()
            ),

        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Text(
            "Saisie rapide",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        MonthPicker(
            month
        ) {}

        Box {

            OutlinedButton(
                onClick = {
                    menuOpen = true
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(category)
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = {
                    menuOpen = false
                }
            ) {

                categories.forEach { item ->

                    DropdownMenuItem(

                        text = {
                            Text(item)
                        },

                        onClick = {

                            category = item
                            menuOpen = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(

            value = amount,

            onValueChange = {
                amount =
                    it.filter(Char::isDigit)
            },

            label = {
                Text("Montant (Ar)")
            },

            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(

            value = note,

            onValueChange = {
                note = it
            },

            label = {
                Text("Description")
            },

            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(

            value = mode,

            onValueChange = {
                mode = it
            },

            label = {
                Text("Mode de paiement")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Button(

            onClick = {

                val value =
                    amount.toLongOrNull()

                if (value != null && value > 0L) {

                    onAdd(
                        Expense(
                            date = month.toString(),
                            category = category,
                            amount = value,
                            note = note,
                            mode = mode
                        )
                    )

                    amount = ""
                    note = ""
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("ENREGISTRER")
        }
    }
}

@Composable
fun BudgetScreen(
    expenses: List<Expense>,
    month: YearMonth
) {

    val monthlyExpenses =
        expenses.filter {
            it.date.startsWith(
                month.toString()
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        item {

            Text(
                "Budget mensuel",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "${month.monthValue}/${month.year}"
            )
        }

        items(
            budget.toList()
        ) { item ->

            val category =
                item.first

            val planned =
                item.second

            val actual =
                monthlyExpenses
                    .filter {
                        it.category == category
                    }
                    .sumOf {
                        it.amount
                    }

            val percentage =
                if (planned > 0L) {
                    ((actual * 100L) / planned)
                        .toInt()
                } else {
                    0
                }

            val progress =
                if (planned > 0L) {
                    (actual.toFloat() /
                        planned.toFloat())
                        .coerceAtMost(1f)
                } else {
                    0f
                }

            Card {

                Column(
                    modifier =
                        Modifier.padding(12.dp)
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            category,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            "$percentage %"
                        )
                    }

                    Text(
                        "Prévu : ${money(planned)}"
                    )

                    Text(
                        "Réel : ${money(actual)}"
                    )

                    LinearProgressIndicator(
                        progress = {
                            progress
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Text(
                        if (actual > planned) {

                            "DÉPASSEMENT : ${
                                money(actual - planned)
                            }"

                        } else {

                            "Reste : ${
                                money(planned - actual)
                            }"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoanScreen(
    extra: Long,
    onExtra: (Long) -> Unit
) {

    val planned =
        15000000L + extra

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(
                rememberScrollState()
            ),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Suivi du prêt",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Kpi(
            "Capital suivi 15/09/2026",
            money(23516927L),
            Modifier.fillMaxWidth()
        )

        Kpi(
            "Mensualité",
            money(704024L),
            Modifier.fillMaxWidth()
        )

        Kpi(
            "Épargne prévue",
            money(planned),
            Modifier.fillMaxWidth()
        )

        Text(
            "Plan : 5 M Ar déjà disponibles + 5 M Ar en juin + 5 M Ar en décembre."
        )

        OutlinedTextField(

            value =
                if (extra == 0L) {
                    ""
                } else {
                    extra.toString()
                },

            onValueChange = {

                val value =
                    it.filter(Char::isDigit)
                        .toLongOrNull()
                        ?: 0L

                onExtra(value)
            },

            label = {
                Text("Épargne supplémentaire")
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Text(
            "Évolution indicative du capital restant dû"
        )

        LoanChart()

        Text(
            "Le remboursement anticipé réel dépend du décompte fourni par la banque et d'éventuels frais.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun LoanChart() {

    val data =
        loanBalances.entries.toList()

    val max =
        data.firstOrNull()?.value
            ?.toFloat()
            ?.coerceAtLeast(1f)
            ?: 1f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {

        if (data.size < 2) {
            return@Canvas
        }

        data.forEachIndexed { index, entry ->

            if (index > 0) {

                val previous =
                    data[index - 1].value.toFloat()

                val current =
                    entry.value.toFloat()

                val x1 =
                    (index - 1) *
                        size.width /
                        (data.size - 1)

                val x2 =
                    index *
                        size.width /
                        (data.size - 1)

                val y1 =
                    size.height -
                        previous / max *
                        size.height *
                        0.85f

                val y2 =
                    size.height -
                        current / max *
                        size.height *
                        0.85f

                drawLine(

                    color =
                        MaterialTheme.colorScheme.secondary,

                    start = Offset(
                        x1,
                        y1
                    ),

                    end = Offset(
                        x2,
                        y2
                    ),

                    strokeWidth = 5f,

                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun CashScreen(
    cash: Long,
    onCash: (Long) -> Unit
) {

    var value by remember(cash) {

        mutableStateOf(
            if (cash == 0L) {
                ""
            } else {
                cash.toString()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Caisse / trésorerie",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Kpi(
            "Solde de caisse",
            money(cash),
            Modifier.fillMaxWidth()
        )

        OutlinedTextField(

            value = value,

            onValueChange = {
                value =
                    it.filter(Char::isDigit)
            },

            label = {
                Text("Nouveau solde (Ar)")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Button(

            onClick = {

                onCash(
                    value.toLongOrNull()
                        ?: 0L
                )
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("METTRE À JOUR")
        }

        Text(
            "Cette caisse est un solde manuel : elle sert à suivre l'argent disponible hors compte bancaire.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
