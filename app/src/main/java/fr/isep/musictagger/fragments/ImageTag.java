package fr.isep.musictagger.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import fr.isep.musictagger.R;

public class ImageTag extends Fragment {
    public static final int FRAGMENT = R.layout.frag_tag_image;

    private Context context;
    private ImageView importedImage;
    private ImageView localImage;
    private MaterialButton copyImported;
    private MaterialButton reset;
    private final ActivityResultLauncher<Object> chooseFile = registerForActivityResult(new ActivityResultContract<Object, Uri>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull final Context context, final Object input) {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            return Intent.createChooser(intent, "Choose an image file");
        }

        @Override
        public Uri parseResult(final int resultCode, @Nullable final Intent intent) {
            return Optional.ofNullable(intent).map(Intent::getData).orElse(null);
        }
    }, uri -> {
        Log.d("App", String.format("Selected file %s", uri));

        try {
            final InputStream is = context.getContentResolver().openInputStream(uri);
            localImage.setImageDrawable(Drawable.createFromStream(is, uri.toString()));
        } catch (FileNotFoundException e) {
            Log.e("App", "Could not open image", e);
        }
    });

    private String displayName;
    private byte[] defaultValue;
    private byte[] importedValue;

    public void setDefaultValue(final byte[] defaultValue) {
        this.defaultValue = defaultValue;
        Optional.ofNullable(reset).ifPresent(View::callOnClick);
    }

    public void setImportedValue(final byte[] importedValue) {
        this.importedValue = importedValue;

        Optional.ofNullable(importedImage).ifPresent(importedImage -> {
            if (Optional.ofNullable(importedValue).isPresent()) {
                importedImage.setImageBitmap(BitmapFactory.decodeByteArray(importedValue, 0, importedValue.length));
            } else {
                importedImage.setImageDrawable(null);
            }
        });
        Optional.ofNullable(copyImported).ifPresent(copyImported -> copyImported.setEnabled(Optional.ofNullable(importedValue).isPresent()));
    }

    public byte[] getValue() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ((BitmapDrawable) ((ImageView) requireView().findViewById(R.id.local_value)).getDrawable())
                .getBitmap()
                .compress(Bitmap.CompressFormat.JPEG, 100, bs);
        return bs.toByteArray();
    }

    public ImageTag() {
        super(FRAGMENT);
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args != null) {
            displayName = args.getString("display_name");
            defaultValue = args.getByteArray("default_value");
            importedValue = args.getByteArray("imported_value");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        context = null;
    }

    @Override
    public void onInflate(@NonNull final Context ctx, @NonNull final AttributeSet attrs, final Bundle bundle) {
        super.onInflate(ctx, attrs, bundle);

        TypedArray array = ctx.obtainStyledAttributes(attrs, R.styleable.Tag);
        displayName = array.getString(R.styleable.Tag_display_name);
        array.recycle();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle bundle) {
        return inflater.inflate(FRAGMENT, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle bundle) {
        importedImage = view.findViewById(R.id.imported_value);
        localImage = view.findViewById(R.id.local_value);
        copyImported = view.findViewById(R.id.copy_imported);
        reset = view.findViewById(R.id.reset_local);

        copyImported.setEnabled(false);
        Optional.ofNullable(this.displayName).ifPresent(((TextView) view.findViewById(R.id.displayname))::setText);
        Optional.ofNullable(importedValue).ifPresent(this::setImportedValue);
        localImage.setOnClickListener(image -> chooseFile.launch(null));
        copyImported.setOnClickListener(btn -> localImage.setImageDrawable(importedImage.getDrawable()));
        reset.setOnClickListener(btn -> localImage.setImageBitmap(BitmapFactory.decodeByteArray(defaultValue, 0, defaultValue.length)));
        Optional.ofNullable(defaultValue).ifPresent(defaultValue -> reset.callOnClick());
        view.findViewById(R.id.clear_local).setOnClickListener(btn -> localImage.setImageDrawable(null));
    }
}
