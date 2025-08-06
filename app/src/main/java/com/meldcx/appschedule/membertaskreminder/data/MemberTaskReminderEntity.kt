package com.meldcx.appschedule.membertaskreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity("reminder_table")
data class MemberTaskReminderEntity (
    @PrimaryKey val id: UUID,
    val memberId: Long,
    val branchId: Long,
    val poId: Long,//
    val projectId: Long,
    val memberName: String,// member name
    val voId: Long, // nno
    val reminderReason: String,
    val reminderImageFilePath: String,
    val reminderAudioFileUri: String,
    val systemReminderUUID: String,
    val isSystemReminderTriggered: Boolean,
    val reminderTriggerAt: Date?,
    val reminderCreationTime: Date?,
) : UpdatableEntity()