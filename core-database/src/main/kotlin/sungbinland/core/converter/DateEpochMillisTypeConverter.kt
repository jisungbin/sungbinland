package sungbinland.core.converter

import androidx.room.TypeConverter
import java.util.Date

internal class DateEpochMillisTypeConverter {
  @TypeConverter internal fun fromEpochMillis(value: Long?): Date? = value?.let(::Date)
  @TypeConverter internal fun toEpochMillis(value: Date?): Long? = value?.time
}
