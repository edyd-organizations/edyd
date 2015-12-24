package com.oto.edyd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class WelcomeFragment extends Fragment {
	

	public WelcomeFragment() {
	}

		@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			View layout = inflater.inflate(R.layout.fragment_welcome, container, false);
			ImageView imagewc = (ImageView) layout.findViewById(R.id.iv_wc);
			Bundle bundle = getArguments();
			int position = bundle.getInt("position");
			switch (position) {
			case 0:
				imagewc.setImageResource(R.mipmap.guide_1);
				break;
			case 1:
				imagewc.setImageResource(R.mipmap.guide_2);
				break;
			case 2:
				imagewc.setImageResource(R.mipmap.guide_3);
				imagewc.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent=new Intent(getActivity(),MainActivity.class);
						startActivity(intent);
						getActivity().finish();
					}
				});
				break;
			default:
				break;
			}
		return layout;
	}

}
