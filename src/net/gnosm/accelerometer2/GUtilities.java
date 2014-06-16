package net.gnosm.accelerometer2;

import java.util.ArrayList;

public class GUtilities {

	// takes two lists of x,y coordinates and averages them using linear interpolation
	// output could be as big as as list1.size() + list2.size()
	// (if no x-coordinates intersect)
	public static ArrayList<MyGraphViewData> averageTwoFunctions 
					(ArrayList<MyGraphViewData> list1, ArrayList<MyGraphViewData> list2) {
		int i1 = 0;
		int i2 = 0;
		int i3 = 0;
		ArrayList<MyGraphViewData> ret = new ArrayList<MyGraphViewData>();
		
		while (i1 < list1.size() || i2 < list2.size()) {

			
			
		}
		
		return ret;
	}
	
}
