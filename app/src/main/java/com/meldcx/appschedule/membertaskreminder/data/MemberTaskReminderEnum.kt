package com.meldcx.appschedule.membertaskreminder.data

enum class MemberTaskReminderEnum(val fileType: String) {
    AUDIO_FILE("audio_file"),
    IMAGE_FILE("image_file"),
    REMINDER_DETAILS("reminder_details"),
    DELETE_REMINDER("delete_reminder")
}