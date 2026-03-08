package sungbinland.uikit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

public object UiKitColors {
  public val Background: Color = Color(0xFFFAF8F5)
  public val Surface: Color = Color(0xFFFFFFFF)
  public val Border: Color = Color(0xFFE8E5E0)
  public val BorderSoft: Color = Color(0xFFE8E8E8)
  public val BorderStrong: Color = Color(0xFFEFEFEF)
  public val Primary: Color = Color(0xFF1E2432)
  public val Text: Color = Color(0xFF1C1C1C)
  public val MutedText: Color = Color(0xFF8A8A8A)
  public val MutedTextStrong: Color = Color(0xFF666666)
  public val Accent: Color = Color(0xFFE85A4F)
  public val BrandBlue: Color = Color(0xFF1E3A5F)
  public val PositiveSurface: Color = Color(0xFFE8F5E9)
  public val PositiveText: Color = Color(0xFF2E7D32)
  public val ProgressTrack: Color = Color(0xFFF5F4F2)
}

public object UiKitTypography {
  public val Headline: TextStyle = TextStyle(
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 30.sp,
  )
  public val Title: TextStyle = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 24.sp,
  )
  public val TitleLarge: TextStyle = TextStyle(
    fontSize = 22.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 26.sp,
  )
  public val DisplayMetric: TextStyle = TextStyle(
    fontSize = 34.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 36.sp,
  )
  public val Value: TextStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 18.sp,
  )
  public val Label: TextStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp,
  )
  public val Micro: TextStyle = TextStyle(
    fontSize = 10.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 12.sp,
  )
}
