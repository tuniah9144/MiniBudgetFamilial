package mg.budget.familial

import android.content.Context
import android.content.Intent
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
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private const val PREFS = "budget_v2"
private const val EXPENSES = "expenses"
private const val CASH = "cash"
private const val EXTRA_SAVINGS = "extra_savings"

data class Expense(val date:String,val category:String,val amount:Long,val note:String,val mode:String)
private val categories=listOf("Alimentation","PPN","Crédit / petites dettes","Santé","Hygiène","Charbon / Kitay","JIRAMA","Fournitures","Goûter","Transport","Colis / aide","Loisirs / Volo","Imprévus","Loyer","Karama","Connexion","Écolage","Prêt bancaire","Charité","Dîme")
private val budget=linkedMapOf("Alimentation" to 550000L,"PPN" to 150000L,"Crédit / petites dettes" to 10000L,"Santé" to 75000L,"Hygiène" to 80000L,"Charbon / Kitay" to 50000L,"JIRAMA" to 50000L,"Fournitures" to 50000L,"Goûter" to 50000L,"Transport" to 80000L,"Colis / aide" to 30000L,"Loisirs / Volo" to 30000L,"Imprévus" to 120000L,"Loyer" to 200000L,"Karama" to 158000L,"Connexion" to 99000L,"Écolage" to 25000L,"Prêt bancaire" to 704024L,"Charité" to 158927L,"Dîme" to 79464L)
private val revenues=listOf(2911898L,4711425L,4483425L,2911898L,2911898L,10596580L,2911898L,4711425L,4483425L,2911898L,2911898L,10596580L)
private val loanBalances=mapOf("2026-09" to 23516927L,"2026-12" to 22227454L,"2027-12" to 16587647L,"2028-12" to 10089542L,"2029-07" to 5851462L,"2029-12" to 2602521L,"2030-01" to 1929402L,"2030-02" to 248290L,"2030-03" to 0L)
private fun money(v:Long)=NumberFormat.getNumberInstance(Locale.FRANCE).format(v)+" Ar"

class Store(private val c:Context){
 private val p=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
 fun load():List<Expense>{ val a=JSONArray(p.getString(EXPENSES,"[]")); return (0 until a.length()).map{val o=a.getJSONObject(it); Expense(o.getString("date"),o.getString("category"),o.getLong("amount"),o.optString("note"),o.optString("mode"))} }
 fun save(xs:List<Expense>){val a=JSONArray(); xs.forEach{x->a.put(JSONObject().apply{put("date",x.date);put("category",x.category);put("amount",x.amount);put("note",x.note);put("mode",x.mode)})};p.edit().putString(EXPENSES,a.toString()).apply()}
 fun cash()=p.getLong(CASH,0)
 fun setCash(v:Long)=p.edit().putLong(CASH,v).apply()
 fun extra()=p.getLong(EXTRA_SAVINGS,0)
 fun setExtra(v:Long)=p.edit().putLong(EXTRA_SAVINGS,v).apply()
 fun backup(xs:List<Expense>):String=JSONObject().apply{put("cash",cash());put("extraSavings",extra());put("expenses",JSONArray().apply{xs.forEach{x->put(JSONObject().apply{put("date",x.date);put("category",x.category);put("amount",x.amount);put("note",x.note);put("mode",x.mode)})}})}.toString()
 fun restore(s:String):List<Expense>{val o=JSONObject(s);setCash(o.optLong("cash",0));setExtra(o.optLong("extraSavings",0));val a=o.optJSONArray("expenses")?:JSONArray();return (0 until a.length()).map{val x=a.getJSONObject(it);Expense(x.getString("date"),x.getString("category"),x.getLong("amount"),x.optString("note"),x.optString("mode"))}}
}

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{App()}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(){
 val ctx=androidx.compose.ui.platform.LocalContext.current; val store=remember{Store(ctx)}
 var expenses by remember{mutableStateOf(store.load())}; var tab by remember{mutableIntStateOf(0)}; var month by remember{mutableStateOf(YearMonth.now())}; var cash by remember{mutableLongStateOf(store.cash())}; var extra by remember{mutableLongStateOf(store.extra())}; var message by remember{mutableStateOf("")}
 fun add(x:Expense){expenses=listOf(x)+expenses;store.save(expenses)}
 val exportLauncher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")){uri:Uri?->uri?.let{writeUri(ctx,it,csv(expenses))}}
 val backupLauncher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri:Uri?->uri?.let{writeUri(ctx,it,store.backup(expenses))}}
 val restoreLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri:Uri?->uri?.let{try{val xs=store.restore(ctx.contentResolver.openInputStream(it)!!.bufferedReader().readText());expenses=xs;cash=store.cash();extra=store.extra();message="Sauvegarde restaurée"}catch(e:Exception){message="Fichier invalide"}}}
 MaterialTheme(colorScheme=lightColorScheme(primary=androidx.compose.ui.graphics.Color(0xFF2563EB),secondary=androidx.compose.ui.graphics.Color(0xFF16A34A))){Scaffold(topBar={TopAppBar(title={Text("Mini Budget Familial")},actions={IconButton(onClick={backupLauncher.launch("budget-familial-backup.json")}){Icon(Icons.Default.Backup,"Sauvegarder")};IconButton(onClick={restoreLauncher.launch(arrayOf("application/json"))}){Icon(Icons.Default.Restore,"Restaurer")}})},bottomBar={NavigationBar{listOf("Accueil" to Icons.Default.Home,"Saisie" to Icons.Default.Add,"Budget" to Icons.Default.AccountBalanceWallet,"Prêt" to Icons.Default.CreditCard,"Caisse" to Icons.Default.Payments).forEachIndexed{i,(t,ic)->NavigationBarItem(tab==i,{tab=i},icon={Icon(ic,t)},label={Text(t)})}}}){p->Box(Modifier.padding(p)){when(tab){0->Dashboard(expenses,month,{month=it});1->ExpenseScreen(month,::add);2->BudgetScreen(expenses,month);3->LoanScreen(extra){extra=it;store.setExtra(it)};4->CashScreen(cash){cash=it;store.setCash(it)}}};if(message.isNotBlank())SnackbarHost(remember{SnackbarHostState()})}}
 // Export accessible from dashboard through a small action button in screen.
}

fun writeUri(c:Context,u:Uri,s:String){c.contentResolver.openOutputStream(u)?.use{it.write(s.toByteArray())}}
fun csv(xs:List<Expense>)="Date;Catégorie;Montant;Description;Mode\n"+xs.joinToString("\n"){"${it.date};${it.category};${it.amount};${it.note.replace(";",",")};${it.mode}"}

@Composable fun Dashboard(xs:List<Expense>,m:YearMonth,onMonth:(YearMonth)->Unit){
 val xm=xs.filter{it.date.startsWith(m.toString())};val spent=xm.sumOf{it.amount};val idx=m.monthValue-1;val rev=revenues[idx];val b=budget.values.sum();val surplus=rev-spent;val rate=if(rev>0)surplus*100.0/rev else 0.0
 LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Tableau de bord",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)};item{MonthPicker(m,onMonth)};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Kpi("Revenus",money(rev),Modifier.weight(1f));Kpi("Dépenses",money(spent),Modifier.weight(1f))}};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Kpi("Solde",money(surplus),Modifier.weight(1f));Kpi("Taux",String.format(Locale.FRANCE,"%.1f %%",rate),Modifier.weight(1f))}};item{Text("Dépenses par catégorie",fontWeight=FontWeight.Bold)};item{CategoryChart(xm)};item{Text("Évolution récente",fontWeight=FontWeight.Bold)};item{TrendChart(xs,m)};item{Text("Budget de référence mensuel : ${money(b)}",style=MaterialTheme.typography.bodySmall)};item{Button(onClick={}){Text("Pour exporter : utilisez l'icône de sauvegarde en haut pour une copie JSON")}}}
}
@Composable fun Kpi(t:String,v:String,mod:Modifier){Card(mod){Column(Modifier.padding(12.dp)){Text(t,style=MaterialTheme.typography.labelMedium);Text(v,fontWeight=FontWeight.Bold)}}}
@Composable fun MonthPicker(m:YearMonth,onChange:(YearMonth)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){IconButton({onChange(m.minusMonths(1))}){Icon(Icons.Default.ChevronLeft,"Précédent")};Text("${m.monthValue}/${m.year}",fontWeight=FontWeight.Bold);IconButton({onChange(m.plusMonths(1))}){Icon(Icons.Default.ChevronRight,"Suivant")}}}
@Composable fun CategoryChart(xs:List<Expense>){val vals=xs.groupBy{it.category}.mapValues{it.value.sumOf(Expense::amount)}.toList().sortedByDescending{it.second}.take(8);Column{vals.forEach{(k,v)->Row(Modifier.fillMaxWidth().padding(vertical=3.dp),verticalAlignment=Alignment.CenterVertically){Text(k,Modifier.width(150.dp));LinearProgressIndicator(progress={if(xs.sumOf(Expense::amount)>0)v.toFloat()/xs.sumOf(Expense::amount).toFloat() else 0f},Modifier.weight(1f));Text(money(v),Modifier.padding(start=6.dp))}}}}
@Composable fun TrendChart(xs:List<Expense>,m:YearMonth){val data=(0..5).map{m.minusMonths(5-it)}.map{ym->ym to xs.filter{x->x.date.startsWith(ym.toString())}.sumOf{it.amount}};val max=(data.maxOfOrNull{it.second}?:1).coerceAtLeast(1);Canvas(Modifier.fillMaxWidth().height(170.dp).background(MaterialTheme.colorScheme.surfaceVariant)){data.forEachIndexed{i,(_,v)->if(i>0){val x1=(i-1)*size.width/5f;val x2=i*size.width/5f;val y1=size.height-(data[i-1].second.toFloat()/max)*size.height*.85f;val y2=size.height-(v.toFloat()/max)*size.height*.85f;drawLine(MaterialTheme.colorScheme.primary,Offset(x1,y1),Offset(x2,y2),4f,StrokeCap.Round)}}}}

@Composable fun ExpenseScreen(m:YearMonth,onAdd:(Expense)->Unit){var cat by remember{mutableStateOf(categories.first())};var amount by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var mode by remember{mutableStateOf("Espèces")};var open by remember{mutableStateOf(false)};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Saisie rapide",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);MonthPicker(m){};Box{OutlinedButton({open=true},Modifier.fillMaxWidth()){Text(cat)};DropdownMenu(open,{open=false}){categories.forEach{DropdownMenuItem({Text(it)},{cat=it;open=false})}}};OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},label={Text("Montant (Ar)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text("Description")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mode,{mode=it},label={Text("Mode de paiement")},modifier=Modifier.fillMaxWidth());Button({amount.toLongOrNull()?.takeIf{it>0}?.let{onAdd(Expense(m.toString(),cat,it,note,mode));amount="";note=""}},Modifier.fillMaxWidth()){Text("ENREGISTRER")}}
}

@Composable fun BudgetScreen(xs:List<Expense>,m:YearMonth){val xm=xs.filter{it.date.startsWith(m.toString())};LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("Budget mensuel",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("${m.monthValue}/${m.year}")};items(budget.toList()){(k,v)->val real=xm.filter{it.category==k}.sumOf{it.amount};val pct=if(v>0)(real*100/v).toInt() else 0;Card{Column(Modifier.padding(12.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(k,fontWeight=FontWeight.Bold);Text("$pct %")};Text("Prévu : ${money(v)}");Text("Réel : ${money(real)}");LinearProgressIndicator(progress={if(v>0)(real.toFloat()/v).coerceAtMost(1f) else 0f},Modifier.fillMaxWidth());Text(if(real>v)"DÉPASSEMENT : ${money(real-v)}" else "Reste : ${money(v-real)}")}}}}
}

@Composable fun LoanScreen(extra:Long,onExtra:(Long)->Unit){val planned=15000000L+extra;Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Suivi du prêt",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Kpi("Capital suivi 15/09/2026",money(23516927L),Modifier.fillMaxWidth());Kpi("Mensualité",money(704024L),Modifier.fillMaxWidth());Kpi("Épargne prévue",money(planned),Modifier.fillMaxWidth());Text("Plan : 5 M Ar déjà disponibles + 5 M Ar en juin + 5 M Ar en décembre.");OutlinedTextField(if(extra==0L)"" else extra.toString(),{onExtra(it.filter(Char::isDigit).toLongOrNull()?:0)},label={Text("Épargne supplémentaire")},modifier=Modifier.fillMaxWidth());Text("Évolution indicative du capital restant dû");LoanChart() ;Text("Le remboursement anticipé réel dépend du décompte fourni par la banque et d'éventuels frais.",style=MaterialTheme.typography.bodySmall)}}
@Composable fun LoanChart(){val d=loanBalances.entries.toList();val max=d.first().value.toFloat();Canvas(Modifier.fillMaxWidth().height(190.dp)){d.forEachIndexed{i,e->if(i>0){val a=d[i-1].value.toFloat();val x1=(i-1)*size.width/(d.size-1);val x2=i*size.width/(d.size-1);val y1=size.height-a/max*size.height*.85f;val y2=size.height-e.value/max*size.height*.85f;drawLine(MaterialTheme.colorScheme.secondary,Offset(x1,y1),Offset(x2,y2),5f,StrokeCap.Round)}}}}

@Composable fun CashScreen(cash:Long,onCash:(Long)->Unit){var v by remember{mutableStateOf(if(cash==0L)"" else cash.toString())};Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Caisse / trésorerie",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Kpi("Solde de caisse",money(cash),Modifier.fillMaxWidth());OutlinedTextField(v,{v=it.filter(Char::isDigit)},label={Text("Nouveau solde (Ar)")},modifier=Modifier.fillMaxWidth());Button({onCash(v.toLongOrNull()?:0)},Modifier.fillMaxWidth()){Text("METTRE À JOUR")};Text("Cette caisse est un solde manuel : elle sert à suivre l'argent disponible hors compte bancaire." ,style=MaterialTheme.typography.bodySmall)}}
