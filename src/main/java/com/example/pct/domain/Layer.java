package com.example.pct.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Layer {
    private int top;
    private int bottom;
    private int left;
    private int right;
    private int channelCount;
    private int[] channelId;
    private int[] channelLine;
    private int channel;
    private String modeKey;
    private int transparency;
    private boolean clipping;
    private boolean protectTransparency;
    private boolean vision;
}
