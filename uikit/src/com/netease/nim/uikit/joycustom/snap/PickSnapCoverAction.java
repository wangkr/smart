package com.netease.nim.uikit.joycustom.snap;

import android.content.Intent;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.media.picker.model.PhotoInfo;
import com.netease.nim.uikit.common.media.picker.model.PickerContract;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.constant.RequestCode;
import com.netease.nim.uikit.session.helper.SendImageHelper;

import java.io.File;
import java.util.List;

/**
 * Created by Kairong on 2015/11/4.
 * mail:wangkrhust@gmail.com
 */
public abstract class PickSnapCoverAction extends BaseAction {
    private static final int PICK_IMAGE_COUNT = 9;
    private static final int PORTRAIT_IMAGE_WIDTH = 720;

    public static final String MIME_JPEG = "image/jpeg";
    public static final String JPG = ".jpg";

    private boolean multiSelect;
    private boolean crop = false;
    private boolean isPublicSnap = false;

    protected abstract void onSnapPicked(File hiddenFile, String snapCoverName, String contentText);

    protected PickSnapCoverAction(int iconResId, int titleId, boolean multiSelect, boolean isPublicSnap) {
        super(iconResId, titleId);
        this.multiSelect = multiSelect;
        this.isPublicSnap = isPublicSnap;
    }

    @Override
    public void onClick() {
        int requestCode = makeRequestCode(RequestCode.PICK_HIDDEN_IMAGE);
        showSelector(getTitleId(), requestCode, multiSelect, tempFile());
    }

    private String tempFile() {
        String filename = StringUtil.get32UUID() + JPG;
        return StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);
    }

    /**
     * 打开图片选择器
     */
    private void showSelector(int titleId, final int requestCode, final boolean multiSelect, final String outPath) {
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = titleId;
        option.multiSelect = multiSelect;
        option.multiSelectMaxCount = PICK_IMAGE_COUNT;
        option.crop = crop;
        option.cropOutputImageWidth = PORTRAIT_IMAGE_WIDTH;
        option.cropOutputImageHeight = PORTRAIT_IMAGE_WIDTH;
        option.outputPath = outPath;

        if (isPublicSnap) {
            PickImageHelper.myPickImage(getActivity(), requestCode, option);
        } else {
            PickImageHelper.pickImage(getActivity(), requestCode, option);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.PICK_HIDDEN_IMAGE:
                onPickImageActivityResult(requestCode, data);
                break;
            case RequestCode.PICK_SNAP_COVER:
                onPreviewImageActivityResult(requestCode, data);
                break;
        }
    }

    /**
     * 图片选取回调
     */
    private void onPickImageActivityResult(int requestCode, Intent data) {
        if (data == null) {
            Toast.makeText(getActivity(), R.string.picker_image_error, Toast.LENGTH_LONG).show();
            LogUtil.d("PickSnapCoverAction", "error1");
            return;
        }

        boolean local = data.getBooleanExtra(Extras.EXTRA_FROM_LOCAL, false);
        if (local) {
            // 本地相册
            List<PhotoInfo> photos = PickerContract.getPhotos(data);
            if(photos == null || photos.size() > 1) {
                Toast.makeText(getActivity(), R.string.picker_image_error, Toast.LENGTH_LONG).show();
                LogUtil.d("PickSnapCoverAction", "error");
                return;
            }
            Intent intent = new Intent();
            data.putExtra(Extras.EXTRA_FILE_PATH, photos.get(0).getAbsolutePath());
            if (!handleImagePath(intent, data)) {
                return;
            }
            intent.putExtra(Extras.EXTRA_PUBLIC_SNAP, isPublicSnap);
            intent.putExtra(Extras.EXTRA_FROM_LOCAL, true);
            intent.setClass(getActivity(), SelectSnapCoverImageActivity.class);
            getActivity().startActivityForResult(intent, makeRequestCode(RequestCode.PICK_SNAP_COVER));
        } else {
            // 拍照
            Intent intent = new Intent();
            if (!handleImagePath(intent, data)) {
                return;
            }
            intent.putExtra(Extras.EXTRA_PUBLIC_SNAP, isPublicSnap);
            intent.putExtra(Extras.EXTRA_FROM_LOCAL, false);
            intent.setClass(getActivity(), SelectSnapCoverImageActivity.class);
            getActivity().startActivityForResult(intent, makeRequestCode(RequestCode.PICK_SNAP_COVER));
        }
    }

    /**
     * 是否可以获取图片
     */
    private boolean handleImagePath(Intent intent, Intent data) {
        String photoPath = data.getStringExtra(Extras.EXTRA_FILE_PATH);

        if (TextUtils.isEmpty(photoPath)) {
            Toast.makeText(getActivity(), R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return false;
        }

        File imageFile = new File(photoPath);
        File scaledImageFile = ImageUtil.getScaledImageFileWithMD5(imageFile, MIME_JPEG);

        boolean local = data.getExtras().getBoolean(Extras.EXTRA_FROM_LOCAL, true);
        if (!local) {
            // 删除拍照生成的临时文件
            AttachmentStore.delete(photoPath);
        }

        if (scaledImageFile == null) {
            Toast.makeText(getActivity(), R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return false;
        } else {
            intent.putExtra("OrigImageFilePath", scaledImageFile.getPath());
            ImageUtil.makeThumbnail(getActivity(), scaledImageFile);
        }
        intent.putExtra("ImageFilePath", scaledImageFile.getAbsolutePath());
        return true;
    }

    /**
     * 从预览界面点击发送图片
     */
    private void sendImageAfterSelectCoverActivityResult(Intent data) {
        SendImageHelper.sendImageAfterSelectCoverActivityResult(data, new SendImageHelper.SnapCallback() {
            @Override
            public void sendSnapImage(File hiddenFile, String snapCoverName, String contentText, boolean isOrig) {
                onSnapPicked(hiddenFile, snapCoverName,contentText);
            }
        });
    }

    /**
     * 发送图片
     */
    private void sendImageAfterSelfImagePicker(final Intent data) {
        SendImageHelper.sendSnapCoverAfterSelfImagePicker(getActivity(), data, new SendImageHelper.SnapCallback() {
            @Override
            public void sendSnapImage(File hiddenFile, String snapCoverName, String contentText, boolean isOrig) {
                onSnapPicked(hiddenFile, snapCoverName,contentText);
            }
        });
    }

    /**
     * 拍摄回调
     */
    private void onPreviewImageActivityResult(int requestCode, Intent data) {
        if (data.getBooleanExtra(SelectSnapCoverImageActivity.RESULT_SEND, false)) {
            sendImageAfterSelectCoverActivityResult(data);
        } else if (data.getBooleanExtra(SelectSnapCoverImageActivity.RESULT_RETAKE, false)) {
            String filename = StringUtil.get32UUID() + JPG;
            String path = StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);

            if (requestCode == RequestCode.PICK_SNAP_COVER) {
                PickImageActivity.start(getActivity(), makeRequestCode(RequestCode.PICK_HIDDEN_IMAGE), PickImageActivity.FROM_CAMERA, path);
            }
        }
    }
}
