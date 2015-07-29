package net.bozho.easycamera;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.easycamera.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class EasyCameraActivity extends Activity {
    public final static String RESULE_IMAGE_PATH = "RESULE_IMAGE_PATH";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private ImageButton btn_capture_cancel, btn_picker_to_camera, btn_apture_done;


    /**
     * 判断是否在预览状态
     */
    private boolean previewIsRunning = false;

    private EasyCamera mEasyCamera = null;
    private EasyCamera.CameraActions mCameraActions = null;
    private String mImagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_easy_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);

        btn_picker_to_camera = (ImageButton) findViewById(R.id.btn_picker_to_camera);
        btn_capture_cancel = (ImageButton) findViewById(R.id.btn_capture_cancel);
        btn_apture_done = (ImageButton) findViewById(R.id.btn_apture_done);
        btn_apture_done.setVisibility(View.GONE);

        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //　开启后置摄像头
                //Log.d("TAG", "surfaceCreated初始化");
                mEasyCamera = DefaultEasyCamera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                //　正常显示相机预览
                WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                mEasyCamera.alignCameraAndDisplayOrientation(manager);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //Log.d("TAG", "surfaceChanged设置预览," + "format = " + format + ",width = " + width + ",height = " + height);
                if (!previewIsRunning && mEasyCamera != null) {
                    // 设置相机翻转角度
                    mEasyCamera.setDisplayOrientation(90);
                    Camera.Parameters parameters = mEasyCamera.getParameters();
                    parameters.setRotation(90);//保存的图片旋转90度
                    //parameters.setPictureSize(width, height);//设置拍出来的照片大小
                    //
                    List<Size> previewSizes = parameters.getSupportedPreviewSizes();
                    Size pictureS = CameraParaUtil.getInstance().getPreviewSize(previewSizes, width);
                    parameters.setPreviewSize(pictureS.width, pictureS.height);
                    //
                    List<Size> pictureSizes = parameters.getSupportedPictureSizes();
                    pictureS = CameraParaUtil.getInstance().getPictureSize(pictureSizes, width);
                    parameters.setPictureSize(pictureS.width, pictureS.height);
                    //
                    mEasyCamera.setParameters(parameters);//把上面的设置 赋给摄像头
                    try {
                        // 开启预览
                        mCameraActions = mEasyCamera.startPreview(holder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    previewIsRunning = true;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //Log.d("TAG", "surfaceDestroyed停止并释放");
                // 停止预览并释放
                mEasyCamera.stopPreview();
                mEasyCamera.release();
                mEasyCamera = null;
                mCameraActions = null;
                previewIsRunning = false;
            }
        });

        btn_picker_to_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyCamera.PictureCallback mPictureCallback = new EasyCamera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, EasyCamera.CameraActions actions) {
                        showDoneButton();
                        // 停止预览
                        mEasyCamera.stopPreview();
                        try {
                            // 延时1000s，看的更加清楚
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // 存储图片的操作
                        FileOutputStream fos = null;
                        try {
                            //Log.d("TAG", getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "abc.jpg");
                            mImagePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "abc.jpg";
                            fos = new FileOutputStream(new File(mImagePath));
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                // 拍摄照片的操作
                mCameraActions.takePicture(EasyCamera.Callbacks.create().withJpegCallback(mPictureCallback));
            }
        });

        btn_capture_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImagePath != null) {
                    hideDoneButton();
                    // 开启预览
                    EasyCamera.Callbacks.create().withRestartPreviewAfterCallbacks(true);
                    try {
                        mEasyCamera.startPreview(mSurfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mImagePath = null;
                } else {
                    finish();
                }
            }
        });
        btn_apture_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent().putExtra(RESULE_IMAGE_PATH, mImagePath));
                finish();
            }
        });
    }

    private void showDoneButton() {
        float[] alpha = {0f, 1f};
        float[] scale = {0.5f, 1f};
        btn_apture_done.setVisibility(View.VISIBLE);
        propertyValuesHolder(btn_apture_done, alpha, scale, null);
    }

    private void hideDoneButton() {
        float[] alpha = {1f, 0f};
        float[] scale = {1f, 0.5f};
        propertyValuesHolder(btn_apture_done, alpha, scale, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                btn_apture_done.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }


    private void propertyValuesHolder(View view, float[] alpha, float[] scale,Animator.AnimatorListener listener) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", alpha[0], alpha[1]);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", scale[0], scale[1]);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", scale[0], scale[1]);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ);
        anim.setDuration(200).start();
        if(listener!=null){
            anim.addListener(listener);
        }
    }
}
