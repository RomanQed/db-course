package com.github.romanqed.course;

import com.github.romanqed.course.hash.Encoder;

class EncoderImpl implements Encoder {

    @Override
    public String encode(String value) {
        return value;
    }

    @Override
    public boolean matches(String raw, String encoded) {
        return raw.equals(encoded);
    }
}
