package com.netease.nim.uikit.common.media.picker.joycamera.activity;

/**
 * Created by Kairong on 2016/7/29.
 * mail:wangkrhust@gmail.com
 */
import com.bonzaiengine.BonzaiEngine;
import com.bonzaiengine.camera.VolumeManipulator;
import com.bonzaiengine.io.Stream;
import com.bonzaiengine.light.Light;
import com.bonzaiengine.light.Lights;
import com.bonzaiengine.math.Color3;
import com.bonzaiengine.math.Transform;
import com.bonzaiengine.math.Vec3;
import com.bonzaiengine.model.Model;
import com.bonzaiengine.model.asset.AssetManager;
import com.bonzaiengine.model.asset.AsyncModel;
import com.bonzaiengine.model.asset.MaterialLibrary;
import com.bonzaiengine.model.asset.ModelLibrary;
import com.bonzaiengine.model.reader.LightingMode;
import com.bonzaiengine.model.reader.ModelReaderSettings;
import com.bonzaiengine.opengl.GLApiExt;
import com.bonzaiengine.opengl.IGL;
import com.bonzaiengine.opengl.android.AndroidApi;
import com.bonzaiengine.opengl.helper.MatrixStack;
import com.bonzaiengine.renderer.RenderOptions;
import com.bonzaiengine.renderer.Renderer;
import com.bonzaiengine.shader.ShaderGenerator;
import com.bonzaiengine.shader.ShaderGeneratorConfig;
import com.bonzaiengine.shader.ShaderList;
import com.bonzaiengine.texture.TextureList;
import com.bonzaiengine.texture.TextureLoader;
import com.bonzaiengine.threading.WorkerQueue;
import com.bonzaiengine.visibility.Box;
import com.bonzaiengine.visibility.VolumeType;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.media.picker.joycamera.CameraHelper;
import com.netease.nim.uikit.common.media.picker.joycamera.CameraSharedPreference;
import com.netease.nim.uikit.common.media.picker.joycamera.Constant;
import com.netease.nim.uikit.common.media.picker.joycamera.ICamOnLineResMgr;
import com.netease.nim.uikit.common.media.picker.joycamera.ThumbImageAdapter;
import com.netease.nim.uikit.common.media.picker.joycamera.ThumbImageHolder;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CameraMode;
import com.netease.nim.uikit.common.media.picker.model.PhotoInfo;
import com.netease.nim.uikit.common.media.picker.model.PickerContract;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.bonzaiengine.visibility.VolumeOptions.axisAlignedVisible;


public class JoyCameraActivity extends TActionBarActivity implements SurfaceHolder.Callback {

    public static final int REQ_COVER_PICKIMAGE = 1;
    public static final int REQ_NORMAL_PICKIMAGE = 2;
    public static final int REQ_CROP_IMAGE = 3;
    public static final int MSG_SHOW_PROGRESS = 1;

    private CameraHelper cameraHelper;
    private SurfaceView surface;
    private GLSurfaceView gl_surface;
    private ImageView focus_view;                // 显示对焦光标
    private ImageView flash_light;               // 闪光灯
    private ImageView switch_camera;             // 切换摄像头
    private ImageView cover_view;                // 封面图片
    private ImageView expand_button;             // 扩展按钮
    private ImageView gallery_button;            // 本地相册按钮
    private ImageView close_button;
    private ProgressBar load_model_pb;
    // added in v1.2.0
//    private TextView mode_title;
    private ImageView tab_ar;
    private ImageView tab_cover;
    private TextView  tab_normal;
    private LinearLayout previewing_barrier;
    private RecyclerView thumbIconList;
    private ThumbImageAdapter coverListAdapter;
    private ThumbImageAdapter arListAdapter;
    private View cameraTopBarBg;
    private RelativeLayout cameraBottomBarRl;
    private StandaloneScene standaloneScene;
    private ScaleGestureDetector scaleGestureDetector;

    private ICamOnLineResMgr joyCamResMgr;


    private boolean continue_tag1 = false;
    private float xpos, ypos;
    private volatile float move_x;
    private volatile float move_z;

    public enum State{NONE, SAVING, ERROR, DONE}

    protected volatile State save_picture_state = State.NONE;
    protected final Object lock = new Object();
    protected String saved_picture;

    private CameraMode current_mode = CameraMode.NORMAL;
    /*显示封面选项菜单*/
    private boolean ifCoverMenuShown = true;

    enum ANIM_TYPE{SCALE,HIDE}

    private int barrier_height = 0;               // 拍照的遮幅高度
    private volatile int curCoverIndex = -1;       // 当前选择的封面
    private volatile int curARModel = -1;         // 当前选择的模型

    public class StandaloneScene implements GLSurfaceView.Renderer, ScaleGestureDetector.OnScaleGestureListener{
        private final static boolean USE_LIGHT = true;

        private boolean transluscent;
        private boolean capture = false;

        /* OpenGL Es interface */
        private final AndroidApi glApi;
        /* Keep track of matrices states */
        private final com.bonzaiengine.camera.View view;
        /* Model related */
        private final AssetManager assets;
        private AsyncModel asyncModel;

        private final VolumeManipulator camera;
        private final RenderOptions renderOptions;
        private final Lights<Light> lights;
        private final List<CamOnLineRes> resList;
        private final Handler takePhotoHandler;
        private boolean initLookAt = false;
        private int motionFrames = 0;
        private boolean isScaling = false;
        private float moveFactor = 1.0f;
        private int lastARModel = -1;

        private final Runnable showPb = new Runnable() {
            @Override
            public void run() {
                load_model_pb.setVisibility(View.VISIBLE);
            }
        };

        private final Runnable hidePb = new Runnable() {
            @Override
            public void run() {
                load_model_pb.setVisibility(View.GONE);
            }
        };

        public StandaloneScene(GLSurfaceView glSurfaceView, List<CamOnLineRes> resList, Handler takePhotoHandler, boolean transluscent) {
            if(BonzaiEngine.get().getWorker() == null) {
                BonzaiEngine.get().setWorker(new WorkerQueue());
            }

            this.takePhotoHandler = takePhotoHandler;
            this.transluscent = transluscent;
            this.resList = resList;

            glApi = new AndroidApi(null, glSurfaceView);

            view = new com.bonzaiengine.camera.View();
            view.projection.setPerspective(45, 1, 1, 100);

            // AssetManager
            assets = new AssetManager();
            assets.textures = new TextureList(new TextureLoader());
            assets.setReadTextureAsync(true);
            assets.models = new ModelLibrary();

            camera = new VolumeManipulator(view);

            renderOptions = new RenderOptions();

            if(USE_LIGHT) {
                lights = Lights.create(1);
                for(int i = 0;i < 1; ++i) {
                    Light l = lights.getLight(i);
                    l.setAmbient(new Color3(0.5f, 0.5f, 0.5f));
                    l.setDiffuse(new Color3(0.5f, 0.5f, 0.5f));
                    l.setRelTo(camera);
                    l.setEnabled(true);
                }
                renderOptions.setLights(lights);
            }
            else {
                lights = null;
            }
        }

        private final AsyncModel loadModel(AssetManager assets, String path) {
			/*
			 * Load any model supported by the API
			 */
            final ModelReaderSettings settings = new ModelReaderSettings();
            settings.lighting = LightingMode.Default;
            settings.animated = true;
            settings.stream = new Stream(path);
            return assets.loadModelAsync(settings);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            glApi.update();
            final IGL gl = glApi.getGL();
            final MatrixStack projectionStack = gl.getGLc().getProjection();

            height = Math.max(height, 1);

            view.viewport.set(0, 0, width, height);
            view.projection.getPerspective().setRatio((float)width / height);

            gl.glViewport(view.viewport.getX(), view.viewport.getY(), view.viewport.getWidth(), view.viewport.getHeight());

            projectionStack.set(view.projection.getMatrix());
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            glApi.update();
            final IGL gl = glApi.getGL();

            if(!gl.isSupported(GLApiExt.fixedPipeline)) {
                final ShaderGeneratorConfig shaderConfig = new ShaderGeneratorConfig();
                shaderConfig.maxNumLights = USE_LIGHT ? 1 : 0;

                assets.shaders = new ShaderList();
                assets.materials = new MaterialLibrary();
                assets.setShaderGenerator(new ShaderGenerator(shaderConfig));
            }

            // Initialize some OpenGL states
            if(transluscent) {
                gl.glClearColor(0.f, 0.f, 0.f, 0.0f);
            }
            else {
                gl.glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
            }
            gl.glClearDepth(1.0);
            gl.glDepthFunc(IGL.GL_LEQUAL);
            gl.glEnable(IGL.GL_DEPTH_TEST);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            glApi.update();

            final IGL gl = glApi.getGL();
            final MatrixStack modelviewStack = gl.getGLc().getModelview();
            final MatrixStack projectionStack = gl.getGLc().getProjection();

            // Upload texture and load shaders
            assets.textures.load(gl);
            if(assets.shaders != null) {
                assets.shaders.load(gl);
            }

            if (curARModel < 0) return;

            if (curARModel != lastARModel){
                if (asyncModel != null){
                    assets.models.destroy(gl, asyncModel);
                }
                runOnUiThread(showPb);
                asyncModel = loadModel(assets, resList.get(curARModel).getCachePath());
                lastARModel = curARModel;
                initLookAt = false;
            }

            if (asyncModel == null) {
                runOnUiThread(showPb);
                asyncModel = loadModel(assets, resList.get(curARModel).getCachePath());
                initLookAt = false;
            }

            final Model model = asyncModel.get();

            // Update modelview matrix
            if(model != null && !initLookAt) {
                runOnUiThread(hidePb);
                // model.transform(new Transform().setRotation(new Quat(0, 2, 0, 1)));
                // Quick modelview matrix without using VolumeManipulator
                final Box bbox = (Box)model.getVolume(axisAlignedVisible(VolumeType.BOX));

                // Look toward the model center
                Vec3 ref = new Vec3();
                bbox.getCenter(ref);
                // Move back from the model center to approximately the model size
                Vec3 eye = new Vec3();
                bbox.getSize(eye);
                moveFactor = eye.x / 720;
                eye.add(ref);
                eye.y /= 2;
                eye.x = 0;
                eye.z *=3;

                // Compute a look at matrix
                view.modelview.getMatrix().lookAt(eye, ref, Vec3.axisY);
                modelviewStack.set(view.modelview.getMatrix());

                camera.setVolume(model);   // Camera to manipulate the model
                camera.fitToFrustum();     // Fit the model to cover the entire screen area
                camera.adjustProjection(); // Adjust near / far planes for better accuracy
                projectionStack.set(view.projection.getMatrix());

                Transform transform = new Transform();
                //transform.setRotation(new Quat(0, MathFunc.sin(1.74f), 0, MathFunc.cos(1.74f)));
                model.transform(transform);
                initLookAt = true;
            }

            if (model != null) {
                Transform transform = new Transform();
                //transform.setRotation(new Quat(0, MathFunc.sin(rotate_x), 0, MathFunc.cos(rotate_x)));

                Vec3 posi = new Vec3();
                transform.getPosition(posi);
                posi.x += move_x*moveFactor;
                posi.y += -move_z*moveFactor;
                transform.setPosition(posi);

                model.transform(transform);
            }


            if(USE_LIGHT) {
                lights.updateToView(view);
            }

            if(capture) {
                LogUtil.d(TAG, "capture start");
                capture = false;
                int bitmapBuffer[] = new int[ScreenUtil.screenHeight*ScreenUtil.screenWidth];

                IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
                intBuffer.position(0);

                try{
                    GLES20.glReadPixels(0, 0, ScreenUtil.screenWidth, ScreenUtil.screenHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer);
                } catch (GLException e){
                    e.printStackTrace();
                    return;
                }
                capture(bitmapBuffer);
            }

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            move_x = 0;
            move_z = 0;

            try {
                // Model rendering
                if (model != null) {
                    Renderer.render(glApi, model, renderOptions);
                }
            } catch (NullPointerException e){

            }
        }

        public void startCapture() {
            capture = true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (isScaling && motionFrames++ % 3 == 0) {
                glApi.update();

                final IGL gl = glApi.getGL();

                final Model model = asyncModel.get();
                // Update modelview matrix
                if(model != null) {
                    Transform transform = new Transform();
                    Vec3 scale = new Vec3();
                    scale.set(detector.getScaleFactor());
                    transform.setScale(scale);
                    model.transform(transform);
                    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                }
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;
            motionFrames = 0;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            motionFrames = 0;
            isScaling = false;
        }

        public void capture(final int[] bitmapBuffer){
            new Thread(){
                @Override
                public void run() {
                    takePhotoHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogMaker.showProgressDialog(JoyCameraActivity.this,"").setCanceledOnTouchOutside(false);
                        }
                    });
                    takePhotoHandler.obtainMessage(CameraHelper.MSG_TAKE_PICTURE).sendToTarget();

                    synchronized (lock) {
                        save_picture_state = JoyCameraActivity.State.SAVING;
                    }

                    Bitmap bitmap = null;
                    try {
                        bitmap = createBitmapFromGLSurface(ScreenUtil.screenWidth, ScreenUtil.screenHeight, bitmapBuffer);
                        synchronized (lock) {
                            while (save_picture_state == JoyCameraActivity.State.SAVING) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            LogUtil.d(TAG, "wait end");

                            if (save_picture_state == JoyCameraActivity.State.DONE && saved_picture != null) {
                                Bitmap newb = Bitmap.createBitmap(ScreenUtil.screenWidth, ScreenUtil.screenHeight, Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(newb);

                                Bitmap picture = BitmapFactory.decodeFile(saved_picture);
                                canvas.drawBitmap(picture, 0, 0, null);
                                canvas.drawBitmap(bitmap, 0, 0, null);
                                final String filePath = StorageUtil.getWritePath(new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(new Date()), StorageType.TYPE_TEMP);
                                File bitFile = new File(filePath);
                                FileOutputStream out = new FileOutputStream(bitFile);
                                newb.compress(Bitmap.CompressFormat.PNG, 90, out);
                                out.close();
                                newb.recycle();
                                picture.recycle();
                                bitmap.recycle();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (DialogMaker.isShowing()) {
                                            DialogMaker.dismissProgressDialog();
                                        }
                                        CropImageActivity.start(JoyCameraActivity.this, filePath,(Class)getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                                    }
                                });
                            }
                        }

                    } catch (OutOfMemoryError | NullPointerException | IOException error){
                        error.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogMaker.dismissProgressDialog();
                                Toast.makeText(JoyCameraActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } finally {
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    }
                }
            }.start();

        }

        private Bitmap createBitmapFromGLSurface(int w, int h, int[] bitmapBuffer){
            int bitmapSource[] = new int[w*h];

            int offset1, offset2;
            for(int i = 0;i < h;++i){
                offset1 = i*w;
                offset2 = (h - i - 1)*w;
                for(int j = 0;j < w;++j){
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2+j] = pixel;
                }
            }

            return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
        }
    }

    private String TAG = "JoyCameraActivity";

    public static void start(Context context, Class<? extends TActionBarActivity> call){
        Intent intent = new Intent();
        intent.setClass(context, JoyCameraActivity.class);
        intent.putExtra(Extras.EXTRA_CALL_CLASS, call);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_joy_camera);

        joyCamResMgr = NimUIKit.getCamOnLineResMgr();

        if (savedInstanceState != null){
            Bundle data = savedInstanceState.getBundle("JoyCameraData");
            if (data != null){
                current_mode = CameraMode.valueOf(data.getString("current_mode", "NORMAL"));
                curARModel = data.getInt("curARModel", 0);
                curCoverIndex = data.getInt("curCoverIndex", 0);
            }
        }

        initSurfaceView();
        initGlSurfaceView();
        // 初始化视图
        initViews();

        resumeView();

        _takePhotoHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUpdate();
            }
        }, 500);
    }

    private void checkUpdate(){
        if (NetworkUtil.isNetAvailable(JoyCameraActivity.this)){
            final int arStart = joyCamResMgr.getARItems().size();
            final int coverStart = joyCamResMgr.getCoverItems().size();
            joyCamResMgr.pullOnlineReses(new ICamOnLineResMgr.Callback<Integer>() {
                @Override
                public void onSuccess(final Integer updateType) {
                    // 在主线程上进行更新
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ((updateType & CamOnLineRes.Type.AR.v()) > 0) {
                                arListAdapter.notifyItemRangeInserted(arStart, joyCamResMgr.getARItems().size() - arStart);
                            }

                            if ((updateType & CamOnLineRes.Type.COVER.v()) > 0) {
                                coverListAdapter.notifyItemRangeInserted(coverStart, joyCamResMgr.getCoverItems().size() - coverStart);
                            }
                        }
                    });
                }

                @Override
                public void onFailed(String msg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(JoyCameraActivity.this, "资源更新失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(JoyCameraActivity.this, "网络无连接", Toast.LENGTH_LONG).show();
        }
    }

    private void resumeView(){
        if (current_mode == CameraMode.AR){
            thumbIconList.swapAdapter(arListAdapter, false);
            cover_view.setVisibility(View.GONE);
            thumbIconList.setVisibility(View.VISIBLE);
            gallery_button.setVisibility(View.GONE);

            if (gl_surface != null){
                gl_surface.setVisibility(View.VISIBLE);
                gl_surface.onResume();
            }

            current_mode = CameraMode.AR;
            tab_cover.setBackgroundColor(Color.TRANSPARENT);
            tab_normal.setBackgroundColor(Color.TRANSPARENT);
            previewing_barrier.setVisibility(View.GONE);
            cameraBottomBarRl.setBackgroundColor(Color.TRANSPARENT);
            cameraTopBarBg.setBackgroundColor(Color.TRANSPARENT);
            tab_ar.setBackgroundColor(getResources().getColor(R.color.joy_camera_focused_color));
        }

        if (current_mode == CameraMode.COVER){
            thumbIconList.swapAdapter(coverListAdapter, false);
            cover_view.setVisibility(View.VISIBLE);
            thumbIconList.setVisibility(View.VISIBLE);

            if (current_mode == CameraMode.AR) {
                if (gl_surface != null){
                    gl_surface.onPause();
                    gl_surface.setVisibility(View.GONE);
                }
            }

            current_mode = CameraMode.COVER;
            tab_ar.setBackgroundColor(Color.TRANSPARENT);
            tab_normal.setBackgroundColor(Color.TRANSPARENT);
            cameraBottomBarRl.setBackgroundColor(getResources().getColor(R.color.joy_camera_theme_color));
            cameraTopBarBg.setBackgroundColor(getResources().getColor(R.color.joy_camera_theme_color));
            tab_cover.setBackgroundColor(getResources().getColor(R.color.joy_camera_focused_color));
            previewing_barrier.setVisibility(View.VISIBLE);
            // 设置封面
            ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(joyCamResMgr.getItem(CamOnLineRes.Type.COVER, curCoverIndex).getCachePath()), cover_view);
        }
    }


    // 初始化cameraHelper
    private void initSurfaceView(){
        SurfaceHolder holder;
        surface = (SurfaceView)findViewById(R.id.surfaceview);
        holder = surface.getHolder();   //获得句柄
        holder.addCallback(this);       //添加回调
        // surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraHelper = new CameraHelper(this,holder, _takePhotoHandler);
    }

    private void initGlSurfaceView(){
        gl_surface = (GLSurfaceView)findViewById(R.id.gl_surfaceview);
        if (gl_surface != null) {
            gl_surface.setEGLContextClientVersion(2);
            gl_surface.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            gl_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            gl_surface.setZOrderOnTop(false);
        }

        standaloneScene = new StandaloneScene(gl_surface, joyCamResMgr.getARItems(), _takePhotoHandler, true);
        scaleGestureDetector = new ScaleGestureDetector(this, standaloneScene);

        gl_surface.setRenderer(standaloneScene);
        gl_surface.onPause();
    }

    private void initRV(){
        thumbIconList = (RecyclerView) findViewById(R.id.thumb_icon_list);
        thumbIconList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        coverListAdapter = new ThumbImageAdapter(this, joyCamResMgr.getCoverItems(), onCoverThumbClickEvent);
        arListAdapter = new ThumbImageAdapter(this, joyCamResMgr.getARItems(), onARThumbClickEvent);
    }

    // 初始化视图资源
    private void initViews(){
        // 设置控件资源ID
        previewing_barrier = (LinearLayout)findViewById(R.id.barrier);

        load_model_pb = (ProgressBar)findViewById(R.id.load_model_progress_pb);
        focus_view = (ImageView)findViewById(R.id.focus_view);
        cover_view = (ImageView)findViewById(R.id.cover);
        switch_camera = (ImageView)findViewById(R.id.switch_camera);
        expand_button = (ImageView)findViewById(R.id.expand_button);
        gallery_button = (ImageView)findViewById(R.id.gallery_button);
        flash_light = (ImageView)findViewById(R.id.flash_light);
        close_button = (ImageView)findViewById(R.id.camera_close_iv);
        cameraBottomBarRl = (RelativeLayout)findViewById(R.id.camera_bottom_bar);
        cameraTopBarBg = (View)findViewById(R.id.camera_top_bar_bg);
        // added in v1.1.0
        tab_ar = (ImageView)findViewById(R.id.tab_ar_iv);
        tab_cover = (ImageView)findViewById(R.id.tab_cover_iv);
        tab_normal = (TextView)findViewById(R.id.tab_normal_tv);


        // 设置监听
        expand_button.setOnClickListener(onClickListener);
        gallery_button.setOnClickListener(onClickListener);
        flash_light.setOnClickListener(onClickListener);
        switch_camera.setOnClickListener(onClickListener);
        tab_ar.setOnClickListener(onClickListener);
        tab_cover.setOnClickListener(onClickListener);
        tab_normal.setOnClickListener(onClickListener);
        close_button.setOnClickListener(onClickListener);
        // 切换摄像头图标的显示
        View shutter = findViewById(R.id.shutter);
        if(shutter != null){
            shutter.setOnClickListener(onClickListener);
        }


        int camera_top_bar_height = getResources().getDimensionPixelSize(R.dimen.camera_top_bar_height);
        int barrier_margin_top = Math.round(ScreenUtil.screenWidth / Constant.pictureRatio) + camera_top_bar_height;
        // 初始化遮幅高度
        barrier_height = ScreenUtil.screenHeight - barrier_margin_top;
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ScreenUtil.screenWidth, barrier_height);
        View v = new View(this);
        previewing_barrier.addView(v, lp);
        previewing_barrier.setVisibility(View.GONE);

        // top bar和bottom bar透明
        cameraTopBarBg.setBackgroundColor(Color.TRANSPARENT);
        cameraBottomBarRl.setBackgroundColor(Color.TRANSPARENT);

        // 闪光灯图标的显示
        if(!Constant.hasFlashLight){
            cameraHelper.flashLightMode = CameraHelper.FLIGHT_NONE;
            flash_light.setVisibility(View.GONE);
        }else {
            // 读取闪光灯默认选项
            cameraHelper.flashLightMode = CameraSharedPreference.getFlashLight();
            setFlashLightView(CameraSharedPreference.getFlashLight());
        }
        // 切换摄像头图标的显示
        View switch_camera = findViewById(R.id.switch_camera);
        if(switch_camera != null){
            switch_camera.setVisibility(cameraHelper.cameraCount < 2 ? View.GONE:View.VISIBLE);
        }

        initRV();

    }

    private ThumbImageHolder.OnThumbClickEvent onCoverThumbClickEvent = new ThumbImageHolder.OnThumbClickEvent() {
        @Override
        public void onDownload(final CamOnLineRes item, final int position) {
            item.setStatus(CamOnLineRes.Status.DOWNLOADING);
            coverListAdapter.notifyItemChanged(position);

            new Thread() {
                @Override
                public void run(){
                    joyCamResMgr.downloadSingleRes(item, new ICamOnLineResMgr.Callback<CamOnLineRes>() {
                        @Override
                        public void onSuccess(final CamOnLineRes newRes) {
                            joyCamResMgr.saveSingleRes(newRes);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    joyCamResMgr.getItem(CamOnLineRes.Type.COVER, position).update(newRes);
                                    coverListAdapter.notifyItemChanged(position);
                                    Log.d(TAG, "download success");
                                }
                            });
                        }

                        @Override
                        public void onFailed(final String msg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    joyCamResMgr.getItem(CamOnLineRes.Type.COVER, position).setStatus(CamOnLineRes.Status.NONE);
                                    coverListAdapter.notifyItemChanged(position);
                                    Log.d(TAG, "download failed"+msg);
                                }
                            });
                        }
                    });
                }
            }.start();
        }

        @Override
        public void onSelect(CamOnLineRes item) {
            if(item.getLocalIndex() != curCoverIndex){
                ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(item.getCachePath()), cover_view);
                curCoverIndex = item.getLocalIndex();
            }
        }
    };

    private ThumbImageHolder.OnThumbClickEvent onARThumbClickEvent = new ThumbImageHolder.OnThumbClickEvent() {
        @Override
        public void onDownload(final CamOnLineRes item,final int position) {
            item.setStatus(CamOnLineRes.Status.DOWNLOADING);
            arListAdapter.notifyItemChanged(position);

            new Thread() {
                @Override
                public void run(){
                    joyCamResMgr.downloadSingleRes(item, new ICamOnLineResMgr.Callback<CamOnLineRes>() {
                        @Override
                        public void onSuccess(final CamOnLineRes t2) {
                            joyCamResMgr.saveSingleRes(t2);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    joyCamResMgr.getItem(CamOnLineRes.Type.AR, position).update(t2);
                                    arListAdapter.notifyItemChanged(position);
                                    Log.d(TAG, "download success");
                                }
                            });
                        }

                        @Override
                        public void onFailed(final String msg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    joyCamResMgr.getItem(CamOnLineRes.Type.AR, position).setStatus(CamOnLineRes.Status.NONE);
                                    arListAdapter.notifyItemChanged(position);
                                    Log.d(TAG, "download failed"+msg);
                                }
                            });
                        }
                    });
                }
            }.start();
        }

        @Override
        public void onSelect(CamOnLineRes item) {
            if(item.getLocalIndex() != curARModel){
                curARModel = item.getLocalIndex();
            }
        }
    };

    private void onStartFocus(MotionEvent motionEvent) {
        Rect surfaceRect = new Rect(0, 0, ScreenUtil.screenWidth, ScreenUtil.screenHeight);
        int focus_view_size = getResources().getDimensionPixelSize(R.dimen.focus_view_size);
        int photo_bar_height = getResources().getDimensionPixelSize(R.dimen.camera_top_bar_height) + Math.round(focus_view_size / 2);
        int bottom_bar_height = getResources().getDimensionPixelSize(R.dimen.camera_bottom_bar_height) + Math.round(focus_view_size / 2);
        surfaceRect.top += photo_bar_height;
        surfaceRect.bottom -= bottom_bar_height;
        surfaceRect.left += Math.round(focus_view_size / 2);
        surfaceRect.right -= Math.round(focus_view_size / 2);
        float m_X = motionEvent.getX(0);
        float m_Y = motionEvent.getY(0);
        if (surfaceRect.contains((int) m_X, (int) m_Y)) {
            if (cameraHelper.isPreviewing && !cameraHelper.focusing && cameraHelper.camera_position == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(focus_view.getLayoutParams());
                // 将focus_view中心移动到点击的地方
                lp.setMargins((int) (m_X - focus_view_size / 2), (int) (m_Y - focus_view_size / 2), 0, 0);
                focus_view.setLayoutParams(lp);
                cameraHelper.autoFocus(false, false);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (current_mode == CameraMode.AR) {
            if (me.getAction() == MotionEvent.ACTION_DOWN
                    || me.getAction() == MotionEvent.ACTION_UP
                    || me.getAction() == MotionEvent.ACTION_CANCEL) {
                continue_tag1 = false;
                return false;
            }

            if (me.getAction() == MotionEvent.ACTION_MOVE) {
                if (me.getPointerCount() == 1) {
                    if (!continue_tag1) {
                        xpos = me.getX();
                        ypos = me.getY();
                        continue_tag1 = true;
                    } else {
                        float xd = me.getX() - xpos;
                        float yd = me.getY() - ypos;

                        xpos = me.getX();
                        ypos = me.getY();

                        move_x = xd;
                        move_z = yd;
                    }
                    return true;
                }
                continue_tag1 = false;
            }
            return scaleGestureDetector.onTouchEvent(me);
        } else {
            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                onStartFocus(me);
            }
        }
        return super.onTouchEvent(me);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        // 全局控件点击事件监听
        @Override
        public void onClick(View v) {

            if( v.getId() ==  R.id.switch_camera) {
                // 切换前后摄像头
                switchCamera();
            }
            else if(v.getId() == R.id.shutter){
                // 拍照
                takePhoto();
            }
            else if(v.getId() == R.id.flash_light){
                // 闪光灯
                switchFlashLightMode();
            }
            else if(v.getId() == R.id.expand_button){
                // 显示封面选项
                showCoverList();
            }
            else if(v.getId() == R.id.gallery_button){
                // 显示本地相册
                showLocalGallery();
            }
            else if(v.getId() == R.id.camera_close_iv) {
                onBackPressed();
            }
            else {
                if (v.getId() == R.id.tab_ar_iv || v.getId() == R.id.tab_cover_iv || v.getId() == R.id.tab_normal_tv) {
                    switchCameraMode(v.getId());
                }
            }
        }
    };

    private void switchCameraMode(int resId){
        if (resId == R.id.tab_ar_iv) {
            if (current_mode != CameraMode.AR){
                thumbIconList.swapAdapter(arListAdapter, false);

                cover_view.setVisibility(View.GONE);
                thumbIconList.setVisibility(View.VISIBLE);
                gallery_button.setVisibility(View.GONE);
                previewing_barrier.setVisibility(View.GONE);

                if (gl_surface != null){
                    gl_surface.setVisibility(View.VISIBLE);
                    gl_surface.onResume();
                }

                current_mode = CameraMode.AR;
                tab_cover.setBackgroundColor(Color.TRANSPARENT);
                tab_normal.setBackgroundColor(Color.TRANSPARENT);
                cameraTopBarBg.setBackgroundColor(Color.TRANSPARENT);
                cameraBottomBarRl.setBackgroundColor(Color.TRANSPARENT);
                tab_ar.setBackgroundColor(getResources().getColor(R.color.joy_camera_focused_color));
                if (curARModel < 0) {
                    curARModel = 0;
                }
//                mode_title.setText("AR模式");
            }
        }
        else if(resId == R.id.tab_cover_iv) {
            if (current_mode != CameraMode.COVER){
                load_model_pb.setVisibility(View.GONE);
                thumbIconList.swapAdapter(coverListAdapter, false);
                cover_view.setVisibility(View.VISIBLE);
                gallery_button.setVisibility(View.VISIBLE);
                thumbIconList.setVisibility(View.VISIBLE);

                if (current_mode == CameraMode.AR) {
                    if (gl_surface != null){
                        gl_surface.onPause();
                        gl_surface.setVisibility(View.GONE);
                    }
                }

                current_mode = CameraMode.COVER;
                tab_ar.setBackgroundColor(Color.TRANSPARENT);
                tab_normal.setBackgroundColor(Color.TRANSPARENT);
                previewing_barrier.setVisibility(View.VISIBLE);
                cameraTopBarBg.setBackgroundColor(getResources().getColor(R.color.joy_camera_theme_color));
                cameraBottomBarRl.setBackgroundColor(getResources().getColor(R.color.joy_camera_theme_color));
                tab_cover.setBackgroundColor(getResources().getColor(R.color.joy_camera_focused_color));
//                mode_title.setText("封面模式");
                // 设置封面
                if (curCoverIndex < 0) {
                    curCoverIndex = 0;
                }
                ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(joyCamResMgr.getItem(CamOnLineRes.Type.COVER, curCoverIndex).getCachePath()), cover_view);
            }
        }

        else if(resId == R.id.tab_normal_tv) {
            if (current_mode != CameraMode.NORMAL){
                cover_view.setVisibility(View.GONE);
                load_model_pb.setVisibility(View.GONE);
                thumbIconList.setVisibility(View.GONE);
                gallery_button.setVisibility(View.VISIBLE);

                if (current_mode == CameraMode.AR) {
                    if (gl_surface != null){
                        gl_surface.onPause();
                        gl_surface.setVisibility(View.GONE);
                    }
                }

                current_mode = CameraMode.NORMAL;
                tab_ar.setBackgroundColor(Color.TRANSPARENT);
                tab_cover.setBackgroundColor(Color.TRANSPARENT);
                previewing_barrier.setVisibility(View.GONE);
                cameraTopBarBg.setBackgroundColor(Color.TRANSPARENT);
                cameraBottomBarRl.setBackgroundColor(Color.TRANSPARENT);
                tab_normal.setBackgroundColor(getResources().getColor(R.color.joy_camera_focused_color));
//                mode_title.setText("原图模式");
            }
        }
    }

    public void startFocusViewAnimation(final ANIM_TYPE type){
        Animation.AnimationListener AniListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(type == ANIM_TYPE.HIDE){
                    focus_view.setVisibility(View.INVISIBLE);
                }
                focus_view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        if(type == ANIM_TYPE.SCALE) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(2f, 1f, 2f, 1f, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
            scaleAnimation.setDuration(1000);
            scaleAnimation.setAnimationListener(AniListener);
            focus_view.startAnimation(scaleAnimation);
        }else if(type == ANIM_TYPE.HIDE){
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f,1f);
            alphaAnimation.setDuration(1000);
            alphaAnimation.setAnimationListener(AniListener);
            focus_view.startAnimation(alphaAnimation);
        }
    }

    /**
     * 为了防止系统gc时发生内存泄露
     * 自定义的一个Handler静态类
     */
    static class TakePhotoHandler extends Handler {
        WeakReference<JoyCameraActivity> mActivity;
        TakePhotoHandler(JoyCameraActivity activity){
            mActivity = new WeakReference<JoyCameraActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            JoyCameraActivity theActivity = mActivity.get();
            switch (msg.what) {
                case CameraHelper.MSG_FOCUSING:
                    theActivity.focus_view.setImageResource(R.drawable.focus_icn);
                    theActivity.focus_view.setVisibility(View.VISIBLE);
                    theActivity.startFocusViewAnimation(ANIM_TYPE.SCALE);
                    break;
                case CameraHelper.MSG_FOCUSED:
                    if (theActivity.cameraHelper.camera_position == Camera.CameraInfo.CAMERA_FACING_BACK
                            &&theActivity.cameraHelper.isPreviewing) {
                        theActivity.focus_view.setImageResource(R.drawable.focus_success_icn);
                        theActivity.focus_view.setVisibility(View.VISIBLE);
                        theActivity.startFocusViewAnimation(ANIM_TYPE.HIDE);
                    }
                    break;
                case CameraHelper.MSG_FOCUS_FAILED:
                    if (theActivity.cameraHelper.camera_position == Camera.CameraInfo.CAMERA_FACING_BACK
                            &&theActivity.cameraHelper.isPreviewing) {
                        theActivity.focus_view.setImageResource(R.drawable.focus_failed_icn);
                        theActivity.focus_view.setVisibility(View.VISIBLE);
                        theActivity.startFocusViewAnimation(ANIM_TYPE.HIDE);
                    }
                    break;
                case CameraHelper.SAVE_PICTURE_DONE:
                    String savedPath = (String)msg.obj;

                    if(savedPath==null) {
                        DialogMaker.dismissProgressDialog();
                        Toast.makeText(theActivity,"保存图片出错",Toast.LENGTH_SHORT).show();
                        if (theActivity.current_mode == CameraMode.AR) {
                            synchronized (theActivity.lock) {
                                theActivity.save_picture_state = State.ERROR;
                                theActivity.lock.notifyAll();
                            }
                        }
                        break;
                    }

                    theActivity.saved_picture = savedPath;

                    switch (theActivity.current_mode) {
                        case COVER:
                            DialogMaker.dismissProgressDialog();
                            CoverBgEditActivity.start(theActivity, Uri.fromFile(new File(savedPath)), theActivity.curCoverIndex,
                                    (Class)theActivity.getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                            break;
                        case NORMAL:
                            DialogMaker.dismissProgressDialog();
                            CropImageActivity.start(theActivity, savedPath,(Class)theActivity.getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                            break;
                        case AR:
                            synchronized (theActivity.lock) {
                                theActivity.save_picture_state = State.DONE;
                                theActivity.lock.notifyAll();
                                LogUtil.d(theActivity.TAG, "saved done");
                            }
                            break;
                    }
                    break;
                case CameraHelper.SAVED_ERROR:
                    DialogMaker.dismissProgressDialog();
                    if (theActivity.current_mode == CameraMode.AR) {
                        synchronized (theActivity.lock) {
                            theActivity.save_picture_state = State.ERROR;
                            theActivity.lock.notifyAll();
                        }
                    }
                    theActivity.cameraHelper.restartPreview();
                    Toast.makeText(theActivity,"发生错误",Toast.LENGTH_SHORT).show();
                    break;
                case CameraHelper.MSG_TAKE_PICTURE:
                    theActivity.startTakePhotoThread();
                    break;
                case CameraHelper.MSG_EXIT_APP:
                    DialogMaker.dismissProgressDialog();
                    theActivity.finish();
                    if (theActivity.current_mode == CameraMode.AR) {
                        synchronized (theActivity.lock) {
                            theActivity.save_picture_state = State.ERROR;
                            theActivity.lock.notifyAll();
                        }
                    }
                    break;
            }
        }
    }
    private TakePhotoHandler _takePhotoHandler = new TakePhotoHandler(this);

    /**
     * 切换摄像头
     */
    private void switchCamera(){
        cameraHelper.switchCamera();
        CameraSharedPreference.setCameraPos(cameraHelper.camera_position);
        if(cameraHelper.camera_position == Camera.CameraInfo.CAMERA_FACING_BACK){
            switch_camera.setImageResource(R.drawable.ic_camera_front);
            flash_light.setVisibility(Constant.hasFlashLight?View.VISIBLE:View.GONE);
            cameraHelper.autoFocus(true,false);
        }else{
            switch_camera.setImageResource(R.drawable.ic_camera_back);
            flash_light.setVisibility(View.GONE);
            focus_view.setVisibility(View.GONE);
        }
    }
    /**
     * 显示/隐藏 封面选项面板
     */
    private void showCoverList(){
        final RelativeLayout cover_icn_rl = (RelativeLayout)findViewById(R.id.cover_icn_rl);
        if(ifCoverMenuShown){
            expand_button.setImageResource(R.drawable.expand);
            if (cover_icn_rl != null) {
                cover_icn_rl.setVisibility(View.GONE);
            }
            ifCoverMenuShown = false;
        }else{
            expand_button.setImageResource(R.drawable.collapse);
            if (cover_icn_rl != null) {
                cover_icn_rl.setVisibility(View.VISIBLE);
            }
            ifCoverMenuShown = true;
        }
    }
    /**
     * 加载本地相册
     */
    private void showLocalGallery(){
        PickImageHelper.PickImageOption pickImageOption = new PickImageHelper.PickImageOption();
        pickImageOption.crop = false;
        pickImageOption.multiSelect = false;

        PickImageActivity.start(this, current_mode == CameraMode.NORMAL ? REQ_NORMAL_PICKIMAGE : REQ_COVER_PICKIMAGE,
                PickImageActivity.FROM_LOCAL, null, false, 1 , false, false, 0, 0);
    }

    /**
     * 切换闪光灯状态
     */
    public void switchFlashLightMode(){
        switch (cameraHelper.flashLightMode){
            case CameraHelper.FLIGHT_OFF:
                cameraHelper.switchFlashLightMode();
                setFlashLightView(CameraHelper.FLIGHT_ON);
                break;
            case CameraHelper.FLIGHT_ON:
                cameraHelper.switchFlashLightMode();
                setFlashLightView(CameraHelper.FLIGHT_AUTO);
                break;
            case CameraHelper.FLIGHT_AUTO:
                cameraHelper.switchFlashLightMode();
                setFlashLightView(CameraHelper.FLIGHT_OFF);
                break;
            default:
                break;
        }
    }


    /**
     * 设置闪光灯状态
     */
    private void setFlashLightView(int mode){
        switch(mode){
            case CameraHelper.FLIGHT_OFF:
                flash_light.setImageResource(R.drawable.flash_light_icn_disabled);
                break;
            case CameraHelper.FLIGHT_ON:
                flash_light.setImageResource(R.drawable.flash_light_icn_enable);
                break;
            case CameraHelper.FLIGHT_AUTO:
                flash_light.setImageResource(R.drawable.flash_light_icn_auto);
                break;
            default:
                break;
        }
        // 写入用户闪光灯首选项
        CameraSharedPreference.setFlashLight(mode);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,int format, int width, int height){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 如果只有一个摄像头，则默认打开后置摄像头，否则打开前置摄像头
        if(cameraHelper.cameraCount == 1) {
            cameraHelper.open();
            switch_camera.setVisibility(View.GONE);
        } else if(cameraHelper.cameraCount >=2){
            int defCamPos = CameraSharedPreference.getCameraPos();
            if (defCamPos == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                flash_light.setVisibility(View.GONE);
                switch_camera.setImageResource(R.drawable.ic_camera_back);
            } else {
                switch_camera.setImageResource(R.drawable.ic_camera_front);
            }
            cameraHelper.open(defCamPos);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 置预览回调为空，再关闭预览
        cameraHelper.stop();
        surface = null;
    }

    /**
     * 带自动对焦功能的拍照
     */
    private void takePhoto(){
        switch (current_mode) {
            case NORMAL:
            case COVER:
                // 后置摄像头拍照
                if(cameraHelper.camera_position == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    if (!cameraHelper.focusing) {
                        // 当前为对焦完成状态则快速拍照
                        DialogMaker.showProgressDialog(JoyCameraActivity.this, "").setCancelable(false);
                        startTakePhotoThread();
                    }
                } else if(cameraHelper.camera_position == Camera.CameraInfo.CAMERA_FACING_FRONT){// 前置摄像头拍照
                    DialogMaker.showProgressDialog(JoyCameraActivity.this, "").setCancelable(false);
                    startTakePhotoThread();
                }
                break;
            case AR:
                standaloneScene.startCapture();
                break;
        }

    }

    /**
     * 拍照线程
     */
    private void startTakePhotoThread(){
        cameraHelper.takePhoto();
    }

    /**
     * 设置屏幕高亮
     */
    private void setScreen(){
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        joyCamResMgr.saveAll();
        if (current_mode == CameraMode.AR) {
            gl_surface.onPause();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraHelper.focusing = false;
        cameraHelper.isPreviewing = true;
        if (current_mode == CameraMode.AR) {
            gl_surface.onResume();
        }
        setScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle data = new Bundle();
        data.putString("current_mode", current_mode.name());
        data.putInt("curARModel", curARModel);
        data.putInt("curCoverIndex", curCoverIndex);
        outState.putBundle("JoyCameraData", data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK){
            return;
        }

        switch(requestCode){
            case REQ_COVER_PICKIMAGE:
            case REQ_NORMAL_PICKIMAGE:
                List<PhotoInfo> photos = PickerContract.getPhotos(data);
                if(photos == null || photos.size() > 1) {
                    Toast.makeText(this, R.string.picker_image_error, Toast.LENGTH_LONG).show();
                    return;
                }
                String path = photos.get(0).getAbsolutePath();
                if (requestCode == REQ_COVER_PICKIMAGE) {
                    CoverBgEditActivity.start(this, Uri.fromFile(new File(path)), curCoverIndex, (Class) getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                } else {
                    CropImageActivity.start(this, path, (Class) getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                }
                break;
            case REQ_CROP_IMAGE:
                PicturePreviewActivity.start(this, saved_picture,
                        (Class)getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                break;
        }
    }
}

