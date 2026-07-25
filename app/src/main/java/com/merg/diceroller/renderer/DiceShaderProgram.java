package com.merg.diceroller.renderer;

import android.opengl.GLES20;

public final class DiceShaderProgram {
    private static final String VERTEX =
            "uniform mat4 uMVP;uniform mat4 uModel;uniform mat4 uNormalMatrix;" +
            "attribute vec3 aPosition;attribute vec3 aNormal;attribute vec2 aTexCoord;" +
            "varying vec3 vNormal;varying vec3 vPosition;varying vec2 vTexCoord;" +
            "void main(){vNormal=normalize((uNormalMatrix*vec4(aNormal,0.0)).xyz);" +
            "vPosition=(uModel*vec4(aPosition,1.0)).xyz;vTexCoord=aTexCoord;gl_Position=uMVP*vec4(aPosition,1.0);}";
    private static final String FRAGMENT =
            "precision mediump float;uniform sampler2D uTexture;uniform vec3 uLightDir;uniform vec3 uViewPos;" +
            "uniform float uAmbient;uniform float uDiffuse;uniform float uSpecular;varying vec3 vNormal;varying vec3 vPosition;varying vec2 vTexCoord;" +
            "void main(){vec4 tex=texture2D(uTexture,vTexCoord);vec3 n=normalize(vNormal);float diff=max(dot(n,normalize(-uLightDir)),0.0);" +
            "vec3 viewDir=normalize(uViewPos-vPosition);vec3 reflectDir=reflect(normalize(uLightDir),n);float spec=pow(max(dot(viewDir,reflectDir),0.0),18.0)*uSpecular;" +
            "vec3 color=tex.rgb*(uAmbient+diff*uDiffuse)+vec3(spec);gl_FragColor=vec4(color,tex.a);}";
    public final int program;
    public final int aPosition, aNormal, aTexCoord, uMVP, uModel, uNormalMatrix, uTexture, uLightDir, uViewPos, uAmbient, uDiffuse, uSpecular;

    public DiceShaderProgram() {
        program = OpenGLUtils.linkProgram(VERTEX, FRAGMENT);
        aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        aNormal = GLES20.glGetAttribLocation(program, "aNormal");
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
        uMVP = GLES20.glGetUniformLocation(program, "uMVP");
        uModel = GLES20.glGetUniformLocation(program, "uModel");
        uNormalMatrix = GLES20.glGetUniformLocation(program, "uNormalMatrix");
        uTexture = GLES20.glGetUniformLocation(program, "uTexture");
        uLightDir = GLES20.glGetUniformLocation(program, "uLightDir");
        uViewPos = GLES20.glGetUniformLocation(program, "uViewPos");
        uAmbient = GLES20.glGetUniformLocation(program, "uAmbient");
        uDiffuse = GLES20.glGetUniformLocation(program, "uDiffuse");
        uSpecular = GLES20.glGetUniformLocation(program, "uSpecular");
    }
}
