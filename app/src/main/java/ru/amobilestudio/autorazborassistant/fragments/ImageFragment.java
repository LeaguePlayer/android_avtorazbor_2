package ru.amobilestudio.autorazborassistant.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import ru.amobilestudio.autorazborassistant.app.R;
import ru.amobilestudio.autorazborassistant.db.ImagesDataDb;
import ru.amobilestudio.autorazborassistant.helpers.ActivityHelper;

/**
 * Created by vetal on 15.06.14.
 */
public class ImageFragment extends Fragment {

    private ImagesDataDb.Image _image;
    private Context _context;

    public ImageFragment(Context context, ImagesDataDb.Image image) {
        _image = image;
        _context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_image, container, false);

        try {
            URI uri = new URI(_image.get_uri().toString());
            File file = new File(uri);

            Bitmap bitmap = decodeSampledBitmapFromResource(file.getPath(), 800, 600);

            //rotate image
            try {
                ExifInterface exif = new ExifInterface(file.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);

                int rotationInDegrees = exifToDegrees(rotation);
                Log.d(ActivityHelper.TAG, "deg " + rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ImageView imageView = (ImageView) rootView.findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
            //imageView.setImageURI(_image.get_uri());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathFile, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(pathFile, options);

//        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(pathFile, options);
    }

    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }
}
