package com.ecozym.wastemanagement.utils

object SignUpData {
    var email: String = ""
    var password: String = ""
    var companyName: String = ""
    var industryType: String = ""
    var address: String = ""
    var phoneNumber: String = ""
    var documentUrl: String = ""

    fun clear() {
        email = ""
        password = ""
        companyName = ""
        industryType = ""
        address = ""
        phoneNumber = ""
        documentUrl = ""
    }

    fun isValid(): Boolean {
        return email.isNotEmpty() &&
                password.isNotEmpty() &&
                companyName.isNotEmpty() &&
                industryType.isNotEmpty() &&
                address.isNotEmpty() &&
                phoneNumber.isNotEmpty()
    }
}