package sungbinland.core.workout.dao

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import dev.drewhamilton.poko.Poko
import sungbinland.core.workout.entity.SupplementIntakeEntity
import sungbinland.core.workout.entity.SupplementIntakeItemEntity

@Immutable
@Poko public class SupplementIntakeWithItems internal constructor(
  @Embedded public val intake: SupplementIntakeEntity,
  @Relation(
    parentColumn = "intake_at",
    entityColumn = "intake_at",
  ) public val items: List<SupplementIntakeItemEntity>,
)
