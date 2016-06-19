package com.roselism.rosen.net;

import com.google.common.base.Optional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 16-6-18.
 */
public class RosenTest {
    @Test
    public void addition_optional() throws Exception {
        Optional<String> optional = Optional.absent();
        optional.fromNullable(null);

        assertEquals(optional.orNull(), null);
        assertEquals(optional.or("wangzhen"), "wangzhen");

    }
}
