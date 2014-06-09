package ru.amobilestudio.autorazborassistant.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.amobilestudio.autorazborassistant.app.R;

/**
 * Created by vetal on 09.06.14.
 */
public class ReservedFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reserved, container, false);

        return rootView;
    }
}
