package sungbinland.core.fixture

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import kotlin.random.Random
import sungbinland.core.nutrition.NutritionDatabase
import sungbinland.core.nutrition.entity.BodyInfoEntity
import sungbinland.core.nutrition.entity.EatenFoodEntity
import sungbinland.core.nutrition.entity.FoodEntity
import sungbinland.core.study.StudyDatabase
import sungbinland.core.study.entity.StudyEntryEntity
import sungbinland.core.workout.WorkoutDatabase
import sungbinland.core.workout.entity.SupplementEntity
import sungbinland.core.workout.entity.SupplementIntakeEntity
import sungbinland.core.workout.entity.SupplementIntakeItemEntity
import sungbinland.core.workout.entity.TimerRecordEntity
import sungbinland.core.workout.entity.WorkoutExerciseEntity
import sungbinland.core.workout.entity.WorkoutRoutineEntity
import sungbinland.core.workout.entity.WorkoutSessionEntity

public class DatabaseFixture(
  private val nutritionDatabase: NutritionDatabase,
  private val studyDatabase: StudyDatabase,
  private val workoutDatabase: WorkoutDatabase,
) {
  private val zoneId: ZoneId = ZoneId.systemDefault()
  private val random: Random = Random(seed = 42)
  private val today: LocalDate = LocalDate.now()

  /**
   * @return `true` if fixture data was inserted, `false` if data already existed.
   */
  public suspend fun populate(): Boolean {
    val nutritionInserted = populateNutrition()
    val workoutInserted = populateWorkout()
    val studyInserted = populateStudy()
    return nutritionInserted || workoutInserted || studyInserted
  }

  private suspend fun populateNutrition(): Boolean {
    val foodDao = nutritionDatabase.foodDao()
    val eatenFoodDao = nutritionDatabase.eatenFoodDao()
    val bodyInfoDao = nutritionDatabase.bodyInfoDao()

    if (foodDao.getAllFoods().isNotEmpty()) return false

    val foods = listOf(
      FoodEntity(name = "현미밥", calories = 250, carbohydrateGrams = 55, proteinGrams = 5),
      FoodEntity(name = "닭가슴살", calories = 165, carbohydrateGrams = 0, proteinGrams = 31),
      FoodEntity(name = "삶은 계란", calories = 78, carbohydrateGrams = 1, proteinGrams = 6),
      FoodEntity(name = "샐러드", calories = 120, carbohydrateGrams = 15, proteinGrams = 3),
      FoodEntity(name = "고구마", calories = 130, carbohydrateGrams = 30, proteinGrams = 2),
      FoodEntity(name = "바나나", calories = 105, carbohydrateGrams = 27, proteinGrams = 1),
      FoodEntity(name = "프로틴 쉐이크", calories = 200, carbohydrateGrams = 8, proteinGrams = 30),
      FoodEntity(name = "김치찌개", calories = 180, carbohydrateGrams = 12, proteinGrams = 15),
      FoodEntity(name = "불고기", calories = 250, carbohydrateGrams = 10, proteinGrams = 25),
      FoodEntity(name = "된장국", calories = 80, carbohydrateGrams = 8, proteinGrams = 6),
      FoodEntity(name = "두부", calories = 76, carbohydrateGrams = 2, proteinGrams = 8),
      FoodEntity(name = "오트밀", calories = 150, carbohydrateGrams = 27, proteinGrams = 5),
    )
    foods.forEach { food -> foodDao.upsertFood(food) }

    val breakfastFoods = listOf("현미밥", "삶은 계란", "오트밀", "바나나", "된장국")
    val lunchFoods = listOf("현미밥", "닭가슴살", "김치찌개", "불고기", "샐러드", "두부")
    val dinnerFoods = listOf("현미밥", "불고기", "김치찌개", "닭가슴살", "된장국", "두부")
    val snackFoods = listOf("고구마", "바나나", "프로틴 쉐이크", "삶은 계란")

    var weightKg = 73.0

    for (dayOffset in 60L downTo 0L) {
      val date = today.minusDays(dayOffset)

      weightKg += random.nextDouble(-0.5, 0.4)
      weightKg = weightKg.coerceIn(68.0, 76.0)
      bodyInfoDao.upsertBodyInfo(
        BodyInfoEntity(
          recordedAt = date.toDate(LocalTime.of(7, 0)),
          bodyWeightKg = weightKg.toInt(),
        ),
      )

      pickMeal(breakfastFoods, count = 2).forEach { foodName ->
        eatenFoodDao.upsertEatenFood(
          EatenFoodEntity(
            foodName = foodName,
            quantity = 1,
            consumedAt = date.toDate(LocalTime.of(7, random.nextInt(0, 50))),
          ),
        )
      }

      pickMeal(lunchFoods, count = 2).forEach { foodName ->
        eatenFoodDao.upsertEatenFood(
          EatenFoodEntity(
            foodName = foodName,
            quantity = 1,
            consumedAt = date.toDate(LocalTime.of(12, random.nextInt(0, 50))),
          ),
        )
      }

      pickMeal(dinnerFoods, count = 2).forEach { foodName ->
        eatenFoodDao.upsertEatenFood(
          EatenFoodEntity(
            foodName = foodName,
            quantity = 1,
            consumedAt = date.toDate(LocalTime.of(18, random.nextInt(0, 50))),
          ),
        )
      }

      if (random.nextFloat() < 0.6f) {
        val snack = snackFoods[random.nextInt(snackFoods.size)]
        eatenFoodDao.upsertEatenFood(
          EatenFoodEntity(
            foodName = snack,
            quantity = 1,
            consumedAt = date.toDate(LocalTime.of(15, random.nextInt(0, 50))),
          ),
        )
      }
    }
    return true
  }

  private suspend fun populateWorkout(): Boolean {
    val supplementDao = workoutDatabase.supplementDao()
    val supplementIntakeDao = workoutDatabase.supplementIntakeDao()
    val timerRecordDao = workoutDatabase.timerRecordDao()
    val workoutSessionDao = workoutDatabase.workoutSessionDao()
    val routineDao = workoutDatabase.workoutRoutineDao()
    val exerciseDao = workoutDatabase.workoutExerciseDao()

    val now = java.util.Date()
    val dayAgo = java.util.Date(now.time - 86_400_000L)
    if (workoutSessionDao.getWorkoutSessionsByDate(startOfDay = dayAgo, endOfDayExclusive = now).isNotEmpty()) return false

    val routines = mapOf(
      "상체" to listOf("벤치프레스", "오버헤드프레스", "풀업", "덤벨 플라이", "바벨 로우"),
      "하체" to listOf("스쿼트", "데드리프트", "레그프레스", "런지", "레그컬"),
    )
    routines.forEach { (routineName, exercises) ->
      routineDao.upsertWorkoutRoutine(WorkoutRoutineEntity(name = routineName))
      exercises.forEach { exerciseName ->
        exerciseDao.upsertWorkoutExercise(
          WorkoutExerciseEntity(name = exerciseName, routineName = routineName),
        )
      }
    }

    val supplements = listOf("크레아틴", "비타민D", "오메가3", "아르기닌")
    supplements.forEach { name -> supplementDao.upsertSupplement(SupplementEntity(name = name)) }

    val routineNames = routines.keys.toList()
    var routineIndex = 0

    for (dayOffset in 60L downTo 0L) {
      val date = today.minusDays(dayOffset)
      val dayOfWeek = date.dayOfWeek.value

      val isRestDay = dayOfWeek == 7 || (dayOfWeek == 6 && random.nextFloat() < 0.5f)
      if (isRestDay) continue

      val routineName = routineNames[routineIndex % routineNames.size]
      val exercises = routines.getValue(routineName)
      val mainExercise = exercises[random.nextInt(exercises.size)]
      routineIndex++

      workoutSessionDao.upsertWorkoutSession(
        WorkoutSessionEntity(
          routineName = routineName,
          mainExerciseName = mainExercise,
          performedAt = date.toDate(LocalTime.of(6, 30)),
        ),
      )

      val timerCount = random.nextInt(3, 6)
      var timerMinute = 30
      for (i in 0 until timerCount) {
        timerRecordDao.upsertTimerRecord(
          TimerRecordEntity(
            startedAt = date.toDate(LocalTime.of(6, timerMinute)),
          ),
        )
        timerMinute += random.nextInt(5, 15)
        if (timerMinute >= 60) {
          timerMinute -= 60
        }
      }

      val intakeAt = date.toDate(LocalTime.of(7, 0))
      val intakeCount = random.nextInt(2, 5).coerceAtMost(supplements.size)
      val todaySupplements = supplements.shuffled(random).take(intakeCount).sorted()

      supplementIntakeDao.upsertIntake(
        intake = SupplementIntakeEntity(intakeAt = intakeAt),
        items = todaySupplements.map { name ->
          SupplementIntakeItemEntity(intakeAt = intakeAt, supplementName = name)
        },
      )
    }
    return true
  }

  private suspend fun populateStudy(): Boolean {
    val dao = studyDatabase.studyEntryDao()

    if (dao.getAllStudyEntries().isNotEmpty()) return false

    val entries = listOf(
      StudyEntryEntity(
        category = "Kotlin",
        name = "코루틴 구조적 동시성",
        content = "SupervisorJob을 사용하면 자식 코루틴 실패가 부모에게 전파되지 않는다.",
      ),
      StudyEntryEntity(
        category = "Kotlin",
        name = "Flow 냉/온 스트림",
        content = "Flow는 cold stream이고 SharedFlow/StateFlow는 hot stream이다.",
      ),
      StudyEntryEntity(
        category = "Kotlin",
        name = "inline 함수",
        content = "inline 함수는 호출 지점에 바이트코드가 인라인되어 람다 객체 할당을 피한다.",
      ),
      StudyEntryEntity(
        category = "Compose",
        name = "리컴포지션 스킵",
        content = "모든 매개변수가 stable이고 이전과 동일하면 리컴포지션을 스킵한다.",
      ),
      StudyEntryEntity(
        category = "Compose",
        name = "remember vs rememberSaveable",
        content = "remember는 컴포지션 수명, rememberSaveable은 구성 변경을 넘어 유지된다.",
      ),
      StudyEntryEntity(
        category = "Compose",
        name = "derivedStateOf",
        content = "다른 State에서 파생된 값을 계산할 때 불필요한 리컴포지션을 방지한다.",
      ),
      StudyEntryEntity(
        category = "Architecture",
        name = "단방향 데이터 흐름",
        content = "State는 위에서 아래로, Event는 아래에서 위로 흐르는 패턴.",
      ),
      StudyEntryEntity(
        category = "Architecture",
        name = "Molecule",
        content = "Compose 런타임으로 비즈니스 로직을 작성하여 StateFlow를 생성하는 라이브러리.",
      ),
      StudyEntryEntity(
        category = "Room",
        name = "Upsert 동작",
        content = "PrimaryKey 충돌 시 UPDATE, 없으면 INSERT를 수행하는 편의 어노테이션.",
      ),
      StudyEntryEntity(
        category = "Room",
        name = "TypeConverter",
        content = "Room이 지원하지 않는 타입을 저장 가능한 타입으로 변환하는 어댑터.",
      ),
    )
    entries.forEach { entry -> dao.upsertStudyEntry(entry) }
    return true
  }

  private fun pickMeal(pool: List<String>, count: Int): List<String> =
    pool.shuffled(random).take(count)

  private fun LocalDate.toDate(time: LocalTime): Date =
    Date.from(atTime(time).atZone(zoneId).toInstant())
}
