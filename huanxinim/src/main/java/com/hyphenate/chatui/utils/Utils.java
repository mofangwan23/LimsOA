package com.hyphenate.chatui.utils;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import java.util.Comparator;
import java.util.List;

public class Utils {

	public static List<Size> getResolutionList(Camera camera)
	{ 
		Parameters parameters = camera.getParameters();
		return parameters.getSupportedPreviewSizes();
	}
	
	public static class ResolutionComparator implements Comparator<Size>{

		@Override
		public int compare(Size lhs, Size rhs) {
			if(lhs.height!=rhs.height)
			return lhs.height-rhs.height;
			else
			return lhs.width-rhs.width;
		}
		 
	}
	
	
	
	
}
