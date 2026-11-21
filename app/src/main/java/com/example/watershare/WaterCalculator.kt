package com.example.watershare

import kotlin.math.round

data class TariffSettings(
    val tierBase: Double = 110000.0, val tierHigher: Double = 143000.0,
    val targetAverage: Double = 13.0, val sharedWater: Double = 12.0,
    val billDays: Int = 37, val baseAbonman: Double = 88000.0,
    val budgetBase: Double = 2000.0, val budgetAbove: Double = 4000.0,
    val tierMultiplierBase: Double = 2.0, val tierMultiplierAbove: Double = 2.1,
    val youthCharge: Double = 1000.0, val billTotal: Double = 14954000.0
)
data class UnitInput(val unit: Int, val currentReading: Double, val priorReading: Double, val offsetDays: Int, val initialConsumption: Double)
data class UnitResult(
    val unit: Int, val estimatedReading: Double, val usage: Double, val monthlyAverage: Double,
    val water: Double, val wastewater: Double, val legal: Double, val abonman: Double,
    val tax: Double, val rawAmount: Double, val correctedAmount: Double, val tomanRounded: Double
)
object WaterCalculator {
    fun estimateReading(current: Double, prior: Double, offset: Int, billDays: Int): Double {
        val denom = offset + billDays
        if (denom <= 0) return current
        return round(current - ((current - prior) / denom * offset))
    }
    fun calculate(inputs: List<UnitInput>, s: TariffSettings): List<UnitResult> {
        val preliminary = inputs.map { input ->
            val estimated = estimateReading(input.currentReading, input.priorReading, input.offsetDays, s.billDays)
            val usage = input.initialConsumption + s.sharedWater / 4.0
            val avg = usage / s.billDays * 30.0
            val rate = if (avg <= s.targetAverage) s.tierBase else s.tierHigher
            val f = .01 * rate * avg
            val g = .02 * rate * (avg - s.targetAverage)
            val h = .03 * rate * (avg - s.targetAverage)
            val l = if (avg <= s.targetAverage) f else if (avg <= 3 * s.targetAverage) f + g else f + h
            val multiplier = if (avg <= s.targetAverage) s.tierMultiplierBase else s.tierMultiplierAbove
            val water = l * avg * s.billDays / 30.0 * multiplier
            val sewage = water * .7
            val p = if (avg >= s.targetAverage) avg - s.targetAverage else 0.0
            val q = s.targetAverage * s.billDays / 30.0 * s.budgetBase + p * s.billDays / 30.0 * s.budgetAbove
            val r = if (p != 0.0) usage * s.youthCharge else 0.0
            val legal = q + r
            val v = if (avg <= s.targetAverage) s.baseAbonman else s.baseAbonman * (143000.0 / 110000.0)
            val abonman = 2 * v * s.billDays / 30.0
            val x = abonman + water + sewage + legal
            val tax = (x - legal) * .1
            val total = x + tax
            UnitResult(input.unit, estimated, usage, avg, water, sewage, legal, abonman, tax, total, total, round(total / 10000.0) * 1000.0)
        }
        val zSum = preliminary.sumOf { it.rawAmount }
        val delta = zSum - s.billTotal
        val excesses = preliminary.map { if (it.monthlyAverage >= s.targetAverage) it.monthlyAverage - (preliminary.sumOf { r -> r.usage } / s.billDays * 30.0 / 4.0) else 0.0 }
        val totalExcess = excesses.sum()
        return preliminary.mapIndexed { index, r ->
            val correction = if (excesses[index] != 0.0 && totalExcess != 0.0) delta * excesses[index] / totalExcess else 0.0
            val corrected = r.rawAmount - correction
            r.copy(correctedAmount = corrected, tomanRounded = round(corrected / 10000.0) * 1000.0)
        }
    }
}
