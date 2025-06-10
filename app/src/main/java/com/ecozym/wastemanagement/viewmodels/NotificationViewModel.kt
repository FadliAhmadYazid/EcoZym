package com.ecozym.wastemanagement.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecozym.wastemanagement.models.Notification
import com.ecozym.wastemanagement.repositories.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val notificationRepository = NotificationRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _adminNotifications = MutableLiveData<List<Notification>>()
    val adminNotifications: LiveData<List<Notification>> = _adminNotifications

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadNotifications(userId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val notificationList = if (userId != null) {
                    notificationRepository.getUserNotifications(userId)
                } else {
                    notificationRepository.getAllNotifications()
                }
                _notifications.value = notificationList

                // Calculate unread count
                val unreadCount = notificationList.count { !it.isRead }
                _unreadCount.value = unreadCount

            } catch (e: Exception) {
                _notifications.value = emptyList()
                _unreadCount.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAdminNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val notificationList = notificationRepository.getAdminNotifications()
                _adminNotifications.value = notificationList

                // Calculate unread count
                val unreadCount = notificationList.count { !it.isRead }
                _unreadCount.value = unreadCount

            } catch (e: Exception) {
                _adminNotifications.value = emptyList()
                _unreadCount.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecentActivity() {
        viewModelScope.launch {
            try {
                val notificationList = notificationRepository.getRecentActivity(5)
                _notifications.value = notificationList

                // Update unread count for recent activity too
                val unreadCount = notificationList.count { !it.isRead }
                _unreadCount.value = unreadCount

            } catch (e: Exception) {
                _notifications.value = emptyList()
                _unreadCount.value = 0
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)

                // Update the notification in the list
                _notifications.value = _notifications.value?.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true, readAt = com.google.firebase.Timestamp.now())
                    } else {
                        notification
                    }
                }

                // Update admin notifications if needed
                _adminNotifications.value = _adminNotifications.value?.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true, readAt = com.google.firebase.Timestamp.now())
                    } else {
                        notification
                    }
                }

                // Recalculate unread count
                val currentNotifications = _adminNotifications.value ?: _notifications.value ?: emptyList()
                val unreadCount = currentNotifications.count { !it.isRead }
                _unreadCount.value = unreadCount

            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead(userId)
                // Reload notifications
                loadNotifications(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getUnreadNotificationCount(): LiveData<Int> {
        return unreadCount
    }
}