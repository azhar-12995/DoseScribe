package com.azhar.dosescribe.data.util

import com.azhar.dosescribe.data.model.Lesson
import com.google.firebase.firestore.FirebaseFirestore

fun seedLessons(firestore: FirebaseFirestore) {
    val lessons = listOf(
        Lesson(
            title = "Introduction to Jetpack Compose",
            description = "Learn the basics of building UIs with Jetpack Compose.",
            content = "This is the full content of the introduction to Jetpack Compose lesson."
        ),
        Lesson(
            title = "State Management in Compose",
            description = "Understand how to manage state in your Compose apps.",
            content = "This is the full content of the state management in Compose lesson."
        ),
        Lesson(
            title = "Navigation in Compose",
            description = "Learn how to navigate between screens in your Compose app.",
            content = "This is the full content of the navigation in Compose lesson."
        )
    )

    lessons.forEach { lesson ->
        firestore.collection("lessons").add(lesson)
    }
}
