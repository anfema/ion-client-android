package com.anfema.ionclient.pages.models.contents;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import androidx.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ImageContentParcelableTest {

    @Test
    public void testParcelImageContent() {
        Parcel parcel = Parcel.obtain();

        ImageContent imageContent = new ImageContent();
        imageContent.variation = "default";
        imageContent.image = "http://blabla.com/image";
        imageContent.checksum = "sasdf98a7fd98a7df98a7sdf";

        imageContent.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        ImageContent imageContent2 = ImageContent.CREATOR.createFromParcel(parcel);

        assertEquals(imageContent.variation, imageContent2.variation);
        assertEquals(imageContent.image, imageContent2.image);
        assertEquals(imageContent.checksum, imageContent2.checksum);
    }

    @Test
    public void testParcelBundleImageContent() {
        Parcel parcel = Parcel.obtain();

        Bundle bundle = new Bundle();
        String key = "key";

        ImageContent imageContent = new ImageContent();
        imageContent.variation = "default";
        imageContent.image = "http://blabla.com/image";
        imageContent.checksum = "sasdf98a7fd98a7df98a7sdf";

        bundle.putParcelable(key, imageContent);

        bundle.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        Bundle bundle2 = Bundle.CREATOR.createFromParcel(parcel);
        // The following line is essential and indicates an Android platform bug!
        bundle2.setClassLoader(getClass().getClassLoader());
        ImageContent imageContent2 = bundle2.getParcelable(key);

        assertEquals(imageContent.variation, imageContent2.variation);
        assertEquals(imageContent.image, imageContent2.image);
        assertEquals(imageContent.checksum, imageContent2.checksum);
    }

    @Test
    public void testIntentImageContent() {
        Intent intent = new Intent();
        String key = "key";

        ImageContent imageContent = new ImageContent();
        imageContent.variation = "default";
        imageContent.image = "http://blabla.com/image";
        imageContent.checksum = "sasdf98a7fd98a7df98a7sdf";

        intent.putExtra(key, imageContent);

        ImageContent imageContent2 = intent.getParcelableExtra(key);

        assertEquals(imageContent.variation, imageContent2.variation);
        assertEquals(imageContent.image, imageContent2.image);
        assertEquals(imageContent.checksum, imageContent2.checksum);
    }
}


