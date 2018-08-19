package com.battlelancer.seriesguide.billing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Base64Test {

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"1234", "MTIzNA=="},
                {"abcdefghijklmnopqrstuvwxyz", "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo="},
                {"+/AB", "Ky9BQg=="}
        });
    }

    private String source;
    private String encoded;

    public Base64Test(String source, String encoded) {
        this.source = source;
        this.encoded = encoded;
    }

    @Test
    public void test_decodeWithString_formalMethod() throws Base64DecoderException {
        byte[] seriesGuideBytes = com.battlelancer.seriesguide.billing.Base64.decode(encoded);
        byte[] javaBytes = Base64.getDecoder().decode(encoded);
        assertArrayEquals(javaBytes, seriesGuideBytes);
    }

    @Test
    public void test_decodeWithString_comparison() throws Base64DecoderException {
        byte[] seriesGuideBytes = com.battlelancer.seriesguide.billing.Base64.decode(encoded);

        StringBuilder builder = new StringBuilder();
        for(byte b : seriesGuideBytes) {
            builder.append((char)b);
        }

        assertEquals(source, builder.toString());
    }
}