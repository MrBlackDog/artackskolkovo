package com.artack2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;

class Icon {
    Vector3 coord;
    Texture texture;
    String id;

    Icon(Vector3 coord,String imagename)
    {
        this.coord = coord;
        texture = new Texture(imagename);
    }

    Icon(float x,float y,float z,String id)
    {
        coord = new Vector3(x,y,z);
        this.id = id;
    }

    public void SetTexture(String imagename){
        texture = new Texture(imagename);
    }
}