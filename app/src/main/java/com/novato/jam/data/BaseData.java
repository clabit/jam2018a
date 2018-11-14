package com.novato.jam.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Constructor;

public abstract class BaseData implements Parcelable {

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		@Override
		public BaseData createFromParcel(Parcel source) {
			BaseData result = null;

			try {
				final String className = source.readString();
				final Class clazz = Class.forName(className).asSubclass(BaseData.class);

				Constructor[] constructors = clazz.getDeclaredConstructors();
				Constructor constructor = null;
				for(int i=0; i < constructors.length; i++) {
				    if(constructors[i].getGenericParameterTypes().length == 0) {
				    	constructor = constructors[i];
				    	break;
				    }
				}
				if(constructor == null) {
					throw new RuntimeException("Cannot find default constructor. " + className);
				}

				constructor.setAccessible(true);
				result = (BaseData)constructor.newInstance();
				result.readFromParcel(source);
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}

			return result;
		}
		@Override
		public BaseData[] newArray(int size) {
			return new BaseData[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getClass().getName());
	}



	public void readFromParcel(Parcel src) {
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
