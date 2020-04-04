package amirz.dngprocessor.pipeline.post;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

import amirz.dngprocessor.R;
import amirz.dngprocessor.gl.GLPrograms;
import amirz.dngprocessor.gl.Texture;
import amirz.dngprocessor.params.ProcessParams;
import amirz.dngprocessor.params.SensorParams;
import amirz.dngprocessor.pipeline.Stage;
import amirz.dngprocessor.pipeline.StagePipeline;
import amirz.dngprocessor.pipeline.intermediate.NoiseMap;

import static amirz.dngprocessor.colorspace.ColorspaceConstants.CUSTOM_ACR3_TONEMAP_CURVE_COEFFS;
import static android.opengl.GLES20.*;

public class ToneMap extends Stage {
    private static final String TAG = "ToneMap";

    private final int[] mFbo = new int[1];
    private final SensorParams mSensorParams;
    private final ProcessParams mProcessParams;
    private final float[] mXYZtoProPhoto, mProPhotoToSRGB;

    private final int ditherSize = 128;
    private final byte[] dither = new byte[ditherSize * ditherSize * 2];

    public ToneMap(SensorParams sensor, ProcessParams process,
                   float[] XYZtoProPhoto, float[] proPhotoToSRGB) {
        mSensorParams = sensor;
        mProcessParams = process;
        mXYZtoProPhoto = XYZtoProPhoto;
        mProPhotoToSRGB = proPhotoToSRGB;
    }

    @Override
    public void init(GLPrograms converter) {
        super.init(converter);
        glGetIntegerv(GL_FRAMEBUFFER_BINDING, mFbo, 0);
    }

    @Override
    protected void execute(StagePipeline.StageMap previousStages) {
        GLPrograms converter = getConverter();

        glBindFramebuffer(GL_FRAMEBUFFER, mFbo[0]);

        // Load intermediate buffers as textures
        Texture intermediate = previousStages.getStage(NoiseReduce.class).getDenoised();
        intermediate.bind(GL_TEXTURE0);

        converter.seti("intermediateBuffer", 0);
        converter.seti("intermediateWidth", intermediate.getWidth());
        converter.seti("intermediateHeight", intermediate.getHeight());

        if (mProcessParams.lce) {
            BlurLCE blur = previousStages.getStage(BlurLCE.class);
            blur.getWeakBlur().bind(GL_TEXTURE2);
            converter.seti("weakBlur", 2);
            blur.getMediumBlur().bind(GL_TEXTURE4);
            converter.seti("mediumBlur", 4);
            blur.getStrongBlur().bind(GL_TEXTURE6);
            converter.seti("strongBlur", 6);
        }

        float satLimit = mProcessParams.satLimit;
        Log.d(TAG, "Saturation limit " + satLimit);
        converter.setf("satLimit", satLimit);

        converter.setf("toneMapCoeffs", CUSTOM_ACR3_TONEMAP_CURVE_COEFFS);
        converter.setf("XYZtoProPhoto", mXYZtoProPhoto);
        converter.setf("proPhotoToSRGB", mProPhotoToSRGB);
        converter.seti("outOffset", mSensorParams.outputOffsetX, mSensorParams.outputOffsetY);

        NoiseReduce.NRParams nrParams = previousStages.getStage(NoiseReduce.class).getNRParams();
        converter.seti("lce", mProcessParams.lce && (nrParams.sharpenFactor >= 0f) ? 1 : 0);
        converter.setf("sharpenFactor", nrParams.sharpenFactor);
        converter.setf("adaptiveSaturation", nrParams.adaptiveSaturation, nrParams.adaptiveSaturationPow);

        float[] saturation = mProcessParams.saturationMap;
        float[] sat = new float[saturation.length + 1];
        System.arraycopy(saturation, 0, sat, 0, saturation.length);
        sat[saturation.length] = saturation[0];

        Texture satTex = new Texture(sat.length, 1, 1, Texture.Format.Float16,
                FloatBuffer.wrap(sat), GL_LINEAR, GL_CLAMP_TO_EDGE);
        satTex.bind(GL_TEXTURE8);
        converter.seti("saturation", 8);

        Texture noiseTex = previousStages.getStage(NoiseMap.class).getNoiseTex();
        noiseTex.bind(GL_TEXTURE10);
        converter.seti("noiseTex", 10);

        // Fill with noise
        new Random(8682522807148012L).nextBytes(dither);
        Texture ditherTex = new Texture(ditherSize, ditherSize, 1, Texture.Format.UInt16,
                ByteBuffer.wrap(dither));
        ditherTex.bind(GL_TEXTURE12);
        converter.seti("ditherTex", 12);
        converter.seti("ditherSize", ditherSize);
    }

    @Override
    public int getShader() {
        return R.raw.stage3_3_tonemap_fs;
    }
}
