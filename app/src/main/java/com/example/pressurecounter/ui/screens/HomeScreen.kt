package com.example.pressurecounter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pressurecounter.data.model.Measurement
import com.example.pressurecounter.ui.components.EmptyStateCard
import com.example.pressurecounter.ui.components.PressureCard
import com.example.pressurecounter.ui.components.StatCard
import com.example.pressurecounter.ui.theme.*
import com.example.pressurecounter.ui.viewmodel.MeasurementViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MeasurementViewModel,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestMeasurement by viewModel.latestMeasurement.collectAsState()
    val weekMeasurements by viewModel.weekMeasurements.collectAsState()
    val weekStats by viewModel.weekStatistics.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    
    Scaffold(
        topBar = {
             CenterAlignedTopAppBar(
                 title = { Text("Мое здоровье", fontWeight = FontWeight.Bold) },
                 colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                     containerColor = MaterialTheme.colorScheme.background,
                     titleContentColor = MaterialTheme.colorScheme.primary
                 )
             )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Добавить")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            
            // Welcome / Summary Header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Всего измерений",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "$totalCount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Decorative element or Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // Or Activity icon
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Latest measurement
            SectionHeader(title = "Последнее измерение")
            
            if (latestMeasurement != null) {
                PressureCard(
                    measurement = latestMeasurement!!,
                    isLarge = true
                )
            } else {
                EmptyStateCard(
                    message = "Нет измерений.\nНажмите + чтобы добавить первое!"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Week statistics
            if (weekStats != null && weekStats!!.avgSystolic != null) {
                SectionHeader(title = "Статистика за неделю")
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Ср. Давление",
                        value = "${weekStats!!.avgSystolic?.toInt() ?: "-"}/${weekStats!!.avgDiastolic?.toInt() ?: "-"}",
                        subtitle = "мм рт.ст.",
                        color = ChartSystolic,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        icon = null // Or a gauge icon
                    )
                    StatCard(
                        title = "Ср. Пульс",
                        value = "${weekStats!!.avgPulse?.toInt() ?: "-"}",
                        subtitle = "уд/мин",
                        color = ChartPulse,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        icon = Icons.Default.Favorite
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Week chart
            if (weekMeasurements.isNotEmpty()) {
                SectionHeader(title = "График за неделю")
                WeekChart(measurements = weekMeasurements)
            }
            
            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun WeekChart(
    measurements: List<Measurement>,
    modifier: Modifier = Modifier
) {
    if (measurements.isEmpty()) return
    
    val sortedMeasurements = measurements.sortedBy { it.timestamp }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Систолическое давление
        SingleLineChart(
            measurements = sortedMeasurements,
            title = "Систолическое давление",
            unit = "мм рт.ст.",
            color = ChartSystolic,
            valueExtractor = { it.systolic.toFloat() },
            minY = 80f,
            maxY = 180f
        )
        
        // Диастолическое давление
        SingleLineChart(
            measurements = sortedMeasurements,
            title = "Диастолическое давление",
            unit = "мм рт.ст.",
            color = ChartDiastolic,
            valueExtractor = { it.diastolic.toFloat() },
            minY = 50f,
            maxY = 120f
        )
        
        // Пульс
        SingleLineChart(
            measurements = sortedMeasurements,
            title = "Пульс",
            unit = "уд/мин",
            color = ChartPulse,
            valueExtractor = { it.pulse.toFloat() },
            minY = 50f,
            maxY = 120f
        )
    }
}

@Composable
fun SingleLineChart(
    measurements: List<Measurement>,
    title: String,
    unit: String,
    color: Color,
    valueExtractor: (Measurement) -> Float,
    minY: Float,
    maxY: Float,
    modifier: Modifier = Modifier
) {
    val entries = measurements.mapIndexed { index, m ->
        entryOf(index.toFloat(), valueExtractor(m))
    }
    
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    var modelReady by remember { mutableStateOf(false) }
    LaunchedEffect(entries) {
        modelReady = false
        if (entries.isNotEmpty()) {
            chartEntryModelProducer.setEntries(listOf(entries))
            modelReady = true
        }
    }
    
    val dateFormatter = remember { SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru")) }
    
    val axisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index in measurements.indices) {
            dateFormatter.format(Date(measurements[index].timestamp))
        } else ""
    }
    
    // Динамический расчет min/max для лучшего масштабирования
    val values = measurements.map { valueExtractor(it) }
    val dynamicMinY = (values.minOrNull() ?: minY).let { (it - 10).coerceAtLeast(minY) }
    val dynamicMaxY = (values.maxOrNull() ?: maxY).let { (it + 10).coerceAtMost(maxY) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (entries.isNotEmpty() && modelReady) {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = color,
                                lineThickness = 2.5.dp,
                                point = shapeComponent(
                                    shape = Shapes.pillShape,
                                    color = color
                                ),
                                pointSize = 6.dp,
                                pointConnector = DefaultPointConnector(cubicStrength = 0f) // Прямые линии
                            )
                        ),
                        axisValuesOverrider = AxisValuesOverrider.fixed(
                            minY = dynamicMinY,
                            maxY = dynamicMaxY
                        )
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        guideline = null,
                        itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 5) }
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = axisValueFormatter,
                        guideline = null
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет данных для графика",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
