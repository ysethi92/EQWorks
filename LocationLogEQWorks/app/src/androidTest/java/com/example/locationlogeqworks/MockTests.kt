package com.example.locationlogeqworks

import android.util.Log
import com.example.eqworkslocationlibrary.Library
import com.example.eqworkslocationlibrary.LocationEvent
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.random.Random

@RunWith(JUnit4::class)
class MockTests {

    @Test
    fun test1() = runBlocking{
        val res = Library().log(getMockLocation(true))

        Log.d("Response", res.body()?.data.toString())
        assert(res.body()?.data?.isEmpty() == false)
    }

    @Test
    fun test2() = runBlocking {
        val res = Library().log(getMockLocation(false))

        Log.d("Response", res.body()?.data.toString())
        assert(res.body()?.data?.isEmpty() == false)
    }


    private fun getMockLocation(provideTimeStamp: Boolean): LocationEvent {
        if (provideTimeStamp) return LocationEvent(getRandom(-100, 100), getRandom(-200, 200), 1001)
        return LocationEvent(getRandom(-100, 100), getRandom(-200, 200))
    }

    private fun getRandom(min: Int, max: Int): Float {
        require(min < max) { "Invalid range [$min, $max]" }
        return (min + Random.nextFloat() * (max - min))
    }
}