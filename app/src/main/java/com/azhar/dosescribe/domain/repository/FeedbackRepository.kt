package com.azhar.dosescribe.domain.repository

import com.azhar.dosescribe.data.model.Feedback
import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {
    fun submitFeedback(feedback: Feedback): Flow<Result<Unit>>
    fun getAllFeedback(): Flow<Result<List<Feedback>>>
    fun replyToFeedback(feedbackId: String, reply: String): Flow<Result<Unit>>
}

