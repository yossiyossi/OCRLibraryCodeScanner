/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);

            int isInPlace = 0;
            if(i > 0){
                isInPlace = compare(items.get(i-1), item); //will call this in for loop, compare with previous item in loop
            }
            Log.d("OcrDetectorProcessor", "isInPlace value is " + isInPlace);


            if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
            }
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);

            if(isInPlace < 0) {
                graphic.makeRed();
            } else if (isInPlace > 0){
                graphic.setColor(Color.GREEN);
            } else{
                graphic.setColor(Color.WHITE);
            }
            mGraphicOverlay.add(graphic);
            isInPlace = 0;
        }

    }

    //returns 1 if t2 > t1
    public static int compare(TextBlock t1, TextBlock t2){
        //list of lines for t1
        List<Line> lines1 = (List<Line>) t1.getComponents();
        for(Line elements : lines1){
            Log.i("current lines ", ": " + elements.getValue());
        }

        //list of lines for t2
        List<Line> lines2 = (List<Line>) t2.getComponents();
        for(Line elements : lines2){
            Log.i("current lines ", ": " + elements.getValue());
        }

//        Split lineones by letter
//        (is L greater than or less than LA? Jk yes it is i found online)
//        For each letter     from left to right, looping num times of least num letters between mine and other:
//        If my letter greater than other letter, return 1 (my is greater)

        for(int i = 0; i < min(lines1.size(),lines2.size()); i++){
            Line L1 = lines1.get(i);
            Line L2 = lines2.get(i);
            String s1 = L1.getValue();
            String s2 = L2.getValue();
            Log.d("OcrDetectorProcessor", "s1: " + s1 + ". s2: " + s2);

            int compare = s2.compareToIgnoreCase(s1);

            if(compare > 0) return 1;
            if(compare < 0) return -1;
        }

        return 0;
        /*
        Line l1 = lines1.get(0);
        Log.i("current line l1 value ", ": " + l1.getValue());
        Line l2 = lines2.get(0);
        //ArrayList<CharSequence> charsl1 =   new ArrayList<CharSequence>((l1.getValue().toCharArray()));
       // Log.i("line l1 character(0) ", ": " + charsl1.get(0));
        List charsl2 = l2.getComponents();
        */
    }


    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
