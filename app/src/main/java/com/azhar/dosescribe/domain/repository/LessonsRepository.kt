package com.azhar.dosescribe.domain.repository

import com.azhar.dosescribe.data.model.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonsRepository {
    fun getLessons(): Flow<Result<List<Lesson>>>
    fun getLessonById(lessonId: String): Flow<Result<Lesson>>
    fun markLessonAsCompleted(lessonId: String): Flow<Result<Unit>>
    fun isLessonCompleted(lessonId: String): Flow<Result<Boolean>>
}