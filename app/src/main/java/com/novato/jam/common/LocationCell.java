package com.novato.jam.common;

import com.google.android.gms.maps.model.LatLng;
import com.pokegoapi.google.common.geometry.MutableInteger;
import com.pokegoapi.google.common.geometry.S2Cell;
import com.pokegoapi.google.common.geometry.S2CellId;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokegoapi.google.common.geometry.S2Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by poshaly on 2018. 2. 5..
 */

public class LocationCell {

    static public List<Long> getCellIds(double latitude, double longitude, int cellSize) {
//        cellSize = 10;
        //12 동 3개정도 규모 //마을버스로 이동가능 할법한 거리? 근접한 거리
        //10 하나 정도 규모 //같은 지역


        int width = 1;



        S2LatLng latLng = S2LatLng.fromDegrees(latitude, longitude);
        S2CellId cellId = S2CellId.fromLatLng(latLng).parent(cellSize);

        MutableInteger index = new MutableInteger(0);
        MutableInteger jindex = new MutableInteger(0);

        int level = cellId.level();
        int size = 1 << (S2CellId.MAX_LEVEL - level);
        int face = cellId.toFaceIJOrientation(index, jindex, null);

        List<Long> cells = new ArrayList<>();

        int halfWidth = (int) Math.floor(width / 2);
        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int y = -halfWidth; y <= halfWidth; y++) {
                cells.add(S2CellId.fromFaceIJ(face, index.intValue() + x * size,
                        jindex.intValue() + y * size).parent(cellSize).id());
            }
        }
        return cells;
    }

    static public LatLng getCellCenterPosition(long cellId){
        LatLng mLatLng = null;

        try {
            mLatLng = new LatLng((double) new S2CellId(cellId).toLatLng().lat().e7() / 10000000, (double) new S2CellId(cellId).toLatLng().lng().e7() / 10000000);
        }catch (Exception e){
        }

        if(mLatLng == null){
            mLatLng = new LatLng(Double.NaN, Double.NaN);
        }

        return mLatLng;
    }
}
