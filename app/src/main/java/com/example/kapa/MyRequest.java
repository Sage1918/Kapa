package com.example.kapa;

public class MyRequest {

        String id;
        String name;

        public MyRequest() {
        }

        public MyRequest(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

}
