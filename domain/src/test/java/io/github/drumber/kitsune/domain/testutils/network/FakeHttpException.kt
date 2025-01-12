package io.github.drumber.kitsune.domain.testutils.network

import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class FakeHttpException(code: Int) : HttpException(
    Response.error<Any>(code, "".toResponseBody())
)