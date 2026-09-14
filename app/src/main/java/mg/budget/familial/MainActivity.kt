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
    "2029-07"
