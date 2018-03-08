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
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.green;
import static android.hardware.camera2.params.RggbChannelVector.RED;
import static java.lang.Float.parseFloat;
import static java.lang.Math.min;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 * TODO: Make this implement Detector.Processor<TextBlock> and add text to the GraphicOverlay
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
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
    public boolean psuedo_sort(TextBlock lhs, TextBlock rhs){
        if (rhs != null && rhs.getValue() != null && lhs != null && lhs.getValue() != null) {
            List<Line> llines = (List<Line>) lhs.getComponents();
            List<Line> rlines = (List<Line>) rhs.getComponents();
            Log.d("debug", "first" + llines.get(0).getValue());
            Log.d("debug", "second" + rlines.get(0).getValue());

            int i = 0;
            while(i < llines.size() && i < rlines.size()) {
                //compare first line numerically
                if (i == 0)
                {
                    Log.d("debug", "comparing first line" + llines.get(i).getValue() + " " + rlines.get(i).getValue());
                    if (llines.get(i).getValue().compareToIgnoreCase(rlines.get(i).getValue()) > 0) {
                        Log.d("debug", "left greater than right");
                        return false;
                    } else if (llines.get(i).getValue().compareToIgnoreCase(rlines.get(i).getValue()) < 0) {
                        return true;
                    }
                }
                //compare the lines after the first by their letters and then their numbers as decimal ignore last line because it is a year number
                if (i > 0 && i < llines.size() -1 )
                {
                    Log.d("debug", "comparing next line");
                    String[] split_left = llines.get(i).getValue().split("(?<=\\D)(?=\\d)");
                    String[] split_right = rlines.get(i).getValue().split("(?<=\\D)(?=\\d)");
                    float right_numbers = parseFloat("." + split_right[1]);
                    float left_numbers = parseFloat("." + split_left[1]);
                    Log.d("debug", split_left[0] + "," + split_right[0] + "," + left_numbers + "," + right_numbers);
                    if (split_left[0].compareToIgnoreCase(split_right[0]) > 0) {
                        Log.d("debug", "comparing" + split_left[0] + split_right[0]);
                        return false;
                    } else if (llines.get(i).getValue().compareToIgnoreCase(rlines.get(i).getValue()) < 0) {
                        return true;
                    }

                    if (left_numbers > right_numbers) {
                        Log.d("debug", "comparing" + left_numbers + right_numbers);
                        return false;
                    } else if (llines.get(i).getValue().compareToIgnoreCase(rlines.get(i).getValue()) < 0) {
                        return true;
                    }
                }

                ++i;
            }

           /*if (llines.get(0).getValue().compareToIgnoreCase(rlines.get(0).getValue()) > 0) {
               return false;
           }*/
        }
        return true;
    }

    public ArrayList<String> compare(SparseArray<TextBlock> items) {
        //Log.d("Found", "compare");
        ArrayList<String> out_of_order = new ArrayList<String>();
        int compare_at = 0;
        for (int i = 0; i < items.size(); i++) {
            if (compare_at == i + 1){
                return out_of_order;
            }
            TextBlock item1 = items.valueAt(compare_at);
            TextBlock item2 = items.valueAt(i + 1);
            // Log.d("debug:", item1.getValue() + "," + item2.getValue());
            if (psuedo_sort(item1, item2) == false){
                // Log.d("Found: ", "out of order");
                out_of_order.add(item2.getValue());
            }
            else{
                //  Log.d("Found: ", "not out of order");
                compare_at = i + 1;
            }


        }
        return out_of_order;
    }

    public ArrayList<TextBlock> addCallNums(SparseArray<TextBlock> items) {
        ArrayList<TextBlock> callNums = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            TextBlock item = items.valueAt(i);
            if (item != null && item.getValue() != null) {
                if (isValidCallNumber(item)) {
                    callNums.add(item);
                    //Log.d("Get Value: ", item.getValue());
                }
            }
        }
        return callNums;
    }


    public int find(ArrayList<TextBlock> callNums){

        String target = WelcomeScreen.callNumTarget;
        for (int i = 0; i < callNums.size(); i++){
            Log.d("Target: ", target);
            Log.d("CallNum: ", callNums.get(i).getValue());
            if (callNums.get(i).getValue().contains(target)) {
                return i;
            }
        }
        return -1;
        /*ArrayList<String> target = WelcomeScreen.target;
        ArrayList<Integer> count = new ArrayList<>();

        for (int i= 0; i < callNums.size(); i++) {
            count.add(i, 0);
        }
        int index = -1;
        for (int i = 0; i < callNums.size(); i++) {
            for (int j = 0; j < callNums.get(i).getComponents().size(); j++) {
                if (target.get(j).equals(callNums.get(i).getComponents().get(j))) {
                    int value = count.get(i);
                    value++;
                    count.set(i, value);
                }
            }
        }
        int max = Collections.max(count);
        if (max > 2) {
            index = count.indexOf(max);
        }
        return index;*/
    }
    // TODO:  Once this implements Detector.Processor<TextBlock>, implement the abstract methods.

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        ArrayList<Integer> sequence = new ArrayList<Integer>();
        SparseArray<TextBlock> items = detections.getDetectedItems();

        int compare_at = 0;
        ArrayList<TextBlock> callNums = addCallNums(items);


        if (WelcomeScreen.search == true) {
            int index = find(callNums);
            if (index > -1) {
                TextBlock target = callNums.get(index);
                OcrGraphic targetGraphic = new OcrGraphic(mGraphicOverlay, target);
                targetGraphic.setColor(Color.GREEN);
                mGraphicOverlay.add(targetGraphic);
            }
        }
        else {
            for (int i = 0; i < callNums.size(); i++) {

                if (i == callNums.size() - 1) return;

                TextBlock item = callNums.get(compare_at);
                TextBlock item2 = callNums.get(i + 1);
                OcrGraphic graphic1 = new OcrGraphic(mGraphicOverlay, item);
                OcrGraphic graphic2 = new OcrGraphic(mGraphicOverlay, item2);
                //make graphic white and post to graphic, as it is what we are comparing the rest against
                //graphic.setColor(Color.WHITE);
                //mGraphicOverlay.add(graphic);

                Log.d("debug:", item.getValue() + "," + item2.getValue());
                if (psuedo_sort(item, item2) == false && item != item2) {
                    Log.d("debug", "out of order adding " + item.getValue() + " " + compare_at);
                    sequence.add(compare_at);
                    graphic2.makeRed();
                    mGraphicOverlay.add(graphic2);
                } else {
                    Log.d("debug", "not out of order");
                    //make that box green if not out of order and add to mgraphic
                    //graphic1.setColor(Color.GREEN);
                    //mGraphicOverlay.add(graphic1);
                    compare_at = i + 1;
                }
            }
        }



    }


    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
