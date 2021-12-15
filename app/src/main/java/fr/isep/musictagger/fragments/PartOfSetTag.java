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

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.isep.musictagger.Metadata;
import fr.isep.musictagger.R;

public class PartOfSetTag extends Fragment {
    public static final int FRAGMENT = R.layout.frag_tag_partofset;

    private String displayName;
    private Metadata.PartOfSet defaultValue;

    public void setDefaultValue(final Metadata.PartOfSet defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Metadata.PartOfSet getValue() {
        final View view = requireView();
        final String value = ((EditText) view.findViewById(R.id.local_value)).getText().toString();
        final String total = ((EditText) view.findViewById(R.id.local_total)).getText().toString();
        return new Metadata.PartOfSet(total.length() > 0 ? value + "/" + total : value);
    }

    public PartOfSetTag() {
        super(FRAGMENT);
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        Bundle args = getArguments();
        if (args != null) {
            displayName = args.getString("display_name");
            defaultValue = new Metadata.PartOfSet(args.getString("default_value"));
        }
    }

    @Override
    public void onInflate(@NonNull final Context ctx, @NonNull final AttributeSet attrs, final Bundle bundle) {
        super.onInflate(ctx, attrs, bundle);

        TypedArray array = ctx.obtainStyledAttributes(attrs, R.styleable.Tag);
        displayName = array.getString(R.styleable.Tag_display_name);
        defaultValue = Optional.ofNullable(array.getString(R.styleable.Tag_default_value)).map(Metadata.PartOfSet::new).orElse(null);
        array.recycle();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle bundle) {
        return inflater.inflate(FRAGMENT, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle bundle) {
        Optional.ofNullable(this.displayName).ifPresent(((TextView) view.findViewById(R.id.displayname))::setText);
        final Editable importedValue = ((EditText) view.findViewById(R.id.imported_value)).getText();
        final Editable importedTotal = ((EditText) view.findViewById(R.id.imported_total)).getText();
        final MaterialButton copyImported = view.findViewById(R.id.copy_imported);
        final EditText localValue = view.findViewById(R.id.local_value);
        final EditText localTotal = view.findViewById(R.id.local_total);
        if (importedValue.length() > 0) {
            copyImported.setOnClickListener(btn -> {
                localValue.setText(importedValue);
                localTotal.setText(importedTotal);
            });
        } else {
            copyImported.setEnabled(false);
        }
        final MaterialButton reset = view.findViewById(R.id.reset_local);
        reset.setOnClickListener(btn -> {
            final Optional<Metadata.PartOfSet> defaultValue = Optional.ofNullable(this.defaultValue);
            localValue.setText(defaultValue.map(val -> String.valueOf(val.getNumber())).orElse(null));
            localTotal.setText(defaultValue.flatMap(Metadata.PartOfSet::getTotal).map(String::valueOf).orElse(null));
        });
        reset.callOnClick();
    }
}
