/*
 * Copyright (c) 2012-2013 "Monowai Developments Limited"
 *
 * This file is part of AuditBucket.
 *
 * AuditBucket is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuditBucket is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuditBucket.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.auditbucket.helper;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * User: Mike Holdsworth
 * Since: 20/07/13
 */
public class CompressionHelper {
    public static CompressionResult compress(String text) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] bytes = text.getBytes("UTF-8");
            if (bytes.length > 512) {
                OutputStream out = new GZIPOutputStream(baos);
                out.write(bytes);
                out.close();
                return new CompressionResult(baos.toByteArray());
            } else {
                // Not worth compressing
                return new CompressionResult(text);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String decompress(CompressionResult result) {
        try {
            if (result.getMethod().equals(CompressionResult.Method.NONE))
                return new String(result.getBytes());

            InputStream in = new GZIPInputStream(new ByteArrayInputStream(result.getBytes()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0)
                baos.write(buffer, 0, len);
            return new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String decompress(byte[] what, boolean compressed) {
        CompressionResult result = new CompressionResult(what, compressed);
        return decompress(result);
    }
}