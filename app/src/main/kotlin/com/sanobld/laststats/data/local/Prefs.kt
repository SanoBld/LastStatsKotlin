package com.sanobld.laststats.data.local

/** Core and appearance settings. Service-specific keys live next to their service. */
object Prefs {
    // ── Account / data source ────────────────────────────────────────────────
    val username = StringPref("ls_username")
    val apiKey = StringPref("ls_apikey")
    val lbUsername = StringPref("ls_lb_username")
    val lbToken = StringPref("ls_lb_token")
    val source = StringPref("ls_source")            // 'lfm' | 'lb'
    val cacheSource = StringPref("ls_cache_source")
    val secretKey = StringPref("ls_secret_key")
    val sessionKey = StringPref("ls_session_key")
    val displayName = StringPref("ls_display_name")
    val accounts = StringPref("ls_accounts")        // JSON
    val activeAccount = IntPref("ls_active_account", 0)
    val comparisonsCount = IntPref("ls_comparisons_count", 0)

    // ── Appearance ───────────────────────────────────────────────────────────
    val theme = StringPref("ls_theme", "system")    // system | light | dark
    val themeStyle = StringPref("ls_theme_style", "default")
    val accent = StringPref("ls_accent", "purple")  // named key or #RRGGBB
    val accentDark = StringPref("ls_accent_dark")
    val useDayNightAccent = BoolPref("ls_use_daynight_accent", false)
    val dayNightUseHours = BoolPref("ls_daynight_use_hours", false)
    val dayStartHour = IntPref("ls_daynight_day_start_hour", 7)
    val nightStartHour = IntPref("ls_daynight_night_start_hour", 20)
    val useDynamicColor = BoolPref("ls_use_dynamic_color", false)
    val useNowPlayingColor = BoolPref("ls_use_nowplaying_color", false)
    val nowPlayingFallbackColor = StringPref("ls_nowplaying_fallback_color")
    val lastArtworkColor = StringPref("ls_last_artwork_color")
    val artworkColorTheme = BoolPref("ls_artwork_color_theme", false)
    val keepLastArtworkColor = BoolPref("ls_keep_last_artwork_color", false)
    val oledMode = BoolPref("ls_oled_mode", false)
    val colorHarmony = StringPref("ls_color_harmony", "mono") // mono | classic | vivid | soft | tinted
    val locale = StringPref("ls_locale", "fr")
    val textScale = DoublePref("ls_text_scale", 1.0)
    val appFont = StringPref("ls_app_font", "GoogleSansFlex")
    val customFontB64 = StringPref("ls_custom_font_b64")
    val customFontName = StringPref("ls_custom_font_name")
    val imageShape = StringPref("ls_image_shape", "mix") // mix | square | circle | shape:N
    val navLabels = BoolPref("ls_nav_labels", true)
    val navLayout = StringPref("ls_pc_mode", "auto")     // auto | on (side rail) | off (bottom bar)
    val railCollapsed = BoolPref("ls_rail_collapsed", false)
    val hapticFeedback = BoolPref("ls_haptic_feedback", true)
    val motionBlur = BoolPref("ls_motion_blur", true)
    val livingArtwork = BoolPref("ls_living_artwork", true)
    val startupTab = IntPref("ls_startup_tab", 0)

    // ── Animated covers ──────────────────────────────────────────────────────
    val motionArtwork = BoolPref("ls_motion_artwork", true)
    val dashMotion = BoolPref("ls_dash_motion", true)
    val motionAlbums = BoolPref("ls_motion_albums", true)
    val motionArtists = BoolPref("ls_motion_artists", true)
    val motionTracks = BoolPref("ls_motion_tracks", true)
    val motionQuality = StringPref("ls_motion_quality", "auto")
    val motionSource = StringPref("ls_motion_source", "auto")
    val motionOrder = StringPref("ls_motion_order")
    val motionYtDash = BoolPref("ls_motion_yt_dash", false)
    val imgOrder = StringPref("ls_img_order")

    // ── Library clean-up ─────────────────────────────────────────────────────
    val mergeVersions = BoolPref("ls_merge_versions", false)
    val splitCollabs = BoolPref("ls_split_collabs", false)

    // ── Achievements ─────────────────────────────────────────────────────────
    val achievementsEnabled = BoolPref("ls_achievements_enabled", true)

    // ── Eco mode ─────────────────────────────────────────────────────────────
    val ecoModeManual = BoolPref("ls_eco_mode_manual", false)
    val ecoModeAuto = BoolPref("ls_eco_mode_auto", false)
    val ecoModeThreshold = IntPref("ls_eco_mode_threshold", 20)
    val ecoModeSystem = BoolPref("ls_eco_mode_system", false)

    // ── Dashboard ────────────────────────────────────────────────────────────
    val showNowPlaying = BoolPref("ls_show_nowplay", true)
    val showRecent = BoolPref("ls_show_recent", true)
    val showStats = BoolPref("ls_show_stats", true)
    val showFriends = BoolPref("ls_show_friends", true)
    val showDiscover = BoolPref("ls_show_discover", true)
    val showFavoritesStat = BoolPref("ls_show_favorites", true)
    val showLovedBadge = BoolPref("ls_show_loved_badge", true)
    val showNewsBadge = BoolPref("ls_show_news_badge", true)
    val sectionOrder = ListPref("ls_section_order")
    val statCards = ListPref("ls_stat_cards")
    val favStatMigrated = BoolPref("ls_fav_stat_migrated", false)
    val dashboardChart = StringPref("ls_dashboard_chart", "calendar")
    val infiniteScroll = BoolPref("ls_infinite_scroll", false)
    val discoverSources = ListPref("ls_discover_sources")
    val discoverSolo = ListPref("ls_discover_solo")
    val discoverSmart = BoolPref("ls_discover_smart", false)
    val discoverV2 = BoolPref("ls_discover_v2", false)
    val discoverTaps = StringPref("ls_discover_taps") // JSON

    // ── Dashboard header ─────────────────────────────────────────────────────
    val headerSource = StringPref("ls_header_source", "nowplaying")
    val headerPeriod = StringPref("ls_header_period", "overall")
    val headerAnimation = StringPref("ls_header_animation", "fade")
    val headerBlur = DoublePref("ls_header_blur", 0.0)
    val headerCustomUrl = StringPref("ls_header_custom_url")
    val headerLocalB64 = StringPref("ls_header_local_b64")
    val headerMusicAnim = BoolPref("ls_header_music_anim", false)
    val headerFallbackEnabled = BoolPref("ls_header_fallback_enabled", false)
    val headerFallbackType = StringPref("ls_header_fallback_type", "none")
    val headerFallbackPeriod = StringPref("ls_header_fallback_period", "overall")
    val headerFallbackUrl = StringPref("ls_header_fallback_url")

    // ── Friends, favorites, search ───────────────────────────────────────────
    val favProfiles = ListPref("ls_fav_profiles")
    val favFriends = ListPref("ls_fav_friends")
    val recentItems = ListPref("ls_recent_items")
    val recentSearches = ListPref("ls_recent_searches")
    val musicPlatform = StringPref("ls_music_platform") // comma list of spotify | ytmusic | other | all

    // ── Updates and sync ─────────────────────────────────────────────────────
    val autoUpdateCheck = BoolPref("ls_auto_update_check", true)
    val betaChannel = BoolPref("ls_beta_channel", false)
    val lastUpdateCheck = LongPref("ls_last_update_check", 0L)
    val scrobbleSyncEnabled = BoolPref("ls_scrobble_sync_enabled", false)
    val scrobbleSyncFreqHours = IntPref("ls_scrobble_sync_freq_hours", 6)

    // ── Notifications ────────────────────────────────────────────────────────
    val notifDailyEnabled = BoolPref("ls_notif_daily_enabled", true)
    val notifWeeklyEnabled = BoolPref("ls_notif_weekly_enabled", true)
    val notifMilestoneEnabled = BoolPref("ls_notif_milestone_enabled", true)
    val notifGrandEnabled = BoolPref("ls_notif_grand_enabled", true)
    val notifSyncEnabled = BoolPref("ls_notif_sync_enabled", true)
    val notifSyncProgressDetail = BoolPref("ls_notif_sync_progress_detail", true)
    val notifDownloadEnabled = BoolPref("ls_notif_download_enabled", true)
    val notifNewsEnabled = BoolPref("ls_notif_news_enabled", false)

    // ── Widgets and shares ───────────────────────────────────────────────────
    val widgetBg = StringPref("ls_widget_bg", "poster")
    val widgetTint = BoolPref("ls_widget_tint", false)
    val yearShareProfile = BoolPref("ls_year_share_profile", true)
    val yearSumBg = StringPref("ls_year_sum_bg", "cover")
    val yearBg = StringPref("ls_year_bg")
    val yearHeroBg = StringPref("ls_year_hero_bg")
}
