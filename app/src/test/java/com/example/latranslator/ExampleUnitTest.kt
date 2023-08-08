package com.example.latranslator

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun lambdaTest() {
        HOFun ({
            println("hoFun1 invoked")
            return@HOFun
        }, {
            println("hoFun2 invoked")
            return@HOFun
        })
    }

    fun HOFun(lambda1: ()-> Unit, lambda2: ()-> Unit) {
        lambda1()
        lambda2()
    }

    fun HOFun2(lambda: ()-> Unit) {
        lambda()
    }
}