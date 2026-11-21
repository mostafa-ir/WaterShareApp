package com.example.watershare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { WaterShareScreen() } } }
@Composable private fun WaterShareScreen() {
    val current = remember { mutableStateListOf("", "", "", "") }
    val previous = remember { mutableStateListOf("", "", "", "") }
    val offset = remember { mutableStateListOf("0", "0", "0", "0") }
    val usage = remember { mutableStateListOf("14", "35", "14", "14") }
    var days by remember { mutableStateOf("37") }; var shared by remember { mutableStateOf("12") }
    var mainBill by remember { mutableStateOf("14954000") }; var results by remember { mutableStateOf<List<UnitResult>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    MaterialTheme { Scaffold { padding -> LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("تقسیم قبض آب چهار واحد", style=MaterialTheme.typography.headlineSmall) }
        item { Text("اطلاعات قبض شهرداری و مصرف هر واحد را وارد کنید.") }
        item { OutlinedTextField(days,{days=it},label={Text("مدت دوره قبض (روز)")},modifier=Modifier.fillMaxWidth()) }
        item { OutlinedTextField(shared,{shared=it},label={Text("مصرف آب مشترک (مترمکعب)")},modifier=Modifier.fillMaxWidth()) }
        item { OutlinedTextField(mainBill,{mainBill=it},label={Text("مبلغ قبض اصلی (ریال)")},modifier=Modifier.fillMaxWidth()) }
        (0..3).forEach { i -> item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("واحد ${i+1}",style=MaterialTheme.typography.titleMedium)
            OutlinedTextField(current[i],{current[i]=it},label={Text("قرائت فعلی کنتور داخلی")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(previous[i],{previous[i]=it},label={Text("قرائت قبلی کنتور شهرداری (سابقه)")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(offset[i],{offset[i]=it},label={Text("فاصله روز قرائت داخلی تا قرائت مأمور")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(usage[i],{usage[i]=it},label={Text("مصرف اولیه طبق جدول تعرفه (مترمکعب)")},modifier=Modifier.fillMaxWidth())
        } } } }
        item { Button(onClick={
            val d=days.toIntOrNull(); val sh=shared.toDoubleOrNull(); val bill=mainBill.toDoubleOrNull()
            if(d==null||d<=0||sh==null||bill==null||bill<0||(0..3).any{current[it].toDoubleOrNull()==null||previous[it].toDoubleOrNull()==null||offset[it].toIntOrNull()==null||usage[it].toDoubleOrNull()==null||offset[it].toIntOrNull()!!<0||usage[it].toDoubleOrNull()!!<0}) { error="ورودی‌ها را بررسی کنید؛ همه مقادیر باید عدد معتبر باشند."; results=emptyList() }
            else { error=""; results=WaterCalculator.calculate((0..3).map{UnitInput(it+1,current[it].toDouble(),previous[it].toDouble(),offset[it].toInt(),usage[it].toDouble())},TariffSettings(billDays=d,sharedWater=sh,billTotal=bill)) }
        },modifier=Modifier.fillMaxWidth()){Text("محاسبه سهم واحدها")} }
        if(error.isNotEmpty()) item { Text(error,color=MaterialTheme.colorScheme.error) }
        results.forEach { r -> item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text("واحد ${r.unit}",style=MaterialTheme.typography.titleMedium)
            Text("قرائت تخمینی در تاریخ مأمور: ${r.estimatedReading.toLong()}")
            Text("مصرف مبنای تعرفه با سهم مشترک: %.2f مترمکعب".format(Locale("fa"),r.usage))
            Text("میانگین ماهانه: %.2f".format(Locale("fa"),r.monthlyAverage))
            Text("آب‌بها: %.0f ریال".format(Locale("fa"),r.water)); Text("فاضلاب: %.0f ریال".format(Locale("fa"),r.wastewater))
            Text("تکالیف قانونی: %.0f ریال".format(Locale("fa"),r.legal)); Text("آبونمان: %.0f ریال".format(Locale("fa"),r.abonman)); Text("مالیات: %.0f ریال".format(Locale("fa"),r.tax))
            Text("مبلغ قبل از اصلاح: %.0f ریال".format(Locale("fa"),r.rawAmount)); Text("مبلغ اصلاح‌شده: %.0f ریال".format(Locale("fa"),r.correctedAmount))
            Text("سهم نهایی: %.0f تومان".format(Locale("fa"),r.tomanRounded),style=MaterialTheme.typography.titleMedium)
        } } } }
        item { Text("توجه: برآورد قرائت از فرمول فایل تاریخچه انجام می‌شود. تعرفه مطابق فرمول‌های فایل تعرفه و اصلاح جمع واحدها نسبت به مبلغ قبض اصلی محاسبه شده است.",style=MaterialTheme.typography.bodySmall) }
    } } }
}
