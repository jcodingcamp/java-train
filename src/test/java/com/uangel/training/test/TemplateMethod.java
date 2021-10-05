package com.uangel.training.test;

import org.junit.Test;

public class TemplateMethod {
     abstract class InputStream {

         abstract int read();

        public byte[] readNBytes(int n)  {
            byte[] ret = new byte[n];

            for(int i=0;i<n;i++) {
                int ch = this.read();
                if ( ch < 0) {
                    return ret;
                }
            }
            return ret;
        }
    }

    class FileInputStream extends InputStream {
        @Override
        int read() {
            return 0;
        }
    }

    @Test
    public void test() {
        var finput = new FileInputStream();
        finput.readNBytes(100);
    }
}
