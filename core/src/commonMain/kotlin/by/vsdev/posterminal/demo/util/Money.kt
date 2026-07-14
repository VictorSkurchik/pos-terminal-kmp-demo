package by.vsdev.posterminal.demo.util

/** Formats a price in cents as "$12.34". Shared utility for the Android UI and the web admin. */
fun formatCents(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val abs = if (cents < 0) -cents else cents
    val dollars = abs / 100
    val rem = (abs % 100).toInt()
    val remStr = if (rem < 10) "0$rem" else "$rem"
    return "$sign$$dollars.$remStr"
}
