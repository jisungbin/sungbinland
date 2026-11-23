package sungbinland.workout.domain

internal data class RoutineExercise(
  val targetSets: Int,
  val slotPosition: Int,
) {
  val key: String get() = slotPosition.toString()
}

internal const val SETS_PER_EXERCISE: Int = 4
internal const val EXERCISES_PER_DAY: Int = 4

internal fun dailyExercises(): List<RoutineExercise> =
  List(EXERCISES_PER_DAY) { position ->
    RoutineExercise(
      targetSets = SETS_PER_EXERCISE,
      slotPosition = position,
    )
  }
