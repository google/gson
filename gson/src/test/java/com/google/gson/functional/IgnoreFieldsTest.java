package com.google.gson.functional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Ignore;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the regarding functional "@Expose" type tests.
 *
 * @author  Rao Mengnan
 */
public class IgnoreFieldsTest extends TestCase {

    private Gson gson;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gson = new GsonBuilder()
                .excludeFieldsWithIgnoreAnnotation()
                .create();
    }

    public void testIgnoreSerialize() {
        BookShop shop = new BookShop();

        shop.books = new ArrayList<Book>();
        shop.address = "s.c";

        Book book = new Book(100.1, "test", 100);
        shop.books.add(book);
        shop.books.add(book);
        shop.books.add(book);

        String json = gson.toJson(shop);
        assertEquals(shop.toString(), json);

    }

    public void testIgnoreDeserilize() {
        String address = "DC";
        Double attrA = 1.1;
        String attrB = "attrB";
        int attrC = 2;

        String json = String.format("{\"address\":\"%s\",\"books\":[{\"attrA\":%s,\"attrB\":\"%s\",\"attrC\":%s}]}",
                address, attrA, attrB, attrC);
        BookShop bookShop = gson.fromJson(json, BookShop.class);
        assertEquals(1, bookShop.books.size());
        assertEquals(address, bookShop.address);
        assertEquals(attrA, bookShop.books.get(0).attrA);
        assertEquals(attrB, bookShop.books.get(0).attrB);
        assertNotSame(attrC, bookShop.books.get(0).attrC);
    }

    private static class BookShop {
        private List<Book> books;

        @Ignore(deserialize = false)
        private String address;

        @Override
        public String toString() {

            StringBuilder booksStr = new StringBuilder();
            for (int i = 0; i < books.size(); ++i) {
                if (i > 0) {
                    booksStr.append(",");
                }
                booksStr.append(books.get(i));
            }
            return String.format("{\"books\":[%s]}", booksStr);
        }
    }

    private static class Book {
        private double attrA;
        private String attrB;
        @Ignore
        private int attrC;

        Book(double attrA, String attrB, int attrC) {
            this.attrA = attrA;
            this.attrB = attrB;
            this.attrC = attrC;
        }

        @Override
        public String toString() {
            return String.format("{\"attrA\":%s,\"attrB\":\"%s\"}", attrA, attrB);
        }
    }
}
