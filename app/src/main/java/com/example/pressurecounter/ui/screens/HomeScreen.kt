package com.example.pressurecounter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.legend.legendItem
import com.patrykandpatrick.vico.compose.legend.verticalLegend
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, "Добавить") },
                text = { Text("Добавить") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Мониторинг давления",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Всего измерений: $totalCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Latest measurement
            Text(
                text = "Последнее измерение",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                Text(
                    text = "Статистика за неделю",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Среднее давление",
                        value = "${weekStats!!.avgSystolic?.toInt() ?: "-"}/${weekStats!!.avgDiastolic?.toInt() ?: "-"}",
                        subtitle = "мм рт.ст.",
                        color = ChartSystolic,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Средний пульс",
                        value = "${weekStats!!.avgPulse?.toInt() ?: "-"}",
                        subtitle = "уд/мин",
                        color = ChartPulse,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Week chart
            if (weekMeasurements.isNotEmpty()) {
                Text(
                    text = "График за неделю",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                WeekChart(measurements = weekMeasurements)
            }
            
            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun WeekChart(
    measurements: List<Measurement>,
    modifier: Modifier = Modifier
) {
    if (measurements.isEmpty()) return
    
    val sortedMeasurements = measurements.sortedBy { it.timestamp }
    
    val systolicEntries = sortedMeasurements.mapIndexed { index, m ->
        entryOf(index.toFloat(), m.systolic.toFloat())
    }
    val diastolicEntries = sortedMeasurements.mapIndexed { index, m ->
        entryOf(index.toFloat(), m.diastolic.toFloat())
    }
    val pulseEntries = sortedMeasurements.mapIndexed { index, m ->
        entryOf(index.toFloat(), m.pulse.toFloat())
    }
    
    val chartEntryModelProducer = remember(measurements) {
        ChartEntryModelProducer(systolicEntries, diastolicEntries, pulseEntries)
    }
    
    val dateFormatter = remember { SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru")) }
    
    val axisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index in sortedMeasurements.indices) {
            dateFormatter.format(Date(sortedMeasurements[index].timestamp))
        } else ""
    }
    
    val legend = verticalLegend(
        items = listOf(
            legendItem(
                icon = shapeComponent(shape = Shapes.pillShape, color = ChartSystolic),
                label = textComponent(color = MaterialTheme.colorScheme.onSurface),
                labelText = "Систолическое"
            ),
            legendItem(
                icon = shapeComponent(shape = Shapes.pillShape, color = ChartDiastolic),
                label = textComponent(color = MaterialTheme.colorScheme.onSurface),
                labelText = "Диастолическое"
            ),
            legendItem(
                icon = shapeComponent(shape = Shapes.pillShape, color = ChartPulse),
                label = textComponent(color = MaterialTheme.colorScheme.onSurface),
                labelText = "Пульс"
            )
        ),
        iconSize = 8.dp,
        iconPadding = 8.dp,
        spacing = 4.dp,
        padding = dimensionsOf(top = 8.dp)
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Chart(
                chart = lineChart(
                    lines = listOf(
                        lineSpec(lineColor = ChartSystolic, lineThickness = 2.dp),
                        lineSpec(lineColor = ChartDiastolic, lineThickness = 2.dp),
                        lineSpec(lineColor = ChartPulse, lineThickness = 2.dp)
                    ),
                    axisValuesOverrider = AxisValuesOverrider.fixed(
                        minY = 40f,
                        maxY = 200f
                    )
                ),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(valueFormatter = axisValueFormatter),
                legend = legend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
