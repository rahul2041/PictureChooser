/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.j4velin.picturechooser;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.PermissionChecker;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.j4velin.picturechooser.crop.CropFragment;
import de.j4velin.picturechooser.util.API8Wrapper;

public class Main extends FragmentActivity {

    public final static String EXTRA_CROP = "crop";

    public final static String ASPECT_X = "aspectX";
    public final static String ASPECT_Y = "aspectY";

    public final static String IMAGE_PATH = "imgPath";

    private final static int REQUEST_STORAGE_PERMISSION = 1;
    private final static int REQUEST_IMAGE = 2;

    @Override
    protected void onCreate(final Bundle b) {
        super.onCreate(b);

        setResult(RESULT_CANCELED);

        if (Build.VERSION.SDK_INT >= 23 && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            start();
        }
    }

    private void start() {
        if (Build.VERSION.SDK_INT >= 19) {
            startActivityForResult(
                    new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                            .setType("image/*"), REQUEST_IMAGE);
        } else {
            // Create new fragment and transaction
            Fragment newFragment = new BucketsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this
            // fragment,
            // and add the transaction to the back stack
            transaction.replace(android.R.id.content, newFragment);

            // Commit the transaction
            try {
                transaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    void showBucket(final int bucketId) {
        Bundle b = new Bundle();
        b.putInt("bucket", bucketId);
        Fragment f = new ImagesFragment();
        f.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f)
                .addToBackStack(null).commit();
    }

    void imageSelected(final String imgPath) {
        if (getIntent().getBooleanExtra(EXTRA_CROP, false)) {
            Bundle b = new Bundle();
            b.putString(IMAGE_PATH, imgPath);
            b.putFloat("aspect", getIntent().getIntExtra(ASPECT_X, 0) /
                    (float) getIntent().getIntExtra(ASPECT_Y, 1));
            Fragment f = new CropFragment();
            f.setArguments(b);
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f)
                    .addToBackStack(null).commitAllowingStateLoss();
        } else {
            returnResult(imgPath);
        }
    }

    public void cropped(final String imgPath) {
        returnResult(imgPath);
    }

    private void returnResult(final String imgPath) {
        Intent result = new Intent();
        result.putExtra(IMAGE_PATH, imgPath);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                InputStream input = null;
                OutputStream output = null;
                Uri uri = data.getData();
                try {
                    input = getContentResolver().openInputStream(uri);
                    String extension = MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(getContentResolver().getType(uri));
                    File f = new File(API8Wrapper.getExternalFilesDir(this).getAbsolutePath(),
                            uri.getLastPathSegment() + "." + extension);
                    output = new FileOutputStream(f);

                    byte[] buffer = new byte[4096];
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();

                    imageSelected(f.getPath());

                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    finish();
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
