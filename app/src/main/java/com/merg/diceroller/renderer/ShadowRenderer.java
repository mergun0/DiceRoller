package com.merg.diceroller.renderer;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class ShadowRenderer {
    private static final String VERTEX = "uniform mat4 uMVP;attribute vec3 aPosition;varying vec2 vPos;void main(){vPos=aPosition.xz;gl_Position=uMVP*vec4(aPosition,1.0);}";
    private static final String FRAGMENT = "precision mediump float;uniform float uAlpha;varying vec2 vPos;void main(){float d=length(vPos);float a=smoothstep(1.0,0.05,d)*uAlpha;gl_FragColor=vec4(0.0,0.0,0.0,a);}";
    private final int program, aPosition, uMVP, uAlpha;
    private final FloatBuffer buffer;

    public ShadowRenderer() {
        program = OpenGLUtils.linkProgram(VERTEX, FRAGMENT);
        aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        uMVP = GLES20.glGetUniformLocation(program, "uMVP");
        uAlpha = GLES20.glGetUniformLocation(program, "uAlpha");
        float[] vertices = {-1,0,-1, 1,0,-1, 1,0,1, -1,0,-1, 1,0,1, -1,0,1};
        buffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(vertices).position(0);
    }

    public void draw(float[] mvp, float alpha) {
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
        GLES20.glUniform1f(uAlpha, Math.max(0f, alpha));
        buffer.position(0);
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, buffer);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glDisableVertexAttribArray(aPosition);
    }
}
