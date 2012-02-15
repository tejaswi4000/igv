/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.feature.tribble;

import org.broad.igv.feature.genome.Genome;
import org.broad.igv.util.TestUtils;
import org.broad.igv.variant.vcf.VCFVariant;
import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.FeatureCodec;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class CachingFeatureReaderTest {

    String path = TestUtils.LARGE_DATA_DIR + "/CEU.SRP000032.2010_03.genotypes.vcf.gz";
    AbstractFeatureReader baseReader;
    CachingFeatureReader cacheReader;

    @Before
    public void setUp() throws IOException {
        Genome genome = null; // <= don't do chromosome alias conversion
        FeatureCodec codec = CodecFactory.getCodec(path, null);
        baseReader = AbstractFeatureReader.getFeatureReader(path, codec);
        cacheReader = new CachingFeatureReader(baseReader);

    }

    @Test
    /*
    1 182773022 182773023
1 182773812 182773813
1 182774171 182774172
1 182774425 182774426
1 182774496 182774497
1 182774630 182774631
1 182774894 182774895
1 182775195 182775196
1 182775253 182775254
1 182776704 182776705
1 182776741 182776742
     */
    public void testGetSequenceNames() throws Exception {

        List<String> seqNames = cacheReader.getSequenceNames();
        assertEquals(23, seqNames.size());
    }

    @Test
    public void testQuery() throws Exception {

        Set<String> baseReaderLoci = new HashSet();
        final int start = 182773022;
        final int end = 182776742;
        final String chr = "1";
        Iterator<VCFVariant> iter = baseReader.query(chr, start, end);

        while (iter.hasNext()) {
            VCFVariant var = iter.next();
            assertEquals(chr, var.getChr());
            assertTrue(var.getEnd() >= start && var.getStart() <= end);
            baseReaderLoci.add(getLocusString(var));
        }
        assertTrue(baseReaderLoci.size() > 0);

        // Now use CachingFeatureReader and insure results are the same
        Set<String> cacheReaderLoci = new HashSet();
        iter = cacheReader.query(chr, start, end);
        while (iter.hasNext()) {
            VCFVariant var = iter.next();
            assertEquals(chr, var.getChr());
            assertTrue(var.getEnd() >= start && var.getStart() <= end);
            cacheReaderLoci.add(getLocusString(var));
        }

        assertEquals(baseReaderLoci.size(), cacheReaderLoci.size());
        for (String locus : cacheReaderLoci) {
            assertTrue(baseReaderLoci.contains(locus));
        }
    }

    String getLocusString(VCFVariant variant) {
        return variant.getChr() + ":" + variant.getStart() + "-" + variant.getEnd();
    }
}
