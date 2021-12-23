package fr.isep.musictagger.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.Optional;

import fr.isep.musictagger.R;

public class StringTag extends Fragment {
    public static final int FRAGMENT = R.layout.frag_tag_string;

    private String displayName;
    private String defaultValue;
    private String importedValue;

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setImportedValue(final String importedValue) {
        this.importedValue = importedValue;
    }

    public String getValue() {
        return ((EditText) requireView().findViewById(R.id.local_value)).getText().toString();
    }

    public StringTag() {
        super(FRAGMENT);
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args != null) {
            displayName = args.getString("display_name");
            defaultValue = args.getString("default_value");
            importedValue = args.getString("imported_value");
        }
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
        final MaterialButton copyImported = view.findViewById(R.id.copy_imported);
        final EditText localValue = view.findViewById(R.id.local_value);
        final MaterialButton reset = view.findViewById(R.id.reset_local);

        copyImported.setEnabled(false);
        Optional.ofNullable(this.displayName).ifPresent(((TextView) view.findViewById(R.id.displayname))::setText);
        Optional.ofNullable(this.importedValue).ifPresent(val -> {
            ((EditText) view.findViewById(R.id.imported_value)).setText(val);
            if (val.length() > 0) {
                copyImported.setOnClickListener(btn -> localValue.setText(importedValue));
                copyImported.setEnabled(true);
            }
        });
        reset.setOnClickListener(btn -> localValue.setText(defaultValue));
        reset.callOnClick();
    }
}
