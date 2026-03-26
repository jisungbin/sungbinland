package sungbinland.muscle

import androidx.annotation.DrawableRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal enum class MuscleCategory(val label: String) {
  ALL("전체"),
  BACK("등"),
  SHOULDER("어깨"),
  CHEST("가슴"),
  ARM("팔"),
  CORE("코어"),
  HIP("골반 / 엉덩이"),
  THIGH("허벅지"),
  CALF("종아리"),
}

internal class MuscleItem(
  val name: String,
  val detail: String,
  val role: String,
  val category: MuscleCategory,
  @param:DrawableRes val imageRes: Int,
)

internal val AllMuscles: ImmutableList<MuscleItem> = persistentListOf(
  // 등
  MuscleItem(
    name = "승모근",
    detail = "상부 / 중부 / 하부",
    role = "어깨를 으쓱하고, 견갑골을 모으거나 내리는 근육",
    category = MuscleCategory.BACK,
    imageRes = R.drawable.muscle_trapezius,
  ),
  MuscleItem(
    name = "능형근",
    detail = "기시점 / 정지점 / 힘선",
    role = "견갑골을 척추 쪽으로 당겨 모아주는 근육",
    category = MuscleCategory.BACK,
    imageRes = R.drawable.muscle_rhomboid,
  ),
  MuscleItem(
    name = "광배근",
    detail = "Latissimus Dorsi + 흉요근막",
    role = "팔을 아래·뒤로 당기는 등에서 가장 넓은 근육",
    category = MuscleCategory.BACK,
    imageRes = R.drawable.muscle_latissimus,
  ),
  MuscleItem(
    name = "척추기립근",
    detail = "3가지 뷰",
    role = "척추를 세우고 상체를 뒤로 젖히는 근육",
    category = MuscleCategory.BACK,
    imageRes = R.drawable.muscle_erector_spinae,
  ),
  // 어깨
  MuscleItem(
    name = "삼각근",
    detail = "전면 / 측면 / 후면",
    role = "팔을 앞·옆·뒤로 들어올리는 어깨 겉면의 근육",
    category = MuscleCategory.SHOULDER,
    imageRes = R.drawable.muscle_deltoid,
  ),
  MuscleItem(
    name = "견갑골",
    detail = "어깨 / 쇄골 / 상완골 / 견갑골",
    role = "어깨 관절의 뼈대를 이루는 날개뼈 구조",
    category = MuscleCategory.SHOULDER,
    imageRes = R.drawable.muscle_scapula,
  ),
  MuscleItem(
    name = "회전근개",
    detail = "극상근 / 극하근 / 소원근 / 견갑하근",
    role = "어깨 관절을 감싸며 팔을 돌리고 안정시키는 4개 근육",
    category = MuscleCategory.SHOULDER,
    imageRes = R.drawable.muscle_rotator_cuff,
  ),
  MuscleItem(
    name = "소원근 & 대원근",
    detail = "후면 비교",
    role = "팔을 몸쪽으로 당기고 안쪽으로 회전시키는 어깨 뒤쪽 근육",
    category = MuscleCategory.SHOULDER,
    imageRes = R.drawable.muscle_teres,
  ),
  // 가슴
  MuscleItem(
    name = "소흉근 & 전거근",
    detail = "Pectoralis minor / Serratus anterior",
    role = "견갑골을 앞으로 밀고 내리며 호흡을 보조하는 가슴 심부 근육",
    category = MuscleCategory.CHEST,
    imageRes = R.drawable.muscle_pectoralis_minor,
  ),
  // 팔
  MuscleItem(
    name = "상완이두근",
    detail = "장두 / 단두 + 상완요골근 / 전완근",
    role = "팔꿈치를 굽히고 전완을 바깥으로 회전시키는 팔 앞쪽 근육",
    category = MuscleCategory.ARM,
    imageRes = R.drawable.muscle_biceps,
  ),
  MuscleItem(
    name = "상완삼두근",
    detail = "장두 / 외측두 / 내측두",
    role = "팔꿈치를 펴는 팔 뒤쪽 근육",
    category = MuscleCategory.ARM,
    imageRes = R.drawable.muscle_triceps,
  ),
  // 코어
  MuscleItem(
    name = "코어 근육",
    detail = "전면 + 후면 뷰",
    role = "몸통을 안정시키고 상·하체 힘 전달을 연결하는 근육군",
    category = MuscleCategory.CORE,
    imageRes = R.drawable.muscle_core,
  ),
  MuscleItem(
    name = "복근",
    detail = "횡복근 / 외복사근 / 내복사근 / 복직근",
    role = "몸통을 굽히고 회전시키며 복압을 유지하는 배 앞쪽 근육",
    category = MuscleCategory.CORE,
    imageRes = R.drawable.muscle_abdominals,
  ),
  // 골반 / 엉덩이
  MuscleItem(
    name = "골반뼈",
    detail = "고관절 / 치골 / 대퇴골",
    role = "상체와 하체를 연결하고 내장을 보호하는 뼈 구조",
    category = MuscleCategory.HIP,
    imageRes = R.drawable.muscle_pelvis,
  ),
  MuscleItem(
    name = "둔근",
    detail = "대둔근 / 중둔근 / 소둔근",
    role = "고관절을 펴고 벌리며 걸을 때 골반을 안정시키는 엉덩이 근육",
    category = MuscleCategory.HIP,
    imageRes = R.drawable.muscle_glutes,
  ),
  // 허벅지
  MuscleItem(
    name = "내전근",
    detail = "치골근 / 장내전근 / 단내전근 / 대내전근 / 박근",
    role = "다리를 안쪽으로 모으는 허벅지 안쪽 근육",
    category = MuscleCategory.THIGH,
    imageRes = R.drawable.muscle_adductor,
  ),
  MuscleItem(
    name = "대퇴사두근",
    detail = "봉근 / 중간광근 / 대퇴직근 / 외측광근 / 내측광근",
    role = "무릎을 펴고 걷기·달리기·점프 동력을 만드는 허벅지 앞쪽 근육",
    category = MuscleCategory.THIGH,
    imageRes = R.drawable.muscle_quadriceps,
  ),
  MuscleItem(
    name = "허벅지 전체",
    detail = "대퇴사두근 / 햄스트링 / 내전근",
    role = "앞쪽(펴기)·뒤쪽(굽히기)·안쪽(모으기)을 담당하는 허벅지 3면 구성",
    category = MuscleCategory.THIGH,
    imageRes = R.drawable.muscle_thigh,
  ),
  // 종아리
  MuscleItem(
    name = "종아리 상세",
    detail = "비복근 / 가자미근 / 경골 + 건",
    role = "발목을 아래로 굽혀 걷기·점프 시 지면을 미는 하퇴 후면 근육",
    category = MuscleCategory.CALF,
    imageRes = R.drawable.muscle_calf_detail,
  ),
  MuscleItem(
    name = "종아리",
    detail = "비복근 / 가자미근",
    role = "발뒤꿈치를 들어올리고 서 있을 때 균형을 잡는 근육",
    category = MuscleCategory.CALF,
    imageRes = R.drawable.muscle_calf,
  ),
)
