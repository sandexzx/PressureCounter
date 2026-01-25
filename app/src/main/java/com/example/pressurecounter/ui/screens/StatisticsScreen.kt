package com.example.pressurecounter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pressurecounter.data.model.Measurement
import com.example.pressurecounter.data.repository.Statistics
import com.example.pressurecounter.ui.components.StatCard
import com.example.pressurecounter.ui.screens.WeekChart
import com.example.pressurecounter.ui.theme.*
import com.example.pressurecounter.ui.viewmodel.MeasurementViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val weekMeasurements by viewModel.weekMeasurements.collectAsState()
    val monthMeasurements by viewModel.monthMeasurements.collectAsState()
    val weekStats by viewModel.weekStatistics.collectAsState()
    val monthStats by viewModel.monthStatistics.collectAsState()
    val yearStats by viewModel.yearStatistics.collectAsState()
    
    val tabs = listOf("Неделя", "Месяц", "Год")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = pagerState.currentPage
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
            
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> StatisticsPageContent(
                        statistics = weekStats,
                        measurements = weekMeasurements,
                        periodName = "неделю"
                    )
                    1 -> StatisticsPageContent(
                        statistics = monthStats,
                        measurements = monthMeasurements,
                        periodName = "месяц"
                    )
                    2 -> StatisticsPageContent(
                        statistics = yearStats,
                        measurements = emptyList(), // Chart would be too dense for a year
                        periodName = "год"
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsPageContent(
    statistics: Statistics?,
    measurements: List<Measurement>,
    periodName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (statistics == null || statistics.avgSystolic == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📊",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нет данных за $periodName",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Average values
            Text(
                text = "Средние значения",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Систолическое",
                    value = "${statistics.avgSystolic?.toInt() ?: "-"}",
                    subtitle = "мм рт.ст.",
                    color = ChartSystolic,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Диастолическое",
                    value = "${statistics.avgDiastolic?.toInt() ?: "-"}",
                    subtitle = "мм рт.ст.",
                    color = ChartDiastolic,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Пульс",
                    value = "${statistics.avgPulse?.toInt() ?: "-"}",
                    subtitle = "уд/мин",
                    color = ChartPulse,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Min/Max values
            Text(
                text = "Минимум / Максимум",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Систолическое",
                    value = "${statistics.minSystolic ?: "-"} / ${statistics.maxSystolic ?: "-"}",
                    subtitle = "мин / макс",
                    color = ChartSystolic,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Диастолическое",
                    value = "${statistics.minDiastolic ?: "-"} / ${statistics.maxDiastolic ?: "-"}",
                    subtitle = "мин / макс",
                    color = ChartDiastolic,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Пульс",
                    value = "${statistics.minPulse ?: "-"} / ${statistics.maxPulse ?: "-"}",
                    subtitle = "мин / макс",
                    color = ChartPulse,
                    modifier = Modifier.weight(1f)
                )
                
                // Pulse pressure (average)
                val avgPulsePressure = statistics.avgDiastolic?.let { diastolic ->
                    (statistics.avgSystolic - diastolic).toInt()
                }
                
                StatCard(
                    title = "Пульсовое давление",
                    value = "${avgPulsePressure ?: "-"}",
                    subtitle = "среднее",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Chart (only for week and month)
            if (measurements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "График изменений",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                WeekChart(measurements = measurements)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
