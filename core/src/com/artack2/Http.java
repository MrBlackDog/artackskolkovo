package com.artack2;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Http {

    JsonReader run(String url) throws IOException {
    //String run(String url, Callback callback)  {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url("ws://46.138.43.20:533/ws")
                .build();

        Response response = client.newCall(request).execute();
        //Если не заробит
        //Response response = client.newCall(request).execute();

        // try( Response response = client.newCall(request).execute()) ;
        return new JsonReader();
    }

    public Http() throws IOException {
    }
    // GET SERVER_URL/venue/getAll?sublocationId={sublocationId}&userHash={userHash}&format={format}
}
