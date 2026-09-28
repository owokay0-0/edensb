package dev.eden.client

enum class EdenHudWidget(
	val displayName: String,
	val configPrefix: String,
) {
	PET("Pet Level", "Visuals.Pet Overlay"),
	PRESSURE("Pressure Display", "Visuals.Pressure Display"),
	DRILL_FUEL("Drill Fuel Meter", "Visuals.Drill Fuel Meter"),
	DUNGEON_SCORE("Dungeon Score Meter", "Dungeons.Dungeon Score Meter"),
	SHITTER_ALERT("Shitter Alert", "Dungeons.Shitter Alert"),
	PERFORMANCE("Performance HUD", "Visuals.Performance HUD"),
}
