package com.dave.soul.exchange_app.util

import org.joda.time.DateTime

class AdChecker(private val mCommonPref: CommonSharedPref) {

    fun showAdCheck(showAdListener: ()-> Unit) {
        if (isShowCount()) {
            showAdListener()
            resetCount()
            return
        }
        increaseCount()
    }

    private fun increaseCount() {
        mCommonPref.add(Global.EXTRA_DETAIL_COUNT, mCommonPref.countDetailView + 1)
    }

    private fun resetCount() {
        mCommonPref.add(Global.EXTRA_DETAIL_COUNT, 0)
    }

    private fun isShowCount(): Boolean = mCommonPref.countDetailView >= MAX_COUNT

    fun clickedAd() {
        mCommonPref.add(Global.EXTRA_CLICKED_AD_DATE, DateTime.now().toString(DateUtil.DATE_PATTERN_YEAR_MONTH_DAY_ADD_DASH))
    }

    fun isClickedAd(): Boolean {
        val isEmptyDate = mCommonPref.clickedAdDate.isNullOrEmpty()
        val clickedDate =  mCommonPref.clickedAdDate
        val currentDate = DateTime.now().toString(DateUtil.DATE_PATTERN_YEAR_MONTH_DAY_ADD_DASH)

        if (isEmptyDate) {
            return false
        }

        if (clickedDate != currentDate) {
            return false
        }

        return true
    }

    companion object {
        private const val MAX_COUNT = 4
        private const val DATE_FORMAT = ""
    }
}
