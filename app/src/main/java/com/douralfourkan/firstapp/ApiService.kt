package com.douralfourkan.firstapp
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET


interface ApiService {
    @POST("/api/sendMessage")
    fun sendMessage(@Body message: Message): Call<ResponseBody>

    @GET("/api/getMessages")
    fun getMessages(): Call<GetMessagesResponse>
}
data class GetMessagesResponse(val message: Message)