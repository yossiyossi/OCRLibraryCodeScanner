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
import com.google.android.gms.vision.text.Text;
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
        ArrayList<TextBlock> callNums = new ArrayList<>();

        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            //clear out non call numbers
            if (item != null && item.getValue() != null) {
                Log.d("OcrDetectorProcessor", "Text detected! " + item.getValue());
                if(isValidCallNumber(item)){
                    callNums.add(item);
                }
            }

        }

        for(int i = 0; i < callNums.size(); i++){
            TextBlock item = callNums.get(i);
            int isInPlace = 0;
            if(i > 0){
                isInPlace = compare(callNums.get(i-1), callNums.get(i)); //will call this in for loop, compare with previous item in loop
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

    public boolean isValidCallNumber(TextBlock b) {
        List<? extends Text> lines = b.getComponents();

        if (lines.size() > 6 || lines.size() < 2) {
            return false;
        }

        if (isLetters(lines.get(0).getValue())) {
            lines.remove(0);
            // Log.d("Letters", "Letters Detected, Deleted index 0, New index: " + lines.get(0).getValue());
        }

        int i = 0;
        while (lines != null & lines.get(i) != null & i < lines.size() - 1) {

            if (isNums(lines.get(0).getValue())) {
                i++;
                //Log.d("Numbers", "Numbers Detected, Incremented i");
            } else {
                //Log.d("False", "First If else statement false");
                return false;

            }

            if (isLetterNumberCombo(lines.get(i).getValue())) {
                i++;
                //Log.d("Letters and numbers", "Letters and numbersDetected, Incremented i");
            } else if (isDate(lines.get(i).getValue())) {  //Needs to be edited for vol. after date
                return true;
            } else {
                //  Log.d("False2", "Second if else false");
                return false;
            }
        }
        return true;
    }

    public boolean isVol(String S) {
        if (S.charAt(0) == 'v' || S.charAt(0) == 'V') { return true; }
        else { return false; }
    }

    /**
     *
     * @param lines
     * @return true if detection is only one line, false otherwise
     */
    public boolean isOneLine(List<? extends Text> lines) {
        if (lines.size() < 2) { return true; }
        else { return false; }
    }

    /**
     *
     * @param S
     * @return returns true of it is in the format of a date, could be improved by filtering for only certain dates
     * ie) 1800 to 2100
     */
    public boolean isDate(String S) {
        if(!isNums(S)) { return false; }
        else if(S.length() != 4) { return false; }
        else { return true; }
    }

    /**
     *
     * @param S
     * @return true if given S consists of only letters
     */
    public boolean isLetters(String S) {
        for (int i = 0; i < S.length(); i++) {
            if (!Character.isLetter(S.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param S
     * @return true if S consists of only numbers
     */
    public boolean isNums(String S) {
        for (int i = 0; i < S.length(); i++) {
            if (!Character.isDigit(S.charAt(i)) & S.charAt(i) != '.') {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param S
     * @return return true if S consists of letters and numbers
     */
    public boolean isLetterNumberCombo(String S) {
        Boolean letter = false;
        Boolean number = false;

        for (int i = 0; i < S.length(); i++) {
            if (Character.isDigit(S.charAt(i))) {
                number = true;
            }
            else if (Character.isLetter(S.charAt(i))) {
                letter = true;
            }
        }
        if (letter == true & number == true) { return true; }
        else { return false; }
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
