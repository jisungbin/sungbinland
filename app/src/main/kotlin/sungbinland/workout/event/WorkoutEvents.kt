package sungbinland.workout.event

// 오늘 루틴의 종목별 완료 세트 수. todayExercises와 같은 순서.
internal data class SetCountsChanged(val counts: List<Int>) : Event

// 0L이면 오늘 아직 세트를 하나도 완료하지 않은 상태.
internal data class FirstSetChanged(val epochMillis: Long) : Event
