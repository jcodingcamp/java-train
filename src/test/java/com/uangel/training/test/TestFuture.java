package com.uangel.training.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestFuture {




    class File implements AutoCloseable {
        @Override
        public void close() {

        }
    }

    // Future 를 리턴하는  file open 함수
    CompletableFuture<File> open(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new File();
        });
    }


    // file 을 두개 open 한 상태
    class FilePair implements AutoCloseable {
        File file1;
        File file2;

        public FilePair(File file1, File file2) {
            this.file1 = file1;
            this.file2 = file2;
        }

        @Override
        public void close() throws Exception {
            this.file2.close();
        }
    }

    // copy 하는 함수
    CompletableFuture<Integer> copyStream(FilePair pair) {
        System.out.println("copy stream");
        //CompletableFuture.failedFuture(new Exception());
        return CompletableFuture.completedFuture(100);

    }


    // 두번째 file을 open 하는 함수
    CompletableFuture<FilePair> openSecond(File file1, String name) {
        System.out.println("openSecond");
        var f3 = open(name).thenApply(file2 -> new FilePair(file1, file2));
        return f3;
        //return CompletableFuture.failedFuture(new IOException("no such file"));
    }

    @Test
    public void copyFile() throws InterruptedException {

        // future 를 사용할 때는 open -> openSecond -> copyStream 을  flatMap ( thenCompose ) 으로  chain 하면 됨
        // 하지만 이 경우는  open 한 file을 close 하지 않기 때문에
        // close 할 필요가 없도록  각 상태를 코딩해야 함 ( open 할 때 file 내용을 전부 read 하도록 )
        var f = open("file1")
                .thenCompose(file1 -> openSecond(file1, "name"))
                .thenCompose(this::copyStream);

        f.thenAccept(n -> {
            System.out.printf("file copied %d bytes\n", n);
        });

        f.whenComplete((integer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });


        System.out.println("before sleep");
        Thread.sleep(100);
    }

    class SeekResult {
    }

    SeekResult seek(File file) throws IOException {
        return new SeekResult();
    }

    @Test
    // open 후에 seek 으로 검사하는 코드가 추가된 코드
    public void copyFileSeek() throws InterruptedException {
        var f = open("file1")
                .thenCompose(file -> {
                    try {
                        var sr = seek(file);
                        return CompletableFuture.completedFuture(file);
                    } catch (IOException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                })
                .thenCompose(file1 -> openSecond(file1, "name"))
                .thenCompose(this::copyStream);

        f.thenAccept(n -> {
            System.out.printf("file copied %d bytes\n", n);
        });

        f.whenComplete((integer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });


        System.out.println("before sleep");
        Thread.sleep(100);
    }


    // try-with resource 의 future 버젼 구현
    <T extends AutoCloseable, R> CompletableFuture<R> withResource(T a, Function<T, CompletableFuture<R>> mapFunc) {
        var f = mapFunc.apply(a);
        f.whenComplete((r, throwable) -> {
            try {
                a.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return f;
    }

    @Test
    // withResource 를 사용하여 copy 를 구현 한 것
    public void copyFileWithResource() throws InterruptedException, ExecutionException {

        // file1 open
        var t1 = open("file1");


        var t2 = t1.thenCompose(file -> {
            // withResource 를 사용하여 ,  callback 실행이 끝나면 , close 하도록 함
            return withResource(file, file1 -> {
                // file1 은 이 block안에서만 유효하기 때문에
                // 이 block 바깥에서  file1 을 접근하면 안됨
                // openSecond 와 copyStream 이  수직적으로 구성되지 않고
                // 중첩되도록 코딩해야 함

                // 두번째 file을 open 하고
                var t3 = openSecond(file1, "file2");


                return t3.thenCompose(filePair -> {

                    // 두번째 file도 close 되도록 withResource 호출
                    return withResource(filePair, this::copyStream);
                });
            });
        });

        Assert.assertEquals(Integer.valueOf(100), t2.get());

    }

    @Test
    // optional 의 lambda 에서 Exception이 발생할 때 ,  처리 방법
    public void test2() {
        var optString = Optional.<String>ofNullable("http://hello.com");

        var optUri = optString.map(s -> {
            // lambda 안에서 exception이 발생하면
            // CompletableFuture 를 리턴하도록 함
            try {
                return CompletableFuture.completedFuture(new URI(s));
            } catch (URISyntaxException e) {
                return CompletableFuture.<URI>failedFuture(e);
            }
        });

        // Optional이 비어 있는 경우에는  failedFuture 를 리턴하게 해서
        // 결과적으로  Optional<String> -> Future<URI> 로 변환됨
        var futreUri = optUri.orElseGet(() -> {
            return CompletableFuture.failedFuture(new NoSuchElementException("url is null"));
        });

        futreUri.whenComplete((uri, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                System.out.println("send request to " + uri);
            }
        });

    }

    class Response {
        URI uri;

        public Response(URI uri) {
            this.uri = uri;
        }

        @Override
        public String toString() {
            return uri.toString();
        }
    }

    CompletableFuture<Response> sendRequest( URI uri ) {
        return CompletableFuture.completedFuture(new Response(uri));
    }


    // List<Future<T>> 를 Future<List<T>> 로 바꾸는 함수
    // 모두 성공해야지만 Future 가 성공이 되고
    // 하나라도 실패하면 Future가 실패가 됨
    static<T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }


    // List<Future<T>> 를 Future<List<T>> 로 변환하는데
    // 실패는 버리고 성공만  filter 하는 함수
    <T> CompletableFuture<List<T>> filterSuccess(List<CompletableFuture<T>> list) {
        // 재귀 함수이기 때문에 , 종료조건 검사
        if (list.size() > 0 ) {
            // 첫번째 future 를 optional 로 변환
            var f = list.get(0);
            var o = f.handle((t, throwable) -> {
                if (throwable != null) {
                    return Optional.<T>empty();
                }
                return Optional.of(t);
            });


            var o4 = o.thenCompose( l1result -> {
                // 나머지 future 를 filterSuccess 로 재귀적으로 호출
                var otherList = filterSuccess( list.subList(1, list.size()) );

                // 첫번째 결과의 optional 과 합침
                var o3 = otherList.thenApply(l2 -> {
                    if (l1result.isPresent()) {
                        l2.add(l1result.get());
                    }
                    return l2;
                });
                return o3;
            });
            return o4;
        } else {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

    }

    @Test
    // List 에 주소가 들어 있을 때,  모두에게 전송하고
    // 성공한 것만 출력하게 하는 예제
    public void test3() {

        // list의 일부에는 잘못된 주소가 들어 있음
        List<String> list = new ArrayList<>();
        list.add("http://hello.com");
        list.add(":://");


        // 주소를 parsing
        var l2 = list.stream().map( s -> {
            System.out.println("hello " + s);
            try {
                return CompletableFuture.completedFuture(new URI(s));
            } catch (URISyntaxException e) {
                return CompletableFuture.<URI>failedFuture(e);
            }
        }).map( uriCompletableFuture -> {
            // parsing 에 성공했을 때만 sendRequest 실행
            return uriCompletableFuture.thenCompose(this::sendRequest);
        });

        var l3 = l2.collect(Collectors.toList());

        CompletableFuture<List<Response>> l4 = null;

       // CompletableFuture.allOf(futures.stream().toArray(CompletableFuture[]::new)).join();

        // 성공한 future 만 filter
        var l5 = filterSuccess(l3);
        l5.whenComplete( (responses, throwable) -> {
            if (throwable != null ) {
                throwable.printStackTrace();
            } else {
                responses.stream().forEach(r -> {
                    System.out.println("r = " + r);
                });
            }
        });
    }
}