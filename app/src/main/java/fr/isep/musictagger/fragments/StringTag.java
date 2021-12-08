package fr.isep.musictagger.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.Optional;

import fr.isep.musictagger.R;

public class StringTag extends Fragment {
    private String displayName;
    private String defaultValue;

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return ((EditText) requireView().findViewById(R.id.local_value)).getText().toString();
    }

    public void setValue(final String value) {
        ((EditText) requireView().findViewById(R.id.local_value)).setText(value);
    }

    public StringTag() {
        super(R.layout.frag_tag_string);
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args != null) {
            displayName = args.getString("display_name");
            defaultValue = args.getString("default_value");
        }
    }

    @Override
    public void onInflate(@NonNull final Context ctx, @NonNull final AttributeSet attrs, final Bundle bundle) {
        super.onInflate(ctx, attrs, bundle);

        TypedArray array = ctx.obtainStyledAttributes(attrs, R.styleable.StringTag);
        displayName = array.getString(R.styleable.StringTag_display_name);
        defaultValue = array.getString(R.styleable.StringTag_default_value);
        array.recycle();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle bundle) {
        return inflater.inflate(R.layout.frag_tag_string, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle bundle) {
        Optional.ofNullable(this.displayName).ifPresent(((TextView) view.findViewById(R.id.displayname))::setText);
        final Editable importedValue = ((EditText) view.findViewById(R.id.imported_value)).getText();
        final MaterialButton copyImported = view.findViewById(R.id.copy_imported);
        final EditText localValue = view.findViewById(R.id.local_value);
        if (importedValue.length() > 0) {
            copyImported.setOnClickListener(btn -> localValue.setText(importedValue));
        } else {
            copyImported.setEnabled(false);
        }
        localValue.setText(defaultValue);
        ((MaterialButton) view.findViewById(R.id.reset_local)).setOnClickListener(btn -> localValue.setText(defaultValue));
    }
}
