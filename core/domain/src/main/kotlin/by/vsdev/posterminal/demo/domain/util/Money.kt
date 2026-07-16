package by.vsdev.posterminal.demo.domain.util

/** Formats a price in minor units (cents) as "$12.34", handling sign and zero-padding. */
fun formatCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val abs = if (cents < 0) -cents else cents
    val dollars = abs / 100
    val rem = (abs % 100).toInt()
    val remStr = if (rem < 10) "0$rem" else "$rem"
    return "$sign$$dollars.$remStr"
}
