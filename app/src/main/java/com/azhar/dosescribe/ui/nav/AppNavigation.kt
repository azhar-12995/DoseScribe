package com.azhar.dosescribe.ui.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azhar.dosescribe.ui.feature.admin.*
import com.azhar.dosescribe.ui.feature.auth.forgotpassword.ForgotPasswordScreen
import com.azhar.dosescribe.ui.feature.auth.signin.SignInScreen
import com.azhar.dosescribe.ui.feature.auth.signup.SignUpScreen
import com.azhar.dosescribe.ui.feature.dashboard.DashboardScreen
import com.azhar.dosescribe.ui.feature.lessons.*
import com.azhar.dosescribe.ui.feature.menu.*
import com.azhar.dosescribe.ui.feature.notifications.NotificationsScreen
import com.azhar.dosescribe.ui.feature.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedProgressVm: LessonProgressViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("signin") {
            SignInScreen(navController = navController)
        }
        composable("signup") {
            SignUpScreen(navController = navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController, progressVm = sharedProgressVm)
        }

        // All lessons list
        composable("lessons") {
            LessonsScreen(navController = navController, progressVm = sharedProgressVm)
        }

        // Lesson steps screen
        composable("lesson_steps/{moduleId}") { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            LessonStepsScreen(navController = navController, moduleId = moduleId, progressVm = sharedProgressVm)
        }

        // Individual step screen
        composable("lesson_step/{moduleId}/{stepIndex}") { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            val stepIndex = backStackEntry.arguments?.getString("stepIndex")?.toIntOrNull() ?: 0
            when (stepIndex) {
                0 -> PreQuestionnaireScreen(navController = navController, moduleId = moduleId, progressVm = sharedProgressVm)
                1 -> LearningModuleScreen(navController = navController, moduleId = moduleId, progressVm = sharedProgressVm)
                2 -> SimulationScreen(navController = navController, moduleId = moduleId, progressVm = sharedProgressVm)
                3 -> PostQuestionnaireScreen(navController = navController, moduleId = moduleId, progressVm = sharedProgressVm)
                4 -> ResultsScreen(navController = navController, moduleId = moduleId, progressVm = sharedProgressVm)
            }
        }

        // Side menu screens
        composable("support") { SupportScreen(navController = navController) }
        composable("feedback") { SupportScreen(navController = navController) }
        composable("certificates") { CertificatesScreen(navController = navController, progressVm = sharedProgressVm) }
        composable("account") { AccountScreen(navController = navController) }
        composable("change_password") { ChangePasswordScreen(navController = navController) }
        composable("settings") { SettingsScreen(navController = navController) }
        composable("progress") { ProgressScreen(navController = navController, progressVm = sharedProgressVm) }

        // Notifications
        composable("notifications") { NotificationsScreen(navController = navController) }

        // ── Admin routes ──
        composable("admin_dashboard") { AdminDashboardScreen(navController = navController) }
        composable("admin_users") { AdminUsersScreen(navController = navController) }
        composable("admin_user_detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AdminUserDetailScreen(navController = navController, userId = userId)
        }
        composable("admin_banners") { AdminBannersScreen(navController = navController) }
        composable("admin_notifications") { AdminNotificationsScreen(navController = navController) }
        composable("admin_sent_notifications") { AdminSentNotificationsScreen(navController = navController) }
        composable("admin_feedback") { AdminFeedbackScreen(navController = navController) }
        composable("admin_analytics") { AdminAnalyticsScreen(navController = navController) }
    }
}
