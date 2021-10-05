package com.uangel.training.test;

import org.junit.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

public class TestCatch {

    interface Hello {
        String say();
    }

    class File implements AutoCloseable {
        @Override
        public void close()  {

        }
    }

    File open(String name) throws IOException {
        return new File();
    }

    void copyStream( File file1 , File file2) throws IOException, TimeoutException {

    }

    // try-catch를 잘못 사용하는 케이스
    public void copyFile(String name1 , String name2) {

        File file1 = null;
        File file2 = null;
        try {
            file1 = open(name1);
            file2 = open(name2);
            copyStream( file1 , file2);

            file1.close();
            file2.close();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        if (file1 != null ) {
            file1.close();
        }

        if (file2 != null ) {
            file2.close();
        }
    }


    // try-catch 를 올바르게 사용하는 케이스 ( 좀 극단적인 )
    public void copyFile2(String name1 , String name2) {
        try {
            var file1 = open(name1);
            try {
                var file2 = open(name2);
                try {
                    copyStream(file1, file2);
                } catch(IOException e) {
                    e.printStackTrace();

                } catch (TimeoutException e) {
                    e.printStackTrace();
                } finally {
                    file2.close();
                }

            } catch ( IOException e)  {
                e.printStackTrace();
            } finally {
                file1.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // try-catch 를 올바르게 사용하는 케이스 ( 일반적인 )
    public void copyFile3(String name1 , String name2) throws IOException, TimeoutException {
        var file1 = open(name1);
        try {
            var file2 = open(name2);
            try {
                var file3 = open(name2);
                try {
                    copyStream(file1, file2);
                } finally {
                    file3.close();
                }
            } finally {
                file2.close();
            }
        } finally {
            file1.close();
        }
    }

    // try-with-resource 를 사용하여  깔끔하게 수정한 케이스
    public void copyFile4(String name1 , String name2) throws IOException, TimeoutException {
      try( var file1 = open(name1)) {
          try ( var file2 = open(name2)) {
            copyStream(file1, file2 );
          }
      }
    }






    class SSS extends RuntimeException {

    }


    class HelloImpl implements Hello {

        // runtime exception은 throws 명시하지 않음
        @Override
        public String say() {
            throw new SSS();
        }
    }

    // error 도 throws 명시하지 않음
    public void error() {
        throw new Error("hello");
    }

    // runtime exception 이지만 throws 명시해도 상관은 없음
    public void runtime() throws NoSuchElementException{
        throw new NoSuchElementException("hello");
    }

    @Test(expected = NoSuchElementException.class)
    public void test3() {

        // 하지만 try -catch 강제하지 않음
        // try-catch 없이 호출 가능
            runtime();

    }


    @Test(expected = InterruptedException.class)
    public void test() throws InterruptedException {
        // 일반 exception은 try-catch 하지 않을 경우 throws 에 명시해야 함
        throw new InterruptedException("hello");
    }

    void hello() throws IOException {

    }

    @Test(expected = InterruptedException.class)
    // 세부 exception을 일일히 나열하지 않고,  상위 exception 으로 퉁 치는게 가능
    public void test2() throws Exception {
            hello();
            test();
    }
}
